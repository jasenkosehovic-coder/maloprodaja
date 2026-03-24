import { type Page, type Locator, expect } from '@playwright/test';

/**
 * Page Object Model za glavni layout (topbar + sidebar).
 */
export class AppLayoutPage {
  readonly page: Page;

  readonly topbar: Locator;
  readonly appTitle: Locator;
  readonly poslovnicaBadge: Locator;
  readonly userMenuButton: Locator;
  readonly chatButton: Locator;
  readonly logoutMenuItem: Locator;
  readonly postavkeMenuItem: Locator;
  readonly userRoleInMenu: Locator;
  readonly sidebar: Locator;
  readonly navBlagajna: Locator;
  readonly navDokumenti: Locator;
  readonly navSifarnici: Locator;
  readonly navFiskalni: Locator;
  readonly navIzvjestaji: Locator;
  readonly navChat: Locator;
  readonly navKorisnici: Locator;
  readonly navPostavke: Locator;

  constructor(page: Page) {
    this.page = page;

    this.topbar          = page.getByRole('banner');
    this.appTitle        = page.locator('.app-title');
    this.poslovnicaBadge = page.locator('.poslovnica-badge');
    this.userMenuButton  = page.getByRole('button', { name: /meni korisnika/i });
    this.chatButton      = page.getByRole('button', { name: /otvori chat/i });

    this.logoutMenuItem   = page.getByRole('menuitem', { name: /odjava/i });
    this.postavkeMenuItem = page.getByRole('menuitem', { name: /postavke/i });
    this.userRoleInMenu   = page.locator('.menu-user-role');

    this.sidebar       = page.getByRole('navigation', { name: /glavna navigacija/i });
    this.navBlagajna   = page.getByRole('link', { name: /blagajna/i });
    this.navDokumenti  = page.getByRole('link', { name: /dokumenti/i });
    this.navSifarnici  = page.getByRole('link', { name: /šifarnici/i });
    this.navFiskalni   = page.getByRole('link', { name: /fiskalni/i });
    this.navIzvjestaji = page.getByRole('link', { name: /izvještaji/i });
    this.navChat       = page.getByRole('link', { name: /chat/i });
    this.navKorisnici  = page.getByRole('link', { name: /korisnici/i });
    this.navPostavke   = page.getByRole('link', { name: /postavke/i });
  }

  async openUserMenu(): Promise<void> {
    await this.userMenuButton.click();
    await expect(this.logoutMenuItem).toBeVisible({ timeout: 3_000 });
  }

  async logout(): Promise<void> {
    await this.openUserMenu();
    await this.logoutMenuItem.click();
    await this.page.waitForURL('**/login', { timeout: 5_000 });
  }

  async navigateTo(route: string): Promise<void> {
    await this.page.goto(route);
    await this.page.waitForLoadState('networkidle');
  }
}
