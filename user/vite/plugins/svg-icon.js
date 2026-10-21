import svgSpritePlugin from '@pivanov/vite-plugin-svg-sprite'
import path from 'path'

export default function createSvgIcon() {
    return svgSpritePlugin({
        iconDirs: [path.resolve(process.cwd(), 'src/assets/icons/svg')],
        symbolId: 'icon-[name]',
        svgDomId: '__svg__icons__dom__',
        verbose: false
    })
}
