import { type Page, type Locator, expect } from '@playwright/test';

/**
 * Page Object Model za login stranicu.
 * Enkapsulira sve selektore i akcije vezane za /login.
 */
export class LoginPage {
  readonly page: Page;

  readonly usernameInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;
  readonly togglePasswordButton: Locator;
  readonly loginErrorAlert: Locator;
  readonly usernameError: Locator;
  readonly passwordError: Locator;
  readonly pageTitle: Locator;
  readonly pageSubtitle: Locator;

  constructor(page: Page) {
    this.page = page;

    this.usernameInput        = page.locator('#username');
    this.passwordInput        = page.locator('#password');
    this.submitButton         = page.getByRole('button', { name: /prijava/i });
    this.togglePasswordButton = page.getByRole('button', { name: /prikaži lozinku|sakrij lozinku/i });

    this.loginErrorAlert = page.locator('[role="alert"]').filter({ hasText: /pogrešno|netočno|greška/i });
    this.usernameError   = page.locator('[id$="-error"]').filter({ hasText: /korisničko ime/i }).first();
    this.passwordError   = page.locator('[id$="-error"]').filter({ hasText: /lozinka/i }).first();

    this.pageTitle    = page.locator('#login-title');
    this.pageSubtitle = page.getByText('Prijavite se na svoj nalog');
  }

  async goto(): Promise<void> {
    await this.page.goto('/login');
    await this.page.waitForLoadState('networkidle');
  }

  async fillUsername(value: string): Promise<void> {
    await this.usernameInput.fill(value);
  }

  async fillPassword(value: string): Promise<void> {
    await this.passwordInput.fill(value);
  }

  async submit(): Promise<void> {
    await this.submitButton.click();
  }

  async login(username: string, password: string): Promise<void> {
    await this.fillUsername(username);
    await this.fillPassword(password);
    await this.submit();
  }

  async waitForLoginError(): Promise<void> {
    await expect(this.loginErrorAlert).toBeVisible({ timeout: 8_000 });
  }

  async waitForRedirectAfterLogin(): Promise<void> {
    await this.page.waitForURL('**/blagajna', { timeout: 10_000 });
  }

  async isSubmitDisabled(): Promise<boolean> {
    return this.submitButton.isDisabled();
  }

  async getPasswordInputType(): Promise<string | null> {
    return this.passwordInput.getAttribute('type');
  }
}
