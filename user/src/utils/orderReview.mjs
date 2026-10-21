function hasValidProductId(productId) {
  return productId !== null
    && productId !== undefined
    && productId !== ''
    && Number.isFinite(Number(productId))
    && Number(productId) > 0
}

export function buildOrderReviewItems(order) {
  const details = order?.orderDetails
  if (!Array.isArray(details) || details.length === 0 || details.some(item => !hasValidProductId(item?.productId))) {
    return []
  }

  return details.map(item => ({
    productId: item.productId,
    productName: item.productName,
    productMainImage: item.productMainImage,
    score: 5,
    content: '',
    isAnonymous: false
  }))
}

export async function submitOrderReviewBatch(order, reviewItems, submitReview, completeOrder) {
  if (!order?.id || !order?.orderNo || !Array.isArray(reviewItems) || reviewItems.length === 0) {
    throw new Error('订单评价信息不完整')
  }

  for (const item of reviewItems) {
    await submitReview({
      productId: item.productId,
      orderId: order.id,
      content: item.content || '用户未填写评价内容',
      score: item.score,
      isAnonymous: item.isAnonymous ? 1 : 0
    })
  }

  await completeOrder(order.orderNo)
}
