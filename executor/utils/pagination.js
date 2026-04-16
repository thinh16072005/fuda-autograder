async function findItemLocator(page, name, maxPages = 20) {
    for (let i = 0; i < maxPages; i++) {

        const items = page.locator('[data-testid^="item-"]')
            .filter({ hasText: name });

        if (await items.count() > 0) {
            return items.first();
        }

        const nextBtn = page.getByRole('button', { name: /next|>|→/i });

        if (await nextBtn.count() === 0) return null;

        const disabled = await nextBtn.first().isDisabled().catch(() => true);
        if (disabled) return null;

        await nextBtn.first().click();
        await page.waitForTimeout(600);
    }

    return null;
}

async function existsAcrossPages(page, name) {
    const item = await findItemLocator(page, name);
    return item !== null;
}

module.exports = {
    findItemLocator,
    existsAcrossPages
};