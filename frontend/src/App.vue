<template>
    <div class="container-fluid min-vh-100 d-flex flex-column">
        <div class="row">
            <h1>C4Viz: SystemLandscape</h1>
        </div>
        <div class="row flex-grow-1">
            <div class="col-2">
                View List
            </div>
            <div class="col-10">
                <C4Viz/>
            </div>
        </div>
    </div>
</template>

<script>
import C4Viz from './components/Views.vue'

export default {
  name: 'App',
  components: {
    C4Viz
  },
  mounted: function () {
    console.log("App mounted");
  }
}
</script>

<style>
</style>
