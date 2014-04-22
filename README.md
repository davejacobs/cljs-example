Clojurescript example
=====================

Run the code:

    npm install -g bower
    bower install
    lein cljsbuild auto
    lein serve

Notes:

- Leiningen likes to keep track of state with dotfiles.
  You may want to add the following to your `~/.gitignore`.

        .lein-repl-history
        .repl
        target
