<template>
  <div id="stream-loader">
    <div class="text">
      {{ message }}
    </div>
    <div class="spinner">
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
    </div>
  </div>
</template>

<script>
  import {mapState} from "vuex";
  import videojs from 'video.js/core'; // we only need the core library because we are not including http-streaming

  export default {
    name: 'stream-loader',
    computed: {
      ...mapState('stream', ['status', 'startTime', 'endTime']),
      ...mapState('time', ['now']),
      message() {
        switch (this.status) {
          case "OFFLINE": return "Waiting for the stream to go live";
          case "PAUSE": return "The stream has been paused";
          case "ONLINE": {
            if (this.endTime) { // Only pre-recorded streams have end times
              if (this.now - this.endTime > 0) {
                return "The stream has ended"; // The stream has ended but we haven't gotten the OFFLINE message yet
              } else {
                const timeUntil = (this.startTime - this.now) / 1000;
                return "The stream will start in " + videojs.formatTime(timeUntil, 1);
              }
            }
          }
        }
      }
    }
  }
</script>

<style lang="scss">
  #stream-loader {
    align-content: center;
    align-items: center;
    align-self: stretch;
    background: black;
    display: flex;
    flex-direction: column;
    height: 100%;
    justify-content: center;
    .text {
      color: white;
      font-family: 'Open Sans', sans-serif;
      font-size: 35px;
      padding-bottom: 30px;
    }
    .spinner {
      height: 38px;
      width: 38px;
      .dot {
        animation-duration: 5.5s;
        animation-iteration-count: infinite;
        animation-name: spin;
        height: 38px;
        opacity: 0;
        position: absolute;
        transform: rotate(225deg);
        width: 38px;
        &:after {
          background: white;
          border-radius: 5px;
          content: '';
          height: 6px;
          position: absolute;
          width: 6px;
        }
        &:nth-child(2) {
          animation-delay: 240ms;
        }
        &:nth-child(3) {
          animation-delay: 480ms;
        }
        &:nth-child(4) {
          animation-delay: 720ms;
        }
        &:nth-child(5) {
          animation-delay: 960ms;
        }
      }
    }
  }

  @font-face {
    font-display: swap;
    font-family: 'Open Sans';
    font-style: normal;
    font-weight: 400;
    src: local('Open Sans Regular'),
    local('OpenSans-Regular'),
    url("../assets/fonts/open-sans-regular.woff2") format('woff2');
  }

  @keyframes spin {
    0% {
      animation-timing-function: ease-out;
      opacity: 1;
      transform: rotate(225deg);
    }
    7% {
      animation-timing-function: linear;
      transform: rotate(345deg);
    }
    30% {
      animation-timing-function: ease-in-out;
      transform: rotate(455deg);
    }
    39% {
      animation-timing-function: linear;
      transform: rotate(690deg);
    }
    70% {
      animation-timing-function: ease-out;
      opacity: 1;
      transform: rotate(815deg);
    }
    75% {
      animation-timing-function: ease-out;
      transform: rotate(945deg);
    }
    76% {
      opacity: 0;
      transform: rotate(945deg);
    }
    100% {
      opacity: 0;
      transform: rotate(945deg);
    }
  }
</style>
