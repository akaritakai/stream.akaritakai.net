const MESSAGE_CAPACITY = 500;

const state = {
  opened: true,
  enabled: false,
  epoch: 0, // (helps trigger map changes)
  position: 0, // (helps trigger map changes)
  messages: new Map(), // map of long (position) -> ChatMessage
  sendQueue: [] // list of strings (chat message text)
};

const mutations = {
  openChat(state) {
    state.opened = true
  },
  closeChat(state) {
    state.opened = false
  },
  onMessage(state, messageResponse) {
    // Expected: message is a 'ChatMessageResponse' object
    const message = messageResponse.message;
    // We ignore messages that have an epoch before our store's epoch
    if (message.sequence.epoch === state.epoch) {
      // This message belongs to the current epoch
      state.messages.set(message.sequence.position, message);
    } else if (message.sequence.epoch > state.epoch) {
      // This message belongs to a new epoch, so we must be about to get a new epoch status message
      state.epoch = message.sequence.epoch;
      state.messages.clear();
      state.messages.set(message.sequence.position, message);
    }
  },
  onStatus(state, statusResponse) {
    // Expected: status is a 'ChatStatusResponse' object
    const status = statusResponse;
    if (status.enabled === true) { // Chat has been enabled or reset
      state.enabled = true;
      if (status.sequence.epoch === state.epoch) { // This status applies to our current epoch
        status.messages.forEach(message => {
          state.messages.set(message.sequence.position, message);
        });
      } else if (status.sequence.epoch > state.epoch) { // We're in a new status and should clean up the old one
        state.epoch = status.sequence.epoch;
        state.messages.clear();
        status.messages.forEach(message => {
          state.messages.set(message.sequence.position, message);
        });
      }
    } else { // Chat has been disabled
      state.enabled = false;
      state.epoch = 0;
      state.messages.clear();
      state.sendQueue = [];
    }
  },
  updatePosition(state) {
    // sets our store's position to whatever the latest message's position is (position as in a message's sequence)
    // this property setting helps signal that the messages map has changed which is necessary because Vue.js doesn't
    // support observing changes to a Map() (yet)
    // https://github.com/vuejs/vue/issues/2410#issuecomment-318487855
    if (state.messages.size > 0) {
      state.position = Math.max(...Array.from(state.messages.keys()));
    } else {
      state.position = 0;
    }
  },
  gcMessages(state) {
    // we don't want to have endless chat message history (would slow everything down and waste a lot of memory)
    // so we only keep a message capacity of a certain pre-configured length
    // this mutation is triggered whenever the list of messages is potentially updated
    if (state.messages.size > MESSAGE_CAPACITY) {
      const keys = Array.from(state.messages.keys()).sort();
      let i;
      for (i = 0; state.messages.size > MESSAGE_CAPACITY && i < keys.length; i++) {
        state.messages.delete(keys[i]);
      }
    }
  },
  sendMessage(state, message) {
    state.sendQueue.push(message);
  },
  messageSent(state) {
    if (state.sendQueue.length > 0) { // guard against possible race condition when chat is reset right as we are sending
      state.sendQueue.shift();
    }
  }
};

const actions = {
  openChat(context) {
    context.commit('openChat');
  },
  closeChat(context) {
    context.commit('closeChat');
  },
  onMessage(context, message) {
    context.commit('onMessage', message);
    context.commit('updatePosition');
    context.commit('gcMessages');
  },
  onStatus(context, status) {
    context.commit('onStatus', status);
    context.commit('updatePosition');
    context.commit('gcMessages');
  },
  sendMessage(context, message) {
    context.commit('sendMessage', message);
  },
  messageSent(context) {
    context.commit('messageSent');
  }
};

export const chat = {
  namespaced: true,
  state,
  mutations,
  actions
};
