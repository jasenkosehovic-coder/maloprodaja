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

test.describe('Sidebar navigacija — ADMIN uloga', () => {
  test('prikazuje sve navigacijske stavke dostupne ADMIN-u', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);

    await expect(appLayout.navBlagajna).toBeVisible();
    await expect(appLayout.navDokumenti).toBeVisible();
    await expect(appLayout.navSifarnici).toBeVisible();
    await expect(appLayout.navChat).toBeVisible();
    await expect(appLayout.navFiskalni).toBeVisible();
    await expect(appLayout.navIzvjestaji).toBeVisible();
    await expect(appLayout.navKorisnici).toBeVisible();
    await expect(appLayout.navPostavke).toBeVisible();
  });

  test('klik na Korisnici navigira na /korisnici', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    await new AppLayoutPage(page).navKorisnici.click();
    await expect(page).toHaveURL(/\/korisnici/);
  });

  test('klik na Chat navigira na /chat', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    await new AppLayoutPage(page).navChat.click();
    await expect(page).toHaveURL(/\/chat/);
  });
});

test.describe('Sidebar navigacija — BLAGAJNIK uloga (ograničen pristup)', () => {
  test('BLAGAJNIK ne vidi Fiskalni, Izvještaji, Korisnici, Postavke', async ({ page }) => {
    await injectMockSession(page, 'BLAGAJNIK');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);

    await expect(appLayout.navBlagajna).toBeVisible();
    await expect(appLayout.navDokumenti).toBeVisible();
    await expect(appLayout.navChat).toBeVisible();

    await expect(appLayout.navFiskalni).not.toBeVisible();
    await expect(appLayout.navIzvjestaji).not.toBeVisible();
    await expect(appLayout.navKorisnici).not.toBeVisible();
    await expect(appLayout.navPostavke).not.toBeVisible();
  });

  test('BLAGAJNIK direktno na /korisnici dobija redirect', async ({ page }) => {
    await injectMockSession(page, 'BLAGAJNIK');
    await page.goto('/korisnici');
    await expect(page).not.toHaveURL(/\/korisnici/);
  });
});

test.describe('Navigacija — browser history', () => {
  test('browser back dugme radi ispravno između ruta', async ({ page }) => {
    await injectMockSession(page, 'ADMIN');
    await page.goto('/blagajna');
    const appLayout = new AppLayoutPage(page);

    await appLayout.navDokumenti.click();
    await expect(page).toHaveURL(/\/dokumenti/);
    await page.goBack();
    await expect(page).toHaveURL(/\/blagajna/);
  });
});
