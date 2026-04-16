const { chromium } = require('playwright');
const createFlow = require('../flows/create.flow');
const deleteFlow = require('../flows/delete.flow');
const { existsAcrossPages, findItemLocator } = require('../utils/pagination'); // Lấy cả 2 hàm
const { sleep } = require('../utils/wait');

async function run(url) {
    const browser = await chromium.launch({
        headless: true, // Để false để debug như bạn muốn
    });

    const page = await browser.newPage();
    let score = 0;
    const details = [];
    const uniqueName = `Test_${Date.now()}`;

    try {
        await page.goto(url, { waitUntil: 'domcontentloaded' });
        await page.waitForSelector('[data-testid^="item-"]', { timeout: 15000 });

        // 1. CREATE FLOW
        const createResult = await createFlow(page, uniqueName);
        if (createResult.ok) {
            score += 5;
            details.push("✅ Create API OK");
        }

        // 2. VERIFY CREATE
        await page.goto(url);
        await sleep(800);
        const created = await existsAcrossPages(page, uniqueName);
        if (created) {
            score += 5;
            details.push("✅ Create reflected in UI");
        } else {
            details.push("❌ Create NOT found in UI");
        }

        // 3. DELETE FLOW
        await page.goto(url);
        await sleep(800);
        const deleteResult = await deleteFlow(page, uniqueName);
        if (deleteResult.ok) {
            // 4. VERIFY DELETE
            await page.goto(url);
            await sleep(800);
            const stillExists = await existsAcrossPages(page, uniqueName);
            if (!stillExists) {
                score += 5;
                details.push("✅ Delete OK");
            } else {
                details.push("❌ Delete still exists in UI");
            }
        }

    } catch (err) {
        details.push("❌ Error: " + err.message);
    } finally {
        await browser.close();
        // In kết quả cuối cùng để Java đọc
        console.log(JSON.stringify({
            success: score === 15, // File gốc của bạn có 3 bước tính điểm (5+5+5)
            score,
            details
        }, null, 2));
    }
}

// --- ĐOẠN QUAN TRỌNG NHẤT ĐỂ CHẠY ĐƯỢC ---
const urlArg = process.argv[2];
if (urlArg) {
    run(urlArg);
} else {
    console.error("Missing URL argument!");
}