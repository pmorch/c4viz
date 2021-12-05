<template>
  <div class="current-view h-100 w-100" v-html="svgContents">
  </div>
</template>

<script>
import * as Constants from '../Constants.js'

export default {
  name: 'CurrentView',
  props: {
    current: {}
  },
  mounted: function () {
        let thisVue = this
        // Patches welcome to replace this with a vanilla js approach,
        // as long as we agree that the 'a' elements
        // don't exist yet when this is called
        jQuery('.current-view').on('click', 'a', function () {
            try {
                let $e = jQuery(this);
                let newName = $e.attr('href').replace(/https:..view.(.*)/, "$1")
                thisVue.$emit('changeView', newName)
            } catch(e) {
                console.log("error setting new drawing", e)
            }
            return false
        })
  },
  computed: {
      svgContents: function () {
          if (this.current == null) {
            return Constants.PAGE_LOADING
          }
          
          return this.current.svg
      }
  },
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style>
  .current-view svg {
      display: block;
      margin: 0 auto;
      max-height: 100%;
      max-width: 100%;
  }
</style>
