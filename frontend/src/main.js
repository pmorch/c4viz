import { createApp } from 'vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import RouteWrapper from './components/RouteWrapper.vue'
import App from './App.vue'

// 2. Define some routes
// Each route should map to a component.
// We'll talk about nested routes later.
const routes = [
    {
        name: "views",
        path: '/:views*',
        component: App,
        props: route => {
            // Why on earth would /a/b/c be an array: ['a','b','c'], but / be a string "" and not []?
            // Fix that here, so App only sees an array.

            // console.log(route);
            let views;
            if (route.params.views instanceof Array) {
                views = route.params.views
            } else {
                views = []
            }
            return { views }
        }
    },
]

// 3. Create the router instance and pass the `routes` option
// You can pass in additional options here, but let's
// keep it simple for now.
const router = createRouter({
    // 4. Provide the history implementation to use. We are using the hash history for simplicity here.
    history: createWebHashHistory(),
    routes, // short for `routes: routes`
})

const app = createApp(RouteWrapper)
app.use(router)
app.mount('#app')
