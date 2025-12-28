import request from '@/utils/request'
import { getShopProducts } from '@/api/system/shop'
import { listProductRandom } from '@/api/system/product'
import { ref } from 'vue'

export function useBlindBox() {
    const isLoading = ref(false)
    const isPoolReady = ref(true)

    // Helper to get random item from array
    const getRandom = (arr) => {
        if (!arr || arr.length === 0) return null
        return arr[Math.floor(Math.random() * arr.length)]
    }

    // Draw logic
    const drawPrize = async () => {
        isLoading.value = true
        try {
            // Variable declarations
            let winningShopId
            let currentShopId
            const others = [6, 7, 8, 9, 12, 13, 14]

            // Probability Logic
            const randomVal = Math.random() * 100

            if (randomVal < 85) {
                // 85% 概率 - Shop 10 (酷派数码配件)
                winningShopId = 10
                console.log(`Lottery: Hit Shop 10 (85% Tier)`)
            } else if (randomVal < 99) {
                // 14% 概率 - Shop 11 (罗马仕充电宝)
                winningShopId = 11
                console.log(`Lottery: Hit Shop 11 (14% Tier)`)
            } else {
                // 1% 概率 - Others
                winningShopId = getRandom(others)
                console.log(`Lottery: Hit Shop ${winningShopId} (1% Tier)`)
            }

            // Fetch Strategy:
            // 1. Try Winning Shop
            // 2. If empty, try Shop 10 (Main)
            // 3. If empty, try Shop 11 (Backup)
            // 4. If empty, try Global Random (Last Resort)

            let products = []

            // Attempt 1: Target Shop
            try {
                const res = await getShopProducts(winningShopId)
                if (res.code === 200 && res.rows && res.rows.length > 0) {
                    products = res.rows
                    currentShopId = winningShopId
                } else {
                    console.warn(`Target Shop ${winningShopId} is empty.`)
                }
            } catch (e) {
                console.warn(`Failed to fetch Shop ${winningShopId}`, e)
            }

            // Attempt 2: Shop 10 (if target wasn't 10)
            if (products.length === 0 && winningShopId !== 10) {
                try {
                    const res = await getShopProducts(10)
                    if (res.code === 200 && res.rows && res.rows.length > 0) {
                        products = res.rows
                        currentShopId = 10
                        console.log("Fallback to Shop 10")
                    }
                } catch (e) { }
            }

            // Attempt 3: Shop 11 (if target/10 empty)
            if (products.length === 0 && currentShopId !== 11) {
                try {
                    const res = await getShopProducts(11)
                    if (res.code === 200 && res.rows && res.rows.length > 0) {
                        products = res.rows
                        currentShopId = 11
                        console.log("Fallback to Shop 11")
                    }
                } catch (e) { }
            }

            // Attempt 4: Global Random
            if (products.length === 0) {
                console.warn("All specific shops empty. Using global fallback.")
                try {
                    const fallbackRes = await listProductRandom({ limit: 10 })
                    if (fallbackRes.rows && fallbackRes.rows.length > 0) {
                        products = fallbackRes.rows
                        // Determine shop from product if possible
                        currentShopId = products[0].shopId || 10
                    }
                } catch (e) {
                    console.error("Global fallback failed", e)
                }
            }

            if (products.length === 0) {
                // Absolute Last Resort
                products = [{
                    id: 99999,
                    productName: '神秘幸运大奖',
                    productMainImage: '',
                    productUnitPrice: 99.00,
                    shopId: 10
                }]
                currentShopId = 10
            }

            const product = getRandom(products)
            console.log("Raw won product:", product)

            // Normalize product fields (handle both formats)
            const name = product.productName || product.name || '未知商品'
            const price = product.productUnitPrice || product.price || 0
            const image = product.productMainImage || product.mainImage || ''

            console.log("Won product:", name, "ID:", product.id, "Shop:", currentShopId)

            return {
                ...product,
                shopId: product.shopId || currentShopId,
                id: product.id,
                // Ensure standardized fields exist
                name: name,
                productName: name,
                mainImage: image,
                productMainImage: image,
                price: price,
                productUnitPrice: price
            }

        } catch (e) {
            console.error("Lottery failed", e)
            throw e // Propagate error so UI handles it
        } finally {
            isLoading.value = false
        }
    }

    // Backward compatibility
    const initLotteryPool = async () => {
        // No-op or pre-check
        isPoolReady.value = true
    }

    return {
        drawPrize,
        initLotteryPool,
        isLoading,
        isPoolReady
    }
}
