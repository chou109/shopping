import * as echarts from 'echarts/core'
import { BarChart, GaugeChart, LineChart, PieChart } from 'echarts/charts'
import {
  AxisPointerComponent,
  GridComponent,
  LegendComponent,
  TooltipComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  BarChart,
  GaugeChart,
  LineChart,
  PieChart,
  AxisPointerComponent,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  CanvasRenderer
])

export default echarts
