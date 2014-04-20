(ns server.repl)

(defn cljs-browser-repl
  "Fire up a browser-connected ClojureScript REPL"
  []
  (let [repl-env (reset! cemerick.austin.repls/browser-repl-env
                         (cemerick.austin/repl-env))]
    (cemerick.austin.repls/cljs-repl repl-env)))
