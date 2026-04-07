import { test as base, type Page } from '@playwright/test';
import { LoginPage } from '../pages/login.page';
import { AppLayoutPage } from '../pages/app-layout.page';

export const TEST_CREDENTIALS = {
  admin:     { username: 'admin',     password: 'Admin123!' },
  invalid:   { username: 'nepostoji', password: 'pogresna'  },
} as const;

const ACCESS_TOKEN_KEY  = 'mp_access_token';
const REFRESH_TOKEN_KEY = 'mp_refresh_token';
const KORISNIK_KEY      = 'mp_korisnik';

export type AuthFixtures = {
  loginPage: LoginPage;
  appLayout: AppLayoutPage;
  authenticatedPage: Page;
};

export const test = base.extend<AuthFixtures>({
  loginPage: async ({ page }, use) => {
    await use(new LoginPage(page));
  },

  appLayout: async ({ page }, use) => {
    await use(new AppLayoutPage(page));
  },

  authenticatedPage: async ({ page }, use) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login(
      TEST_CREDENTIALS.admin.username,
      TEST_CREDENTIALS.admin.password
    );
    await loginPage.waitForRedirectAfterLogin();
    await use(page);

    await page.evaluate((keys) => {
      keys.forEach((k) => sessionStorage.removeItem(k));
    }, [ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY, KORISNIK_KEY]);
  },
});

export { expect } from '@playwright/test';
