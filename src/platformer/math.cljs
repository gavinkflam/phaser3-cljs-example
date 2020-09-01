(ns platformer.math)

(defn rand-int-range
  "Generate a random integer bewteen `from` and `to` inclusively."
  [from to]
  (+ from (rand-int (- to from))))

(defn rand-float-range
  "Generate a random floating point number bewteen `from` (inclusive)
  and `to` (exclusive)."
  [from to]
  (+ from (rand (- to from))))
