(ns signal.filters)

(defmulti filter "filtering mechanism")

(defmethod filter identity)