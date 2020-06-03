<template>
  <div id="chat"
       ref="chat"
       v-bind:class="{
         'chat-wide': this.isWide,
         'chat-narrow': this.isNarrow
       }">
    <chat-header/>
    <chat-content/>
    <chat-input/>
  </div>
</template>

<script>
  const ChatHeader = () => import(
    /* webpackPrefetch: true */
    /* webpackChunkName: "chatHeader" */
    './ChatHeader.vue');
  const ChatContent = () => import(
    /* webpackPrefetch: true */
    /* webpackChunkName: "chatContent" */
    './ChatContent.vue');
  const ChatInput = () => import(
    /* webpackPrefetch: true */
    /* webpackChunkName: "chatInput" */
    './ChatInput.vue');

  export default {
    name: 'chat-view',
    components: {
      ChatHeader,
      ChatContent,
      ChatInput
    },
    data() {
      return {
        width: 0
      }
    },
    computed: {
      isNarrow() {
        return this.width < 680;
      },
      isWide() {
        return !this.isNarrow;
      }
    },
    mounted() {
      this.$nextTick(() => {
        window.addEventListener('resize', this.onResize);
        this.onResize();
      });
    },
    beforeDestroy() {
      window.removeEventListener('resize', this.onResize);
    },
    methods: {
      onResize() {
        this.width = document.getElementById("root").clientWidth;
      }
    }
  }
</script>

<style lang="scss">
  #chat {
    // Take up a maximum of 340px
    &.chat-wide {
      max-width: 340px;
      flex-shrink: 0;
      flex-basis: 340px;
    }

    // Fill the container
    &.chat-narrow {
      max-height: 50%;
      flex-grow: 1;
    }

    // Flex container
    align-content: space-between;
    align-items: stretch;
    display: inline-flex;
    flex-direction: column;
    flex-wrap: nowrap;
    justify-content: space-between;

    background-color: rgb(24, 24, 27);
  }
</style>
