(ns moving-charge.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn setup []
  ; Set frame rate to 30 frames per second.
  (q/frame-rate 30)
  ; setup function returns initial state. It contains
  ; circle color and position.
  {:time 0
   :charge-x 0
   :arrows []})

(defn advance-arrow [arrow]
  "Advance an arrows location based on its direction"
  {:vx (:vx arrow)
   :vy (:vy arrow)
   :x (+ (:x arrow) (* 2 (:vx arrow)))
   :y (+ (:y arrow) (* 2 (:vy arrow)))})

(defn update-state [state]
  (let [t (+ (:time state) 0.04)
        x (min (* 40 (Math/sin t)) 0)
        new-arrows (for [i (range 16)]
                     (let [t (* i (/ Math/PI 8))
                           dx (Math/cos t)
                           dy (Math/sin t)]
                       {:x (+ x dx)
                        :y dy
                        :vx dx
                        :vy dy}))
        arrows (map advance-arrow (:arrows state))
        filtered (filter #(and (> 250 (:x %))
                               (> 250 (:y %))
                               (< -250 (:x %))
                               (< -250 (:y %)))
                               arrows)]
  {:time t
   :charge-x x
   :arrows (concat new-arrows filtered)}))

(defn draw-arrows [arrows]
  "Draw field arrows."
  (if (> (count arrows) 0)
    (let [arrow (first arrows)
          x0 (:x arrow)
          y0 (:y arrow)
          x1 (+ x0 (:vx arrow))
          y1 (+ y0 (:vy arrow))]
      (q/line x0 y0 x1 y1)
      (recur (rest arrows)))))

(defn draw-state [state]
  (let [t (:time state)]
    ; Clear the sketch by filling it with light-grey color.
    (q/background 255)
    (q/ellipse-mode :radius)
    (q/with-translation [(/ (q/width) 2) (/ (q/height) 2)]
      (q/stroke 255 0 0)
      (draw-arrows (:arrows state))
      (q/stroke 0 0 0)
      (q/fill 0 0 0)
      (q/ellipse (:charge-x state) 0 5 5)
      )

    ; save frame
    (q/save-frame "moving-charge-####.png")
    ))


(q/defsketch moving-charge
  :title "Moving charge"
  :size [500 500]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  :features [:keep-on-top]
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
