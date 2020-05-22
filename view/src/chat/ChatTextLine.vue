<template>
  <div class="chat-text-line">
    <span class="chat-line-nick" v-bind:style="{color: selectedNickColor}">{{ nick }}</span>
    <span>: </span>
    <template v-for="part in messageParts">
      <template v-if="part.type === 'link'">
        <span>
          <a class="chat-text-line-link" v-bind:href="part.content">{{ part.content }}</a>
        </span>
      </template>
      <template v-if="part.type === 'plaintext'">
        <span>{{ part.content }}</span>
      </template>
    </template>
  </div>
</template>

<script>
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
      message: String
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
      messageParts() {
        const parts = [];

        // Split the message into parts
        const tokens = this.message.split(/(\s+)/);
        tokens.forEach(token => {
          let validUrl = false;
          if (token.startsWith("http://") || token.startsWith("https://")) {
            try {
              const url = new URL(token);
              if (url.protocol === 'http:' || url.protocol === 'https:') {
                validUrl = true;
                parts.push({
                  type: "link",
                  content: token
                });
              }
            } catch (_) {
            }
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
    }
  }
</script>

<style lang="scss">
  .chat-text-line {
    font-size: 0; // Allows the span elements to be on multiple lines in the source code
    span {
      color: rgb(239, 239, 241);
      font-family: 'Questrial', sans-serif;
      font-size: 12px;
      font-weight: 400;
      line-height: 20px;
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
  }

  a.chat-text-line-link {
    &:link {
      color: darkgoldenrod;
    }
    &:visited {
      color: olive;
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
