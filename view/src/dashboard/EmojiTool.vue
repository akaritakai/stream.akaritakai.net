<template>
  <table class="table" width="100%">
    <tbody>
    <tr><td width="50%">
      <div class="form-group">
      <input v-model="selectionFilter" type="text" class="form-control" id="prefix"
           placeholder="Filter/Search - max 10 results">
      <br>
      <b-table class="scroll-x max50wt" small head-variant="light"
           id="emoji-entries" ref="emojiEntries"
           :busy.sync="updating"
           primary-key="name"
           :fields="fields"
           select-mode="single"
           :items="provideEmojiEntries"
           responsive="sm" selectable
           @row-selected="onRowSelected">
        <template #table-busy>
          <div class="text-center text-danger my-2">
            <strong>Please wait...</strong>
          </div>
        </template>
        <template #cell(icon)="data">
          <img class="emoji-icon" :src="data.item.url" height="28" width="28">
        </template>
        <template #cell(name)="data">
          {{ data.value }}
        </template>
        <template #cell(url)="data">
          {{ data.value }}
        </template>
      </b-table>
      </div>
    </td>
    <td width="50%">
      <b-form v-if="!needApiKey" @submit.stop.prevent="alwaysTrue">
        <div class="form-group">
          <label for="emojiName" class="form-label">Name</label>
          <input v-model="emojiName" type="text" class="form-control max30w" id="emojiName" placeholder="The name of emoji" required>
        </div>
        <div class="form-group">
          <label for="emojiUrl" class="form-label">URL</label>
          <input v-model="emojiUrl" type="text" class="form-control max50w" id="emojiUrl" placeholder="The url for the emoji" required>
        </div>
        <div class="form-group">
          <button type="submit" class="btn btn-primary" @click="updateEmoji" v-if="!inProgress">Update</button>
          <button type="submit" class="btn btn-primary" v-if="inProgress" disabled>
            <b-spinner small/> Starting...
          </button>
        </div>
      </b-form>
    </td>
    </tr><tr>
    <td colspan=2>
      <div class="alert alert-success" role="alert" v-if="result.show && result.success">
        {{ result.message }}
      </div>
      <div class="alert alert-danger" role="alert" v-if="result.show && !result.success">
        {{ result.message }}
      </div>
    </td>
    </tr></tbody>
  </table>
</template>

<script>
  import {mapGetters, mapState} from 'vuex';
  import axios from 'axios';

  export default {
    name: 'emoji-tool',
    data () {
      return {
        inProgress: false,
        updating: false,
        emojiName: '',
        emojiUrl: '',
        filter: '',
        selected: [],
        selectionFilter: '',
        result: {
          message: '',
          success: false,
          show: false
        },
        fields: [
          { key: "icon", label: "", class: 'emojiIcon' },
          { key: "name", label: "Name", class: 'emojiName' },
          { key: "url", label: "URL", class: 'emojiUrl' }
        ]
      };
    },
    computed: {
      ...mapState('apiKey', ['apiKey']),
      needApiKey() {
        return this.apiKey == null || this.apiKey.length === 0;
      },
    },
    watch: {
      selectionFilter(value) {
        this.filter = value
        this.$refs.emojiEntries.refresh()
      }
    },
    methods: {
      onRowSelected(items) {
        this.selected = items;
        if (items.length == 1) {
          let entry = items[0];
          [ this.emojiName, this.emojiUrl ] = [ entry.name, entry.url ]
        }
      },
      provideEmojiEntries(ctx) {
        return axios.post('/chat/emojis', {
          key: this.apiKey,
          filter: this.filter
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
      showResult(successValue, messageValue) {
        var result = {
          message: messageValue,
          success: successValue,
          show: true
        }
        this.result = result;
        setTimeout(function() { result.show = false }, 10000)
      },
      updateEmoji() {
        this.inProgress = true;
        axios.post('/chat/emoji', {
          key: this.apiKey,
          emoji: {
            name: this.emojiName,
            url: this.emojiUrl
          }
        }).then(response => {
          this.showResult(true, response.data);
          this.inProgress = false;
          this.$refs.emojiEntries.refresh()
        }).catch(error => {
          if (error.response) {
            this.showResult(false, error.response.data);
          } else {
            this.showResult(false, error.message);
          }
          this.inProgress = false;
        })
      }
    }
  }
</script>

<style lang="scss">
  .max50wt {
    max-width: calc(50vw - 25pt);
  }
  .max50w {
    max-width: calc(50vw - 100pt);
  }
  .max30w {
    max-width: calc(30vw - 100pt);
  }
  .emojiIcon {
    width: 32px;
  }
  img.emoji-icon {
    vertical-align: middle;
  }
  .emojiName {
    text-align: left;
    width: 15em;
  }
  .emojiUrl {
    text-align: left;
  }
</style>
