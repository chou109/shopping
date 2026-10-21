export const addressLabelOptions = [
  { value: 1, label: '家' },
  { value: 2, label: '公司' },
  { value: 3, label: '学校' },
  { value: 4, label: '其他' }
]

export const getAddressLabelName = label =>
  addressLabelOptions.find(option => option.value === Number(label))?.label || '其他'

export const formatFullAddress = ({ province, city, area, street } = {}) => {
  const regions = [province, city, area]
    .filter(Boolean)
    .filter((name, index, values) => values.indexOf(name) === index)
  return `${regions.join('')}${street || ''}`
}
