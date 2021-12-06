<template>
  <div class="container-fluid vh-100 d-flex flex-column">
    <div class="row">
      <h1>C4Viz: {{ svgTitle }}</h1>
      <!-- Handy for debugging:
        <p>Current page: {{ svgTitle }}</p>
        <p>views: {{ views }}</p>
        <p>breadcrumbs: {{ breadcrumbs }}</p>
      -->
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <template v-for="bc in breadcrumbs" :key="bc.name">
            <li class="breadcrumb-item" v-if="'to' in bc">
              <router-link :to="bc.to">{{ bc.name }}</router-link>
            </li>
            <li v-else class="breadcrumb-item active" aria-current="page">
              {{ bc.name }}
            </li>
          </template>
        </ol>
      </nav>
    </div>
    <div class="row flex-grow-1 overflow-hidden">
      <div class="col-2">View List</div>
      <div class="col-10 container-fluid d-flex flex-row mh-100 justify-content-center">
        <CurrentView :current="current" @changeView="navigateSubview" />
      </div>
    </div>
  </div>
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
  if (title) {
    document.title = "C4Viz: " + title;
  } else {
    document.title = "C4Viz"
  }
}

export default {
  name: "App",
  props: [ 'views' ],
  data: function () {
    return {
      current: null,
      vizArray: null,
      vizMap: {},
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
        for (let viz of vizArray) {
          this.vizMap[viz.shortName] = viz
        }
        // console.log("views", this.views, Object.keys(this.vizMap));
        if (this.views.length > 0) {
          this.updateCurrentFromViews(this.views)
        } else {
          this.$router.replace({ name: 'views', params: { views: [vizArray[0].shortName] } })
        }
      });
  },
  methods: {
    navigateSubview: function (newName) {
      this.$router.push({ name: 'views', params: { views: [...this.views, newName] } })
    },
    updateCurrentFromViews: function (views) {
      if (! (views instanceof Array)) {
        throw new Error("How could value not be an array?")
      }
      if (views.length > 0) {
        this.current = this.vizMap[ views [ views.length - 1 ] ]
      } else {
        this.current = null
      }
      console.log("views watcher", views, this.current)

    }
  },
  computed: {
    svgTitle: function () {
      if (this.current == null) {
        return Constants.PAGE_LOADING
      }
      return this.current.displayTitle
    },
    breadcrumbs: function () {
      let viewsCopy = [ ...this.views ]
      let breadcrumbs = []
      while (viewsCopy.length > 0) {
        let viewName = viewsCopy[ viewsCopy.length - 1 ];
        if (! (viewName in this.vizMap)) {
          return []
        }
        // console.log(viewName, Object.keys(this.vizMap));
        let bc = {
          name: this.vizMap[viewName].displayTitle,
          to: { name: 'views', params: { views: [ ...viewsCopy ] } },
        }
        breadcrumbs.unshift(bc)
        viewsCopy.pop()
      }
      if (breadcrumbs.length > 0) {
        delete breadcrumbs[breadcrumbs.length - 1].to
      }
      return breadcrumbs
    }
  },
  watch: {
    svgTitle: function (value) {
        setTitle(value);
    },
    views: function (value) {
      this.updateCurrentFromViews(value)
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
