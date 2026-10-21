const PROVINCES = [
  { code: '110000', name: '北京市' },
  { code: '120000', name: '天津市' },
  { code: '130000', name: '河北省' },
  { code: '140000', name: '山西省' },
  { code: '150000', name: '内蒙古自治区' },
  { code: '210000', name: '辽宁省' },
  { code: '220000', name: '吉林省' },
  { code: '230000', name: '黑龙江省' },
  { code: '310000', name: '上海市' },
  { code: '320000', name: '江苏省' },
  { code: '330000', name: '浙江省' },
  { code: '340000', name: '安徽省' },
  { code: '350000', name: '福建省' },
  { code: '360000', name: '江西省' },
  { code: '370000', name: '山东省' },
  { code: '410000', name: '河南省' },
  { code: '420000', name: '湖北省' },
  { code: '430000', name: '湖南省' },
  { code: '440000', name: '广东省' },
  { code: '450000', name: '广西壮族自治区' },
  { code: '460000', name: '海南省' },
  { code: '500000', name: '重庆市' },
  { code: '510000', name: '四川省' },
  { code: '520000', name: '贵州省' },
  { code: '530000', name: '云南省' },
  { code: '540000', name: '西藏自治区' },
  { code: '610000', name: '陕西省' },
  { code: '620000', name: '甘肃省' },
  { code: '630000', name: '青海省' },
  { code: '640000', name: '宁夏回族自治区' },
  { code: '650000', name: '新疆维吾尔自治区' }
]

export class AreaDataService {
  constructor({
    fetchImpl = (...args) => globalThis.fetch(...args),
    setTimeoutImpl = (...args) => globalThis.setTimeout(...args),
    clearTimeoutImpl = (...args) => globalThis.clearTimeout(...args),
    timeoutMs = 8000
  } = {}) {
    this.cache = new Map()
    this.fetchImpl = fetchImpl
    this.setTimeoutImpl = setTimeoutImpl
    this.clearTimeoutImpl = clearTimeoutImpl
    this.timeoutMs = timeoutMs
  }

  async getProvinces() {
    return PROVINCES.map(province => ({ ...province }))
  }

  async getCities(provinceCode) {
    return this.getCachedChildren(`cities_${provinceCode}`, provinceCode)
  }

  async getAreas(cityCode) {
    return this.getCachedChildren(`areas_${cityCode}`, cityCode)
  }

  async getCachedChildren(cacheKey, parentCode) {
    if (this.cache.has(cacheKey)) return this.cache.get(cacheKey)

    const children = await this.fetchChildren(parentCode)
    this.cache.set(cacheKey, children)
    return children
  }

  async fetchChildren(parentCode) {
    const controller = new AbortController()
    const timeout = this.setTimeoutImpl(() => controller.abort(), this.timeoutMs)

    try {
      const response = await this.fetchImpl(
        `https://geo.datav.aliyun.com/areas_v3/bound/${parentCode}_full.json`,
        { signal: controller.signal }
      )
      if (!response.ok) throw new Error(`HTTP ${response.status}`)

      const data = await response.json()
      if (!Array.isArray(data?.features) || data.features.length === 0) {
        throw new Error('地区数据格式无效或当前区域无下级地区')
      }

      return data.features.map(feature => {
        const properties = feature?.properties
        if (properties?.adcode == null || typeof properties.name !== 'string' || !properties.name) {
          throw new Error('地区数据格式无效')
        }
        return { code: String(properties.adcode), name: properties.name }
      })
    } catch (error) {
      const message = error?.name === 'AbortError' ? '地区数据服务响应超时' : '地区数据服务暂不可用'
      throw new Error(message, { cause: error })
    } finally {
      this.clearTimeoutImpl(timeout)
    }
  }
}
