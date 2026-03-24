import { test, expect } from '../fixtures/auth.fixture';
import { AppLayoutPage } from '../pages/app-layout.page';

async function injectMockSession(
  page: import('@playwright/test').Page,
  uloga: 'ADMIN' | 'BLAGAJNIK' | 'SUPER_ADMIN' | 'MENADZER' = 'ADMIN'
): Promise<void> {
  await page.goto('/login');
  await page.evaluate(({ uloga }) => {
    const korisnik = {
      id: 1, username: 'testkorisnik', ime: 'Test', prezime: 'Korisnik',
      email: 'test@maloprodaja.ba', uloga,
      poslovnicaId: 1, poslovnicaNaziv: 'Centralna poslovnica', aktivan: true,
    };
    localStorage.setItem('mp_access_token', 'mock-token-za-navigaciju');
    localStorage.setItem('mp_refresh_token', 'mock-refresh-token');
    localStorage.setItem('mp_korisnik', JSON.stringify(korisnik));
  }, { uloga });
}

test.describe('Topbar — prikaz i elementi', () => {
  test('topbar je vidljiv sa naslovom aplikacije', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);
    await expect(appLayout.topbar).toBeVisible();
    await expect(appLayout.appTitle).toHaveText('Maloprodaja');
  });

  test('prikazuje ime poslovnice u topbaru', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);
    await expect(appLayout.poslovnicaBadge).toBeVisible();
    await expect(appLayout.poslovnicaBadge).toContainText('Centralna poslovnica');
  });
});

test.describe('User menu — odjava', () => {
  test('user meni se otvara na klik', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);
    await appLayout.openUserMenu();
    await expect(appLayout.logoutMenuItem).toBeVisible();
  });

  test('odjava čisti localStorage i preusmjerava na /login', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);
    await appLayout.logout();

    await expect(page).toHaveURL(/\/login/);
    expect(await page.evaluate(() => localStorage.getItem('mp_access_token'))).toBeNull();
    expect(await page.evaluate(() => localStorage.getItem('mp_korisnik'))).toBeNull();
  });
});

test.describe('Topbar navigacija — ADMIN uloga', () => {
  test('prikazuje sve dropdown izbornike dostupne ADMIN-u', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);

    await expect(appLayout.navSifarnici).toBeVisible();
    await expect(appLayout.navBlagajna).toBeVisible();
    await expect(appLayout.navFiskalni).toBeVisible();
    await expect(appLayout.navDokumenti).toBeVisible();
    await expect(appLayout.navIzvjestaji).toBeVisible();
    await expect(appLayout.navPostavke).toBeVisible();
    await expect(appLayout.navChat).toBeVisible();
  });

  test('Šifarnici dropdown sadrži Korisnici stavku za ADMIN-a', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);

    await appLayout.navSifarnici.click();
    await expect(appLayout.navKorisnici).toBeVisible({ timeout: 3_000 });
  });

  test('klik na Korisnici navigira na /sifarnici/korisnici', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);

    await appLayout.navSifarnici.click();
    await appLayout.navKorisnici.click();
    await expect(page).toHaveURL(/\/sifarnici\/korisnici/);
  });

  test('klik na Chat navigira na /chat', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    await new AppLayoutPage(page).navChat.click();
    await expect(page).toHaveURL(/\/chat/);
  });
});

test.describe('Topbar navigacija — BLAGAJNIK uloga (ograničen pristup)', () => {
  test('BLAGAJNIK vidi Šifarnici, Blagajna, Fiskalni, Izvještaji ali ne Dokumenti i Postavke', async ({ page }) => {
    await injectMockSession(page, 'BLAGAJNIK');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);

    // BLAGAJNIK ima pristup ovim grupama
    await expect(appLayout.navBlagajna).toBeVisible();
    await expect(appLayout.navSifarnici).toBeVisible();   // ima stavke za ALL/ADMIN_BLAG
    await expect(appLayout.navFiskalni).toBeVisible();    // ADMIN_BLAG uključuje BLAGAJNIK
    await expect(appLayout.navIzvjestaji).toBeVisible();  // Knjiga blagajni je ADMIN_MEN_BLAG
    await expect(appLayout.navChat).toBeVisible();

    // BLAGAJNIK nema pristup ovim grupama
    await expect(appLayout.navDokumenti).not.toBeVisible();
    await expect(appLayout.navPostavke).not.toBeVisible();
  });

  test('BLAGAJNIK direktno na /sifarnici/korisnici dobija redirect', async ({ page }) => {
    await injectMockSession(page, 'BLAGAJNIK');
    await page.goto('/sifarnici/korisnici');
    await expect(page).not.toHaveURL(/\/sifarnici\/korisnici/);
  });
});

test.describe('Navigacija — browser history', () => {
  test('browser back dugme radi ispravno između ruta', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);

    // Otvoriti Šifarnici dropdown i kliknuti "Artikli u poslovnici"
    await appLayout.navSifarnici.click();
    await page.getByRole('menuitem', { name: /artikli u poslovnici/i }).click();
    await expect(page).toHaveURL(/\/sifarnici\/artikli-poslovnica/);

    await page.goBack();
    await expect(page).toHaveURL(/\/blagajna/);
  });
});
