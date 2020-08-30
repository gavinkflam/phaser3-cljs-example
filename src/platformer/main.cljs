(ns platformer.main
  (:require ["phaser" :as phaser]))

;; Game actors

(defonce player    (atom nil))
(defonce platforms (atom nil))
(defonce cursors   (atom nil))
(defonce stars     (atom nil))
(defonce bombs     (atom nil))

;; Scores

(defonce score      (atom 0))
(defonce score-text (atom nil))

;; Game state

(defonce game-over (atom false))

;; Game events

(defn collect-star
  [player star]

  ; Destroy star
  (.disableBody star true true)

  ; Update score
  (swap! score + 10)
  (.setText @score-text (str "Score: " @score))

  ; When all stars were collected
  (when (zero? (.countActive @stars true))
    ; Release a new batch of stars to collect
    (doseq [star (.. @stars -children -entries)]
      (.enableBody star true (.-x star) 0 true true))

    ; Release a bomb
    (let [x (if (< (.-x player) 400)
              (.. phaser -Math (Between 400 800))
              (.. phaser -Math (Between   0 400)))
          bomb (.create @bombs x 16 "bomb")]
      (doto bomb
        (.setBounce 1)
        (.setCollideWorldBounds true)
        (.setVelocity (.. phaser -Math (Between -200 200))
                      20))
      (set! (.-allowGravity bomb) false)))

  nil)

(defn hit-bomb
  [scene player bomb]

  ; Stop the game
  (.. scene -physics pause)

  ; Show player in red
  (.setTint player 0xff0000)
  (.. player -anims (play "turn"))

  ; Game over
  (reset! game-over true)
  
  nil)

;; Game lifecycle

(defn preload-game
  [scene]

  (doto (.-load scene)
    (.image "sky"    "assets/sky.png")
    (.image "ground" "assets/platform.png")
    (.image "star"   "assets/star.png")
    (.image "bomb"   "assets/bomb.png")
    (.spritesheet "dude" "assets/dude.png"
                  #js {:frameWidth 32 :frameHeight 48}))

  nil)

(defn create-game
  [scene]

  ; Sky background
  (.. scene -add (image 400 300 "sky"))

  ; Platforms
  (reset! platforms (.. scene -physics -add staticGroup))
  (doto @platforms
    ; Ground
    (.. (create 400 568 "ground") (setScale 2) refreshBody)

    ; Ledges
    (.create 600 400 "ground")
    (.create 50  250 "ground")
    (.create 750 220 "ground"))

  ; Player
  (reset! player (.. scene -physics -add (sprite 100 450 "dude")))
  (doto @player
    (.setBounce 0.2)
    (.setCollideWorldBounds true))

  ; Player animations
  (doto (.-anims scene)
    (.create #js {:key "left"
                  :frames (.generateFrameNumbers (.-anims scene) "dude"
                                                 #js {:start 0 :end 3})
                  :frameRate 10
                  :repeat -1})
    (.create #js {:key "right"
                  :frames (.generateFrameNumbers (.-anims scene) "dude"
                                                 #js {:start 5 :end 8})
                  :frameRate 10
                  :repeat -1})
    (.create #js {:key "turn"
                  :frames #js [#js {:key "dude" :frame 4}]
                  :frameRate 20}))

  ; Input events
  (reset! cursors (.. scene -input -keyboard createCursorKeys))

  ; Spawn 12 Stars that are evenly spaced 70 pixels apart along the x axis
  (reset! stars (.. scene -physics -add
                    (group #js {:key "star"
                                :repeat 11
                                :setXY #js {:x 12 :y 0 :stepX 70}})))
  (doseq [star (.. @stars -children -entries)]
    (.setBounceY star
                 (.. phaser -Math (FloatBetween 0.4 0.8))))

  ; Bombs
  (reset! bombs (.. scene -physics -add group))

  ; Scores
  (reset! score-text
          (.. scene -add (text 16 16 "Score: 0"
                               #js {:fontSize "32px" :fill "#000"})))

  (doto (.. scene -physics -add)
    ; Colliders
    (.collider @player @platforms)
    (.collider @stars @platforms)
    (.collider @bombs @platforms)
    (.overlap  @player @stars collect-star nil)
    (.collider @player @bombs (partial hit-bomb scene) nil))

  nil)

(defn update-game
  [scene]

  ; Controlling horizontal movements
  (cond
    (.. @cursors -left -isDown)
    (doto @player
      (.setVelocityX -160)
      (.. -anims (play "left" true)))

    (.. @cursors -right -isDown)
    (doto @player
      (.setVelocityX 160)
      (.. -anims (play "right" true)))

    :else
    (doto @player
      (.setVelocityX 0)
      (.. -anims (play "turn"))))

  ; Controlling jump
  (if (and (.. @cursors -up -isDown)
           (.. @player -body -touching -down))
    (.setVelocityY @player -330))

  nil)

;; Game definition

(def config
  #js
  {:type   phaser/AUTO
   :width  800
   :height 600
   :physics #js {:default "arcade"
                 :arcade #js {:gravity #js {:y 300}
                              :debug false}}
   :scene  #js {:preload #(this-as t (preload-game t))
                :create  #(this-as t (create-game t))
                :update  #(this-as t (update-game t))}})

;; shadow-cljs lifecycle

(defn reload! []
  (println "Code updated."))

(defn main! []
  (phaser/Game. config)
  (println "Platformer loaded!"))
