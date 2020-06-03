<template>
  <div id="chat-content-container" v-bind:class="{'v-center': !(enabled && nick && nick.length > 0)}">
    <template v-if="enabled">
      <!-- Chat enabled -->
      <template v-if="nick && nick.length > 0">
        <!-- Nick set -->
        <div id="chat-content" ref="chatContent" v-if="enabled && nick && nick.length > 0">
          <template v-for="message in messages">
            <div class="chat-line">
              <template v-if="message.messageType === 'TEXT'">
                <chat-text-line :nick="message.nickname" :message="message.message"/>
              </template>
            </div>
          </template>
        </div>
      </template>
      <template v-else>
        <!-- Nick needs to be set -->
        <span class="text-align-center">Choose a nick to start chatting</span>
        <span class="text-align-center">
          <!--suppress HtmlFormInputWithoutLabel -->
          <input v-bind:class="{'error': inputError}"
                 type="text"
                 v-model="inputNick"
                 maxlength="25"
                 @keydown.enter.prevent="setNick"
                 autofocus/>
          <svg class="icon" viewBox="0 0 512 512" @click="setNick">
            <g><path d="M173.898 439.404l-166.4-166.4c-9.997-9.997-9.997-26.206 0-36.204l36.203-36.204c9.997-9.998
                        26.207-9.998 36.204 0L192 312.69 432.095 72.596c9.997-9.997 26.207-9.997 36.204 0l36.203
                        36.204c9.997 9.997 9.997 26.206 0 36.204l-294.4 294.401c-9.998 9.997-26.207
                        9.997-36.204-.001z"/></g>
          </svg>
        </span>
        <span v-if="inputError" class="error-message">{{ inputError }}</span>
      </template>
    </template>
    <template v-else>
      <!-- Chat disabled -->
      <span class="text-align-center">Chat has been disabled</span>
    </template>
  </div>
</template>

<script>
  const ChatTextLine = () => import(
    /* webpackChunkName: "chatTextLine" */
    /* webpackPrefetch: true */
    './ChatTextLine.vue');

  import {mapState} from "vuex";

  export default {
    name: 'chat-content',
    data() {
      return {
        messages: this.orderedMessageList(),
        inputNick: '',
        inputError: null
      }
    },
    components: {
      ChatTextLine
    },
    computed: {
      ...mapState('chat', ['enabled']), // if the chat is enabled
      ...mapState('nick', ['nick']), // if our nick is set
      ...mapState('chat', ['epoch', 'position']), // epoch and position comprise a sequence (see 'ChatSequence')
      messageSequence() { // watchable object to trigger changes to the list of messages
        return {
          epoch: this.epoch,
          position: this.position
        }
      }
    },
    watch: {
      messageSequence: {
        // a change to messageSequence implies a change in chat messages
        deep: true,
        handler() {
          this.messages = this.orderedMessageList(); // update the list of messages
          this.$nextTick(() => this.scrollToBottom());
        }
      }
    },
    methods: {
      orderedMessageList() {
        return Array.from(this.$store.state.chat.messages.values()).sort((message1, message2) => {
          return message1.sequence.position - message2.sequence.position;
        });
      },
      scrollToBottom() {
        if (this.$refs.chatContent) {
          const lastChatMessage = this.$refs.chatContent.lastElementChild;
          if (lastChatMessage) {
            lastChatMessage.scrollIntoView();
          }
        }
      },
      setNick() {
        // Remove previous errors and re-render
        this.inputError = null;
        this.$nextTick(() => {
          // Check that the nick is valid
          if (this.inputNick.length > 25) {
            this.onNickError("Nick must be less than 25 characters");
            return;
          }
          if (this.inputNick.match(/^_/)) {
            this.onNickError("Nick cannot start with an underscore");
            return;
          }
          if (!this.inputNick.match(/^[A-Za-z0-9_]*$/)) {
            this.onNickError("Nick must contain only:\n"
              + "- uppercase letters (A-Z)\n"
              + "- lowercase letters (a-z)\n"
              + "- digits (0-9)\n"
              + "- underscores (_)");
            return;
          }

          // Nick is valid
          this.$store.dispatch('nick/setNick', this.inputNick);
        });
      },
      onNickError(error) {
        this.inputError = error;
      },
      onResize() {
        this.$nextTick(() => this.scrollToBottom());
      }
    },
    mounted() {
      this.$nextTick(() => {
        this.scrollToBottom();
        window.addEventListener('resize', this.onResize);
        this.onResize();
      });
    },
    updated() {
      this.$nextTick(() => this.scrollToBottom());
    },
    beforeDestroy() {
      window.removeEventListener('resize', this.onResize);
    }
  }
