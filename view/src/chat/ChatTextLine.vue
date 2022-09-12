<template>
  <div class="chat-text-line">
    <span class="chat-line-time"><time>{{ time }}</time></span>
    <span class="chat-line-nick" v-bind:style="{color: selectedNickColor}">{{ nick }}</span>
    <span>: </span>
    <template v-for="part in messageParts">
      <template v-if="part.type === 'link'">
        <span><a class="chat-text-line-link" v-bind:href="part.content" rel="nofollow noopener noreferrer" target="_blank">{{ part.content }}</a></span>
      </template>
      <template v-if="part.type === 'img'">
        <span><img class="chat-text-line-link" v-bind:src="part.content" height="28" width="28"/></span>
      </template>
      <template v-if="part.type === 'plaintext'">
        <span class="chat-text-span" v-html="format_text(part.content)"></span>
      </template>
    </template>
  </div>
</template>

<script>
  import sheet_apple from "../../node_modules/rm-emoji-picker/sheets/sheet_apple_64_indexed_128.png";
  import sheet_google from "../../node_modules/rm-emoji-picker/sheets/sheet_google_64_indexed_128.png";
  import sheet_twitter from "../../node_modules/rm-emoji-picker/sheets/sheet_twitter_64_indexed_128.png";
  import { checkText } from 'smile2emoji';
  var EmojiConvertor = require('emoji-js');
  var emoji = new EmojiConvertor();
  emoji.replace_mode = 'css';
  emoji.use_sheet = true;
  emoji.img_sets.apple.sheet = sheet_apple;
  emoji.img_sets.google.sheet = sheet_google;
  emoji.img_sets.twitter.sheet = sheet_twitter;
  emoji.allow_caps = true;

  export default {
    name: 'chat-text-line',
    data() {
      return {
        nickColors: [
          'Aqua',
          'Aquamarine',
          'Chartreuse',
          'CornflowerBlue',
          'Cyan',
          'DarkGoldenRod',
          'DarkOrange',
          'DeepPink',
          'Fuchsia',
          'Gold',
          'GoldenRod',
          'GreenYellow',
          'LimeGreen',
          'Wheat',
          'Violet',
          'Yellow',
          'YellowGreen'
        ],
        selectedNickColor: null
      }
    },
    props: {
      nick: String,
      message: String,
      timestamp: Number
    },
    created() {
      // Select the nick color by:
      // - deterministically hash the nick
      // - select the nth color where n = hash % nickColors.length
      function hashCode(nick) {
        let i;
        let hash = 0;
        for (i = 0; i < nick.length; i++) {
          hash = ((hash << 5) - hash) + nick.charCodeAt(i);
        }
        return hash;
      }
      this.selectedNickColor = this.nickColors[Math.abs(hashCode(this.nick)) % this.nickColors.length];
    },
    computed: {
      time() {
        const timestamp = this.timestamp;
        if (isNaN(timestamp)) {
          return "--:--";
        }
        const date = new Date(timestamp);
        const hours = date.getHours();
        const minutes = date.getMinutes();
        return hours.toString().padStart(2, '0') + ':' + minutes.toString().padStart(2, '0');
      },
      messageParts() {
        const parts = [];

        // Split the message into parts
        const tokens = this.message.split(/(\s+)/);
        tokens.forEach(token => {
          let validUrl = false;
          try {
            if (token.startsWith("http://") || token.startsWith("https://")) {
              const url = new URL(token);
              if (url.protocol === 'http:' || url.protocol === 'https:') {
                validUrl = true;
                parts.push({
                  type: "link",
                  content: token
                });
              }
            } else if (token.startsWith("[data:image/") && token.endsWith("]")) {
              const url = new URL(token.substring(1, token.length-1));
              validUrl = true;
              parts.push({
                type: "img",
                content: url
              });
            }
          } catch (_) {
          }
          if (!validUrl) {
            parts.push({
              type: "plaintext",
              content: token
            })
          }
        });

        return parts;
      }
    },
    methods: {
      format_text(value) {
        if (value.length > 100) {
          value = value.substring(0, 100) + "...";
        }
        var ctl = document.createElement("textarea");
        ctl.innerText = checkText(value);
        return emoji.replace_colons(ctl.innerHTML);
      }
    }
  }
</script>

<style lang="scss">
  .chat-text-line {
    font-size: 0; // Allows the span elements to be on multiple lines in the source code
    span {
      color: rgb(239, 239, 241);
      font-family: 'Questrial', sans-serif;
      font-size: 20px;
      font-weight: 400;
      line-height: 24px;
      margin: 0;
      overflow-wrap: break-word;
      padding: 0;
      vertical-align: baseline;
      white-space: pre-wrap;
      &.chat-line-nick {
        font-weight: 700;
        word-break: break-all;
      }
    }
    time {
      font-size: 12px;
       background-color: rgb(192, 192, 192);
       color: rgb(0, 0, 0);
    }
  }

  a.chat-text-line-link {
    &:link {
      color: darkgoldenrod;
    }
    &:visited {
      color: olive;
    }
  }

  chat-text-span {
    white-space: nowrap;
    word-break: keep-all;
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
