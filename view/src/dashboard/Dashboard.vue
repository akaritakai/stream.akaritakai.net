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
          </b-tab>
          <b-tab title="Viewers">
            <stream-viewers :telemetry="telemetry"/>
          </b-tab>
          <b-tab title="Actions">
            <table class="table max100w">
              <tbody>
              <tr><td width="50%">
                <div class="form-group">
                <input v-model="selectionFilter" type="text" class="form-control" id="prefix"
                     placeholder="Filter/Search - max 10 results">
                <br>
                <b-table class="scroll-x max50wt" small head-variant="light"
                     id="stream-entries"
                     :busy.sync="form.selection.inProgress"
                     primary-key="name"
                     :fields="form.selection.fields"
                     select-mode="single"
                     :items="provideStreamEntries"
                     responsive="sm" selectable
                     @row-selected="onRowSelected">
                  <template #table-busy>
                    <div class="text-center text-danger my-2">
                      <strong>Please wait...</strong>
                    </div>
                  </template>
                  <template #cell(name)="data">
                    {{ data.value }}
                  </template>
                  <template #cell(desc)="data">
                    {{ data.item.metadata.name }}
                  </template>
                  <template #cell(duration)="data">
                    {{ liveOrDuration(data.item.metadata) }}
                  </template>
                </b-table>
                </div>
              </td>
              <td width="50%">
                <b-form v-if="!needApiKey && streamStopped" @submit.stop.prevent="alwaysTrue">
                  <div class="form-group">
                    <label for="startName" class="form-label">Name</label>
                    <input v-model="form.start.name" type="text" class="form-control max50w" id="startName" placeholder="The name of the media to start" required>
                  </div>
                  <div class="form-group">
                    <label for="startSeekTime" class="form-label">Seek Time</label>
                    <input v-model="form.start.seekTime" type="text" class="form-control max50w" id="startSeekTime" placeholder="(Optional) The time to seek to in the media as seconds, [mm:ss], or [hh:mm:ss]">
                  </div>
                  <div class="form-group">
                    <label for="startStartAt" class="form-label">Start At</label>
                    <input v-model="form.start.startAt" type="text" class="form-control max50w" id="startStartAt" placeholder="(Optional) The time to start at as [hh:mm a] (e.g. '03:45 pm')">
                  </div>
                  <div class="form-group">
                    <label for="startDelay" class="form-label">Delay</label>
                    <input v-model="form.start.delay" type="text" class="form-control max50w" id="startDelay" placeholder="(Optional) The delay in seconds to start">
                  </div>
                  <div class="form-group">
                    <button type="submit" class="btn btn-primary" @click="startStream" v-if="!form.start.inProgress">Start Stream</button>
                    <button type="submit" class="btn btn-primary" v-if="form.start.inProgress" disabled>
                      <b-spinner small/> Starting...
                    </button>
                  </div>
                </b-form>
                <b-form v-if="!needApiKey && streamStartingSoon" @submit.stop.prevent="alwaysTrue">
                  <div class="form-group">
                    <button type="submit" class="btn btn-primary" @click="stopStream" v-if="!form.stop.inProgress">Stop Stream</button>
                    <button type="submit" class="btn btn-primary" v-if="form.stop.inProgress" disabled>
                      <b-spinner small/> Stopping...
                    </button>
                  </div>
                </b-form>
                <b-form v-if="!needApiKey && streamRunning" @submit.stop.prevent="alwaysTrue">
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
                <b-form v-if="!needApiKey && streamPaused" @submit.stop.prevent="alwaysTrue">
                  <div class="form-group">
                    <label for="resumeSeekTime" class="form-label">Seek Time</label>
                    <input v-model="form.resume.seekTime" type="text" class="form-control max50w" id="resumeSeekTime" placeholder="(Optional) The time to seek to in the media as seconds, [mm:ss], or [hh:mm:ss]">
                  </div>
                  <div class="form-group">
                    <label for="resumeStartAt" class="form-label">Start At</label>
                    <input v-model="form.resume.startAt" type="text" class="form-control max50w" id="resumeStartAt" placeholder="(Optional) The time to start at as [hh:mm a] (e.g. '03:45 pm')">
                  </div>
                  <div class="form-group">
                    <label for="resumeDelay" class="form-label">Delay</label>
                    <input v-model="form.resume.delay" type="text" class="form-control max50w" id="resumeDelay" placeholder="(Optional) The delay in seconds to start">
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
                <b-form v-if="!needApiKey && enabled" @submit.stop.prevent="alwaysTrue">
                  <div class="form-group">
                    <button type="submit" class="btn btn-primary" @click="disableChat" v-if="!form.disableChat.inProgress">Disable Chat</button>
                    <button type="submit" class="btn btn-primary" v-if="form.disableChat.inProgress" disabled>
                      <b-spinner small/> Disabling...
                    </button>
                  </div>
                </b-form>
                <b-form v-if="!needApiKey && !enabled" @submit.stop.prevent="alwaysTrue">
                  <div class="form-group">
                    <button type="submit" class="btn btn-primary" @click="enableChat" v-if="!form.enableChat.inProgress">Enable Chat</button>
                    <button type="submit" class="btn btn-primary" v-if="form.enableChat.inProgress" disabled>
                      <b-spinner small/> Enabling...
                    </button>
                  </div>
                </b-form>
                <b-form v-if="!needApiKey" @submit.stop.prevent="alwaysTrue">
                  <div class="form-group">
                    <button type="submit" class="btn btn-primary" @click="resetApiKey">Reset API Key</button>
                  </div>
                </b-form>
             </td></tr>
             </tbody>
           </table>
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
          <b-tab title="Info">
            <textarea readonly rows="25" class="form-control monospace" style="min-width:500px;max-width:100%;min-height:50px;height:100%;width:100%;" v-model="form.info.body">
            </textarea>
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
  import axios from 'axios';
  import videojs from 'video.js/core';
  import moment from 'moment';

  const StreamViewers = () => import(
    /* webpackChunkName: "streamViewers" */
    /* webpackPrefetch: true */
    './StreamViewers.vue');

  Vue.use(BootstrapVue);
  Vue.use(AlertPlugin);

  export default {
    name: 'Dashboard',
    components: {
      StreamViewers
    },
    data() {
      return {
        form: {
          apiKey: '',
          start: {
            inProgress: false,
            name: '',
            delay: '',
            seekTime: '',
            startAt: ''
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
          },
          selection: {
            filter: '',
            fields: [
              { key: 'name', label: 'Name' },
              { key: 'desc', label: 'Description'},
              { key: 'duration', label: 'Duration'}
            ],
            items: [],
            selected: null,
            inProgress: false
          },
          info: {
            body: 'placeholder'
          }
        },
        selectionFilter: '',
        result: {
          show: false,
          success: false,
          message: ''
        },
        quartz: {
          jobFields: []
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
    watch: {
      selectionFilter(value) {
        this.form.selection.filter = value
        this.$root.$emit('bv::refresh::table', 'stream-entries')
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

      function establishLogListener() {
        const url = new URL('/log/fetch', window.location.href);
        const instance = axios.create({
          onDownloadProgress: event => {
            dashboard.form.info.body = event.currentTarget.response;
          }
        });
        instance.post(url.href, {
          key: dashboard.apiKey
        }).then(response => {
          setTimeout(establishLogListener, 1000);
        });
      }
      establishLogListener();
    },
    methods: {
      alwaysTrue() {
        return true;
      },
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
          startAt: this.convertStartAt(this.form.start.startAt)
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
      onRowSelected(items) {
        this.form.selection.selected = items;
        if (items.length == 1) {
          let entry = items[0];
          [ this.form.start.name ] = [ entry.name ]
        }
      },
      provideStreamEntries(ctx) {
        return axios.post('/stream/dir', {
          key: this.apiKey,
          filter: this.form.selection.filter
        }).then(response => {
          const entries = response.data.entries;
          return(entries)
        }).catch(error => {
          if (error.response) {
            this.showResult(false, error.response.data);
          } else {
            this.showResult(false, error.message);
          }
          return([])
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
      },
      liveOrDuration(metadata) {
        return metadata.live ? "LIVE" : videojs.formatTime(metadata.duration / 1000, 1)
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
