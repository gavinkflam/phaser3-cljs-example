(ns platformer.main
  (:require [applied-science.js-interop :as j]
            ["phaser" :as phaser]
            [platformer.math :as m]))

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
  [_ star]

  ; Destroy star
  (j/call star :disableBody true true)

  ; Update score
  (swap! score + 10)
  (j/call @score-text :setText (str "Score: " @score))

  ; When all stars were collected
  (when (zero? (j/call @stars :countActive true))
    ; Release a new batch of stars to collect
    (doseq [star (j/get-in @stars [:children :entries])]
      (j/call star :enableBody true (j/get star :x) 0 true true))

    ; Release a bomb
    (let [x (if (< (j/get @player :x) 400)
              (m/rand-int-range 400 800)
              (m/rand-int-range 0   400))]
      (doto (j/call @bombs :create x 16 "bomb")
        (j/call :setBounce 1)
        (j/call :setCollideWorldBounds true)
        (j/call :setVelocity (m/rand-int-range -200 200) 20)
        (j/assoc! :allowGravity false))))

  nil)

(defn hit-bomb
  [scene _ _]

  ; Stop the game
  (j/call-in scene [:physics :pause])

  ; Show player in red
  (j/call @player :setTint 0xff0000)
  (j/call-in @player [:anims :play] "turn")

  ; Game over
  (reset! game-over true)

  nil)

;; Game lifecycle

(defn preload-game
  [scene]

  (j/call-in scene [:load :image] "sky"    "assets/sky.png")
  (j/call-in scene [:load :image] "ground" "assets/platform.png")
  (j/call-in scene [:load :image] "star"   "assets/star.png")
  (j/call-in scene [:load :image] "bomb"   "assets/bomb.png")
  (j/call-in scene [:load :spritesheet]
             "dude" "assets/dude.png"
             #js {:frameWidth 32 :frameHeight 48})

  nil)

(defn create-game
  [scene]

  ; Sky background
  (j/call-in scene [:add :image] 400 300 "sky")

  ; Platforms
  (reset! platforms (j/call-in scene [:physics :add :staticGroup]))

  ; Ground
  (-> @platforms
      (j/call :create 400 568 "ground")
      (j/call :setScale 2)
      (j/call :refreshBody))

  ; Ledges
  (doto @platforms
    (j/call :create 600 400 "ground")
    (j/call :create 50  250 "ground")
    (j/call :create 750 220 "ground"))

  ; Player
  (reset! player (j/call-in scene [:physics :add :sprite] 100 450 "dude"))
  (doto @player
    (j/call :setBounce 0.2)
    (j/call :setCollideWorldBounds true))

  ; Player animations
  (doto (j/get scene :anims)
    (j/call :create #js {:key "left"
                         :frames (j/call-in scene [:anims :generateFrameNumbers]
                                            "dude"
                                            #js {:start 0 :end 3})
                         :frameRate 10
                         :repeat -1})
    (j/call :create #js {:key "right"
                         :frames (j/call-in scene [:anims :generateFrameNumbers]
                                            "dude"
                                            #js {:start 5 :end 8})
                         :frameRate 10
                         :repeat -1})
    (j/call :create #js {:key "turn"
                         :frames #js [#js {:key "dude" :frame 4}]
                         :frameRate 20}))

  ; Input events
  (reset! cursors (j/call-in scene [:input :keyboard :createCursorKeys]))

  ; Spawn 12 Stars that are evenly spaced 70 pixels apart along the x axis
  (reset! stars (j/call-in scene [:physics :add :group]
                           #js {:key "star"
                                :repeat 11
                                :setXY #js {:x 12 :y 0 :stepX 70}}))
  (doseq [star (j/get-in @stars [:children :entries])]
    (j/call star :setBounceY (m/rand-float-range 0.4 0.8)))

  ; Bombs
  (reset! bombs (j/call-in scene [:physics :add :group]))

  ; Scores
  (reset! score-text
          (j/call-in scene [:add :text]
                     16 16 "Score: 0"
                     #js {:fontSize "32px" :fill "#000"}))

  (doto (j/get-in scene [:physics :add])
    ; Colliders
    (j/call :collider @player @platforms)
    (j/call :collider @stars  @platforms)
    (j/call :collider @bombs  @platforms)
    (j/call :overlap  @player @stars collect-star nil)
    (j/call :collider @player @bombs (partial hit-bomb scene) nil))

  nil)

(defn update-game
  [_]

  ; Controlling horizontal movements
  (cond
    (j/get-in @cursors [:left :isDown])
    (doto @player
      (j/call :setVelocityX -160)
      (j/call-in [:anims :play] "left"))

    (j/get-in @cursors [:right :isDown])
    (doto @player
      (j/call :setVelocityX 160)
      (j/call-in [:anims :play] "right"))

    :else
    (doto @player
      (j/call :setVelocityX 0)
      (j/call-in [:anims :play] "turn")))

  ; Controlling jump
  (when (and (j/get-in @cursors [:up :isDown])
             (j/get-in @player  [:body :touching :down]))
    (j/call @player :setVelocityY -330))

  nil)

;; Game definition

(def config
  #js
   {:type    phaser/AUTO
    :width   800
    :height  600
    :physics #js {:default "arcade"
                  :arcade #js {:gravity #js {:y 300}
                               :debug   false}}
    :scene   #js {:preload #(this-as t (preload-game t))
                  :create  #(this-as t (create-game t))
                  :update  #(this-as t (update-game t))}})

;; shadow-cljs lifecycle

(defn reload! []
  (println "Code updated."))

(defn main! []
  (phaser/Game. config)
  (println "Platformer loaded!"))
