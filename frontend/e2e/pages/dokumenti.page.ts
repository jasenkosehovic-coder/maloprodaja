import { type Page, type Locator, expect } from '@playwright/test';

/**
 * Page Object Model za Dokumenti feature.
 * Covers: Ulazne fakture, Povrat dobavljacu, Medjuskladisnica
 */
export class DokumentiPage {
  readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  // ---- Navigation ----

  async navigateToFakture(): Promise<void> {
    await this.page.goto('/dokumenti/fakture');
    await this.page.waitForLoadState('networkidle');
  }

  async navigateToPovratDobavljacu(): Promise<void> {
    await this.page.goto('/dokumenti/povrat-dobavljacu');
    await this.page.waitForLoadState('networkidle');
  }

  async navigateToMedjuskladisnica(): Promise<void> {
    await this.page.goto('/dokumenti/medjuskladisnica');
    await this.page.waitForLoadState('networkidle');
  }

  async navigateToNivelacije(): Promise<void> {
    await this.page.goto('/dokumenti/nivelacije');
    await this.page.waitForLoadState('networkidle');
  }

  // ---- Common document list actions ----

  getNewDocumentButton(): Locator {
    return this.page.getByRole('button', { name: /nova|novi|kreir/i }).first();
  }

  getDocumentRows(): Locator {
    return this.page.locator('table tbody tr, mat-table mat-row, [role="row"]:not([role="columnheader"])');
  }

  getStatusBadge(status: string): Locator {
    return this.page.getByText(status, { exact: false }).first();
  }

  // ---- Faktura form ----

  getStatusSelect(): Locator {
    return this.page.locator('[formcontrolname="status"], mat-select[formcontrolname="status"]');
  }

  getAddStavkaButton(): Locator {
    return this.page.getByRole('button', { name: /dodaj stavku|nova stavka|dodaj/i }).first();
  }

  getKolicinaInput(): Locator {
    return this.page.locator('[formcontrolname="kolicina"], input[formcontrolname="kolicina"]').last();
  }

  getCijenaInput(): Locator {
    return this.page.locator('[formcontrolname="cijena"], input[formcontrolname="cijena"]').last();
  }

  getSaveButton(): Locator {
    return this.page.getByRole('button', { name: /sačuvaj|spremi|save/i }).first();
  }

  getPotvrdiButton(): Locator {
    return this.page.getByRole('button', { name: /potvrdi/i }).first();
  }

  getStornirajButton(): Locator {
    return this.page.getByRole('button', { name: /storniraj/i }).first();
  }

  getPdfButton(): Locator {
    return this.page.getByRole('button', { name: /pdf/i }).first();
  }

  getBrojDokumenta(): Locator {
    return this.page.locator('[data-testid="broj-dokumenta"], .broj-dokumenta').first();
  }
}
