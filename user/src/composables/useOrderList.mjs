import { ref } from 'vue'

const STATUS_MAP = Object.freeze({
  all: null,
  waitPay: 10,
  waitShip: 20,
  waitReceive: 30,
  waitReview: 40,
  completed: 50,
  cancelled: 60
})

export function useOrderList(fetchOrders) {
  if (typeof fetchOrders !== 'function') {
    throw new TypeError('useOrderList requires an order fetch function')
  }
  const activeTab = ref('all')
  const loading = ref(false)
  const loadError = ref('')
  const orderList = ref([])
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(10)
  const orderCounts = ref({ waitPay: 0, waitShip: 0, waitReceive: 0 })
  let orderRequestId = 0
  let countsRequestId = 0

  const loadOrders = async () => {
    const requestId = ++orderRequestId
    loading.value = true
    loadError.value = ''
    try {
      const statusCode = STATUS_MAP[activeTab.value]
      const response = await fetchOrders(statusCode, pageNum.value, pageSize.value)
      if (requestId !== orderRequestId) return

      if (response.code === 200) {
        orderList.value = response.rows || []
        total.value = Number(response.total) || 0
        if (orderList.value.length === 0 && total.value > 0 && pageNum.value > 1) {
          pageNum.value -= 1
          await loadOrders()
          return
        }
      } else {
        loadError.value = response.msg || '订单加载失败，请重试'
      }
    } catch (error) {
      if (requestId === orderRequestId) {
        console.error('加载订单失败', error)
        loadError.value = error?.message || (typeof error === 'string' ? error : '') || '订单加载失败，请重试'
      }
    } finally {
      if (requestId === orderRequestId) loading.value = false
    }
  }

  const loadOrderCounts = async () => {
    const requestId = ++countsRequestId
    try {
      const [waitPay, waitShip, waitReceive] = await Promise.all([
        fetchOrders(10, 1, 1),
        fetchOrders(20, 1, 1),
        fetchOrders(30, 1, 1)
      ])
      if (requestId !== countsRequestId) return
      orderCounts.value = {
        waitPay: waitPay.code === 200 ? Number(waitPay.total) || 0 : 0,
        waitShip: waitShip.code === 200 ? Number(waitShip.total) || 0 : 0,
        waitReceive: waitReceive.code === 200 ? Number(waitReceive.total) || 0 : 0
      }
    } catch (error) {
      console.error('加载订单数量失败', error)
    }
  }

  const handleTabChange = () => {
    pageNum.value = 1
    loadOrders()
  }

  const handleSizeChange = value => {
    pageSize.value = value
    pageNum.value = 1
    loadOrders()
  }

  const handleCurrentChange = value => {
    pageNum.value = value
    loadOrders()
  }

  return {
    activeTab,
    loading,
    loadError,
    orderList,
    total,
    pageNum,
    pageSize,
    orderCounts,
    loadOrders,
    loadOrderCounts,
    handleTabChange,
    handleSizeChange,
    handleCurrentChange
  }
}
