import { test, expect, TEST_CREDENTIALS } from '../fixtures/auth.fixture';
import { LoginPage } from '../pages/login.page';

test.describe('Login stranica — prikaz i struktura', () => {
  test('prikazuje naslov i podnaslov aplikacije', async ({ loginPage }) => {
    await loginPage.goto();
    await expect(loginPage.pageTitle).toHaveText('Maloprodaja');
    await expect(loginPage.pageSubtitle).toHaveText('Prijavite se na svoj nalog');
  });

  test('prikazuje polja za username i lozinku', async ({ loginPage }) => {
    await loginPage.goto();
    await expect(loginPage.usernameInput).toBeVisible();
    await expect(loginPage.passwordInput).toBeVisible();
  });

  test('submit dugme je inicijalno onemogućeno (forma prazna)', async ({ loginPage }) => {
    await loginPage.goto();
    await expect(loginPage.submitButton).toBeDisabled();
  });

  test('lozinka je sakrivena po defaultu (type=password)', async ({ loginPage }) => {
    await loginPage.goto();
    expect(await loginPage.getPasswordInputType()).toBe('password');
  });

  test('toggle gumb prikazuje i sakriva lozinku', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.fillPassword('tajnalozinka');

    expect(await loginPage.getPasswordInputType()).toBe('password');
    await loginPage.togglePasswordButton.click();
    expect(await loginPage.getPasswordInputType()).toBe('text');
    await loginPage.togglePasswordButton.click();
    expect(await loginPage.getPasswordInputType()).toBe('password');
  });
});

test.describe('Login forma — validacija polja', () => {
  test('prikazuje grešku za prazno korisničko ime nakon touchanja', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.usernameInput.click();
    await loginPage.usernameInput.blur();
    await expect(loginPage.usernameError).toBeVisible();
    await expect(loginPage.usernameError).toHaveText(/korisničko ime je obavezno/i);
  });

  test('prikazuje grešku ako je korisničko ime kraće od 3 znaka', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.fillUsername('ab');
    await loginPage.usernameInput.blur();
    await expect(loginPage.usernameError).toBeVisible();
    await expect(loginPage.usernameError).toHaveText(/najmanje 3 znaka/i);
  });

  test('prikazuje grešku za praznu lozinku nakon touchanja', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.passwordInput.click();
    await loginPage.passwordInput.blur();
    await expect(loginPage.passwordError).toBeVisible();
    await expect(loginPage.passwordError).toHaveText(/lozinka je obavezna/i);
  });

  test('prikazuje grešku ako je lozinka kraća od 4 znaka', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.fillPassword('abc');
    await loginPage.passwordInput.blur();
    await expect(loginPage.passwordError).toBeVisible();
    await expect(loginPage.passwordError).toHaveText(/najmanje 4 znaka/i);
  });

  test('submit dugme ostaje onemogućeno dok forma nije validna', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.fillUsername('validuser');
    await expect(loginPage.submitButton).toBeDisabled();

    await loginPage.fillUsername('');
    await loginPage.fillPassword('validpass');
    await expect(loginPage.submitButton).toBeDisabled();
  });

  test('submit dugme je omogućeno kad su oba polja validna', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.fillUsername('validuser');
    await loginPage.fillPassword('validpass');
    await expect(loginPage.submitButton).toBeEnabled();
  });
});

test.describe('Login — pogrešni kredencijali (mock API)', () => {
  test('prikazuje error poruku za pogrešne kredencijale', async ({ page, loginPage }) => {
    await loginPage.goto();
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Pogrešno korisničko ime ili lozinka.' }),
      });
    });

    await loginPage.login(TEST_CREDENTIALS.invalid.username, TEST_CREDENTIALS.invalid.password);

    await expect(loginPage.loginErrorAlert).toBeVisible({ timeout: 8_000 });
    await expect(loginPage.loginErrorAlert).toContainText(/pogrešno korisničko ime ili lozinka/i);
  });

  test('error element ima role="alert" i aria-live="assertive"', async ({ page, loginPage }) => {
    await loginPage.goto();
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Pogrešno korisničko ime ili lozinka.' }),
      });
    });

    await loginPage.login(TEST_CREDENTIALS.invalid.username, TEST_CREDENTIALS.invalid.password);
    await loginPage.waitForLoginError();

    const errorEl = page.locator('.login-error[role="alert"]');
    await expect(errorEl).toBeVisible();
    await expect(errorEl).toHaveAttribute('aria-live', 'assertive');
  });

  test('submit dugme se ponovo omogućuje nakon greške', async ({ page, loginPage }) => {
    await loginPage.goto();
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Pogrešno korisničko ime ili lozinka.' }),
      });
    });

    await loginPage.login(TEST_CREDENTIALS.invalid.username, TEST_CREDENTIALS.invalid.password);
    await loginPage.waitForLoginError();
    await expect(loginPage.submitButton).toBeEnabled({ timeout: 5_000 });
  });
});

