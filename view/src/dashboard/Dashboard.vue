<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col text-center">
        <h1>Dashboard</h1>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <h2>Status</h2>
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
        <h2>Viewers</h2>
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
      </div>
      <div class="col">
        <h2>Actions</h2>
        <b-form v-if="needApiKey" @submit.stop.prevent="true">
          <div class="form-group">
            <label for="apiKey">API Key</label>
            <input v-model="form.apiKey" type="text" class="form-control" id="apiKey" placeholder="API Key" required>
          </div>
          <div class="form-group">
            <button type="submit" class="btn btn-primary" @click="setApiKey">Set API Key</button>
          </div>
        </b-form>
        <b-form v-if="!needApiKey && streamStopped" @submit.stop.prevent="true">
          <div class="form-group">
            <label for="startName">Name</label>
            <input v-model="form.start.name" type="text" class="form-control" id="startName" placeholder="The name of the media to start" required>
          </div>
          <div class="form-group">
            <label for="startSeekTime">Seek Time</label>
            <input v-model="form.start.seekTime" type="text" class="form-control" id="startSeekTime" placeholder="(Optional) The time to seek to in the media as seconds, [mm:ss], or [hh:mm:ss]">
          </div>
          <div class="form-group">
            <label for="startStartAt">Start At</label>
            <input v-model="form.start.startAt" type="text" class="form-control" id="startStartAt" placeholder="(Optional) The time to start at as [hh:mm a] (e.g. '03:45 pm')">
          </div>
          <div class="form-group">
            <label for="startDelay">Delay</label>
            <input v-model="form.start.delay" type="text" class="form-control" id="startDelay" placeholder="(Optional) The delay in seconds to start">
          </div>
          <div class="form-group">
            <label for="startLive">Live</label>
            <input v-model="form.start.live" type="checkbox" class="form-control" id="startLive">
          </div>
          <div class="form-group">
            <button type="submit" class="btn btn-primary" @click="startStream" v-if="!form.start.inProgress">Start Stream</button>
            <button type="submit" class="btn btn-primary" v-if="form.start.inProgress" disabled>
              <b-spinner small/> Starting...
            </button>
          </div>
        </b-form>
        <b-form v-if="!needApiKey && streamStartingSoon" @submit.stop.prevent="true">
          <div class="form-group">
            <button type="submit" class="btn btn-primary" @click="stopStream" v-if="!form.stop.inProgress">Stop Stream</button>
            <button type="submit" class="btn btn-primary" v-if="form.stop.inProgress" disabled>
              <b-spinner small/> Stopping...
            </button>
          </div>
        </b-form>
        <b-form v-if="!needApiKey && streamRunning" @submit.stop.prevent="true">
          <div class="form-group">
            <button type="submit" class="btn btn-primary" @click="pauseStream" v-if="!form.pause.inProgress">Pause Stream</button>
            <button type="submit" class="btn btn-primary" v-if="form.pause.inProgress" disabled>
              <b-spinner small/> Pausing...
            </button>
            <button type="submit" class="btn btn-primary" @click="stopStream" v-if="!form.stop.inProgress">Stop Stream</button>
            <button type="submit" class="btn btn-primary" v-if="form.stop.inProgress" disabled>
              <b-spinner small/> Stopping...
            </button>
          </div>
        </b-form>
        <b-form v-if="!needApiKey && streamPaused" @submit.stop.prevent="true">
          <div class="form-group">
            <label for="resumeSeekTime">Seek Time</label>
            <input v-model="form.resume.seekTime" type="text" class="form-control" id="resumeSeekTime" placeholder="(Optional) The time to seek to in the media as seconds, [mm:ss], or [hh:mm:ss]">
          </div>
          <div class="form-group">
            <label for="resumeStartAt">Start At</label>
            <input v-model="form.resume.startAt" type="text" class="form-control" id="resumeStartAt" placeholder="(Optional) The time to start at as [hh:mm a] (e.g. '03:45 pm')">
          </div>
          <div class="form-group">
            <label for="resumeDelay">Delay</label>
            <input v-model="form.resume.delay" type="text" class="form-control" id="resumeDelay" placeholder="(Optional) The delay in seconds to start">
          </div>
          <div class="form-group">
            <button type="submit" class="btn btn-primary" @click="resumeStream" v-if="!form.resume.inProgress">Resume Stream</button>
            <button type="submit" class="btn btn-primary" v-if="form.resume.inProgress" disabled>
              <b-spinner small/> Resuming...
            </button>
            <button type="submit" class="btn btn-primary" @click="stopStream" v-if="!form.stop.inProgress">Stop Stream</button>
            <button type="submit" class="btn btn-primary" v-if="form.stop.inProgress" disabled>
              <b-spinner small/> Stopping...
            </button>
          </div>
        </b-form>
        <b-form v-if="!needApiKey && enabled" @submit.stop.prevent="true">
          <div class="form-group">
            <button type="submit" class="btn btn-primary" @click="disableChat" v-if="!form.disableChat.inProgress">Disable Chat</button>
            <button type="submit" class="btn btn-primary" v-if="form.disableChat.inProgress" disabled>
              <b-spinner small/> Disabling...
            </button>
          </div>
        </b-form>
        <b-form v-if="!needApiKey && !enabled" @submit.stop.prevent="true">
          <div class="form-group">
            <button type="submit" class="btn btn-primary" @click="enableChat" v-if="!form.enableChat.inProgress">Enable Chat</button>
            <button type="submit" class="btn btn-primary" v-if="form.enableChat.inProgress" disabled>
              <b-spinner small/> Enabling...
            </button>
          </div>
        </b-form>
        <b-form v-if="!needApiKey" @submit.stop.prevent="true">
          <div class="form-group">
            <button type="submit" class="btn btn-primary" @click="resetApiKey">Reset API Key</button>
          </div>
        </b-form>
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
  import axios from 'axios';
  import videojs from 'video.js';
  import moment from 'moment';

  Vue.use(BootstrapVue);
  Vue.use(AlertPlugin);

  export default {
    name: 'Dashboard',
    data() {
      return {
        form: {
          apiKey: '',
          start: {
            inProgress: false,
            name: '',
            delay: '',
            seekTime: '',
            startAt: '',
            live: false
          },
          pause: {
            inProgress: false
          },
          resume: {
            inProgress: false,
            delay: '',
            seekTime: '',
            startAt: ''
          },
          stop: {
            inProgress: false
          },
          disableChat: {
            inProgress: false
          },
          enableChat: {
            inProgress: false
          }
        },
        result: {
          show: false,
          success: false,
          message: ''
        },
        telemetry: []
      }
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

      // establish telemetry listener
      function establishTelemetryListener() {
        const url = new URL('/telemetry/fetch', window.location.href);
        axios.post(url.href, {
          key: dashboard.apiKey
        }).then(response => {
          dashboard.telemetry = response.data;
          setTimeout(establishTelemetryListener, 1000);
        });
      }
      establishTelemetryListener();
    },
    methods: {
      disableChat() {
        this.form.disableChat.inProgress = true;
        axios.post('/chat/disable', {
          key: this.apiKey
        }).then(response => {
          this.showResult(true, response.data);
          this.form.disableChat.inProgress = false;
        }).catch(error => {
          if (error.response) {
            this.showResult(false, error.response.data);
          } else {
            this.showResult(false, error.message);
          }
          this.form.disableChat.inProgress = false;
        })
      },
      enableChat() {
        this.form.enableChat.inProgress = true;
        axios.post('/chat/enable', {
          key: this.apiKey
        }).then(response => {
          this.showResult(true, response.data);
          this.form.enableChat.inProgress = false;
        }).catch(error => {
          if (error.response) {
            this.showResult(false, error.response.data);
          } else {
            this.showResult(false, error.message);
          }
          this.form.enableChat.inProgress = false;
        })
      },
      setApiKey() {
        this.$store.dispatch('apiKey/set', this.form.apiKey);
      },
      resetApiKey() {
        this.form.apiKey = this.apiKey; // populate the old value into the field
        this.$store.dispatch('apiKey/set', ''); // clear the api key
      },
      startStream() {
        this.form.start.inProgress = true;
        axios.post('/stream/start', {
          key: this.apiKey,
          name: this.form.start.name,
          seekTime: this.convertSeekTime(this.form.start.seekTime),
          delay: this.convertDelay(this.form.start.delay),
          startAt: this.convertStartAt(this.form.start.startAt),
          live: this.form.start.live
        }).then(response => {
          this.showResult(true, response.data);
          this.form.start.inProgress = false;
        }).catch(error => {
          if (error.response) {
            this.showResult(false, error.response.data);
          } else {
            this.showResult(false, error.message);
          }
          this.form.start.inProgress = false;
        })
      },
      pauseStream() {
        this.form.pause.inProgress = true;
        axios.post('/stream/pause', {
          key: this.apiKey
        }).then(response => {
          this.showResult(true, response.data);
          this.form.pause.inProgress = false;
        }).catch(error => {
          if (error.response) {
            this.showResult(false, error.response.data);
          } else {
            this.showResult(false, error.message);
          }
          this.form.pause.inProgress = false;
        })
      },
      resumeStream() {
        this.form.resume.inProgress = true;
        axios.post('/stream/resume', {
          key: this.apiKey,
          delay: this.convertDelay(this.form.resume.delay),
          seekTime: this.convertSeekTime(this.form.resume.seekTime),
          startAt: this.convertStartAt(this.form.resume.startAt)
        }).then(response => {
          this.showResult(true, response.data);
          this.form.resume.inProgress = false;
        }).catch(error => {
          if (error.response) {
            this.showResult(false, error.response.data);
          } else {
            this.showResult(false, error.message);
          }
          this.form.resume.inProgress = false;
        })
      },
      stopStream() {
        this.form.stop.inProgress = true;
        axios.post('/stream/stop', {
          key: this.apiKey
        }).then(response => {
          this.showResult(true, response.data);
          this.form.stop.inProgress = false;
        }).catch(error => {
          if (error.response) {
            this.showResult(false, error.response.data);
          } else {
            this.showResult(false, error.message);
          }
          this.form.stop.inProgress = false;
        })
      },
      showResult(success, message) {
        this.result.message = message;
        this.result.success = success;
        this.result.show = true;
      },
      convertDelay(delayTimeStr) {
        if (delayTimeStr == null || delayTimeStr.length === 0) {
          return null;
        }
        return parseInt(delayTimeStr) * 1000;
      },
      convertSeekTime(seekTimeStr) {
        if (seekTimeStr == null || seekTimeStr.length === 0) {
          return null;
        }
        let seconds = 0;
        let minutes = 1;
        let tokens = seekTimeStr.split(':');
        while (tokens.length > 0) {
          seconds += minutes * parseInt(tokens.pop());
          minutes *= 60;
        }
        return seconds * 1000;
      },
      convertStartAt(startAtStr) {
        if (startAtStr == null || startAtStr.length === 0) {
          return null;
        }
        let dt = moment(startAtStr, ["h:mm a"]);
        if (dt.isBefore(moment())) {
          dt.add(1, 'days');
        }
        return dt.valueOf();
      }
    }
  };
</script>

<style lang="scss">
  @import 'node_modules/bootstrap/scss/bootstrap';
  @import 'node_modules/bootstrap-vue/src/index.scss';
</style>
