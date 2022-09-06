<template>
  <div class="chat-text-line">
    <span class="chat-line-time"><time>{{ time }}</time></span>
    <span class="chat-line-nick" v-bind:style="{color: selectedNickColor}">{{ nick }}</span>
    <span>: </span>
    <template v-for="part in messageParts">
      <template v-if="part.type === 'link'">
        <span><a class="chat-text-line-link" v-bind:href="part.content" rel="nofollow noopener noreferrer" target="_blank">{{ part.content }}</a></span>
      </template>
      <template v-if="part.type === 'plaintext'">
        <span v-html="replace_colons(part.content)"></span>
      </template>
    </template>
  </div>
</template>

<script>
  import sheetsrc from "../../assets/emoji-data/sheet_apple_64.png";
  //import {emoticon} from 'emoticon';
  var EmojiConvertor = require('emoji-js');
  var emoji = new EmojiConvertor();
  emoji.replace_mode = 'css';
  emoji.use_sheet = true;
  emoji.img_sets.apple.sheet = sheetsrc;
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
    },
    methods: {
      replace_colons(value) {
        var ctl = document.createElement("textarea");
        ctl.innerText = value;
        return emoji.replace_colons(ctl.innerHTML);
        var before = emoji.replace_colons(ctl.innerHTML);
        var after = before
                    .replaceAll('o/'         , '👋')
                    .replaceAll('&lt;/3'        , '💔')
                    .replaceAll('&lt;3'         , '💗')
                    .replaceAll('8-D'        , '😁')
                    .replaceAll('8D'         , '😁')
                    .replaceAll(':-D'        , '😁')
                    .replaceAll('=-3'        , '😁')
                    .replaceAll('=-D'        , '😁')
                    .replaceAll('=3'         , '😁')
                    .replaceAll('=D'         , '😁')
                    .replaceAll('B^D'        , '😁')
                    .replaceAll('X-D'        , '😁')
                    .replaceAll('XD'         , '😁')
                    .replaceAll('x-D'        , '😁')
                    .replaceAll('xD'         , '😁')
                    .replaceAll(':\')'       , '😂')
                    .replaceAll(':\'-)'      , '😂')
                    .replaceAll(':-))'       , '😃')
                    .replaceAll('8)'         , '😄')
                    .replaceAll(':)'         , '😄')
                    .replaceAll(':-)'        , '😄')
                    .replaceAll(':3'         , '😄')
                    .replaceAll(':D'         , '😄')
                    .replaceAll(':]'         , '😄')
                    .replaceAll(':^)'        , '😄')
                    .replaceAll(':c)'        , '😄')
                    .replaceAll(':o)'        , '😄')
                    .replaceAll(':}'         , '😄')
                    .replaceAll(':っ)'        , '😄')
                    .replaceAll('=)'         , '😄')
                    .replaceAll('=]'         , '😄')
                    .replaceAll('0:)'        , '😇')
                    .replaceAll('0:-)'       , '😇')
                    .replaceAll('0:-3'       , '😇')
                    .replaceAll('0:3'        , '😇')
                    .replaceAll('0;^)'       , '😇')
                    .replaceAll('O:-)'       , '😇')
                    .replaceAll('3:)'        , '😈')
                    .replaceAll('3:-)'       , '😈')
                    .replaceAll('}:)'        , '😈')
                    .replaceAll('}:-)'       , '😈')
                    .replaceAll('*)'         , '😉')
                    .replaceAll('*-)'        , '😉')
                    .replaceAll(':-,'        , '😉')
                    .replaceAll(';)'         , '😉')
                    .replaceAll(';-)'        , '😉')
                    .replaceAll(';-]'        , '😉')
                    .replaceAll(';D'         , '😉')
                    .replaceAll(';]'         , '😉')
                    .replaceAll(';^)'        , '😉')
                    .replaceAll(':-|'        , '😐')
                    .replaceAll(':|'         , '😐')
                    .replaceAll(':('         , '😒')
                    .replaceAll(':-('        , '😒')
                    .replaceAll(':-<'        , '😒')
                    .replaceAll(':-['        , '😒')
                    .replaceAll(':-c'        , '😒')
                    .replaceAll(':<'         , '😒')
                    .replaceAll(':['         , '😒')
                    .replaceAll(':c'         , '😒')
                    .replaceAll(':{'         , '😒')
                    .replaceAll(':っC'        , '😒')
                    .replaceAll('%)'         , '😖')
                    .replaceAll('%-)'        , '😖')
                    .replaceAll(':-P'        , '😜')
                    .replaceAll(':-b'        , '😜')
                    .replaceAll(':-p'        , '😜')
                    .replaceAll(':-Þ'        , '😜')
                    .replaceAll(':-þ'        , '😜')
                    .replaceAll(':P'         , '😜')
                    .replaceAll(':b'         , '😜')
                    .replaceAll(':p'         , '😜')
                    .replaceAll(':Þ'         , '😜')
                    .replaceAll(':þ'         , '😜')
                    .replaceAll(';('         , '😜')
                    .replaceAll('=p'         , '😜')
                    .replaceAll('X-P'        , '😜')
                    .replaceAll('XP'         , '😜')
                    .replaceAll('d:'         , '😜')
                    .replaceAll('x-p'        , '😜')
                    .replaceAll('xp'         , '😜')
                    .replaceAll(':-||'       , '😠')
                    .replaceAll(':@'         , '😠')
                    .replaceAll(':-.'        , '😡')
                    .replaceAll(':-/'        , '😡')
                    .replaceAll(':/'         , '😡')
                    .replaceAll(':L'         , '😡')
                    .replaceAll(':S'         , '😡')
                    .replaceAll(':\\'        , '😡')
                    .replaceAll('=/'         , '😡')
                    .replaceAll('=L'         , '😡')
                    .replaceAll('=\\'        , '😡')
                    .replaceAll(':\'('       , '😢')
                    .replaceAll(':\'-('      , '😢')
                    .replaceAll('^5'         , '😤')
                    .replaceAll('^&lt;_&lt;'       , '😤')
                    .replaceAll('o/\\o'      , '😤')
                    .replaceAll('|-O'        , '😫')
                    .replaceAll('|;-)'       , '😫')
                    .replaceAll(':###..'     , '😰')
                    .replaceAll(':-###..'    , '😰')
                    .replaceAll('D-\':'      , '😱')
                    .replaceAll('D8'         , '😱')
                    .replaceAll('D:'         , '😱')
                    .replaceAll('D:&lt;'        , '😱')
                    .replaceAll('D;'         , '😱')
                    .replaceAll('D='         , '😱')
                    .replaceAll('DX'         , '😱')
                    .replaceAll('v.v'        , '😱')
                    .replaceAll('8-0'        , '😲')
                    .replaceAll(':-O'        , '😲')
                    .replaceAll(':-o'        , '😲')
                    .replaceAll(':O'         , '😲')
                    .replaceAll(':o'         , '😲')
                    .replaceAll('O-O'        , '😲')
                    .replaceAll('O_O'        , '😲')
                    .replaceAll('O_o'        , '😲')
                    .replaceAll('o-o'        , '😲')
                    .replaceAll('o_O'        , '😲')
                    .replaceAll('o_o'        , '😲')
                    .replaceAll(':$'         , '😳')
                    .replaceAll('#-)'        , '😵')
                    .replaceAll(':#'         , '😶')
                    .replaceAll(':&amp;'         , '😶')
                    .replaceAll(':-#'        , '😶')
                    .replaceAll(':-&amp;'        , '😶')
                    .replaceAll(':-X'        , '😶')
                    .replaceAll(':X'         , '😶')
                    .replaceAll(':-J'        , '😼')
                    .replaceAll(':*'         , '😽')
                    .replaceAll(':^*'        , '😽')
                    .replaceAll('ಠ_ಠ'        , '🙅')
                    .replaceAll('*\\0/*'     , '🙆')
                    .replaceAll('\\o/'       , '🙆')
                    .replaceAll(':&gt;'         , '😄')
                    .replaceAll('&gt;.&lt;'        , '😡')
                    .replaceAll('&gt;:('        , '😠')
                    .replaceAll('&gt;:)'        , '😈')
                    .replaceAll('&gt;:-)'       , '😈')
                    .replaceAll('&gt;:/'        , '😡')
                    .replaceAll('&gt;:O'        , '😲')
                    .replaceAll('&gt;:P'        , '😜')
                    .replaceAll('&gt;:['        , '😒')
                    .replaceAll('&gt;:\\'       , '😡')
                    .replaceAll('&gt;;)'        , '😈')
                    .replaceAll('&gt;_&gt;^'       , '😤');
        if (before != after) {
          console.log(after);
        }
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
