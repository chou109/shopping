import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AreaDataService } from '@/utils/areaService.mjs'
import { addressLabelOptions, formatFullAddress, getAddressLabelName, isMunicipality } from '@/utils/profile.mjs'
import {
  getMyAddressList,
  addAddress,
  updateAddress,
  delAddress,
  setDefaultAddress
} from '@/api/system/address'

export function useProfileAddresses(currentUserId) {
  const addressFormRef = ref()
  const isLoadingEditData = ref(false)
  const addressDialogVisible = ref(false)
  const addressLoading = ref(false)
  const addressMutations = ref(new Set())
  const isEditAddress = ref(false)
  const currentAddressId = ref(null)

  // 地址列表数据
  const addressList = ref([])
  const addressListLoading = ref(false)
  const addressListError = ref('')
  let addressListRequestId = 0

  // 地址表单数据
  const addressForm = reactive({
    addressId: null,
    receiveName: '',
    receivePhone: '',
    province: '',
    provinceCode: '',
    city: '',
    cityCode: '',
    area: '',
    areaCode: '',
    street: '',
    postalCode: '',
    addressLabel: 1,
    defaultFlag: 0,
    userId: null
  })

  // 地址验证规则
  const addressRules = {
    receiveName: [
      { required: true, message: '请输入收货人姓名', trigger: 'blur' },
      { min: 2, max: 30, message: '姓名长度在 2 到 30 个字符', trigger: 'blur' }
    ],
    receivePhone: [
      { required: true, message: '请输入手机号码', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ],
    provinceCode: [
      { required: true, message: '请选择省级', trigger: 'change' }
    ],
    cityCode: [
      {
        validator: (rule, value, callback) => {
          // 直辖市不需要选择地级
          if (isMunicipality(addressForm.provinceCode)) {
            callback()
          } else if (!value) {
            callback(new Error('请选择地级'))
          } else {
            callback()
          }
        },
        trigger: 'change'
      }
    ],
    areaCode: [
      { required: true, message: '请选择县级', trigger: 'change' }
    ],
    street: [
      { required: true, message: '请输入详细地址', trigger: 'blur' },
      { min: 5, max: 200, message: '详细地址长度在 5 到 200 个字符', trigger: 'blur' }
    ]
  }

  const areaService = new AreaDataService()
  const provinceLoading = ref(false)
  const cityLoading = ref(false)
  const areaLoading = ref(false)
  const regionLoadError = ref('')
  const provinces = ref([])
  const cities = ref([])
  const areas = ref([])
  let provinceRequestId = 0
  let cityRequestId = 0
  let provinceListRequestId = 0
  let addressEditRequestId = 0

  // 加载省份数据
  const loadProvinces = async () => {
    const requestId = ++provinceListRequestId
    provinceLoading.value = true
    regionLoadError.value = ''
    try {
      const result = await areaService.getProvinces()
      if (requestId !== provinceListRequestId) return
      if (!Array.isArray(result)) throw new Error('省份数据格式异常')
      provinces.value = result
    } catch (error) {
      if (requestId === provinceListRequestId) {
        console.error('加载省份数据失败:', error)
        regionLoadError.value = '省份数据加载失败，请检查网络后重试。'
      }
    } finally {
      if (requestId === provinceListRequestId) provinceLoading.value = false
    }
  }

  // 直辖市代码列表
  // 省份改变时加载城市
  const handleProvinceChange = async (provinceCode) => {
    regionLoadError.value = ''
    const requestId = ++provinceRequestId
    // 省份变化后，之前按旧省份发出的城市/区县请求不能再写入表单。
    cityRequestId++

    // 确保 provinceCode 是字符串而不是数组
    if (Array.isArray(provinceCode)) {
      provinceCode = provinceCode[0];
    }

    // 清空下级数据
    addressForm.cityCode = '';
    addressForm.areaCode = '';
    addressForm.city = '';
    addressForm.area = '';
    cities.value = [];
    areas.value = [];

    if (!provinceCode) return;

    // 设置省份名称
    const province = provinces.value.find(p => p.code === provinceCode);
    if (province) {
      addressForm.province = province.name;
      addressForm.provinceCode = provinceCode;
    }

    // 如果是直辖市，直接加载区县数据，跳过地级市
    if (isMunicipality(provinceCode)) {

      // 直辖市没有地级市，直接设置city为直辖市名称
      addressForm.city = province.name;
      addressForm.cityCode = provinceCode; // 使用省份代码作为城市代码

      // 直接加载区县数据
      areaLoading.value = true;
      try {
        // 直辖市的数据通常挂在省级代码下
        areas.value = await areaService.getAreas(provinceCode);
        if (requestId !== provinceRequestId || addressForm.provinceCode !== provinceCode) return
      } catch (error) {
        if (requestId !== provinceRequestId || addressForm.provinceCode !== provinceCode) return
        console.error('加载直辖市区县数据失败:', error);
        // 尝试备用方案：使用直辖市的标准区县代码
        areas.value = await loadMunicipalityAreas(provinceCode);
        if (areas.value.length === 0) {
          regionLoadError.value = '地区数据服务暂不可用，请检查网络后重试。'
        }
      } finally {
        if (requestId === provinceRequestId) areaLoading.value = false;
      }
    } else {
      // 非直辖市，正常加载城市数据
      cityLoading.value = true;
      try {
        cities.value = await areaService.getCities(provinceCode);
        if (requestId !== provinceRequestId || addressForm.provinceCode !== provinceCode) return
      } catch (error) {
        if (requestId !== provinceRequestId || addressForm.provinceCode !== provinceCode) return
        console.error('加载城市失败:', error);
        regionLoadError.value = '城市数据加载失败，请检查网络后重试。'
      } finally {
        if (requestId === provinceRequestId) cityLoading.value = false;
      }
    }
  };

  // 城市改变时加载区县
  const handleCityChange = async (cityCode) => {
    regionLoadError.value = ''
    const requestId = ++cityRequestId

    // 确保 cityCode 是字符串而不是数组
    if (Array.isArray(cityCode)) {
      cityCode = cityCode[0];
    }

    // 清空区域数据
    addressForm.areaCode = '';
    addressForm.area = '';
    areas.value = [];

    if (!cityCode) return;

    // 设置城市名称
    const city = cities.value.find(c => c.code === cityCode);
    if (city) {
      addressForm.city = city.name;
      addressForm.cityCode = cityCode;
    }

    // 如果是直辖市，已经在上一步处理了区县数据，这里不需要重复加载
    const currentProvinceCode = addressForm.provinceCode;
    if (currentProvinceCode && isMunicipality(currentProvinceCode)) {
      return;
    }

    // 非直辖市，正常加载区域
    areaLoading.value = true;
    try {
      areas.value = await areaService.getAreas(cityCode);
      if (requestId !== cityRequestId || addressForm.cityCode !== cityCode) return
    } catch (error) {
      if (requestId !== cityRequestId || addressForm.cityCode !== cityCode) return
      console.error('加载区域失败:', error);
      regionLoadError.value = '区县数据加载失败，请检查网络后重试。'
    } finally {
      if (requestId === cityRequestId) areaLoading.value = false;
    }
  };

  const retryRegionLoad = async () => {
    if (!addressForm.provinceCode) {
      await loadProvinces()
      return
    }
    if (cityLoading.value || areaLoading.value) return
    regionLoadError.value = ''
    if (isEditAddress.value) {
      await loadEditAddressData({ ...addressForm })
    } else {
      await handleProvinceChange(addressForm.provinceCode)
    }
  }

  // 添加直辖市区县数据备用加载函数
  const loadMunicipalityAreas = async (provinceCode) => {
    // 直辖市的标准区县数据
    const municipalityAreas = {
      '110000': [ // 北京
        { code: '110101', name: '东城区' }, { code: '110102', name: '西城区' },
        { code: '110105', name: '朝阳区' }, { code: '110106', name: '丰台区' },
        { code: '110107', name: '石景山区' }, { code: '110108', name: '海淀区' },
        { code: '110109', name: '门头沟区' }, { code: '110111', name: '房山区' },
        { code: '110112', name: '通州区' }, { code: '110113', name: '顺义区' },
        { code: '110114', name: '昌平区' }, { code: '110115', name: '大兴区' },
        { code: '110116', name: '怀柔区' }, { code: '110117', name: '平谷区' },
        { code: '110118', name: '密云区' }, { code: '110119', name: '延庆区' }
      ],
      '120000': [ // 天津
        { code: '120101', name: '和平区' }, { code: '120102', name: '河东区' },
        { code: '120103', name: '河西区' }, { code: '120104', name: '南开区' },
        { code: '120105', name: '河北区' }, { code: '120106', name: '红桥区' },
        { code: '120110', name: '东丽区' }, { code: '120111', name: '西青区' },
        { code: '120112', name: '津南区' }, { code: '120113', name: '北辰区' },
        { code: '120114', name: '武清区' }, { code: '120115', name: '宝坻区' },
        { code: '120116', name: '滨海新区' }, { code: '120117', name: '宁河区' },
        { code: '120118', name: '静海区' }, { code: '120119', name: '蓟州区' }
      ],
      '310000': [ // 上海
        { code: '310101', name: '黄浦区' }, { code: '310104', name: '徐汇区' },
        { code: '310105', name: '长宁区' }, { code: '310106', name: '静安区' },
        { code: '310107', name: '普陀区' }, { code: '310109', name: '虹口区' },
        { code: '310110', name: '杨浦区' }, { code: '310112', name: '闵行区' },
        { code: '310113', name: '宝山区' }, { code: '310114', name: '嘉定区' },
        { code: '310115', name: '浦东新区' }, { code: '310116', name: '金山区' },
        { code: '310117', name: '松江区' }, { code: '310118', name: '青浦区' },
        { code: '310120', name: '奉贤区' }, { code: '310151', name: '崇明区' }
      ],
      '500000': [ // 重庆
        { code: '500101', name: '万州区' }, { code: '500102', name: '涪陵区' },
        { code: '500103', name: '渝中区' }, { code: '500104', name: '大渡口区' },
        { code: '500105', name: '江北区' }, { code: '500106', name: '沙坪坝区' },
        { code: '500107', name: '九龙坡区' }, { code: '500108', name: '南岸区' },
        { code: '500109', name: '北碚区' }, { code: '500110', name: '綦江区' },
        { code: '500111', name: '大足区' }, { code: '500112', name: '渝北区' },
        { code: '500113', name: '巴南区' }, { code: '500114', name: '黔江区' },
        { code: '500115', name: '长寿区' }, { code: '500116', name: '江津区' },
        { code: '500117', name: '合川区' }, { code: '500118', name: '永川区' },
        { code: '500119', name: '南川区' }, { code: '500120', name: '璧山区' },
        { code: '500151', name: '铜梁区' }, { code: '500152', name: '潼南区' },
        { code: '500153', name: '荣昌区' }, { code: '500154', name: '开州区' },
        { code: '500155', name: '梁平区' }, { code: '500156', name: '武隆区' }
      ]
    };

    return municipalityAreas[provinceCode] || [];
  };

  // 区县改变时更新数据
  const handleAreaChange = (areaCode) => {

    // 确保 areaCode 是字符串而不是数组
    if (Array.isArray(areaCode)) {
      areaCode = areaCode[0];
    }

    if (!areaCode) {
      addressForm.area = '';
      addressForm.areaCode = '';
      return;
    }

    // 设置区域名称
    const area = areas.value.find(a => a.code === areaCode);
    if (area) {
      addressForm.area = area.name;
      addressForm.areaCode = areaCode;
    }
  };

  // 加载地址列表
  const loadAddressList = async () => {
    const requestId = ++addressListRequestId
    addressListLoading.value = true
    addressListError.value = ''
    try {
      const response = await getMyAddressList()
      if (requestId !== addressListRequestId) return
      if (response.code !== 200 || !Array.isArray(response.data)) {
        throw new Error(response.msg || '地址数据格式异常')
      }
      addressList.value = response.data
    } catch (error) {
      if (requestId === addressListRequestId) {
        console.error('加载地址列表失败:', error)
        addressListError.value = '收货地址加载失败，请重试'
      }
    } finally {
      if (requestId === addressListRequestId) addressListLoading.value = false
    }
  }

  // 打开新增地址对话框
  const openAddAddressDialog = () => {
    addressEditRequestId++
    isLoadingEditData.value = false
    cityLoading.value = false
    areaLoading.value = false
    isEditAddress.value = false
    currentAddressId.value = null

    // 重置表单
    Object.keys(addressForm).forEach(key => {
      if (key === 'addressLabel') {
        addressForm[key] = 1
      } else if (key === 'defaultFlag') {
        addressForm[key] = 0
      } else if (key === 'addressId') {
        // 生成一个临时的 addressId
        addressForm[key] = Date.now()
      } else {
        addressForm[key] = ''
      }
    })

    // 设置 userId
    addressForm.userId = currentUserId.value

    addressDialogVisible.value = true

    // 重置省市区数据
    cities.value = []
    areas.value = []

    // 加载省份数据
    if (provinces.value.length === 0) {
      loadProvinces()
    }
  }

  // 打开编辑地址对话框
  const openEditAddressDialog = async (address) => {
    const requestId = ++addressEditRequestId
    isLoadingEditData.value = true
    isEditAddress.value = true
    currentAddressId.value = address.id

    // 重置表单（不清空省市区编码，等待数据加载后设置）
    Object.keys(addressForm).forEach(key => {
      if (key === 'addressLabel') {
        addressForm[key] = 1
      } else if (key === 'defaultFlag') {
        addressForm[key] = 0
      } else if (key === 'addressId') {
        addressForm[key] = address[key] || Date.now()
      } else {
        addressForm[key] = ''
      }
    })

    // 设置基本字段
    addressForm.id = address.id
    addressForm.userId = currentUserId.value
    addressForm.receiveName = address.receiveName || ''
    addressForm.receivePhone = address.receivePhone || ''
    addressForm.street = address.street || ''
    addressForm.postalCode = address.postalCode || ''
    addressForm.addressLabel = address.addressLabel || 1
    addressForm.defaultFlag = address.defaultFlag || 0

    // 确保省份数据已加载
    if (provinces.value.length === 0) {
      await loadProvinces()
    }
    if (requestId !== addressEditRequestId) return

    // 先打开对话框
    addressDialogVisible.value = true

    // 等待对话框DOM渲染完成
    await nextTick()
    if (requestId !== addressEditRequestId) return

    // 加载并设置省市区数据
    try {
      await loadEditAddressData(address, requestId)
    } finally {
      if (requestId === addressEditRequestId) isLoadingEditData.value = false
    }

  }

  // 监听省份编码变化，自动加载城市（仅在新增时触发）
  watch(() => addressForm.provinceCode, async (newVal, oldVal) => {
    // 编辑模式下不触发自动加载
    if (isEditAddress.value || isLoadingEditData.value) return
    if (!newVal || newVal === oldVal) return

    // 确保是字符串
    if (Array.isArray(newVal)) {
      newVal = newVal[0]
      addressForm.provinceCode = newVal
    }

    // 设置省份名称
    const province = provinces.value.find(p => p.code === newVal)
    if (province && addressForm.province !== province.name) {
      addressForm.province = province.name
    }

    // 清空下级数据
    addressForm.cityCode = ''
    addressForm.areaCode = ''
    addressForm.city = ''
    addressForm.area = ''
    cities.value = []
    areas.value = []
  })

  // 监听城市编码变化，自动加载区域（仅在新增时触发）
  watch(() => addressForm.cityCode, async (newVal, oldVal) => {
    // 编辑模式下不触发自动加载
    if (isEditAddress.value || isLoadingEditData.value) return
    if (!newVal || newVal === oldVal) return

    // 确保是字符串
    if (Array.isArray(newVal)) {
      newVal = newVal[0]
      addressForm.cityCode = newVal
    }

    // 设置城市名称
    const city = cities.value.find(c => c.code === newVal)
    if (city && addressForm.city !== city.name) {
      addressForm.city = city.name
    }

    // 清空区域数据
    addressForm.areaCode = ''
    addressForm.area = ''
    areas.value = []
  })

  // 监听区域编码变化
  watch(() => addressForm.areaCode, (newVal) => {
    if (!newVal) return

    // 确保是字符串
    if (Array.isArray(newVal)) {
      newVal = newVal[0]
      addressForm.areaCode = newVal
    }

    // 设置区域名称
    const area = areas.value.find(a => a.code === newVal)
    if (area && addressForm.area !== area.name) {
      addressForm.area = area.name
    }
  })

  // 专门处理编辑时的省市区数据加载
  const loadEditAddressData = async (address, requestId = addressEditRequestId) => {
    if (requestId !== addressEditRequestId) return
    // 统一转换为字符串
    const provinceCode = String(address.provinceCode || '')
    const cityCode = String(address.cityCode || '')
    const areaCode = String(address.areaCode || '')

    if (!provinceCode) return

    cities.value = []
    areas.value = []

    try {
      const province = provinces.value.find(p => p.code === provinceCode)
      if (!province) return

      const isMunicipalityArea = isMunicipality(provinceCode)

      if (isMunicipalityArea) {
        areaLoading.value = true
        try {
          areas.value = await areaService.getAreas(provinceCode)
          if (requestId !== addressEditRequestId) return
          if (areas.value.length === 0) {
            areas.value = await loadMunicipalityAreas(provinceCode)
          }
        } finally {
          if (requestId === addressEditRequestId) areaLoading.value = false
        }

        await nextTick()
        await nextTick()
        if (requestId !== addressEditRequestId) return

        addressForm.provinceCode = provinceCode
        addressForm.province = province.name
        addressForm.cityCode = provinceCode
        addressForm.city = province.name

        if (areaCode) {
          const area = areas.value.find(a => a.code === areaCode)
          if (area) {
            addressForm.areaCode = areaCode
            addressForm.area = area.name
          }
        }
      } else {
        cityLoading.value = true
        try {
          cities.value = await areaService.getCities(provinceCode)
          if (requestId !== addressEditRequestId) return
        } finally {
          if (requestId === addressEditRequestId) cityLoading.value = false
        }

        await nextTick()
        await nextTick()
        if (requestId !== addressEditRequestId) return

        addressForm.provinceCode = provinceCode
        addressForm.province = province.name

        if (cityCode && cities.value.length > 0) {
          const city = cities.value.find(c => c.code === cityCode)
          if (!city) return

          areaLoading.value = true
          try {
            areas.value = await areaService.getAreas(cityCode)
            if (requestId !== addressEditRequestId) return
          } finally {
            if (requestId === addressEditRequestId) areaLoading.value = false
          }

          await nextTick()
          await nextTick()
          if (requestId !== addressEditRequestId) return

          addressForm.cityCode = cityCode
          addressForm.city = city.name

          if (areaCode && areas.value.length > 0) {
            const area = areas.value.find(a => a.code === areaCode)
            if (area) {
              addressForm.areaCode = areaCode
              addressForm.area = area.name
            }
          }
        }
      }
    } catch (error) {
      if (requestId !== addressEditRequestId) return
      console.error('加载省市区数据失败:', error)
      regionLoadError.value = '地区数据加载失败，请检查网络后重试。'
    }
  }

  // 处理地址提交
  const handleAddressSubmit = async () => {
    if (!addressFormRef.value || addressLoading.value) return
    addressLoading.value = true

    try {
      try {
        await addressFormRef.value.validate()
      } catch {
        return
      }

      // 修复数据：确保编码是字符串而不是数组
      const submitData = {
        ...addressForm,
        provinceCode: Array.isArray(addressForm.provinceCode) ? addressForm.provinceCode[0] : addressForm.provinceCode,
        cityCode: Array.isArray(addressForm.cityCode) ? addressForm.cityCode[0] : addressForm.cityCode,
        areaCode: Array.isArray(addressForm.areaCode) ? addressForm.areaCode[0] : addressForm.areaCode,
      }

      // 确保名称正确
      if (!submitData.province && submitData.provinceCode) {
        const province = provinces.value.find(p => p.code === submitData.provinceCode)
        if (province) submitData.province = province.name
      }

      if (!submitData.city && submitData.cityCode) {
        const city = cities.value.find(c => c.code === submitData.cityCode)
        if (city) submitData.city = city.name
      }

      if (!submitData.area && submitData.areaCode) {
        const area = areas.value.find(a => a.code === submitData.areaCode)
        if (area) submitData.area = area.name
      }

      let response
      if (isEditAddress.value) {
        response = await updateAddress(submitData)
      } else {
        response = await addAddress(submitData)
      }

      if (response.code === 200) {
        ElMessage.success(isEditAddress.value ? '地址更新成功' : '地址添加成功')
        addressDialogVisible.value = false
        await loadAddressList()
      } else {
        ElMessage.error(response.msg || '操作失败')
      }
    } catch (error) {
      console.error('操作地址失败:', error)
      ElMessage.error(error?.message || '操作失败')
    } finally {
      addressLoading.value = false
    }
  }

  // 删除地址
  const handleDeleteAddress = async (addressId) => {
    if (addressMutations.value.has(addressId)) return
    addressMutations.value.add(addressId)
    try {
      await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      // 直接调用删除接口，不需要先查询
      const response = await delAddress(addressId)
      if (response.code === 200) {
        ElMessage.success('地址删除成功')
        await loadAddressList()
      } else {
        ElMessage.error(response.msg || '地址删除失败')
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除地址失败:', error)
        ElMessage.error('地址删除失败')
      }
    } finally {
      addressMutations.value.delete(addressId)
    }
  }

  // 设置默认地址
  const handleSetDefaultAddress = async (addressId) => {
    if (addressMutations.value.has(addressId)) return
    addressMutations.value.add(addressId)
    try {
      const response = await setDefaultAddress({
        addressId: addressId,
        userId: currentUserId.value
      })

      if (response.code === 200) {
        ElMessage.success('默认地址设置成功')
        await loadAddressList()
      } else {
        ElMessage.error(response.msg || '设置默认地址失败')
      }
    } catch (error) {
      console.error('设置默认地址失败:', error)
      ElMessage.error('设置默认地址失败')
    } finally {
      addressMutations.value.delete(addressId)
    }
  }

  // 关闭地址对话框时的处理
  const handleAddressDialogClose = () => {
    addressEditRequestId++
    isLoadingEditData.value = false
    cityLoading.value = false
    areaLoading.value = false
    // 重置表单
    if (addressFormRef.value) {
      addressFormRef.value.resetFields()
    }
    // 清空省市区数据
    cities.value = []
    areas.value = []
  }

  return {
    addressDialogVisible,
    addressLoading,
    addressMutations,
    addressListLoading,
    addressListError,
    isEditAddress,
    currentAddressId,
    addressList,
    addressForm,
    addressRules,
    addressLabelOptions,
    addressFormRef,
    provinceLoading,
    cityLoading,
    areaLoading,
    regionLoadError,
    provinces,
    cities,
    areas,
    loadAddressList,
    openAddAddressDialog,
    openEditAddressDialog,
    handleProvinceChange,
    handleCityChange,
    handleAreaChange,
    retryRegionLoad,
    handleAddressSubmit,
    handleDeleteAddress,
    handleSetDefaultAddress,
    getAddressLabelName,
    formatFullAddress,
    handleAddressDialogClose
  }
}
