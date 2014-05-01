Clojurescript example
---------------------

### Run the code ###

    npm install -g bower
    bower install
    lein cljsbuild auto
    lein serve

### Notes ###

- Leiningen likes to keep track of state with dotfiles.
  You may want to add the following to your `~/.gitignore`.

        .lein-repl-history
        .repl
        target

### Todo ###

#### Features ####

x As a user, I can enter bacterial genome, chromosome, segment
x When I hit "fetch", I see data streaming in
x I can hit pause to stop data streaming
x I can start multiple data streams at once
- I can re-start paused streams
- I can destroy streaming panels
- A blank panel does not show up until I start loading data
- Searching is dynamic (happens whenever I load new data)

#### Chores ####

- Remove jQuery except for plugins
- Integrate Reagent or Om
- Any subset of nucleotides is selectable (wrappable in a div)
- Try to use monads and CSS animations for controlled, functional state transformations
- Performance: use larger buffers for sending over sequences
- Performance: cache sequences
- Look into a non-React virtual DOM: https://github.com/Matt-Esch/virtual-dom
