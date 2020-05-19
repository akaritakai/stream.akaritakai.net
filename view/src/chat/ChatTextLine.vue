<template>
  <div class="chat-text-line">
    <span class="chat-line-nick" v-bind:style="{color: selectedNickColor}">{{ nick }}</span>
    <span>: </span>
    <span class="chat-line-text-message">{{ message }}</span>
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
      &.chat-line-nick {
        font-weight: 700;
        word-break: break-all;
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
