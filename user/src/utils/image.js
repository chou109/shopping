export function getProductImageUrl(imagePath) {
  if (!imagePath || imagePath === '') {
    return '/src/assets/images/default-product.png';
  }

  // 完整URL直接返回
  if (imagePath.startsWith('http') || imagePath.startsWith('//') || imagePath.startsWith('data:')) {
    return imagePath;
  }

  // 定义一个基础路径
  const basePath = '/dev-api';

  // 如果已经是基础路径开头，直接返回
  if (imagePath.startsWith(basePath)) {
    return imagePath;
  }

  // 如果以 /file/pic/ 开头
  if (imagePath.startsWith('/file/pic/')) {
    return basePath + imagePath;
  }

  // 如果以 file/pic/ 开头（没有前面的/）
  if (imagePath.startsWith('file/pic/')) {
    return basePath + '/' + imagePath;
  }

  // 如果是 src/assets/images/file/pic/xxx.jpg
  if (imagePath.includes('src/assets/images/')) {
    return '/' + imagePath;
  }

  // 其他情况：假设是相对路径，添加基础路径
  const finalPath = imagePath.startsWith('/') ? basePath + imagePath : basePath + '/' + imagePath;
  console.log('处理图片路径:', finalPath);
  return finalPath;
}

// 图片加载错误处理
export function handleImageError(event, defaultImage = '/src/assets/images/default-product.png') {
  console.log('图片加载失败:', event.target.src);
  event.target.src = defaultImage;
  event.target.onerror = null;
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