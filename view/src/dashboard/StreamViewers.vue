<template>
  <table class="table">
    <tbody>
    <tr>
      <th>IP Address</th>
      <th>Nick</th>
      <th>Stream Size</th>
      <th>Chat Opened</th>
      <th>Window Visible</th>
      <th>Full Screen</th>
      <th>Stream Muted</th>
      <th>Quality</th>
      <th>Bandwidth</th>
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
  export default {
    name: 'stream-viewers',
    data () {
      return { };
    },
    props: {
      telemetry: null
    }
  }
</script>
