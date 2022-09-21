<template>
  <table class="table">
    <tbody>
    <tr>
      <th>IP Address</th>
      <th>Nick</th>
      <th style="width: 15em">Stream Size</th>
      <th style="width: 15em">Chat Opened</th>
      <th style="width: 15em">Window Visible</th>
      <th style="width: 15em">Full Screen</th>
      <th style="width: 15em">Stream Muted</th>
      <th style="width: 25em">Quality</th>
      <th style="width: 25em">Bandwidth</th>
    </tr>
    <template v-for="t in telemetry">
      <tr>
        <td>{{ t.ipAddress }}</td>
        <td>{{ t.request.chatNick }}</td>
        <td>{{ t.request.streamWidth }}x{{ t.request.streamHeight }}</td>
        <td>{{ t.request.chatOpened }}</td>
        <td>{{ t.request.visible }}</td>
        <td>{{ t.request.videoFullScreen || t.request.pageFullScreen }}</td>
        <td>{{ t.request.muted }}</td>
        <td>{{ t.request.videoQuality ? t.request.videoQuality : "" }}</td>
        <td>{{ t.request.clientBandwidth ? (t.request.clientBandwidth > 10000000
          ? Math.round(t.request.clientBandwidth / 1000000) + " Mbps"
          : Math.round(t.request.clientBandwidth / 1000) + " Kbps")
          : "" }}</td>
      </tr>
    </template>
    </tbody>
  </table>
</template>

<script>
  import {mapGetters, mapState} from 'vuex';
  import axios from 'axios';

  export default {
    name: 'stream-viewers',
    data () {
      return {
        telemetry: []
      };
    },
    computed: {
      ...mapState('apiKey', ['apiKey']),
      needApiKey() {
        return this.apiKey == null || this.apiKey.length === 0;
      },
    },
    mounted() {
      var self = this;
      // establish telemetry listener
      function establishTelemetryListener() {
        const url = new URL('/telemetry/fetch', window.location.href);
        axios.post(url.href, {
          key: self.apiKey
        }).then(response => {
          self.telemetry = response.data;
          setTimeout(establishTelemetryListener, 1000);
        });
      }
      establishTelemetryListener();
    }
  }
</script>
