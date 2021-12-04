<template>
  <div class="foobar">
      <div v-html="svgContents" />
  </div>
</template>

<script>
export default {
  name: 'Foobar',
  props: {
    current: {}
  },
  mounted: function () {
        let thisVue = this
        // Patches welcome to replace this with a vanilla js approach,
        // as long as we agree that the 'a' elements
        // don't exist yet when this is called
        jQuery('.foobar').on('click', 'a', function () {    
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
            return "no page yet"
          }
          
          return this.current.svg
      }
  },
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
