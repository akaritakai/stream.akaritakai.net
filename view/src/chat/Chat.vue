<template>
  <chat-view v-if="opened"/>
  <div id="open-chat-icon" v-else>
    <svg viewBox="0 0 20 20" @click="openChat">
      <g><path d="M16 16V4h2v12h-2zM6 9l2.501-2.5-1.5-1.5-5 5 5 5 1.5-1.5-2.5-2.5h8V9H6z"/></g>
    </svg>
  </div>
</template>

<script>
  const ChatView = () => import(
    /* webpackChunkName: "chatView" */
    /* webpackPrefetch: true */
    './ChatView.vue');

  import {mapState} from 'vuex';

  export default {
    name: 'chat',
    components: {
      ChatView
    },
    data() {
      return {
        socket: null,
        connected: false
      }
    },
    computed: {
      ...mapState('nick', ['nick']),
      ...mapState('chat', ['opened', 'sendQueue']),
      sendQueueState() { // watchable object to trigger changes to the list of queued messages
        return {
          socket: this.socket, // socket for sending messages
          connected: this.connected, // connection status
          length: this.sendQueue.length // number of messages queued
        }
      }
    },
    watch: {
      sendQueueState: {
        // a change to sendQueueState implies that we can send messages and that we have messages to send
        deep: true,
        handler() {
          if (this.socket !== null && this.connected === true && this.sendQueue.length > 0) {
            try {
              this.socket.send(JSON.stringify({
                requestType: "send",
                messageType: "TEXT",
                nickname: this.nick,
                message: this.sendQueue[0]
              }));
              this.$store.dispatch('chat/messageSent');
            } catch (_) {
              // The socket will be closed and the handler will pick this up again
            }
          }
        }
      }
    },
    mounted() {
      this.establishListener();
    },
    methods: {
      openChat() {
        this.$store.dispatch('chat/openChat');
      },
      establishListener() {
        if (this.socket === null) {
          if (process.env.NODE_ENV === "development") {
            this.socket = new WebSocket("ws://localhost/chat");
          } else {
            this.socket = new WebSocket("wss://stream.akaritakai.net/chat");
          }
          const chat = this;
          this.socket.onmessage = function(event) {
            const response = JSON.parse(event.data);
            if (response.responseType === "message") {
              chat.$store.dispatch('chat/onMessage', response);
            } else if (response.responseType === "status") {
              chat.$store.dispatch('chat/onStatus', response);
            }
          };
          this.socket.onopen = function() {
            chat.connected = true;
            chat.socket.send(JSON.stringify({requestType: "join"}));
          };
          this.socket.onerror = function() {
            chat.socket.close();
          };
          this.socket.onclose = function() {
            chat.connected = false;
            chat.socket = null;
            setTimeout(chat.establishListener, 100);
          };
        }
      }
    }
  }
</script>

<style lang="scss">
  #open-chat-icon {
    color: rgb(239, 239, 241);
    fill: currentColor;
    height: 20px;
    min-height: 20px;
    min-width: 20px;
    padding: 10px;
    position: absolute;
    right: 0;
    top: 0;
    width: 20px;
    z-index: 1;
    svg {
      color: rgb(239, 239, 241);
      fill: currentColor;
      height: 100%;
      width: 100%;
    }
  }
</style>
