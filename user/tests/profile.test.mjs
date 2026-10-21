import test from 'node:test'
import assert from 'node:assert/strict'
import { formatFullAddress, getAddressLabelName, isMunicipality } from '../src/utils/profile.mjs'

test('formats municipality addresses without repeating province and city', () => {
  assert.equal(formatFullAddress({ province: '北京市', city: '北京市', area: '朝阳区', street: '建国路' }), '北京市朝阳区建国路')
  assert.equal(formatFullAddress({ province: '浙江省', city: '杭州市', area: '西湖区', street: '文一路' }), '浙江省杭州市西湖区文一路')
})

test('formats incomplete address data safely', () => {
  assert.equal(formatFullAddress({ city: '杭州市', street: '文一路' }), '杭州市文一路')
  assert.equal(formatFullAddress(), '')
})

test('resolves address labels for numeric and string codes', () => {
  assert.equal(getAddressLabelName(2), '公司')
  assert.equal(getAddressLabelName('3'), '学校')
  assert.equal(getAddressLabelName(99), '其他')
})

test('recognizes municipality codes whether they are numeric or strings', () => {
  assert.equal(isMunicipality('110000'), true)
  assert.equal(isMunicipality(500000), true)
  assert.equal(isMunicipality('330000'), false)
})
