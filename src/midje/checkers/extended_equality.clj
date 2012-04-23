(ns ^{:doc "`=` extended for regular expressions, functions, etc."}
  midje.checkers.extended-equality
  (:use [clojure.core.match :only [match]]
        [midje.checkers.extended-falsehood :only [data-laden-falsehood?]]
        [midje.util.form-utils :only [classic-map? extended-fn? pairs record? regex?]]))

(defn extended-= [actual expected]
  (letfn [(evaluate-extended-fn [] 
            (let [function-result (expected actual)]
              (if (data-laden-falsehood? function-result) 
                false 
                function-result)))]
    (try
      (match [actual expected]
        [(a :when data-laden-falsehood?) _]    actual
        [_ (e :when data-laden-falsehood?)]    expected
        [_ (e :when extended-fn?)]                 (evaluate-extended-fn) 
        [(a :when regex?)  (e :when regex?)]       (= (str actual) (str expected))
        [_                 (e :when regex?)]       (re-find expected actual)
        [(a :when record?) (e :when classic-map?)] (= (into {} actual) expected)
        :else                                      (= actual expected))
      (catch Throwable ex false))))

(defn extended-list-=
  "Element-by-element comparison, using extended-= for the right-hand-side values."
  [actual-args checkers]
  (every? (partial apply extended-=) (pairs actual-args checkers)))
