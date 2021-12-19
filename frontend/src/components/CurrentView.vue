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
  data: function () {
    return {
      /* This is similar to svgContentsRawFromCurrent except that svgContents is changed during animation */
      svgContents: null,
    }
  },
  mounted: function () {
        let thisVue = this
        // Patches welcome to replace this with a vanilla js approach,
        // as long as we agree that the 'a' elements
        // don't exist yet when this is called
        jQuery('.current-view').on('click', 'a', function () {
            try {
                let $e = jQuery(this);
                if ($e.attr('href').match(/https:\/\/view\//)) {
                  let newName = $e.attr('href').replace(/https:\/\/view\/(.*)/, "$1")
                  thisVue.$emit('changeView', newName)
                } else {
                  return true;
                }
            } catch(e) {
                console.log("error setting new drawing", e)
            }
            return false
        })
        this.svgContents = this.svgContentsRawFromCurrent
  },
  computed: {
      svgContentsRawFromCurrent: function () {
          if (this.current == null) {
            return Constants.PAGE_LOADING
          }
          
          return this.current.svg
      }
  },
  watch: {
    svgContentsRawFromCurrent: function (newValue, oldValue) {
      // Skip animation when showing first svg
      if (oldValue == Constants.PAGE_LOADING) {
        this.svgContents = newValue
        return
      }
      let thisVue = this
      let anmiationDuration = 'fast'
      jQuery('.current-view').hide(anmiationDuration, function () {
        thisVue.svgContents = newValue
        jQuery('.current-view').show(anmiationDuration)
      })
    }
  }
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
