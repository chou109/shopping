import defaultSettings from '@/settings'
import { useDynamicTitle } from '@/utils/dynamicTitle'
import { defineStore } from 'pinia'

const { sideTheme, showSettings, topNav, tagsView, fixedHeader, sidebarLogo, dynamicTitle } = defaultSettings

const storageSetting: typeof defaultSettings & { theme: string } = JSON.parse(
  localStorage.getItem('layout-setting') || '{"theme":"#409EFF"}'
)

const useSettingsStore = defineStore(
  'settings',
  {
    state: () => ({
      title: '',
      theme: storageSetting.theme,
      sideTheme: storageSetting.sideTheme || sideTheme,
      showSettings: showSettings,
      topNav: storageSetting.topNav === undefined ? topNav : storageSetting.topNav,
      tagsView: storageSetting.tagsView === undefined ? tagsView : storageSetting.tagsView,
      fixedHeader: storageSetting.fixedHeader === undefined ? fixedHeader : storageSetting.fixedHeader,
      sidebarLogo: storageSetting.sidebarLogo === undefined ? sidebarLogo : storageSetting.sidebarLogo,
      dynamicTitle: storageSetting.dynamicTitle === undefined ? dynamicTitle : storageSetting.dynamicTitle
    }),
    actions: {
      // 修改布局设置
      changeSetting(data: { key: keyof typeof storageSetting, value: any }) {
        const { key, value } = data
        if (this.hasOwnProperty(key)) {
          //@ts-ignore
          this[key] = value
        }
      },
      // 设置网页标题
      setTitle(title:string) {
        this.title = title
        useDynamicTitle();
      }
    }
  })

export default useSettingsStore
