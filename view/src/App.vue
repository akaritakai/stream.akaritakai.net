<template>
  <div id="root">
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

  export default {
    name: 'App',
    components: {
      Chat,
      Stream
    },
    mounted() {
      const store = this.$store;
      const telemetryTimeout = 1000;
      function establishListener() {
        let socket;
        if (process.env.NODE_ENV === "development") {
          socket = new WebSocket("ws://localhost/telemetry");
        } else {
          socket = new WebSocket("wss://stream.akaritakai.net/telemetry");
        }

        const socketState = [false];
        socket.onopen = function() {
          socketState[0] = true;
          function sendTelemetry() {
            if (socketState[0] === true) {
              const root = document.getElementById("root");
              const stream = document.getElementById("stream");
              socket.send(JSON.stringify({
                id: store.state.telemetry.telemetryId,
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
                chatOpened: store.state.chat.opened,
                chatNick: store.state.nick.nick,
                videoQuality: store.state.stream.quality,
                clientBandwidth: store.state.stream.bandwidth,
                muted: store.state.stream.muted
              }));
              setTimeout(sendTelemetry, telemetryTimeout);
            }
          }
          sendTelemetry();
        };
        socket.onclose = function() {
          socketState[0] = false;
          setTimeout(establishListener, telemetryTimeout);
        };
      }
      establishListener();
    }
  };
</script>

<style lang="scss">
  html {
    overflow-x: hidden;
  }
  #root {
    bottom: 0;
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    height: 100%;
    left: 0;
    position: absolute;
    right: 0;
    top: 0;
    width: 100%;
  }
</style>
