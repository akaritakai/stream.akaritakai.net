<template>
  <div id="stream-video">
    <video ref="video" class="video-js"/>
  </div>
</template>

<script>
  import {mapState} from 'vuex';
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
    const hls = this.hls = new Hls();

    this.dispose = function() {
      hls.destroy();
    };

    this.duration = function() {
      return duration || el.duration || 0;
    };

    hls.on(Hls.Events.LEVEL_LOADED, function(event, data) {
      duration = data.details.live ? Infinity : data.details.totalduration;
    });

    Object.keys(Hls.Events).forEach(function(key) {
      const eventName = Hls.Events[key];
      hls.on(eventName, function(event, data) {
        tech.trigger(eventName, data);
      });
    });

    hls.attachMedia(el);
    hls.loadSource(source.src);
  }

  // Add the hls.js handler
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
      ])
    },
    data() {
      return {
        player: null
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
        const hls = this.player.tech({ IWillNotUseThisInPlugins: true }).sourceHandler_.hls;
        const currentQuality = hls.levels[hls.currentLevel];
        const quality = function() {
          if (!currentQuality || !currentQuality.width || !currentQuality.height || !currentQuality.bitrate) {
            return null;
          } else {
            const width = Math.round(currentQuality.width);
            const height = Math.round(currentQuality.height);
            const kbps = Math.round(currentQuality.bitrate / 1000);
            return width + "x" + height + "@" + kbps + "k";
          }
        }
        const bandwidth = function() {
          return isNaN(hls.bandwidthEstimate) ? null : Math.round(hls.bandwidthEstimate);
        }
        this.$store.dispatch('stream/updateQualityInfo', {
          quality: quality(),
          bandwidth: bandwidth()
        });
      });
      this.player.on('volumechange', () => {
        this.$store.dispatch('stream/updateMutedInfo', {
          muted: this.player.muted()
        })
      });

      // Recover any errors that might occur
      this.player.on('error', () => {
        const hls = this.player.tech({ IWillNotUseThisInPlugins: true }).sourceHandler_.hls;
        hls.recoverMediaError();
        this.playStream();
      });

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
        this.player.play().then(_ => {
          this.$store.dispatch('stream/updateMutedInfo', {
            muted: this.player.muted()
          });
          this.seekStream();
        }).catch(_ => {
          this.player.muted(true);
          this.$store.dispatch('stream/updateMutedInfo', {
            muted: true
          });
          this.player.play().then(_ => {
            this.seekStream();
          });
        });
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
    background: black;
    height: 100%;
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
</style>
