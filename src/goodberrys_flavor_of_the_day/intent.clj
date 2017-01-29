(ns goodberrys-flavor-of-the-day.intent
  (:require [amazonica.aws.dynamodbv2 :as db]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [clj-time.predicates :as p]
            [environ.core :refer [env]]
            [goodberrys-flavor-of-the-day.util :refer [help-request]]))


(def cred {:access-key (:dynamodb-access-key env)
           :secret-key (:dynamodb-secret-key env)
           :endpoint (:dynamodb-endpoint env)})


(defn get-day-of-week [yyyy-MM-dd]
  (let [date-time (-> (f/formatter "yyyy-MM-dd")
                      (f/with-zone (t/time-zone-for-id "America/New_York"))
                      (f/parse yyyy-MM-dd))]
    (cond
      (p/sunday? date-time) "Sunday's"
      (p/monday? date-time) "Monday's"
      (p/tuesday? date-time) "Tuesday's"
      (p/wednesday? date-time) "Wednesday's"
      (p/thursday? date-time) "Thursday's"
      (p/friday? date-time) "Friday's"
      (p/saturday? date-time) "Saturday's")))


(defn get-flavor-of-the-day [yyyy-MM-dd]
  (get-in (db/get-item cred
                       :table-name "goodberrys-flavor-of-the-day"
                       :key {:date {:s yyyy-MM-dd}})
          [:item :flavor]))


(defn get-output [date]
  (let [yyyy-MM-dd (if date
                     date
                     (-> (f/formatter "yyyy-MM-dd")
                         (f/with-zone (t/time-zone-for-id "America/New_York"))
                         (f/unparse (t/now))))]
    {:day (get-day-of-week yyyy-MM-dd)
     :flavor-of-the-day (get-flavor-of-the-day yyyy-MM-dd)}))


(defn parse-number
  "Reads a number from a string. Returns nil if not a number."
  [s]
  (if (re-find #"^-?\d+\.?\d*$" s)
    (read-string s)))


(defn invalid-date? [yyyy-MM-dd]
  (and yyyy-MM-dd
       (or (= "" yyyy-MM-dd)
           (let [[year month day] (-> yyyy-MM-dd
                                      (clojure.string/split  #"-")
                                      ((partial map parse-number)))]
             (or (not= year (t/year (l/local-now)))
                 (not= month (t/month (l/local-now)))
                 (not (pos? day))
                 (> day 31))))))


(defn intent-request [{:keys [intent]}]
  (let [date (get-in intent [:slots :Date :value])]
    (if (invalid-date? date)
      (help-request)
      (let [{:keys [day flavor-of-the-day]} (get-output date)]
        {:title "Flavor of the Day"
         :output (str day " Flavor of the Day is " flavor-of-the-day ". Yum.")
         :reprompt-text ""
         :should-end-session true}))))


#_(defn upload-flavors []
    (->> (slurp "/Users/cwolfe/Workbook1.csv")
         ((fn [s] (clojure.string/split s #"\r\n")))
         (map (fn [s] (clojure.string/split s #",")))
         (map (fn [[d f]] (hash-map :date d :flavor f)))
         (map (fn [i]
                (println i)
                (db/put-item cred
                             :table-name "goodberrys-flavor-of-the-day"
                             :return-consumed-capacity "TOTAL"
                             :return-item-collection-metrics "SIZE"
                             :item i)))))
