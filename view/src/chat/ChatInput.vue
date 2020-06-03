<template>
  <div id="chat-input" v-if="enabled && nick && nick.length > 0">
    <div class="chat-input-area">
      <!--suppress HtmlFormInputWithoutLabel -->
      <textarea
        ref="textArea"
        v-model="message"
        maxlength="500"
        placeholder="Send a message"
        v-bind:rows="textAreaRows"
        @keydown.enter.exact.prevent="sendMessage"/>
      <svg class="icon" viewBox="0 0 20 20" @click="sendMessage">
        <g><path d="M11,8.3L2.6,8.8C2.4,8.8,2.3,8.9,2.3,9l-1.2,4.1c-0.2,0.5,0,1.1,0.4,1.5C1.7,14.9,2,15,2.4,15c0.2,0,0.4,
                  0,0.6-0.1l11.2-5.6 C14.8,9,15.1,8.4,15,7.8c-0.1-0.4-0.4-0.8-0.8-1L3,1.1C2.5,0.9,1.9,1,1.5,1.3C1,1.7,
                  0.9,2.3,1.1,2.9L2.3,7c0,0.1,0.2,0.2,0.3,0.2 L11,7.7c0,0,0.3,0,0.3,0.3S11,8.3,11,8.3z"/></g>
      </svg>
    </div>
  </div>
</template>

<script>
  import Vue from 'vue';
  import {mapState} from "vuex";

  export default {
    name: 'chat-input',
    data() {
      return {
        message: '',
        textAreaRows: 1
      }
    },
    computed: {
      ...mapState('chat', ['enabled']),
      ...mapState('nick', ['nick'])
    },
    methods: {
      sendMessage() {
        if (this.message.length > 0) { // Don't allow sending an empty message
          this.$store.dispatch('chat/sendMessage', this.message);
          this.message = '';
        }
      },
      scrollBarIsPresent() {
        return this.$refs.textArea.clientHeight < this.$refs.textArea.scrollHeight;
      }
    },
    watch: {
      // The watch on message exists to dynamically resize the textbox as the message contents change
      async message() {
        const minNumRows = 1; // The minimum number of rows (height) for the textarea
        const maxNumRows = 4; // The maximum number of rows (height) for the textarea
        // I am 100% sure that this is a shit way to do this -- especially since this process results in up to
        // maxNumRows renders, but honestly, after digging through all sorts of code snippets and docs to try to find a
        // better way to calculate this: this approach provided the most reliable results.
        // Hopefully someday I find a better solution to this, but I just cannot waste time on this anymore...
        await Vue.nextTick();
        if (this.scrollBarIsPresent()) {
          while (this.scrollBarIsPresent() && this.textAreaRows < maxNumRows) {
            this.textAreaRows++;
            if (this.textAreaRows < maxNumRows) {
              await Vue.nextTick();
            }
          }
        } else {
          if (this.textAreaRows > minNumRows) {
            this.textAreaRows = minNumRows;
            await Vue.nextTick();
            while (this.scrollBarIsPresent() && this.textAreaRows < maxNumRows) {
              this.textAreaRows++;
              if (this.textAreaRows < maxNumRows) {
                await Vue.nextTick();
              }
            }
          }
        }
      }
    }
  }
</script>

<style lang="scss">
  #chat-input {
    // Fill my container
    flex-shrink: 0;

    // Flex container
    display: inline-flex;
    flex-direction: column;

    padding: 15px 15px 15px 15px;
    .chat-input-area {
      position: relative;
      display: inline-block;
      textarea {
        min-height: 40px;
        background-clip: padding-box;
        background-color: hsla(0, 0%, 100%, 0.15);
        box-sizing: border-box;
        border-radius: 4px;
        border-color: hsla(0, 0%, 100%, 0.12);
        color: rgb(239, 239, 241);
        font-family: 'Questrial', sans-serif;
        font-size: 12px;
        line-height: 18px;
        margin: 0;
        outline: 0;
        overflow-x: hidden;
        overflow-y: scroll;
        padding: 10px 35px 10px 10px;
        resize: none;
        transition-delay: 0s, 0s, 0s;
        transition-duration: 0.1s, 0.1s, 0.1s;
        transition-property: box-shadow, border, background-color;
        transition-timing-function: ease-in, ease-in, ease-in;
        width: 100%;
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
        scrollbar-width: none; // Hide scrollbar on Firefox
        &::-webkit-scrollbar { // Hide scroll bar on Chrome/Safari/Webkit/Edge
          background: transparent;
          width: 0;
        }
      }
      .icon {
        color: rgb(239, 239, 241);
        fill: currentColor;
        height: 35px;
        width: 20px;
        right: 5px;
        bottom: 5px;
        position: absolute;
        &:hover {
          color: rgb(145, 71, 255);
        }
      }
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
