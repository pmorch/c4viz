# c4viz: C4 Visualization

This is an attempt at creating an open source visualization for
[the C4 model for visualising software architecture](https://c4model.com/)

It expects input in the for of a `workspace.dsl` from the
[Structurizr](https://structurizr.org/) project,  e.g. like this
[big bank](https://github.com/structurizr/dsl/blob/master/examples/big-bank-plc.dsl) example.

# Getting Started

You'll need java 11+ and node 10+ installed.

    ./gradlew build
    SERVER_PORT=9000 java -jar build/libs/c4viz-$VERSION.jar

Now access http://localhost:9000, and you're good to go!

# Structure of this project

There is a separate Spring Boot backend (in `./backend`) and a Vue frontend (in `./frontend/`).

During the build process, the frontend is built and copied into `backend/build/resources/main/static`.

So when run, http://localhost:9090/index.html (or just http://localhost:9090) is
the frontend where everything else is the backend.

# Frontend Development Setup

The process of `./gradlew build` for every frontend change is tedious, so there is another option.

`./frontend/vue.config.js` is configured to proxy the backend calls to http://localhost:9000.

So to develop the frontend, first start the spring backend on port 9000, then start the frontend.
Now accessing http://localhost:8080 will access the Vue development server,
but any backend requests will be proxied to spring boot. So:

Terminal 1 for the backend:

    # Either using task
    task serve-backend

    # Or by hand
    cd backend
    ./gradlew build
    SERVER_PORT=9000 java -jar build/libs/c4viz-$VERSION.jar


Terminal 2 for the frontend:

    # Either using task
    task serve-frontend

    # Or by hand
    cd frontend
    npm run serve -- --port 3000
