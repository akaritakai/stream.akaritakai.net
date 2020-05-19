<template>
  <div id="chat-header">
    <div id="chat-title">
      <svg class="icon" viewBox="0 0 20 20" @click="closeChat">
          <g><path d="M4 16V4H2v12h2zM13 15l-1.5-1.5L14 11H6V9h8l-2.5-2.5L13 5l5 5-5 5z"/></g>
      </svg>
      <div class="text">Stream Chat</div>
      <div class="padding"></div>
    </div>
    <div id="chat-settings" v-if="enabled && nick && nick.length > 0">
        <span class="description">Your current nick is: </span>
        <span>{{ nick }}</span>
        <svg class="icon" viewBox="0 0 512 512" @click="editNick">
          <g><path d="M497.9 142.1l-46.1 46.1c-4.7 4.7-12.3 4.7-17 0l-111-111c-4.7-4.7-4.7-12.3 0-17l46.1-46.1c18.7-18.7
                    49.1-18.7 67.9 0l60.1 60.1c18.8 18.7 18.8 49.1 0 67.9zM284.2 99.8L21.6 362.4.4 483.9c-2.9 16.4 11.4
                    30.6 27.8 27.8l121.5-21.3 262.6-262.6c4.7-4.7 4.7-12.3 0-17l-111-111c-4.8-4.7-12.4-4.7-17.1 0zM124.1
                    339.9c-5.5-5.5-5.5-14.3 0-19.8l154-154c5.5-5.5 14.3-5.5 19.8 0s5.5 14.3 0 19.8l-154 154c-5.5
                    5.5-14.3 5.5-19.8 0zM88 424h48v36.3l-64.5 11.3-31.1-31.1L51.7 376H88v48z"/></g>
        </svg>
    </div>
  </div>
</template>

<script>
  import {mapState} from 'vuex';

  export default {
    name: 'chat-header',
    computed: {
      ...mapState('nick', ['nick']),
      ...mapState('chat', ['enabled'])
    },
    methods: {
      editNick() {
        this.$store.dispatch('nick/setNick', '');
      },
      closeChat() {
        this.$store.dispatch('chat/closeChat');
      }
    }
  }
</script>

<style lang="scss">
  #chat-header {
    display: flex;
    flex-direction: column;
    flex-grow: 0;
    font-family: 'Questrial', sans-serif;
  }

  #chat-title {
    border-bottom-color: rgb(39, 39, 40);
    border-bottom-style: solid;
    border-bottom-width: 1px;
    display: flex;
    flex-direction: row;
    height: 40px;
    justify-content: space-between;
    .icon {
      color: rgb(239, 239, 241);
      fill: currentColor;
      height: 20px;
      min-height: 20px;
      min-width: 20px;
      padding: 10px;
      position: static;
      right: auto;
      top: auto;
      width: 20px;
      z-index: auto;
    }
    .text {
      align-self: center;
      color: rgb(222, 222, 227);
      font-size: 12px;
      font-weight: 600;
      line-height: 18px;
      text-transform: uppercase;
    }
    .padding {
      height: 14px;
      min-height: 10px;
      min-width: 10px;
      padding: 10px;
      width: 14px;
    }
  }

  #chat-settings {
    align-content: center;
    align-items: center;
    border-bottom-color: rgb(39, 39, 40);
    border-bottom-style: solid;
    border-bottom-width: 1px;
    color: rgb(222, 222, 227);
    display: flex;
    flex-direction: row;
    font-size: 13px;
    font-weight: 400;
    height: 40px;
    justify-content: center;
    input {
      background-color: rgba(255, 255, 255, 0.15);
      border-color: hsla(0, 0%, 100%, 0.12);
      border-radius: 4px;
      color: rgb(239, 239, 241);
      transition-delay: 0s, 0s, 0s;
      transition-duration: 0.1s, 0.1s, 0.1s;
      transition-property: box-shadow, border, background-color;
      transition-timing-function: ease-in, ease-in, ease-in;
      &:focus {
        background-color: black;
        border-bottom-color: rgb(145, 71, 255);
        border-bottom-style: solid;
        border-bottom-width: 2px;
        border-left-color: rgb(145, 71, 255);
        border-left-style: solid;
        border-left-width: 2px;
        border-right-color: rgb(145, 71, 255);
        border-right-style: solid;
        border-right-width: 2px;
        border-top-color: rgb(145, 71, 255);
        border-top-style: solid;
        border-top-width: 2px;
        color: rgb(239, 239, 241);
      }
    }
    .description {
      font-weight: 600;
      padding-right: 5px;
    }
    .icon {
      color: rgb(239, 239, 241);
      fill: currentColor;
      height: 14px;
      padding-left: 5px;
      width: 14px;
    }
  }

  @font-face {
    font-display: swap;
    font-family: 'Questrial';
    font-style: normal;
    font-weight: 400;
    src: local('Questrial'),
    local('Questrial-Regular'),
    url("../../assets/fonts/questrial-regular.woff2") format('woff2');
  }
</style>
