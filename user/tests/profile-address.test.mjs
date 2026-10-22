import test from 'node:test'
import assert from 'node:assert/strict'

import { buildAddressSubmitData, buildDefaultAddressPayload } from '../src/utils/profileAddress.mjs'

test('consumer address requests never submit user ownership fields', () => {
  const payload = buildAddressSubmitData({
    id: 12,
    addressId: 34,
    userId: 99,
    createUser: 99,
    updateUser: 99,
    receiveName: '测试用户',
    provinceCode: ['110000'],
    cityCode: ['110000'],
    areaCode: ['110101']
  })

  assert.deepEqual(payload, {
    receiveName: '测试用户',
    provinceCode: '110000',
    cityCode: '110000',
    areaCode: '110101'
  })
})

test('address edits keep their record ID while adds leave IDs to the server', () => {
  const address = { id: 12, addressId: 34, receiveName: '测试用户' }

  assert.deepEqual(buildAddressSubmitData(address), { receiveName: '测试用户' })
  assert.deepEqual(buildAddressSubmitData(address, { isEdit: true }), address)
})

test('setting a default address sends only its address ID', () => {
  assert.deepEqual(buildDefaultAddressPayload(34), { addressId: 34 })
})
