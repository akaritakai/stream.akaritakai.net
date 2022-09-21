<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col text-center">
        <h1>Dashboard</h1>
      </div>
    </div>
    <div class="row">
      <div class="col text-center">
        <b-form v-if="needApiKey" @submit.stop.prevent="alwaysTrue">
          <div class="form-group">
            <label for="apiKey">API Key</label>
            <input v-model="form.apiKey" type="text" class="form-control" id="apiKey" placeholder="API Key" required>
          </div>
          <div class="form-group">
            <button type="submit" class="btn btn-primary" @click="setApiKey">Set API Key</button>
          </div>
        </b-form>
        <b-tabs v-if="!needApiKey">
          <b-tab title="Stream Status" active>
            <stream-status/>
          </b-tab>
          <b-tab title="Viewers">
            <stream-viewers/>
          </b-tab>
          <b-tab title="Actions">
            <stream-control v-on:showResult="showResult"/>
          </b-tab>
          <b-tab title="Chat Log">
            <chat-log/>
          </b-tab>
          <b-tab title="Emojis">
            <emoji-tool v-on:showResult="showResult"/>
          </b-tab>
          <b-tab title="Scheduling">
            <b-tabs>
              <b-tab title="Status">
              </b-tab>
              <b-tab title="Variables">
              </b-tab>
              <b-tab title="Jobs">
              </b-tab>
              <b-tab title="Triggers">
              </b-tab>
              <b-tab title="Calendars">
              </b-tab>
            </b-tabs>
          </b-tab>
          <b-tab title="Server Log">
            <server-log v-on:showResult="showResult"/>
          </b-tab>
        </b-tabs>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="alert alert-success" role="alert" v-if="result.show && result.success">
          {{ result.message }}
        </div>
        <div class="alert alert-danger" role="alert" v-if="result.show && !result.success">
          {{ result.message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import Vue from 'vue';
  import {mapGetters, mapState} from 'vuex';
  import {BootstrapVue, AlertPlugin} from 'bootstrap-vue';

  const StreamControl = () => import(
    /* webpackChunkName: "streamControl" */
    /* webpackPrefetch: true */
    './StreamControl.vue');

  const StreamStatus = () => import(
    /* webpackChunkName: "streamStatus" */
    /* webpackPrefetch: true */
    './StreamStatus.vue');

  const StreamViewers = () => import(
    /* webpackChunkName: "streamViewers" */
    /* webpackPrefetch: true */
    './StreamViewers.vue');

  const ChatLog = () => import(
    /* webpackChunkName: "chatLog" */
    /* webpackPrefetch: true */
    './ChatLog.vue');

  const EmojiTool = () => import(
    /* webpackChunkName: "emojiTool" */
    /* webpackPrefetch: true */
    './EmojiTool.vue');

  const ServerLog = () => import(
    /* webpackChunkName: "serverLog" */
    /* webpackPrefetch: true */
    './ServerLog.vue');

  Vue.use(BootstrapVue);
  Vue.use(AlertPlugin);

  export default {
    name: 'Dashboard',
    components: {
      StreamControl,
      StreamStatus,
      StreamViewers,
      ChatLog,
      EmojiTool,
      ServerLog
    },
    data() {
      return {
        form: {
          apiKey: ''
        },
        result: {
          show: false,
          success: false,
          message: ''
        }
      }
    },
    computed: {
      ...mapState('apiKey', ['apiKey']),
      needApiKey() {
        return this.apiKey == null || this.apiKey.length === 0;
      }
    },
    mounted() {
      // establish stream listener
      const dashboard = this;
      function establishStreamListener() {
        const url = new URL('/stream/status', window.location.href);
        url.protocol = url.protocol.replace('http', 'ws');
        const socket = new WebSocket(url.href);
        socket.onmessage = function(event) {
          dashboard.$store.dispatch('stream/updateState', JSON.parse(event.data));
        };
        socket.onclose = function() {
          setTimeout(establishStreamListener, 100);
        };
      }
      establishStreamListener();

      // establish chat listener
      function establishChatListener() {
        const url = new URL('/chat', window.location.href);
        url.protocol = url.protocol.replace('http', 'ws');
        const socket = new WebSocket(url.href);
        socket.onmessage = function(event) {
          const response = JSON.parse(event.data);
          if (response.responseType === "message") {
            dashboard.$store.dispatch('chat/onMessage', response);
          } else if (response.responseType === "status") {
            dashboard.$store.dispatch('chat/onStatus', response);
          }
        };
        socket.onopen = function() {
          socket.send(JSON.stringify({requestType: "join"}));
        };
        socket.onclose = function() {
          setTimeout(establishChatListener, 100);
        };
      }
      establishChatListener();
    },
    methods: {
      alwaysTrue() {
        return true;
      },
      setApiKey() {
        this.$store.dispatch('apiKey/set', this.form.apiKey);
      },
      resetApiKey() {
        this.form.apiKey = this.apiKey; // populate the old value into the field
        this.$store.dispatch('apiKey/set', ''); // clear the api key
      },
      showResult(result) {
        var result = {
          message: result.message,
          success: result.success,
          show: true
        }
        this.result = result;
        setTimeout(function() { result.show = false }, 10000)
      }
    }
  };
</script>

<style lang="scss">
  @import 'node_modules/bootstrap/scss/bootstrap';
  @import 'node_modules/bootstrap-vue/src/index.scss';
  .form-group {
    text-align: left !important;
    white-space: nowrap;
  }
  .form-control {
    display: inline-block;
  }
  .form-label {
    display: inline-block;
    test-alight: right;
    width: 80pt;
  }
  .monospace {
    font-family: Consolas,Monaco,Lucida Console,Liberation Mono,DejaVu Sans Mono,Bitstream Vera Sans Mono,Courier New,monospace;
  }
  .scroll-x {
    overflow-x: scroll;
  }
  .max100w {
    max-width: calc(100vw - 50pt);
  }
  .max50w {
    max-width: calc(50vw - 100pt);
  }
  .max50wt {
    max-width: calc(50vw - 25pt);
  }
</style>
