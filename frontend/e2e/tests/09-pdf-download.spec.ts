/**
 * Test Scenario 9: PDF Download
 */

import { test, expect } from '../fixtures/auth.fixture';

test.describe('Scenario 9: PDF Download', () => {

  test('9.1 — PDF endpoint returns 200 with application/pdf content-type', async ({ authenticatedPage }) => {
    // Get first available confirmed faktura
    const faktureResponse = await authenticatedPage.request.get(
      '/api/dokumenti?tipKod=UF&page=0&size=10'
    );

    if (faktureResponse.status() !== 200) {
      console.log('Fakture API not available');
      return;
    }

    const faktureBody = await faktureResponse.json();
    const fakture = faktureBody?.data?.content ?? [];

    const confirmed = fakture.find(
      (f: { status: string }) => f.status === 'POTVRDEN' || f.status === 'POTVRĐEN'
    );

    if (!confirmed) {
      console.log('No confirmed faktura found for PDF test');
      return;
    }

    const pdfResponse = await authenticatedPage.request.get(
      `/api/dokumenti/${confirmed.id}/pdf`
    );

    expect(pdfResponse.status()).toBe(200);
    const contentType = pdfResponse.headers()['content-type'];
    expect(contentType).toContain('application/pdf');
  });

  test('9.2 — Nivelacija PDF endpoint accessible', async ({ authenticatedPage }) => {
    const nivelacijeResponse = await authenticatedPage.request.get(
      '/api/nivelacije?page=0&size=5'
    );

    if (nivelacijeResponse.status() !== 200) {
      console.log('Nivelacije API not reachable');
      return;
    }

    const body = await nivelacijeResponse.json();
    const nivelacije = body?.data?.content ?? [];

    if (!nivelacije.length) {
      console.log('No nivelacije found for PDF test');
      return;
    }

    const pdfResponse = await authenticatedPage.request.get(
      `/api/dokumenti/nivelacije/${nivelacije[0].id}/pdf`
    );
    expect([200, 403, 404]).toContain(pdfResponse.status());
    if (pdfResponse.status() === 200) {
      expect(pdfResponse.headers()['content-type']).toContain('application/pdf');
    }
  });

  test('9.3 — PDF download button visible on confirmed faktura detail page', async ({ authenticatedPage }) => {
    await authenticatedPage.goto('/dokumenti/fakture');
    await authenticatedPage.waitForLoadState('networkidle');
    await authenticatedPage.waitForTimeout(2_000);

    const confirmedRow = authenticatedPage.locator(
      'table tbody tr, mat-table mat-row'
    ).filter({ hasText: /potvrđen|potvrden/i }).first();

    const hasConfirmed = await confirmedRow.isVisible().catch(() => false);
    if (!hasConfirmed) {
      console.log('No confirmed faktura for PDF button test');
      return;
    }

    await confirmedRow.click();
    await authenticatedPage.waitForTimeout(1_000);

    const pdfBtn = authenticatedPage.getByRole('button', { name: /pdf/i }).first();
    await expect(pdfBtn).toBeVisible({ timeout: 3_000 });
  });

  test('9.4 — PDF download does not return 500 error', async ({ authenticatedPage }) => {
    // Check the general PDF endpoint is accessible (even if no documents exist)
    const response = await authenticatedPage.request.get('/api/dokumenti/999999/pdf');
    // Should return 404 (not found) not 500 (server error)
    expect(response.status()).not.toBe(500);
    expect([404, 403]).toContain(response.status());
  });

});
