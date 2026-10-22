const normalizeRegionCode = (value) => Array.isArray(value) ? value[0] : value

export function buildAddressSubmitData(form, { isEdit = false } = {}) {
  const {
    userId: _userId,
    createUser: _createUser,
    updateUser: _updateUser,
    ...fields
  } = form || {}

  const payload = {
    ...fields
  }
  for (const field of ['provinceCode', 'cityCode', 'areaCode']) {
    if (field in fields) payload[field] = normalizeRegionCode(fields[field])
  }

  if (!isEdit) {
    delete payload.id
    delete payload.addressId
  }

  return payload
}

export function buildDefaultAddressPayload(addressId) {
  return { addressId }
}
