import Vue from 'vue'
import VueRouter from 'vue-router'
import Upload from '@/components/Upload.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Upload',
    component: Upload,
  },
]

const router = new VueRouter({
  routes,
  mode: 'history',
})

export default router
