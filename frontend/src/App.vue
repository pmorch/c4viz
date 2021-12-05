<template>
  <div class="container-fluid vh-100 d-flex flex-column">
    <div class="row">
      <h1>C4Viz: {{ svgTitle }}</h1>
      <p>Current page: {{ svgTitle }}</p>
    </div>
    <div class="row flex-grow-1 overflow-hidden">
      <div class="col-2">View List</div>
      <div class="col-10 container-fluid d-flex flex-row mh-100 justify-content-center">
        <CurrentView :current="current" @changeView="changeView" />
      </div>
    </div>
  </div>
</template>

<script>
import CurrentView from "./components/CurrentView.vue";
import * as Constants from './Constants.js'

function modifyVizSvgs(vizArray) {
  // Unfortunately, the output from PlantUML is not ideal for our display requirements,
  // so modify the SVG files that we got from the server before using them.

  // var start = new Date().getTime();

  for (let view of vizArray) {
    let hidden = jQuery('<div style="display: none"></div>');
    let svg = hidden.append(view.svg).find("> svg");
    svg
      .removeAttr("preserveAspectRatio")
      .removeAttr("width")
      .removeAttr("height")
      .css("width", "")
      .css("height", "");
    view.svg = svg[0].outerHTML
  }

  // var end = new Date().getTime();
  // var time = end - start;
  // console.log("Execution time: " + time);
}

function setTitle(title) {
  console.log("title set", title);
  if (title) {
    document.title = "C4Viz: " + title;
  } else {
    document.title = "C4Viz"
  }
}

export default {
  name: "App",
  data: function () {
    return {
      current: null,
      vizArray: null,
      renderNumber: 0,
    };
  },
  mounted: function () {
    fetch("/api/c4viz")
      .then((res) => res.json())
      .then((vizArray) => {
        console.log("setting vizArray");
        modifyVizSvgs(vizArray)
        this.vizArray = vizArray;
        this.current = vizArray[0];
      });
  },
  methods: {
    changeView: function (newName) {
      this.current = null;
      for (let v of this.vizArray) {
        if (v.shortName === newName) {
          this.current = v;
          break;
        }
      }
      console.log(
        "setViz - new vizArray",
        this.current == null ? "null" : this.current.name
      );
    },
  },
  computed: {
    svgTitle: function () {
      if (this.current == null) {
        return Constants.PAGE_LOADING
      }
      return this.current.displayTitle
    },
  },
  watch: {
    svgTitle: function (value) {
        setTitle(value);
    }
  },
  components: {
    CurrentView,
  },
};
</script>

<style>
</style>
