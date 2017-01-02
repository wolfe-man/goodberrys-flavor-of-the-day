(ns goodberrys-flavor-of-the-day.core
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [goodberrys-flavor-of-the-day.launch :refer [launch-request]]
            [goodberrys-flavor-of-the-day.intent :refer [intent-request]]))


(defn build-speechlet [{:keys [title output reprompt-text
                               should-end-session]}]
  {"version" "1.0"
   "sessionAttributes" {}
   "response" {"outputSpeech" {"type" "PlainText"
                               "text" output}
               "card" {"type" "Simple"
                       "title" (str "SessionSpeechlet - " title)
                       "content" (str "SessionSpeechlet - " output)}
               "reprompt" {"outputSpeech" {"type" "PlainText"
                                           "text" reprompt-text}}
               "shouldEndSession" should-end-session}})


(defn -handleRequest
  [this input output context]
  (with-open [w (io/writer output)]
    (let [req (json/read (io/reader input) :key-fn keyword)
          event-object (:request req)
          file-type (:type event-object)
          resp (case file-type
                 "LaunchRequest" (launch-request)
                 "IntentRequest" (intent-request event-object))]
      (-> resp
          build-speechlet
          (json/write w)))
    (.flush w)))
