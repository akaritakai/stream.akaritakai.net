<template>
  <div id="chat-input" v-if="enabled && nick && nick.length > 0">
    <div class="chat-input-area" ref="inputArea">
      <!--suppress HtmlFormInputWithoutLabel -->
      <textarea
        ref="textArea"
        v-model="message"
        maxlength="32768"
        placeholder="Send a message"
        v-bind:rows="textAreaRows"
        @keydown.enter.exact.prevent="sendMessage"/>
      <svg class="emojiButton" viewBox="0 0 72 72" ref="pickerIcon" v-on:mouseover="emojiMouseOver">
        <g id="color">
          <circle cx="36.0001" cy="36" r="22.9999" fill="#FCEA2B"/>
        </g>
        <g id="line">
          <circle cx="36" cy="36" r="23" fill="none" stroke="#000000" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
          <path fill="none" stroke="#000000" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M45.8149,44.9293 c-2.8995,1.6362-6.2482,2.5699-9.8149,2.5699s-6.9153-0.9336-9.8149-2.5699"/>
          <path d="M30,31c0,1.6568-1.3448,3-3,3c-1.6553,0-3-1.3433-3-3c0-1.6552,1.3447-3,3-3C28.6552,28,30,29.3448,30,31"/>
          <path d="M48,31c0,1.6568-1.3447,3-3,3s-3-1.3433-3-3c0-1.6552,1.3447-3,3-3S48,29.3448,48,31"/>
        </g>
      </svg>
      <svg class="icon" viewBox="0 0 20 20" @click="sendMessage">
        <g><path d="M11,8.3L2.6,8.8C2.4,8.8,2.3,8.9,2.3,9l-1.2,4.1c-0.2,0.5,0,1.1,0.4,1.5C1.7,14.9,2,15,2.4,15c0.2,0,0.4,
                  0,0.6-0.1l11.2-5.6 C14.8,9,15.1,8.4,15,7.8c-0.1-0.4-0.4-0.8-0.8-1L3,1.1C2.5,0.9,1.9,1,1.5,1.3C1,1.7,
                  0.9,2.3,1.1,2.9L2.3,7c0,0.1,0.2,0.2,0.3,0.2 L11,7.7c0,0,0.3,0,0.3,0.3S11,8.3,11,8.3z"/></g>
      </svg>
    </div>
  </div>
</template>

<script>
  require("../../node_modules/rm-emoji-picker/dist/emojipicker.css");
  import sheet_apple from "../../node_modules/rm-emoji-picker/sheets/sheet_apple_64_indexed_128.png";
  import sheet_google from "../../node_modules/rm-emoji-picker/sheets/sheet_google_64_indexed_128.png";
  import sheet_twitter from "../../node_modules/rm-emoji-picker/sheets/sheet_twitter_64_indexed_128.png";

  import Vue from 'vue';
  import {mapState} from "vuex";
  import EmojiPicker from "../../node_modules/rm-emoji-picker/dist/index.js";
  const picker = new EmojiPicker({
    sheets: {
      apple   : sheet_apple,
      google  : sheet_google,
      twitter : sheet_twitter
    },
    positioning: function(tip) {
      if (typeof(tip.element_rect) != 'undefined') {
        let coordinate = {
          top: tip.centered_coordinate.top - (tip.element_rect.height + tip.tooltip_height) / 2,
          left: tip.centered_coordinate.left
        };
        tip._applyPosition(coordinate)('TooltipAbove');
      } else {
        tip.above();
      }
    },
    search_icon : '🔍',
    categories: [
      {
        title: "People",
        icon : '😀'
      },
      {
        title: "Nature",
        icon : '🌳'
      },
      {
        title: "Foods",
        icon : '🍉'
      },
      {
        title: "Activity",
        icon : '⚽'
      },
      {
        title: "Places",
        icon : '🧭'
      },
      {
        title: "Symbols",
        icon : '➗'
      },
      {
        title: "Flags",
        icon : '🏳'
      }
    ]
  });

  export default {
    name: 'chat-input',
    components: {
      EmojiPicker
    },
    data() {
      return {
        message: '',
        textAreaRows: 1,
        pickerInstalled: false
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
      },
      emojiMouseOver(event) {
        if (this.pickerInstalled) {
          return;
        }
        this.pickerInstalled = true;
        const target = event.target;
        const input = this.$refs.textArea;
        const comp = this.$refs.inputArea;
        picker.listenOn(target, comp, input);
        let self = this;
        picker._callback = function(emoji, cat, node) {
          self.message = input.value;
        };
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
      display: inline-block;
      position: relative;
      textarea {
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
        min-height: 40px;
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
      .emojiButton {
        height: 35px;
        width: 20px;
        left: -10px;
        bottom: 5px;
        position: absolute;
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
