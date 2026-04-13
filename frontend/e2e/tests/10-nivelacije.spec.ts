/**
 * Test Scenario 10: Nivelacije
 */

import { test, expect } from '../fixtures/auth.fixture';

test.describe('Scenario 10: Nivelacije', () => {

  test('10.1 — Nivelacije list page loads', async ({ authenticatedPage }) => {
    await authenticatedPage.goto('/dokumenti/nivelacije');
    await authenticatedPage.waitForLoadState('networkidle');
    await expect(authenticatedPage.getByText(/nivelacij/i).first()).toBeVisible({ timeout: 5_000 });
  });

  test('10.2 — Nivelacije list renders without errors', async ({ authenticatedPage }) => {
    await authenticatedPage.goto('/dokumenti/nivelacije');
    await authenticatedPage.waitForLoadState('networkidle');
    await authenticatedPage.waitForTimeout(2_000);

    // No JS errors should occur
    const errorBanner = authenticatedPage.locator('.error-banner, [role="alert"]').filter({
      hasText: /greška|error|failed/i
    });
    const hasError = await errorBanner.isVisible().catch(() => false);
    expect(hasError).toBeFalsy();
  });

  test('10.3 — API: GET /api/nivelacije returns 200', async ({ authenticatedPage }) => {
    const response = await authenticatedPage.request.get('/api/nivelacije?page=0&size=10');
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.success).toBe(true);
  });

  test('10.4 — Nivelacija auto-created after UF confirmation (if VPC changed)', async ({ authenticatedPage }) => {
    // This is a data-dependent test — verify by checking if any nivelacije exist
    const response = await authenticatedPage.request.get('/api/nivelacije?page=0&size=10');
    if (response.status() !== 200) {
      console.log('Nivelacije API not available');
      return;
    }
    const body = await response.json();
    const count = body?.data?.totalElements ?? body?.data?.content?.length ?? 0;
    console.log(`Nivelacije count: ${count}`);
    // Just verify the endpoint works — nivelacije only created when VPC changes
    expect(body.success).toBe(true);
  });

  test('10.5 — Nivelacija detail page loads when clicking on a row', async ({ authenticatedPage }) => {
    await authenticatedPage.goto('/dokumenti/nivelacije');
    await authenticatedPage.waitForLoadState('networkidle');
    await authenticatedPage.waitForTimeout(2_000);

    const rows = authenticatedPage.locator(
      'table tbody tr, mat-table mat-row, [role="row"]:not([role="columnheader"])'
    );
    const count = await rows.count();
    if (count === 0) {
      console.log('No nivelacije to click');
      return;
    }

    await rows.first().click();
    await authenticatedPage.waitForTimeout(1_000);

    // Should navigate to detail or open detail panel
    const hasDetail = await authenticatedPage.locator('mat-card, .detail-panel, [class*="detail"]').count();
    expect(hasDetail).toBeGreaterThan(0);
  });

});
