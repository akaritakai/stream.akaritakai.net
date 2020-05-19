<template>
  <div id="chat">
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
    }
  }
</script>

<style lang="scss">
  #chat {
    align-self: stretch;
    background-color: rgb(24, 24, 27);
    display: flex;
    flex-direction: column;
    flex-grow: 0;
    height: 100%;
    justify-content: space-between;
    width: 340px;
  }
</style>
