(ns mass-download.core)
(use '[clojure.string :only (join split)])
(defn urls-from-file
  ([file]
    (fn [action]
      (with-open [rdr (clojure.java.io/reader file)]
        (dorun (map action (line-seq rdr)))
      )
    )
  )
)

(defn mass-download
  "Download many files and pass them to a handler."
  ([url-source url-downloader file-acceptor url-to-path]
    (url-source (fn [url]
      (url-downloader url (fn [url file] 
        (file-acceptor (url-to-path url) file)
      ))
    ))
  )
)

(defn http-download
  ([url file-acceptor]
    (file-acceptor url (slurp url))))

(defn only-basename
  ([url]
    (.getName (clojure.java.io/file url))))

(defn in-dir
  ([dir file-fn]
    (fn [fname]
      (join "/" [dir (file-fn fname)]))))

(defn -main [] 
  (mass-download (urls-from-file "urls.txt")
                 http-download
                 spit
                 (in-dir "downloaded" only-basename)))
