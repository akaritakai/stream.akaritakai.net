import axios from 'axios';

const state = {
  current: new Date().getTime(),
  offsets: []
};

const getters = {
  now(state, getters) {
    return state.current + getters.offset;
  },
  offset(state) {
    if (state.offsets.length === 0) {
      return 0;
    }
    const median = arr => {
      const mid = Math.floor(arr.length / 2),
        values = [...arr].sort((a, b) => a - b);
      return arr.length % 2 !== 0 ? values[mid] : (values[mid - 1] + values[mid]) / 2;
    };
    return median(state.offsets);
  }
}

const mutations = {
  updateTime(state) {
    state.current = new Date().getTime();
  },
  updateOffset(state, offset) {
    if (state.offsets.length > 10) {
      state.offsets.shift();
    }
    state.offsets.push(offset);
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
