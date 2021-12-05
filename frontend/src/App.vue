<template>
  <h1>Hello App!</h1>
  <p>
    <!-- use the router-link component for navigation. -->
    <!-- specify the link by passing the `to` prop. -->
    <!-- `<router-link>` will render an `<a>` tag with the correct `href` attribute -->
    <router-link to="/">Go to Home</router-link><br>
    <router-link to="/about">Go to About</router-link>
  </p>
  <!-- route outlet -->
  <!-- component matched by the route will render here -->
  <router-view></router-view>
</template>

<script>
import CurrentView from "./components/CurrentView.vue";
import About from "./components/About.vue";
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
    About
  },
};
</script>

<style>
</style>
