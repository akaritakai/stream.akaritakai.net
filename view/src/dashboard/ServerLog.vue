<template>
  <textarea v-if="!needApiKey" readonly rows="25" class="form-control monospace server-log" v-model="content">
  </textarea>
</template>

<script>
  import {mapGetters, mapState} from 'vuex';
  import axios from 'axios';

  export default {
    name: 'server-log',
    data () {
      return {
        content: ''
      };
    },
    computed: {
      ...mapState('apiKey', ['apiKey']),
      needApiKey() {
        return this.apiKey == null || this.apiKey.length === 0;
      },
    },
    mounted() {
      const view = this;
      function establishLogListener() {
        const url = new URL('/log/fetch', window.location.href);
        fetch(url.href, {
          method: 'POST',
          mode: 'cors',
          cache: 'no-cache',
          redirect: 'error',
          referrerPolicy: 'no-referrer',
          body: JSON.stringify({
            key: view.apiKey
          })
        }).then(response => {
          var reader = response.body.getReader();
          var decoder = new TextDecoder();

          function readChunk() {
            return reader.read().then(appendChunks);
          }

          function appendChunks(result) {
            var chunk = decoder.decode(result.value || Uint8Array, { stream: !result.done});
            view.content += chunk;
            if (result.done) {
              return "";
            } else {
              return readChunk();
            }
          }

          return readChunk();
        }).then(result => {
          establishLogListener();
        }).catch(err => {
          console.error("log read error: " + err);
          setTimeout(establishLogListener, 1000);
        });
      }
      establishLogListener();
    }
  }
</script>

<style lang="scss">
  .server-log {
    min-width: 500px;
    max-width: 100%;
    min-height: 50px;
    height: 100%;
    width: 100%;
  }
</style>
