const { findItemLocator } = require('../utils/pagination');

module.exports = async function deleteFlow(page, uniqueName) {

    const item = await findItemLocator(page, uniqueName);

    if (!item) {
        throw new Error("Item not found for delete");
    }

    const deletePromise = page.waitForResponse(res =>
        res.request().method() === 'DELETE' &&
        res.url().includes('restaurant')
    ).catch(() => null);

    await item.locator('[data-testid^="delete-btn-"]').first().click();

    await page.getByTestId('confirm-delete-btn').click();

    const res = await deletePromise;

    return {
        ok: !!res && res.ok()
    };
};