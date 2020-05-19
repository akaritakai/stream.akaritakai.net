const state = {
  now: new Date().getTime()
};

const mutations = {
  update(state) {
    state.now = new Date().getTime();
  }
};

const actions = {
  start ({ commit }) {
    setInterval(() => {
      commit('update');
    }, 100);
  }
};

export const time = {
  namespaced: true,
  state,
  mutations,
  actions
};
