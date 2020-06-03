<template>
  <div id="root"
       ref="root"
       v-bind:class="{
         'root-wide': this.isWide,
         'root-narrow': this.isNarrow
       }">
    <stream/>
    <chat/>
  </div>
</template>

<script>
  const Chat = () => import(
    /* webpackPrefetch: true */
    /* webpackChunkName: "chat" */
    './chat/Chat.vue');
  const Stream = () => import(
    /* webpackPrefetch: true */
    /* webpackChunkName: "stream" */
    './Stream.vue');

  const telemetryTimeout = 1000;

  export default {
    name: 'App',
    components: {
      Chat,
      Stream
    },
    data() {
      return {
        width: 0
      }
    },
    computed: {
      isNarrow() {
        return this.width < 680;
      },
      isWide() {
        return !this.isNarrow;
      }
    },
    mounted() {
      this.establishListener();
      this.$nextTick(() => {
        window.addEventListener('resize', this.onResize);
        this.onResize();
      });
    },
    beforeDestroy() {
      window.removeEventListener('resize', this.onResize);
    },
    methods: {
      onResize() {
        this.width = this.$refs.root.clientWidth;
      },
      establishListener() {
        const url = new URL('/telemetry', window.location.href);
        url.protocol = url.protocol.replace('http', 'ws');
        const socket = new WebSocket(url.href);
        const app = this;
        socket.onopen = function() {
          setTimeout(() => { app.sendTelemetry(socket) }, telemetryTimeout);
        };
        socket.onclose = function() {
          setTimeout(app.establishListener, telemetryTimeout);
        };
      },
      sendTelemetry(socket) {
        const root = document.getElementById("root");
        const stream = document.getElementById("stream");
        try {
          socket.send(JSON.stringify({
            id: this.$store.state.telemetry.telemetryId,
            timestamp: new Date().getTime(),
            userAgent: navigator.userAgent,
            timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
            screenHeight: window.screen.height,
            screenWidth: window.screen.width,
            availHeight: window.screen.availHeight,
            availWidth: window.screen.availWidth,
            rootHeight: root ? root.clientHeight : null,
            rootWidth: root ? root.clientWidth : null,
            streamHeight: stream ? stream.clientHeight : null,
            streamWidth: stream ? stream.clientWidth : null,
            visible: document.visibilityState === "visible",
            videoFullScreen: document.fullscreenElement !== null,
            pageFullScreen: window.innerHeight === screen.height,
            chatOpened: this.$store.state.chat.opened,
            chatNick: this.$store.state.nick.nick,
            videoQuality: this.$store.state.stream.quality,
            clientBandwidth: this.$store.state.stream.bandwidth,
            muted: this.$store.state.stream.muted
          }));
          setTimeout(() => { this.sendTelemetry(socket)}, telemetryTimeout);
        } catch (_) {
        }
      }
    }
  };
</script>

<style lang="scss">
  html {
    overflow-x: hidden;
  }
  body {
    background-color: black;
  }
  #root {
    // Fill the entire screen
    bottom: 0;
    height: 100%;
    left: 0;
    position: absolute;
    right: 0;
    top: 0;
    width: 100%;

    // Flex container
    align-items: stretch;
    display: inline-flex;
    flex-wrap: nowrap;

    // Narrow view
    &.root-narrow {
      flex-direction: column;
    }

    // Wide view
    &.root-wide {
      flex-direction: row;
    }
  }
</style>
