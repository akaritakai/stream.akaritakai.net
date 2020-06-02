<template>
  <div id="stream">
    <stream-video v-if="streamRunning"/>
    <stream-loader v-else/>
  </div>
</template>

<script>
  import {mapState} from "vuex";

  const StreamLoader = () => import(
    /* webpackPrefetch: true */
    /* webpackChunkName: "streamLoader" */
    './StreamLoader.vue');
  const StreamVideo = () => import(
    /* webpackPrefetch: true */
    /* webpackChunkName: "streamVideo" */
    './StreamVideo.vue');

  export default {
    name: 'stream',
    components: {
      StreamLoader,
      StreamVideo
    },
    computed: {
      ...mapState('stream', ['status', 'startTime', 'endTime']),
      ...mapState('time', ['now']),
      streamRunning() {
        return this.status === "ONLINE" // stream is online
          && (this.now - this.startTime >= 0) // stream has started
          && (!this.endTime || (this.now - this.endTime < 0)) // stream hasn't ended yet
      }
    },
    mounted() {
      this.establishListener();
    },
    methods: {
      establishListener() {
        const url = new URL('/stream/status', window.location.href);
        url.protocol = url.protocol.replace('http', 'ws');
        const socket = new WebSocket(url.href);
        const stream = this;
        socket.onmessage = function(event) {
          stream.$store.dispatch('stream/updateState', JSON.parse(event.data));
        };
        socket.onerror = function() {
          socket.close();
        }
        socket.onclose = function() {
          setTimeout(stream.establishListener, 100);
        };
      }
    }
  }
</script>

<style lang="scss">
  #stream {
    align-self: stretch;
    flex-grow: 1;
    height: 100%;
  }
</style>
