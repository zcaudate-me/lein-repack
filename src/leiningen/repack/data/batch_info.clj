(ns leiningen.repack.data.batch-info)
  
(defrecord BatchInfo []
  Object
  (toString [this]
    this))

(defmethod print-method BatchInfo [v w]
  (.write w (str v)))
