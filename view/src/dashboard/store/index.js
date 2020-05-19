import Vue from 'vue';
import Vuex from 'vuex';
import createPersistedState from 'vuex-persistedstate';
import createMutationsSharer, { BroadcastChannelStrategy } from 'vuex-shared-mutations';

import { stream } from '../../store/stream';
import { time } from '../../store/time';
import { chat } from '../../store/chat';

import { apiKey } from './apiKey';

Vue.use(Vuex);

const store = new Vuex.Store({
  modules: {
    stream,
    time,
    chat,
    apiKey
  },
  plugins: [
    createPersistedState({
      key: 'dashboard',
      paths: ['apiKey.apiKey']
    }),
    createMutationsSharer({
      predicate: ['apiKey/set'],
      strategy: new BroadcastChannelStrategy({ key: 'stream_dashboard_apiKey' })
    })
  ]
});

store.dispatch('time/start').then(_ => {});

export default store;
