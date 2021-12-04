<template>
  <div class="container-fluid min-vh-100 d-flex flex-column">
    <div class="row">
      <h1>C4Viz: SystemLandscape</h1>
      <p>Current page: {{ svgTitle }}</p>
    </div>
    <div class="row flex-grow-1">
      <div class="col-2">View List</div>
      <div class="col-10">
        <C4Viz :current="current" @changeView="changeView"/>
      </div>
    </div>
  </div>
</template>

<script>
import C4Viz from "./components/Views.vue";

export default {
  name: "App",
  data: function () {
    return {
      current: null,
      viz: null,
      renderNumber: 0,
    };
  },
  mounted: function () {
    fetch("/api/c4viz")
      .then((res) => res.json())
      .then((viz) => {
        console.log("setting viz");
        this.viz = viz;
        this.current = viz[0];
      });
  },
  methods: {
      changeView: function (newName) {
        this.current = null
        for (let v of this.viz) {
            if (v.name === newName) {
                this.current = v
                break
            }
        }
        console.log("setViz - new viz", this.current == null ? "null" : this.current.name)
      }
  },
  computed: {
    svgTitle: function () {
      if (this.current == null) {
        return "No page yet";
      }
      return this.current.name;
    },
  },
  components: {
    C4Viz,
  },
};
</script>

<style>
</style>
