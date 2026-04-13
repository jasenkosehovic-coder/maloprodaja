import { test, expect } from '../fixtures/auth.fixture';

async function injectSessionAndNavigate(
  page: import('@playwright/test').Page
): Promise<void> {
  await page.goto('/login');
  await page.evaluate(() => {
    const korisnik = {
      id: 1, username: 'testkorisnik', ime: 'Test', prezime: 'Korisnik',
      email: 'test@maloprodaja.ba', uloga: 'ADMIN',
      poslovnicaId: 1, poslovnicaNaziv: 'Centralna poslovnica', aktivan: true,
    };
    sessionStorage.setItem('mp_access_token', 'mock-token');
    sessionStorage.setItem('mp_refresh_token', 'mock-refresh');
    sessionStorage.setItem('mp_korisnik', JSON.stringify(korisnik));
  });
  await page.goto('/blagajna');
  await page.waitForLoadState('networkidle');
}

test.describe('Inactivity logout — simulacija sa Playwright Clock API', () => {
  test('korisnik se odjavljuje nakon neaktivnosti (1 sat)', async ({ page }) => {
    await page.clock.install();
    await injectSessionAndNavigate(page);

    // Skoči preko 1-satnog inactivity timeoutа (3600000ms)
    await page.clock.fastForward(3_601_000);

    await expect(page).toHaveURL(/\/login/, { timeout: 10_000 });
    expect(await page.evaluate(() => sessionStorage.getItem('mp_access_token'))).toBeNull();
  });

  test('korisnička aktivnost (keydown) resetira inactivity timer', async ({ page }) => {
    await page.clock.install();
    await injectSessionAndNavigate(page);

    // Pola sata bez aktivnosti
    await page.clock.fastForward(1_800_000);

    // Korisnik pritisne tipku — timer se resetira
    await page.keyboard.press('Tab');
    await page.clock.fastForward(500); // debounce

    // Još pola sata — ukupno 1h od starta, ali timer resetiran na 30min
    await page.clock.fastForward(1_800_000);

    // Korisnik treba biti i dalje ulogiran
    await expect(page).not.toHaveURL(/\/login/);
    expect(await page.evaluate(() => sessionStorage.getItem('mp_access_token'))).toBe('mock-token');
  });

  test('svi sessionStorage ključevi se brišu nakon inactivity logoutа', async ({ page }) => {
    await page.clock.install();
    await injectSessionAndNavigate(page);

    await page.clock.fastForward(3_601_000);

    await expect(page).toHaveURL(/\/login/, { timeout: 10_000 });

    const keys = await page.evaluate(() => ({
      accessToken:  sessionStorage.getItem('mp_access_token'),
      refreshToken: sessionStorage.getItem('mp_refresh_token'),
      korisnik:     sessionStorage.getItem('mp_korisnik'),
    }));

    expect(keys.accessToken).toBeNull();
    expect(keys.refreshToken).toBeNull();
    expect(keys.korisnik).toBeNull();
  });
});

test.describe('Inactivity logout — ponašanje login stranice', () => {
  test('login forma ostaje funkcionalna nakon inactivity logoutа', async ({ page }) => {
    await page.clock.install();
    await injectSessionAndNavigate(page);

    await page.clock.fastForward(3_601_000);
    await expect(page).toHaveURL(/\/login/, { timeout: 10_000 });

    const usernameInput = page.locator('#username');
    await expect(usernameInput).toBeVisible();
    await usernameInput.fill('novi_korisnik');
    await expect(usernameInput).toHaveValue('novi_korisnik');
  });

  test('InactivityService ne prati aktivnost na /login stranici', async ({ page }) => {
    await page.clock.install();
    // Ne ubrizgavamo sesiju — nije ulogiran
    await page.goto('/login');
    await page.waitForLoadState('networkidle');

    await page.clock.fastForward(3_601_000);

    // Ostajemo na /login — monitoring nije pokrenut
    await expect(page).toHaveURL(/\/login/);
  });
});
