import Vue from 'vue';
import store from './store/index';
import Dashboard from './Dashboard.vue';

new Vue({
  el: '#app',
  store,
  render: h => h(Dashboard)
});
