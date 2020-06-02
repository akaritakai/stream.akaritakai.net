const state = {
  volumePref: 1, // 100% volume
  chatShownPref: true
};

const mutations = {
  setVolumePref(state, newVolumePref) {
    state.volumePref = newVolumePref;
  },
  setChatShownPref(state, newChatShownPref) {
    state.chatShownPref = newChatShownPref;
  }
};

const actions = {
  setVolumePref(context, newVolumePref) {
    context.commit('setVolumePref', newVolumePref);
  },
  setChatShownPref(context, newChatShownPref) {
    context.commit('setChatShownPref', newChatShownPref);
  }
};

export const prefs = {
  namespaced: true,
  state,
  mutations,
  actions
};
