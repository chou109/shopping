import vue from '@vitejs/plugin-vue'

import createAutoImport from './auto-import'
import createSvgIcon from './svg-icon'
import createCompression from './compression'
import createSetupExtend from './setup-extend'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import * as elementIcons from '@element-plus/icons-vue'

const iconNames = new Set(Object.keys(elementIcons))
const elementIconResolver = {
    type: 'component',
    resolve(name) {
        return iconNames.has(name)
            ? { name, from: '@element-plus/icons-vue' }
            : undefined
    }
}

export default function createVitePlugins(viteEnv, isBuild = false) {
    const vitePlugins = [vue()]
    vitePlugins.push(createAutoImport())
	vitePlugins.push(Components({
		resolvers: [ElementPlusResolver({ importStyle: 'css' }), elementIconResolver]
	}))
	vitePlugins.push(createSetupExtend())
    vitePlugins.push(createSvgIcon(isBuild))
	isBuild && vitePlugins.push(...createCompression(viteEnv))
    return vitePlugins
}
