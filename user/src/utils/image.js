// 创建文件：src/utils/image.js
export function getProductImageUrl(imagePath) {
  if (!imagePath || imagePath === '') {
    // 返回默认图片
    return '/src/assets/images/default-product.png';
  }
  
  // 如果已经是完整的URL，直接返回
  if (imagePath.startsWith('http') || imagePath.startsWith('//')) {
    return imagePath;
  }
  
  // 如果是相对路径，添加基础URL
  if (imagePath.startsWith('/')) {
    return `/src/assets/images${imagePath}`;
  }
  
  // 直接使用 src/assets/images 路径
  return `/src/assets/images/${imagePath}`;
}

// 图片加载错误处理
export function handleImageError(event, defaultImage = '/src/assets/images/default-product.png') {
  console.log('图片加载失败:', event.target.src);
  event.target.src = defaultImage;
}

// 检查图片是否存在
export function checkImageExists(url) {
  return new Promise((resolve) => {
    const img = new Image();
    img.onload = () => resolve(true);
    img.onerror = () => resolve(false);
    img.src = url;
  });
}