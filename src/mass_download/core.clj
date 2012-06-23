(ns mass-download.core)

(use '[clojure.string :only (join split)])

(defn http-download
  ([file-acceptor url]
    (file-acceptor url (slurp url))))

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
        fetch-fn  http-download
        output-fn spit
        name-fn   (comp (in-dir "downloaded") only-basename)
        write-out (fn [url data] (output-fn (name-fn url) data))] 
    (with-open [rdr (clojure.java.io/reader url-file)]
      (dorun (map (partial fetch-fn write-out) (line-seq rdr))))))

(defn -main []  
  (mass-download "urls.txt"
                 http-download
                 (comp (in-dir "downloaded") only-basename)
                 spit))
