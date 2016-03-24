dna-cljs: DNA visualization in Clojurescript
--------------------------------------------

### Run the code ###

    npm install -g bower
    bower install
    lein cljsbuild auto
    lein serve

### Todo ###

Note: This is only the beginning of an experiment.

#### Features ####

- [x] As a user, I can enter bacterial genome, chromosome, segment
- [x] When I hit "fetch", I see data streaming in
- [x] I can hit pause to stop data streaming
- [x] I can start multiple data streams at once
- [ ] I can re-start paused streams
- [ ] I can destroy streaming panels
- [ ] A blank panel does not show up until I start loading data
- [ ] Searching is dynamic (happens whenever I load new data)
- [ ] I can visually select any range of nucleotides

#### Technical features/chores ####

- [ ] Tech debt: Remove jQuery except for plugins
- [ ] Spike: Integrate React+Om
- [ ] SpikeLook into a non-React virtual DOM: https://github.com/Matt-Esch/virtual-dom
- [ ] Spike: Try to use monads and CSS animations for controlled, functional state transformations
- [ ] Performance: use larger buffers for sending over sequences
- [ ] Performance: cache sequences
