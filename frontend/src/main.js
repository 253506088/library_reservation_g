import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// 导入UI库
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

import Vant from 'vant'
import 'vant/lib/index.css'

// 注册UI组件
Vue.use(ElementUI)
Vue.use(Vant)

Vue.config.productionTip = false

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')