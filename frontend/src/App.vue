<template>
  <div class="container-fluid vh-100 d-flex flex-column">
    <div class="row">
      <h1>
        <img src="/c4viz.svg" style="height: 50px">
        C4Viz: {{ svgTitle }}
      </h1>
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
    <div v-if="serverError === null" class="row flex-grow-1 overflow-hidden">
      <div id="view-list-scrollable" class="col-2 h-100" style="overflow-y: scroll">
        <ViewList :vizArray="vizArray" :current="current" />
      </div>
      <div class="col-10 container-fluid d-flex flex-row mh-100 justify-content-center">
        <CurrentView :current="current" @changeView="navigateSubview" />
      </div>
    </div>
    <div v-else>
      <h1>Error from server</h1>
      {{ serverError }}
    </div>
  </div>
</template>

<script>
import CurrentView from "./components/CurrentView.vue";
import ViewList from "./components/ViewList.vue";
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
    svg
      .find('a')
      .removeAttr('href')
      .removeAttr('xlink:href')
      .removeAttr('title')
      .removeAttr('xlink:title')
    view.svgNoLinks = svg[0].outerHTML
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
      serverError: null,
    };
  },
  mounted: function () {
    const udpateVizData = (vizArray) => {
        // console.log("setting vizArray");
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
    }
    const fetchVizData = (render) => {
      let url = new URL(document.location)
      url.pathname = "/api/c4viz"
      if (render) {
        url.searchParams.append('render', 'true')
      }
      fetch(url)
        .then((res) => res.json())
        .then((result) => {
          if ("pending" in result) {
            if (render) {
              throw new Error("How could result be pending with render == true");
            }
            // Try again - this time with rendering true
            fetchVizData(true);
          } else if ("viz" in result) {
            udpateVizData(result.viz);
          } else {
            if ("message" in result) {
              this.serverError = result.message
            } else {
              this.serverError = "Unexpected message from server"
            }
          }
        })
        .catch(error => {
          console.log("error", error)
        });
    }
    fetchVizData(false)
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
    ViewList,
  },
};
</script>

<style>
</style>
