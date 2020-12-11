<template>
  <div id="stream-video">
    <video ref="video" class="video-js" playsinline/>
  </div>
</template>

<script>
  import {mapGetters, mapState} from 'vuex';
  import videojs from 'video.js/core'; // we only need the core library because we are not including http-streaming

  // video.js's http-streaming library is awful and broken. Fun unfixed bugs include:
  // - Being unable to play UHD content (or content with HLS segments that are too big)
  //   https://github.com/videojs/http-streaming/issues/192
  // - Hanging indefinitely on the play() API call in Chrome and reporting
  //   "NotSupportedError: MediaSource.addSourceBuffer: Can't play type"
  //   for an unknown reason (probably the first segment is too long, or has an atypical color palette)
  //
  // For these reasons and more, we use the better supported hls.js library. However, we still want video.js's skinning
  // and event listening functions. So, we make an effort to try to bind the libraries together by adding hls.js as a
  // source handler in video.js with some effort.
  import Hls from '../node_modules/hls.js/dist/hls.light.js'; // we don't need subs or alternate audio tracks
  function HlsJsHandler(source, tech) {
    const el = tech.el();
    let duration = null;
    const hls = this.hls = new Hls({
      debug: process.env.NODE_ENV === "development",
      enableWorker: true,
      maxBufferSize: 40*1000*1000
    });

    this.dispose = () => {
      hls.destroy();
    };

    this.duration = () => duration || el.duration || 0;

    hls.on(Hls.Events.LEVEL_LOADED, (event, data) => {
      duration = data.details.live ? Infinity : data.details.totalduration;
    });

    Object.keys(Hls.Events).forEach(key => {
      const eventName = Hls.Events[key];
      hls.on(eventName, function(event, data) {
        tech.trigger(eventName, data);
      });
    });

    hls.loadSource(source.src);
    hls.attachMedia(el);
    hls.on(Hls.Events.ERROR, (event, data) => {
      if (data.details === Hls.ErrorDetails.BUFFER_FULL_ERROR) {
        hls.startLoad();
      } else {
        if (data.fatal) {
          switch (data.type) {
            case Hls.ErrorTypes.NETWORK_ERROR:
              hls.startLoad();
              break;
            case Hls.ErrorTypes.MEDIA_ERROR:
              hls.recoverMediaError();
              break;
          }
        }
      }
    });
  }

  // Add the hls.js handler if the browser does not natively support HLS
  const isAppleDevice = navigator.platform && /iPad|iPhone|iPod/.test(navigator.platform);
  if (!isAppleDevice) {
    videojs.getTech('Html5').registerSourceHandler({
      canHandleSource(source) {
        if (/^application\/(x-mpegURL|vnd\.apple\.mpegURL)$/i.test(source.type)) {
          return 'probably';
        } else if (/\.m3u8/i.test(source.src)) {
          return 'maybe';
        } else {
          return '';
        }
      },
      handleSource(source, tech) {
        return new HlsJsHandler(source, tech);
      },
      canPlayType(type) {
        if (/^application\/(x-mpegURL|vnd\.apple\.mpegURL)$/i.test(type)) {
          return 'probably';
        } else {
          return '';
        }
      }
    }, 0);
  }

  export default {
    name: 'stream-video',
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
      ...mapState('prefs', ['volumePref']),
      ...mapGetters('time', ['now'])
    },
    data() {
      return {
        player: null,
        state: 'STOPPED',
        userControllingVolume: false
      }
    },
    watch: {
      // Guard against the video being paused
      now() {
        if (this.state === 'STARTED' && this.player.paused()) {
          this.playStream();
        }
      }
    },
    mounted() {
      // Stream has started and is running, and we have rendered our content

      // Create the video player
      this.player = videojs(this.$refs.video, {
        // Specifies the controls we want
        controls: true,
        bigPlayButton: false,
        controlBar: {
          playToggle: false,
          progressControl: false,
          remainingTimeDisplay: false,
          pictureInPictureToggle: false
        }
      });
      this.player.fill(true);

      if (!this.live) {
        // Customize the control bar to our liking
        // Goal: [VOLUME -------------- DESCRIPTION DISPLAY -------------- FULLSCREEN]

        // Create a description display control
        const mediaName = this.mediaName;
        const TimeDisplay = videojs.getComponent('TimeDisplay');
        class DescriptionDisplay extends TimeDisplay {
          constructor(player, options) {
            super(player, options);
            this.on(player, 'durationchange', this.updateContent);
          }

          createEl() {
            const el = videojs.dom.createEl('div', {
              className: 'vjs-description-display'
            });
            videojs.dom.appendContent(el, mediaName);
            return el;
          }

          updateContent() {
            // Only called if the stream is pre-recorded
            const currentTime = this.player_.currentTime();
            const duration = this.player_.duration();
            const timeString = videojs.formatTime(currentTime, 1) + " / " + videojs.formatTime(duration, 1);
            videojs.dom.emptyEl(this.el());
            videojs.dom.appendContent(this.el(), mediaName + " - " + timeString);
          }
        }
        const descriptionDisplay = new DescriptionDisplay(this.player);

        // Insert it right before the fullscreen control (at the end of the bar)
        const controlBar = document.getElementsByClassName('vjs-control-bar')[0];
        const fullscreenControl = document.getElementsByClassName('vjs-fullscreen-control')[0];
        controlBar.insertBefore(descriptionDisplay.el(), fullscreenControl);
      }

      // Handle updating quality info
      this.player.on('progress', () => {
        try {
          const hls = this.player.tech({ IWillNotUseThisInPlugins: true }).sourceHandler_.hls;
          const currentQuality = hls.levels[hls.currentLevel];
          const quality = () => {
            if (!currentQuality || !currentQuality.width || !currentQuality.height || !currentQuality.bitrate) {
              return null;
            } else {
              const width = Math.round(currentQuality.width);
              const height = Math.round(currentQuality.height);
              const kbps = Math.round(currentQuality.bitrate / 1000);
              return width + "x" + height + "@" + kbps + "k";
            }
          }
          const bandwidth = () => isNaN(hls.bandwidthEstimate) ? null : Math.round(hls.bandwidthEstimate)
          this.$store.dispatch('stream/updateQualityInfo', {
            quality: quality(),
            bandwidth: bandwidth()
          });
        } catch (_) {
          // No support for hls.js
        }
      });
      this.player.on('volumechange', () => {
        this.$store.dispatch('stream/updateMutedInfo', {
          muted: this.player.muted()
        });
      });

      // Handle drifts during playback (also fixes race conditions for play + seek on Android)
      if (!this.live) {
        this.player.on('durationchange', () => {
          // The stream can only be seeked if it is pre-recorded
          if (this.state === 'STARTED') {
            const startTime = this.startTime; // the time the stream started
            const seekTimeMs = this.seekTime; // where the stream was seeked to when the stream started
            const now = new Date().getTime(); // the current time
            const seekTime = ((now - startTime) + seekTimeMs) / 1000;
            if (seekTime - this.player.currentTime() > 5) { // 5 second drift is too much
              this.player.currentTime(seekTime);
            }
          }
        });
      }

      // Handle iOS pausing the video when transitioning to/from fullscreen
      this.player.on('fullscreenchange', () => {
        this.playStream();
      });

      // Recover any errors that might occur
      this.player.on('error', () => {
        try {
          const hls = this.player.tech({ IWillNotUseThisInPlugins: true }).sourceHandler_.hls;
          hls.recoverMediaError();
          this.playStream();
        } catch (_) {
          this.playStream();
        }
      });

      // Load the user's desired volume and store any changes the user makes
      this.player.volume(this.volumePref);
      this.player.on('volumechange', () => {
        if (this.userControllingVolume) {
          this.$store.dispatch('prefs/setVolumePref', this.player.volume());
        }
      });
      this.userControllingVolume = true;

      // Load the stream
      this.player.src({
        "type": "application/x-mpegURL",
        "src": this.playlist
      });
      this.player.load();

      // Start the steam and seek to the appropriate location
      this.playStream();
    },
    beforeDestroy() {
      // Stream is paused/stopped/not started yet, and our content is about to be disposed

      // Dispose of the player (causes the video to stop and the HTML video tag to be destroyed)
      if (this.player) {
        this.player.dispose();
      }
      this.$store.dispatch('stream/updateQualityInfo', {
        quality: null,
        bandwidth: null
      });
      this.$store.dispatch('stream/updateMutedInfo', {
        muted: null
      });
    },
    methods: {
      playStream() {
        if (this.state === 'STARTING') {
          return;
        }
        this.state = 'STARTING';
        this.player.play().then(_ => {
          this.$store.dispatch('stream/updateMutedInfo', {
            muted: this.player.muted()
          });
          this.onStreamPlaying();
        }).catch(_ => {
          // Mute the player for the user so that autoplay can work
          this.userControllingVolume = false;
          this.player.muted(true);
          this.$store.dispatch('stream/updateMutedInfo', {
            muted: true
          });
          this.player.play().then(_ => {
            this.onStreamPlaying();
          });
        });
      },
      onStreamPlaying() {
        this.seekStream();
        this.userControllingVolume = true;
        this.state = 'STARTED';
      },
      seekStream() {
        // Seeks the stream to the appropriate location
        // Note: We assume the stream has already started and is running
        if (!this.live) { // The stream can only be seeked if it is pre-recorded
          const startTime = this.startTime; // the time the stream started
          const seekTimeMs = this.seekTime; // where the stream was seeked to when the stream started
          const now = new Date().getTime(); // the current time
          const seekTime = ((now - startTime) + seekTimeMs) / 1000;
          this.player.currentTime(seekTime);
        }
      }
    }
  }
</script>

<style lang="scss">
  @import "../node_modules/video.js/dist/video-js.css";

  #stream-video {
    display: flex;
    flex-grow: 1;
    align-items: stretch;
  }

  // Cause the video.js element to fill the available space
  .vjs_video_3-dimensions {
    width: auto;
    height: auto;
    flex-grow: 1;
  }

  // Cause elements to space out evenly on the control bar
  .vjs-control-bar {
    align-items: stretch;
    display: flex;
    flex-wrap: nowrap;
    justify-content: space-between;
  }

  // Center content in the description display
  .vjs-description-display {
    align-content: center;
    align-items: center;
    display: flex;
    justify-content: center;
  }

  // Block touch event from pausing the stream
  .vjs-tech {
    pointer-events: none;
  }

  // Safari unnecessarily shows the subs button
  .vjs-subs-caps-button {
    display: none;
  }
</style>
