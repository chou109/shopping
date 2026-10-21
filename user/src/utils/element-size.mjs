const supportedElementSizes = new Set(['default', 'small', 'large'])

export function normalizeElementSize(size) {
  if (size === 'medium') return 'default'
  if (size === 'mini') return 'small'
  return supportedElementSizes.has(size) ? size : 'default'
}
