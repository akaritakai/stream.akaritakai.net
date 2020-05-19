const state = {
  apiKey: null
};

const mutations = {
  set(state, newKey) {
    state.apiKey = newKey;
  }
};

const actions = {
  set(context, newKey) {
    context.commit('set', newKey);
  }
};

export const apiKey = {
  namespaced: true,
  state,
  mutations,
  actions
};
