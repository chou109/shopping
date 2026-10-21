import vue from '@vitejs/plugin-vue'

import createAutoImport from './auto-import'
import createSvgIcon from './svg-icon'
import createCompression from './compression'
import createSetupExtend from './setup-extend'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default function createVitePlugins(viteEnv, isBuild = false) {
    const vitePlugins = [vue()]
    vitePlugins.push(createAutoImport())
	vitePlugins.push(Components({
		resolvers: [ElementPlusResolver({ importStyle: 'css' })]
	}))
	vitePlugins.push(createSetupExtend())
    vitePlugins.push(createSvgIcon())
	isBuild && vitePlugins.push(...createCompression(viteEnv))
    return vitePlugins
}
