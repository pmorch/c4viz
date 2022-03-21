# c4viz: C4 Visualization

This is an open source visualization for [the C4 model for visualising software
architecture](https://c4model.com/). It expects input in the form of a
`workspace.dsl` from the [Structurizr](https://structurizr.org/) project,  e.g.
like this [big
bank](https://structurizr.com/dsl?example=big-bank-plc)
example.

It allows you to *view* `workspace.dsl` files but does not provide the
ability to directly edit the `.dsl` files. However, if you edit and save the
`.dsl` source file and then hit refresh in the browser, it will render the
updated `.dsl` file, so you can use that workflow to edit `.dsl` files.

## Getting Started

### Demo Server

You can see this running on my [demo server](http://c4viz.morch.com:9000). Be
nice to it, it is a tiny cheap server. There are no guarantees that it will
stick around, do don't rely on it being online forever, but host your own
server if you need it.

### Run as docker container

The easiest is to run this from a docker container:

    docker-compose up

or

    docker run \
        --rm -it \
        -v $PWD/sourceDir:/sourceDir \
        -p 9000:9000 \
        -e SERVER_PORT=9000 \
        -e C4VIZ_SOURCE=big-bank-plc.dsl \
        -e C4VIZ_SOURCE_DIR=/sourceDir \
        -u $(id -u):$(id -g) \
        pmorch/c4viz:latest

You'll need a `big-bank-plc.dsl` file in the `./sourceDir` or modify
`C4VIZ_SOURCE` (and perhaps `C4VIZ_SOURCE_DIR`) to point to *your*
`workspace.dsl` file. After all, if you don't have your own `.dsl` file, you
probably don't need this project...

Now access http://localhost:9000, and you're good to go!

If you want rendering of the original image to survive restarts of the image,
mount the cache as a volume with `-v c4cache:/var/cache/c4viz` (or as a a local
directory).

The above docker command mounts a local directory, which is useful for local
development and for editing of `.dsl` files. You can also build your own docker
image directly containing your `.dsl` file(s) so it is self-contained.

The `pmorch/c4viz` is this [docker hub
image](https://hub.docker.com/r/pmorch/c4viz). In the above example, it uses
the `:latest` tag but you might want to pick a fixed version.

### Run from command line

You'll need these installed:

* java 11+
* node 10+
* [task](https://taskfile.dev)

Now:

    task serve-backend

This will build and start the backend with an embedded frontend.

Again, access http://localhost:9000, and you're good to go!

### Using a `?source=$url` Query Parameter

If you specify a `source` query parameter, you can render other `.dsl` documents too.

#### `source` as a file name

If you put multiple `.dsl` files in your `C4VIZ_SOURCE_DIR` folder, you can
render other files in that folder using:
http://localhost:9000?source=otherfile.dsl . For security reasons, the filename
*must* be in that folder and may not contain e.g. the `/` character.

#### `source` as a URL

In addition to the sources as files in the `C4VIZ_SOURCE` directory, you can
also specify URLs like:
http://localhost:9000?source=https://raw.githubusercontent.com/structurizr/dsl/master/examples/deployment-groups.dsl

*NOTE:* This only works for self-contained `.dsl`-s. Some, e.g.
[test.dsl](https://raw.githubusercontent.com/structurizr/dsl/master/src/test/dsl/test.dsl)
expects `logo.png` to exist in the same directory, and so the using that as a
source will yield an error like this: `logo.png does not exist at line 182:
icon logo.png`. Patches to overcome this are welcome, but are not a priority
for me.

### Environment Variables

You can set these environment variables:

| Environment Variable | Default Value                                                             |
| -------------------- | -------------                                                             |
| `SERVER_PORT`        | *null*                                                                    |
| `C4VIZ_SOURCE`       | *null*                                                                    |
| `C4VIZ_SOURCE_DIR`   | Cmdline: *none*, Docker image: `/sourceDir`                               |
| `C4VIZ_CACHE`        | Cmdline: `/tmp`, Task: `/tmp/c4vizCache` Docker image: `/var/cache/c4viz` |

The web server is only started and the `C4VIZ_*` environment varibles only come
into play if `SERVER_PORT` is set.

Without a value for `SERVER_PORT`, both the docker image and the command line
jar file will convert a `.dsl` file into values useful for the frontend as in:

    $ task build-backend
    ...
    $ mkdir output
    $ java -jar backend/build/libs/c4viz-${version}.jar ./sourceDir/big-bank-plc.dsl output
    ...
    $ ls -l output
    -rw-r--r-- 1 peter peter 162880 Dec 19 12:16 c4.viz.json
    -rw-r--r-- 1 peter peter   4172 Dec 19 12:16 Components.puml
    -rw-r--r-- 1 peter peter  25077 Dec 19 12:16 Components.svg
    ...

## Structure of this project

There is a separate Spring Boot backend (in `./backend`) and a Vue frontend (in `./frontend/`).

During the build process, the frontend is built and copied into `backend/build/resources/main/static`.

So when run, http://localhost:9090/index.html (or just http://localhost:9090) is
the frontend where everything else is the backend.

All the magical incantations to do various development tasks are visible in
[`Taskfile.yaml`](Taskfile.yaml), to be used with [task](https://taskfile.dev).

## Frontend Development Setup

The process of `./gradlew build` for every frontend change is tedious, so there is another option.

`./frontend/vue.config.js` is configured to proxy the backend calls to http://localhost:9000.

So to develop the frontend, first start the spring backend on port 9000, then start the frontend.
Now accessing http://localhost:3000 will access the Vue development server,
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
