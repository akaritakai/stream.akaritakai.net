import Vue from 'vue';
import store from './store/index';

const App = () => import(
  /* webpackPrefetch: true */
  /* webpackChunkName: "root" */
  './App.vue');

new Vue({
  el: '#app',
  store,
  render: h => h(App)
});
