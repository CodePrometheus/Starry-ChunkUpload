import { createVuePlugin } from 'vite-plugin-vue2'
import path from 'path'
import { defineConfig } from 'vite'

const config = defineConfig({
  resolve: {
    alias: {
      '@': `${ path.resolve(__dirname, 'src') }`,
    },
  },
  plugins: [createVuePlugin()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8989',
        changeOrigin: true,
        ws: true,
        rewrite: path => path.replace(/^\/api/, '')
      },
    },
  },
})

export default config
