<template>
  <div>
    <p>views {{ views }}</p>
    <p>currentName {{ currentName }}</p>
    <p>about {{ $route.query.id }}</p>
    <p>
      <router-link :to="{ path: 'about', query: { id: [25, 11, 34] } }">
        About 25
      </router-link>
      <router-link :to="{ name: 'views', params: { views } }">
        About Views
      </router-link>
    </p>
  </div>
</template>

<script>
export default {
  props: ["views"],
  computed: {
    currentName: function () {
      return this.views[this.views.length - 1];
    },
  },
  mounted: function () {
    console.log("about mounted", this.$route, this.$router);
  },
};
</script>

