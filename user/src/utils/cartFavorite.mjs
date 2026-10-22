export async function moveCartItemToFavorite({ productId, cartItemId, checkFavorite, addFavorite, removeCartItem }) {
  const status = await checkFavorite(1, productId)
  if (status?.code !== 200) {
    throw new Error(status?.msg || '暂时无法确认商品收藏状态，请重试')
  }

  const alreadyFavorite = status.data === true || status.data?.isFavorite === true
  if (!alreadyFavorite) {
    const favoriteResponse = await addFavorite(1, productId)
    const favoriteWasSaved = favoriteResponse?.data === true || favoriteResponse?.data === 1
    if (favoriteResponse?.code !== 200 || !favoriteWasSaved) {
      throw new Error(favoriteResponse?.msg || '收藏失败，商品仍保留在购物车')
    }
  }

  try {
    const removalResponse = await removeCartItem(cartItemId)
    if (removalResponse?.code !== 200) {
      throw new Error(removalResponse?.msg || '从购物车移除失败')
    }
  } catch (error) {
    const removalError = error instanceof Error ? error : new Error(String(error))
    removalError.favoriteSaved = true
    removalError.alreadyFavorite = alreadyFavorite
    throw removalError
  }

  return { alreadyFavorite }
}
