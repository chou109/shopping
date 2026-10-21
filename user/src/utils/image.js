import defaultProductImage from '@/assets/images/default-product.png'
import defaultAvatarImage from '@/assets/images/default-avatar.png'
import defaultShopImage from '@/assets/images/default-shop.png'
import blindBoxImage from '@/assets/images/BlindBox.png'

export const DEFAULT_PRODUCT_IMAGE = defaultProductImage
export const DEFAULT_AVATAR_IMAGE = defaultAvatarImage
export const DEFAULT_SHOP_IMAGE = defaultShopImage
export const BLIND_BOX_IMAGE = blindBoxImage

export function getProductImageUrl(imagePath) {
  // 处理Vite图片模块对象
  if (typeof imagePath === 'object' && imagePath.default) {
    imagePath = imagePath.default;
  }
  
  // 处理可能的图片对象格式（如{ url: 'xxx' }）
  if (typeof imagePath === 'object' && imagePath.url) {
    imagePath = imagePath.url;
  }
  
  // 处理数组格式（取第一个元素）
  if (Array.isArray(imagePath)) {
    imagePath = imagePath[0] || '';
    // 如果第一个元素还是对象，递归处理
    if (typeof imagePath === 'object') {
      return getProductImageUrl(imagePath);
    }
  }
  
  // 确保imagePath是字符串
  if (typeof imagePath !== 'string') {
    try {
      imagePath = String(imagePath);
    } catch (e) {
      console.error('无法将图片路径转换为字符串:', imagePath);
      return DEFAULT_PRODUCT_IMAGE;
    }
  }

  if (!imagePath || imagePath === '') {
    return DEFAULT_PRODUCT_IMAGE;
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
  return finalPath;
}

// 图片加载错误处理
export function handleImageError(event, defaultImage = DEFAULT_PRODUCT_IMAGE) {
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
