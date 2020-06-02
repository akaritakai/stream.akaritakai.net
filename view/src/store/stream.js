const state = {
  status: "OFFLINE",
  live: null,
  playlist: null,
  mediaName: null,
  mediaDuration: null,
  startTime: null,
  endTime: null,
  seekTime: null,
  quality: null,
  bandwidth: null,
  muted: null
};

const mutations = {
  updateState(state, newState) {
    state.status = newState.status;
    state.playlist = newState.playlist;
    state.mediaName = newState.mediaName;
    state.mediaDuration = newState.mediaDuration;
    state.startTime = newState.startTime;
    state.endTime = newState.endTime;
    state.seekTime = newState.seekTime;
    state.live = newState.live;
  },
  updateQualityInfo(state, newQualityInfo) {
    state.quality = newQualityInfo.quality;
    state.bandwidth = newQualityInfo.bandwidth;
  },
  updateMutedInfo(state, newMutedInfo) {
    state.muted = newMutedInfo.muted;
  }
};

const actions = {
  updateState(context, newState) {
    context.commit('updateState', newState);
  },
  updateQualityInfo(context, newQualityInfo) {
    context.commit('updateQualityInfo', newQualityInfo);
  },
  updateMutedInfo(context, newMutedInfo) {
    context.commit('updateMutedInfo', newMutedInfo);
  }
};

export const stream = {
  namespaced: true,
  state,
  mutations,
  actions
};
