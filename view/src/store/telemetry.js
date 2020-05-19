const state = {
  telemetryId: null
};

const mutations = {
  setId(state, newId) {
    if (state.telemetryId === null) {
      state.telemetryId = newId;
    }
  }
};

const actions = {
  setId(context, newId) {
    context.commit('setId', newId);
  }
};

export const telemetry = {
  namespaced: true,
  state,
  mutations,
  actions
};
