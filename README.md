# Phaser-cljs-example

![Game preview](https://phaser.io/content/tutorials/making-your-first-phaser-3-game/tutorial_header.png)

This is a simple HTML5 game built with [Phaser 3][phaser3],
[ClojureScript][cljs] and [shadow-cljs][shadow-cljs].

The game is derived from [Making your first Phaser 3 game][phaser-tutorial]
tutorial.

## Requirements

1. Node.JS (most recent version preferred)
2. Java SDK 8 or newer (OpenJDK 11+ preferred)
3. [Clojure command line tools][clojure-cli] (most recent version preferred)

## Install dependencies

`npm install`

## Run development mode

1. `clj -A:dev:shadow-cljs watch platformer`
2. Head to http://localhost:8080.

## Run development REPL

1. `clj -A:dev:shadow-cljs clj-repl`
2. Run watch via `(shadow/watch :platformer)`
3. Head to http://localhost:8080.
4. Connect a ClojureScript REPL to the runtime via `(shadow/repl :platformer)`

## Read more

* About shadow-cljs: [User’s Guide][shadow-cljs-users-guide]
* About Phaser 3: [Learn][phaser3-learn], [API Doc][phaser3-api-doc]

## License

BSD3

[phaser3]: https://phaser.io
[cljs]: https://clojurescript.org
[shadow-cljs]: https://shadow-cljs.org/
[phaser-tutorial]: https://phaser.io/tutorials/making-your-first-phaser-3-game/part1
[clojure-cli]: https://clojure.org/guides/getting_started
[shadow-cljs-users-guide]: https://shadow-cljs.github.io/docs/UsersGuide.html
[phaser3-learn]: https://phaser.io/learn
[phaser3-api-doc]: https://photonstorm.github.io/phaser3-docs/index.html