</script>

<style lang="scss">
  #chat-content-container {
    // Fill my container
    flex-grow: 1;
    flex-shrink: 1;

    // Flex container
    align-content: center;
    display: inline-flex;
    flex-direction: column;

    box-sizing: border-box;
    color: rgb(222, 222, 227);
    font-family: 'Questrial', sans-serif;
    font-size: 20px;
    font-weight: 600;
    line-height: 20px;
    overflow: hidden;
    padding: 15px 15px 0;
    vertical-align: baseline;

    &.v-center {
      justify-content: center;
    }

    input {
      background-color: rgba(255, 255, 255, 0.15);
      border-color: hsla(0, 0%, 100%, 0.12);
      border-radius: 4px;
      color: rgb(239, 239, 241);
      font-size: 13px;
      margin: 10px auto;
      transition-delay: 0s, 0s, 0s;
      transition-duration: 0.1s, 0.1s, 0.1s;
      transition-property: box-shadow, border, background-color;
      transition-timing-function: ease-in, ease-in, ease-in;
      &:focus {
        background-color: black;
        border-bottom-color: rgb(145, 71, 255);
        border-bottom-style: solid;
        border-bottom-width: 2px;
        border-left-color: rgb(145, 71, 255);
        border-left-style: solid;
        border-left-width: 2px;
        border-right-color: rgb(145, 71, 255);
        border-right-style: solid;
        border-right-width: 2px;
        border-top-color: rgb(145, 71, 255);
        border-top-style: solid;
        border-top-width: 2px;
        color: rgb(239, 239, 241);
      }
      &.error {
        animation: shake 0.2s ease-in-out 0s 2;
        box-shadow: 0 0 5px red;
      }
    }
    .error-message {
      animation-duration: 0.5s;
      animation-name: fadein;
      background-color: #492a75;
      border-radius: 5px;
      font-size: 16px;
      padding: 5px;
      text-align: center;
      white-space: pre-wrap;
      align-self: center;
    }
    .icon {
      color: rgb(239, 239, 241);
      fill: currentColor;
      height: 14px;
      padding-left: 5px;
      width: 14px;
      &:hover {
        color: rgb(145, 71, 255);
      }
    }
    .text-align-center {
      text-align: center;
    }
  }

  #chat-content {
    box-sizing: content-box;
    font-size: 12px;
    justify-content: normal;
    line-height: 20px;
    overflow-x: hidden;
    overflow-y: scroll;
    padding: 0;
    vertical-align: baseline;
    scrollbar-width: none; // Hide scrollbar on Firefox
    &::-webkit-scrollbar { // Hide scroll bar on Chrome/Safari/Webkit/Edge
      background: transparent;
      width: 0;
    }
  }

  @keyframes fadein {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }

  @keyframes shake {
    0% {
      transform: translate(0px, 0) rotate(0);
    }
    25% {
      transform: translate(5px, 0) rotate(0);
    }
    75% {
      transform: translate(-10px, 0) rotate(0);
    }
    100% {
      transform: translate(0px, 0) rotate(0);
    }
  }

  @font-face {
    font-display: swap;
    font-family: 'Questrial';
    font-style: normal;
    font-weight: 400;
    src: local('Questrial'),
    local('Questrial-Regular'),
    url("../../assets/fonts/questrial-regular.woff2") format('woff2');
  }
</style>
