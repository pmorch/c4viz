<template>
    <div class="view-list">
        <div v-for="viz in vizArray" :key="viz.shortName"
             :class="{ view: true, current: current == viz }">
            <div v-if="current == viz">
                <p>{{ viz.displayTitle }}</p>
                <div v-html="viz.svgNoLinks"/>
            </div>
            <router-link v-else :to="{ name: 'views', params: { views: [viz.shortName] } }"
             :title="'Go to ' + viz.displayTitle">
                <p>{{ viz.displayTitle }}</p>
                <div v-html="viz.svgNoLinks"/>
            </router-link>
        </div>
    </div>
</template>

<script>
export default {
    props: [ 'vizArray', 'current' ],
    mounted: function () {
        this.scrollCurrentIntoView({ behavior: 'smooth', block: 'center' })
    },
    methods: {
        scrollCurrentIntoView: function () {
            this.$nextTick(function () {
                let scrollable = jQuery('#view-list-scrollable')
                let element = jQuery('.view-list .view.current')
                if (element) {
                    scrollable.scrollTo(element, 700)
                }
            })
        }
    },
    watch: {
        current: function () {
            this.scrollCurrentIntoView()
        }
    }
}
</script>

<style scoped>
.view {
    border: 3px solid #d8d8d8;
    /* Same as padding for #view-list-scrollable (which has rule from Bootstrap's ".row  > *" )*/
    margin: calc(var(--bs-gutter-x) * .5) 0;
    padding: 3px;
    border-radius: 5px;
}

.view:first-child {
    margin-top: 0;
}

.view.current {
    border-color: #888;
}
</style>