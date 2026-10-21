import test from 'node:test'
import assert from 'node:assert/strict'
import { AreaDataService } from '../src/utils/areaService.mjs'

const createService = (fetchImpl, overrides = {}) => new AreaDataService({
  fetchImpl,
  setTimeoutImpl: overrides.setTimeoutImpl || (() => 1),
  clearTimeoutImpl: overrides.clearTimeoutImpl || (() => {}),
  timeoutMs: overrides.timeoutMs || 8000
})

test('returns a defensive copy of the supported province list without a network request', async () => {
  let requests = 0
  const service = createService(async () => { requests += 1 })

  const first = await service.getProvinces()
  first[0].name = '已修改'
  const second = await service.getProvinces()

  assert.equal(second[0].name, '北京市')
  assert.equal(requests, 0)
})

test('loads and caches city and area data separately', async () => {
  const requests = []
  const service = createService(async url => {
    requests.push(url)
    return {
      ok: true,
      json: async () => ({ features: [{ properties: { adcode: 110100, name: '北京市' } }] })
    }
  })

  const cities = await service.getCities('110000')
  const cachedCities = await service.getCities('110000')
  const areas = await service.getAreas('110000')

  assert.deepEqual(cities, [{ code: '110100', name: '北京市' }])
  assert.equal(cachedCities, cities)
  assert.deepEqual(areas, cities)
  assert.equal(requests.length, 2)
  assert.match(requests[0], /110000_full\.json$/)
})

test('reports HTTP and malformed data responses as unavailable', async () => {
  const httpFailure = createService(async () => ({ ok: false, status: 503 }))
  const malformedData = createService(async () => ({ ok: true, json: async () => ({ features: [] }) }))
  const malformedFeature = createService(async () => ({
    ok: true,
    json: async () => ({ features: [{ properties: { name: '缺少编码' } }] })
  }))

  await assert.rejects(httpFailure.getCities('110000'), /地区数据服务暂不可用/)
  await assert.rejects(malformedData.getAreas('110100'), /地区数据服务暂不可用/)
  await assert.rejects(malformedFeature.getCities('110000'), /地区数据服务暂不可用/)
})

test('converts request timeouts to a clear error and clears the timer', async () => {
  let timerCleared = false
  const service = createService(async (_url, { signal }) => {
    if (signal.aborted) {
      const error = new Error('aborted')
      error.name = 'AbortError'
      throw error
    }
    return { ok: true, json: async () => ({ features: [] }) }
  }, {
    setTimeoutImpl: callback => {
      callback()
      return 7
    },
    clearTimeoutImpl: timer => { timerCleared = timer === 7 }
  })

  await assert.rejects(service.getCities('110000'), /地区数据服务响应超时/)
  assert.equal(timerCleared, true)
})
