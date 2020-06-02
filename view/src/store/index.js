import Vue from 'vue';
import Vuex from 'vuex';
import createPersistedState from 'vuex-persistedstate';
import createMutationsSharer from 'vuex-shared-mutations';

import { stream } from './stream';
import { time } from './time';
import { nick } from './nick';
import { chat } from './chat';
import { telemetry } from './telemetry';
import { prefs } from './prefs';

Vue.use(Vuex);

const store = new Vuex.Store({
  modules: {
    stream,
    time,
    nick,
    chat,
    telemetry,
    prefs
  },
  plugins: [
    createPersistedState({
      paths: [
        'nick.nick',
        'telemetry.telemetryId',
        'prefs.volumePref',
        'prefs.chatShownPref'
      ]
    }),
    createMutationsSharer({
      predicate: [
        'nick/setNick',
        'telemetry/setId',
        'prefs/setVolumePref',
        'prefs/setChatShownPref'
      ]
    })
  ]
});

// Start recording time changes
store.dispatch('time/start').then(_ => {});

// Seed a unique ID for telemetry
function generateUUID() {
  return ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
    (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
  );
}
store.dispatch('telemetry/setId', generateUUID()).then(_ => {});

export default store;
