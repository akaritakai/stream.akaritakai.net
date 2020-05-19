const state = {
  nick: ''
};

const mutations = {
  setNick(state, newNick) {
    state.nick = newNick;
  }
};

const actions = {
  setNick(context, newNick) {
    context.commit('setNick', newNick);
  }
};

export const nick = {
  namespaced: true,
  state,
  mutations,
  actions
};
