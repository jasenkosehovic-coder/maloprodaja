/**
 * Test Scenario 1: Login & Navigation
 * Tests that the app loads, login works, and nav items are visible.
 */

import { test, expect, TEST_CREDENTIALS } from '../fixtures/auth.fixture';
import { AppLayoutPage } from '../pages/app-layout.page';

test.describe('Scenario 1: Login & Navigation', () => {

  test('1.1 — App loads at localhost:4200 and shows login page', async ({ page }) => {
    await page.goto('/');
    // Should redirect to login
    await expect(page).toHaveURL(/\/login/, { timeout: 10_000 });
    await expect(page.getByText(/maloprodaja/i)).toBeVisible({ timeout: 5_000 });
  });

  test('1.2 — Login with admin credentials works and redirects to /blagajna', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.login(TEST_CREDENTIALS.admin.username, TEST_CREDENTIALS.admin.password);
    await loginPage.waitForRedirectAfterLogin();
    await expect(loginPage.page).toHaveURL(/\/blagajna/);
  });

  test('1.3 — After login, Šifarnici nav item is visible', async ({ authenticatedPage }) => {
    const layout = new AppLayoutPage(authenticatedPage);
    await authenticatedPage.waitForLoadState('networkidle');
    await expect(layout.navSifarnici).toBeVisible({ timeout: 5_000 });
  });

  test('1.4 — After login, Dokumenti nav item is visible', async ({ authenticatedPage }) => {
    const layout = new AppLayoutPage(authenticatedPage);
    await authenticatedPage.waitForLoadState('networkidle');
    await expect(layout.navDokumenti).toBeVisible({ timeout: 5_000 });
  });

  test('1.5 — Dokumenti menu contains sub-items when opened', async ({ authenticatedPage }) => {
    const layout = new AppLayoutPage(authenticatedPage);
    await authenticatedPage.waitForLoadState('networkidle');
    await layout.navDokumenti.click();
    // Check for sub-menu items
    await expect(authenticatedPage.getByRole('menuitem', { name: /ulazne fakture/i }))
      .toBeVisible({ timeout: 3_000 })
      .catch(() => {
        // Some nav implementations use links instead of menuitems
        return expect(authenticatedPage.getByText(/ulazne fakture/i)).toBeVisible({ timeout: 3_000 });
      });
  });

  test('1.6 — Invalid credentials show error message', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.login('nepostoji', 'pogresna');
    await expect(loginPage.loginErrorAlert).toBeVisible({ timeout: 8_000 });
  });

  test('1.7 — Unauthenticated access to /dokumenti/fakture redirects to login', async ({ page }) => {
    // Navigate to a page first so we can clear storage
    await page.goto('/login');
    await page.evaluate(() => {
      sessionStorage.removeItem('mp_access_token');
      sessionStorage.removeItem('mp_refresh_token');
      sessionStorage.removeItem('mp_korisnik');
    });
    await page.goto('/dokumenti/fakture');
    await expect(page).toHaveURL(/\/login/, { timeout: 5_000 });
  });

});
