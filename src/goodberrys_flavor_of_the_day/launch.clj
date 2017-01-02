(ns goodberrys-flavor-of-the-day.launch)


(defn launch-request []
  {:title "Welcome"
   :output (str "Welcome to Goodberry's Flavor of the Day. "
                "Please ask me for today's flavor of the day by saying, "
                "Alexa, ask good berry's what is the flavor of the day "
                "today or on Friday.")
   :reprompt-text (str "Please ask me for today's flavor of the day by "
                       "saying, Alexa, ask good berry's what is the flavor "
                       "of the day today or on Friday.")
   :should-end-session false})
