module.exports = async function createFlow(page, uniqueName) {

    const createPromise = page.waitForResponse(res =>
        res.request().method() === 'POST' &&
        res.url().includes('restaurant')
    ).catch(() => null);

    await page.getByTestId('create-btn').click();

    await page.getByTestId('name-input').fill(uniqueName);
    await page.getByTestId('price-from-input').fill('10001');
    await page.getByTestId('price-to-input').fill('20010');
    await page.getByTestId('address-input').fill('Da Nang');
    await page.getByTestId('owner-name-input').fill('Tester');

    await page.getByTestId('category-select').selectOption({ index: 1 });
    await page.getByTestId('open-date-input').fill('2024-01-01');

    await page.getByTestId('submit-btn').click();

    const res = await createPromise;

    return {
        ok: !!res && res.ok()
    };
};