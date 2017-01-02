(ns goodberrys-flavor-of-the-day.intent
  (:require [amazonica.aws.dynamodbv2 :as db]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.predicates :as p]
            [environ.core :refer [env]]))


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


(defn get-output [{:keys [slots]}]
  (let [date (get-in slots [:Date :value])
        yyyy-MM-dd (if date
                     date
                     (-> (f/formatter "yyyy-MM-dd")
                         (f/with-zone (t/time-zone-for-id "America/New_York"))
                         (f/unparse (t/now))))]
    {:day (get-day-of-week yyyy-MM-dd)
     :flavor-of-the-day (get-flavor-of-the-day yyyy-MM-dd)}))


(defn intent-request [{:keys [intent]}]
  (let [{:keys [day flavor-of-the-day]} (get-output intent)]
    {:title "Flavor of the Day"
     :output (str day " Flavor of the Day is " flavor-of-the-day ". Yum.")
     :reprompt-text ""
     :should-end-session true}))
