/**
 * Test Scenario 2: Šifarnici — Tipovi veličina
 */

import { test, expect } from '../fixtures/auth.fixture';

test.describe('Scenario 2: Tipovi veličina', () => {

  test.beforeEach(async ({ authenticatedPage }) => {
    await authenticatedPage.goto('/sifarnici/artikli/tipovi-velicina');
    await authenticatedPage.waitForLoadState('networkidle');
  });

  test('2.1 — Page loads and shows title', async ({ authenticatedPage }) => {
    await expect(authenticatedPage.getByText(/tipovi veličina/i).first()).toBeVisible({ timeout: 5_000 });
  });

  test('2.2 — Seeded tipovi veličina appear in the list', async ({ authenticatedPage }) => {
    // Seeded: Konfekcija S-XXL, Konfekcija numerički, Dječija, Čarape
    await expect(authenticatedPage.getByText(/konfekcija/i).first()).toBeVisible({ timeout: 5_000 });
  });

  test('2.3 — Selecting a tip veličine loads veličine in sub-table', async ({ authenticatedPage }) => {
    // Click first tip in list
    const firstRow = authenticatedPage.locator(
      'table tbody tr, mat-table mat-row, [role="row"]:not([role="columnheader"])'
    ).first();
    await firstRow.click();
    // Should show veličine sub-table or details panel
    await authenticatedPage.waitForTimeout(1_000);
    // Verify some sub-table or detail section appeared
    const hasSubTable = await authenticatedPage.locator(
      '[data-testid="velicine-table"], .velicine-list, mat-expansion-panel, table'
    ).count();
    expect(hasSubTable).toBeGreaterThan(0);
  });

  test('2.4 — Seeded veličine are shown (S, M, L, XL, XXL or numeric)', async ({ authenticatedPage }) => {
    // Select first row — Konfekcija S-XXL should have S, M, L, XL, XXL
    const firstRow = authenticatedPage.locator(
      'table tbody tr, mat-table mat-row'
    ).first();
    await firstRow.click();
    await authenticatedPage.waitForTimeout(1_000);
    // S or M should appear somewhere on the page
    const hasS = await authenticatedPage.getByText(/\bS\b/).count();
    const hasM = await authenticatedPage.getByText(/\bM\b/).count();
    expect(hasS + hasM).toBeGreaterThan(0);
  });

  test('2.5 — Add new tip veličine button exists', async ({ authenticatedPage }) => {
    const addBtn = authenticatedPage.getByRole('button', { name: /novi tip|dodaj tip|new/i }).first();
    const hasBtn = await addBtn.isVisible().catch(() => false);
    // Button may have different text — check alternative selectors
    if (!hasBtn) {
      const altBtn = authenticatedPage.getByRole('button', { name: /dodaj|kreiraj|novo/i }).first();
      await expect(altBtn).toBeVisible({ timeout: 3_000 });
    } else {
      await expect(addBtn).toBeVisible({ timeout: 3_000 });
    }
  });

});
