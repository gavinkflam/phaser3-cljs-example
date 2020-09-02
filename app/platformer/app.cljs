(ns platformer.app
  (:require ["expo"]
            ["react"]
            ["expo-status-bar" :as sb]
            ["react-native" :as rn]
            [reagent.core :as r]
            [shadow.expo :as expo]))

(defonce splash-img (js/require "../public/assets/splash.png"))

(def styles
  (rn/StyleSheet.create
   #js
    {:container #js {:flex 1
                     :backgroundColor "#fff"
                     :alignItems "center"
                     :justifyContent "center"}
     :title #js {:fontWeight "bold"
                 :fontSize 24
                 :color "grey"}}))

(defn root
  []
  [:> rn/View {:style (.-container styles)}
   [:> sb/StatusBar {:animated true :hidden true :style "auto"}]
   [:> rn/Text {:style (.-title styles)} "hello world"]
   [:> rn/Image {:source splash-img :style {:width 200 :height 200}}]])

(defn start
  {:dev/after-load true}
  []
  (expo/render-root (r/as-element [root])))

(defn init
  []
  (start))