test.describe('Login — UI loading state', () => {
  test('prikazuje "Prijava u toku..." tokom API poziva', async ({ page, loginPage }) => {
    await loginPage.goto();
    await page.route('**/api/auth/login', async (route) => {
      await new Promise((resolve) => setTimeout(resolve, 1_500));
      await route.fulfill({
        status: 401,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Pogrešno.' }),
      });
    });

    await loginPage.fillUsername(TEST_CREDENTIALS.invalid.username);
    await loginPage.fillPassword(TEST_CREDENTIALS.invalid.password);
    await loginPage.submit();

    await expect(loginPage.submitButton).toContainText(/prijava u toku/i);
    await expect(loginPage.submitButton).toBeDisabled();
  });
});

test.describe('Login — happy path (mock API)', () => {
  const mockLoginResponse = {
    accessToken: 'mock-access-token-12345',
    refreshToken: 'mock-refresh-token-67890',
    tokenType: 'Bearer',
    expiresIn: 3600,
    korisnik: {
      id: 1, username: 'admin', ime: 'Test', prezime: 'Admin',
      email: 'admin@test.ba', uloga: 'ADMIN',
      poslovnicaId: 1, poslovnicaNaziv: 'Centralna poslovnica', aktivan: true,
    },
  };

  test('uspješan login preusmjerava na /blagajna', async ({ page, loginPage }) => {
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        // Backend uvijek wrapa odgovor u ApiResponse<T> — mora biti { success, data }
        body: JSON.stringify({ success: true, data: mockLoginResponse }),
      });
    });

    await loginPage.goto();
    await loginPage.login(TEST_CREDENTIALS.admin.username, TEST_CREDENTIALS.admin.password);
    await page.waitForURL('**/blagajna', { timeout: 10_000 });
    expect(page.url()).toContain('/blagajna');
  });

  test('uspješan login čuva accessToken u localStorage', async ({ page, loginPage }) => {
    await page.route('**/api/auth/login', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        // Backend uvijek wrapa odgovor u ApiResponse<T> — mora biti { success, data }
        body: JSON.stringify({ success: true, data: mockLoginResponse }),
      });
    });

    await loginPage.goto();
    await loginPage.login(TEST_CREDENTIALS.admin.username, TEST_CREDENTIALS.admin.password);
    await page.waitForURL('**/blagajna', { timeout: 10_000 });

    const accessToken = await page.evaluate(() => localStorage.getItem('mp_access_token'));
    expect(accessToken).toBe('mock-access-token-12345');
  });
});

test.describe('Login — auth guard (zaštićene rute)', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.evaluate(() => {
      localStorage.removeItem('mp_access_token');
      localStorage.removeItem('mp_refresh_token');
      localStorage.removeItem('mp_korisnik');
    });
  });

  test('direktan pristup /blagajna bez tokena preusmjerava na /login', async ({ page }) => {
    await page.goto('/blagajna');
    await expect(page).toHaveURL(/\/login/);
  });

  test('direktan pristup /korisnici bez tokena preusmjerava na /login', async ({ page }) => {
    await page.goto('/korisnici');
    await expect(page).toHaveURL(/\/login/);
  });

  test('nepostojeća ruta preusmjerava na /login', async ({ page }) => {
    await page.goto('/nepostojeci-path');
    await expect(page).toHaveURL(/\/login/);
  });
});
