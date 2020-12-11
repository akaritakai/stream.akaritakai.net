import axios from 'axios';

const state = {
  current: new Date().getTime(),
  offset: 0
};

const getters = {
  now(state) {
    return state.current + state.offset;
  }
}

const mutations = {
  updateTime(state) {
    state.current = new Date().getTime();
  },
  updateOffset(state, offset) {
    state.offset = offset;
  }
};

const actions = {
  start ({ commit }) {
    setInterval(() => {
      commit('updateTime');
    }, 10);
    setInterval(() => {
      const t0 = new Date().getTime();
      axios.get('/time').then(response => {
        const t1 = response.data;
        const t2 = response.data;
        const t3 = new Date().getTime();
        const offset = ((t1 - t0) + (t2 - t3)) / 2;
        commit('updateOffset', offset);
      });
    }, 5000);
  }
};

export const time = {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
