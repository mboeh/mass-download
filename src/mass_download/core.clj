(ns mass-download.core)

(use '[clojure.string :only (join split)])

(def log println)

(defn http-download
  ([file-acceptor url]
    (log "downloading" url)
    (file-acceptor url (slurp url))))

(defn store-file
  ([filename data]
    (log "saving" filename)
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
      (dorun (map (partial fetch-fn write-out) (line-seq rdr))))))

(defn -main []  
  (mass-download "urls.txt"
                 http-download
                 (comp (in-dir "downloaded") only-basename)
                 store-file))
