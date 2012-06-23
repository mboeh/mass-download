(ns mass-download.core
  (:use    [clojure.string :only [join split blank?]])
  (:import [java.util.concurrent LinkedBlockingQueue Executors]))

(defn http-download
  ([file-acceptor url]
    (file-acceptor url (slurp url))))

(defn package-with-worker [action]
  (let [
    queue (LinkedBlockingQueue.)
    worker (fn []
      (loop [entry (. queue take)]
        (when-not (blank? (first entry))
          (apply action entry)
          (recur (. queue take)))))
    worker-thread (Thread. worker)]
    (do
      (. worker-thread start)
      (fn
        ([kw] (. queue put ["" ""]))
        ([filename data] (. queue put [filename data]))))))

(defn store-to-file
  ([filename data]
    (spit filename data)))

(defn only-basename
  ([url]
    (.getName (clojure.java.io/file url))))

(defn in-dir
  ([dir]
    (fn [fname]
      (join "/" [dir fname]))))

(defn mass-download 
  [url-file fetch-fn name-fn output-fn]
  (let [url-file "urls.txt"
        write-out (fn [url data] (output-fn (name-fn url) data))] 
    (with-open [rdr (clojure.java.io/reader url-file)]
      (let [tasks (map 
                    (fn [t] 
                      (fn []
                        ((partial fetch-fn write-out) t)))
                    (line-seq rdr))
            pool  (Executors/newFixedThreadPool 10)]
        (.invokeAll pool tasks)
        (output-fn :done)
        (.shutdown pool)))))

(defn -main []
  (let [packager (package-with-worker store-to-file)]
    (mass-download "urls.txt"
                   http-download
                   (comp (in-dir "downloaded") only-basename)
                   packager)))
