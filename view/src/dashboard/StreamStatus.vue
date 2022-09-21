<template>
  <table class="table">
    <tbody>
    <tr>
      <th scope="row">Stream Status</th>
      <td>{{ streamStatusDescription }}</td>
    </tr>
    <tr v-if="!streamStopped">
      <th scope="row">Media Name</th>
      <td>{{ mediaName }}</td>
    </tr>
    <tr v-if="!streamStopped">
      <th scope="row">Media Position</th>
      <td>{{ mediaPositionDescription }}</td>
    </tr>
    <tr v-if="streamStartingSoon">
      <th scope="row">Stream Starts In</th>
      <td>{{ streamStartInDescription }}</td>
    </tr>
    <tr>
      <th scope="row">Chat Status</th>
      <td>{{ chatStatusDescription }}</td>
    </tr>
    </tbody>
  </table>
</template>

<script>
  import {mapGetters, mapState} from 'vuex';
  import videojs from 'video.js/core';

  export default {
    name: 'stream-status',
    data () {
      return { }
    },
    computed: {
      ...mapState('stream', [
        'status',
        'playlist',
        'mediaName',
        'mediaDuration',
        'startTime',
        'endTime',
        'seekTime',
        'live'
      ]),
      ...mapState('apiKey', ['apiKey']),
      ...mapState('chat', ['enabled']),
      ...mapGetters('time', ['now']),
      chatStatusDescription() {
        if (this.enabled) {
          return "ENABLED";
        } else {
          return "DISABLED";
        }
      },
      needApiKey() {
        return this.apiKey == null || this.apiKey.length === 0;
      },
      streamStopped() {
        // stream explicitly offline or "online but finished"
        return this.status === "OFFLINE"
            || (this.status === "ONLINE" && (this.now - this.endTime) >= 0);
      },
      streamStartingSoon() {
        // stream is online and not yet started
        return this.status === "ONLINE"
          && (this.startTime - this.now) > 0;
      },
      streamRunning() {
        // stream explicitly online and started and not yet finished
        return this.status === "ONLINE"
          && (this.now - this.startTime) >= 0
          && (!this.endTime || ((this.endTime - this.now) > 0));
      },
      streamPaused() {
        // stream explicitly paused
        return this.status === "PAUSE";
      },
      streamStatusDescription() {
        if (this.streamStopped) {
          return "The stream is stopped";
        } else if (this.streamStartingSoon) {
          return "The stream is starting soon";
        } else if (this.streamRunning) {
          return "The stream is running";
        } else if (this.streamPaused) {
          return "The stream is paused";
        }
      },
      mediaPositionDescription() {
        if (this.live) {
          return null;
        } else if (this.streamStartingSoon || this.streamPaused) {
          return videojs.formatTime(this.seekTime / 1000, 1)
            + " / "
            + videojs.formatTime(this.mediaDuration / 1000, 1);
        } else if (this.streamRunning) {
          const currentPosition = ((this.now - this.startTime) + this.seekTime) / 1000;
          return videojs.formatTime(currentPosition, 1)
            + " / "
            + videojs.formatTime(this.mediaDuration / 1000, 1);
        } else {
          return null;
        }
      },
      streamStartInDescription() {
        if (this.streamStartingSoon) {
          const timeUntil = (this.startTime - this.now) / 1000;
          return videojs.formatTime(timeUntil, 1);
        } else {
          return null;
        }
      }
    }
  }
</script>
