(ns signal.reducers)

(defmulti reducer "aggregation")

(defmethod :identity identity)