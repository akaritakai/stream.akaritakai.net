<template>
  <b-table sticky-header
     :items="messages" small head-variant="light" id="chat-log"
      primary-key="sequence" :fields="fields">
    <template #cell(messageType)="data">
      {{ data.value }}
    </template>
    <template #cell(messageTime)="data">
      <div :title="data.item.source">{{ formatTimestamp(data.item.timestamp) }}</div>
    </template>
    <template #cell(name)="data">
      {{ data.item.nickname }}
    </template>
    <template #cell(content)="data">
      {{ data.item.message }}
    </template>
  </b-table>
</template>

<script>
  import {mapGetters, mapState} from 'vuex';
  export default {
    name: 'chat-log',
    data () {
      return {
        messages: this.orderedMessageList(),
        fields: [
          { key: "messageType", label: "Type", class: 'messageType' },
          { key: "messageTime", label: "Time", class: 'messageTimeStamp' },
          { key: "name", label: "Name", class: 'messageSender' },
          { key: "content", label: "Content", class: 'messageContent' }
        ]
      };
    },
    computed: {
      ...mapState('chat', ['epoch', 'position']), // epoch and position comprise a sequence (see 'ChatSequence')
      messageSequence() { // watchable object to trigger changes to the list of messages
        return {
          epoch: this.epoch,
          position: this.position
        }
      }
    },
    watch: {
      messageSequence: {
        // a change to messageSequence implies a change in chat messages
        deep: true,
        handler() {
          this.messages = this.orderedMessageList(); // update the list of messages
        }
      }
    },
    methods: {
      orderedMessageList() {
        return Array.from(this.$store.state.chat.messages.values()).sort((message1, message2) => {
          return message1.sequence.position - message2.sequence.position;
        });
      },
      formatTimestamp(timestamp) {
        if (isNaN(timestamp)) {
          return "--:--";
        }
        const date = new Date(timestamp);
        const hours = date.getHours();
        const minutes = date.getMinutes();
        return hours.toString().padStart(2, '0') + ':' + minutes.toString().padStart(2, '0');
      }
    }
  }
</script>

<style lang="scss">
  .chat-log {
    max-height: calc(80vh);
  }
  .messageType {
    width: 5em;
  }
  .messageTimeStamp {
    width: 5em;
  }
  .messageSender {
    text-align: left;
    width: 10em;
  }
  .messageContent {
    text-align: left;
  }
</style>
