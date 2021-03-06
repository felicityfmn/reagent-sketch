(ns rs.views
  "
   This namespace has functions
   that make view components for the UI
  "
  (:require
    [oops.core :refer [oget]]
    [garden.core :as gc :refer [css]]
    [garden.color :as color :refer [hsl rgb rgba hex->rgb as-hex]]
    [garden.units :as u :refer [px pt em ms percent defunit]]
    [garden.compression :refer [compress-stylesheet]]
    [rs.css :as rcss :refer [fr strs]]
    [rs.actions :as actions]
    [clojure.string :as string]))

(defn css-view
  "
    Returns a CSS component
    from the given list of rules
    and optional flags
  "
  ([rules]
    (css-view {} rules))
  ([flags rules]
    [:style {:type "text/css" :scoped true}
     (css flags (map vec (partition 2 rules)))]))

(defn css-root-view
  "
    Returns static CSS for the whole page
  "
  ([{main :main}]
    [css-view {:vendors ["webkit" "moz"] :auto-prefix #{:column-width :user-select :appearance}}
     [
      "body" {
              :margin      0
              :padding     0
              :background  (rgb 50 50 50)
              :font-family ["Gill Sans" "Helvetica" "Verdana" "Sans Serif"]
              :font-size   (em 1)
              :font-weight :normal
              :cursor      :default
              }
      ".main" main
      ".button"
      {
       :cursor :pointer
       }
      ]]))

(defn input-text-view
  "
    Returns a textarea component
    that changes the :text key of the state
  "
  [{v :value title :title path :path}]
  [:input.input.text-input
     {
       :type  :textarea
       :title title
       :value v
       :on-change
        (fn [e] (actions/handle-message! {:path path :value (oget e [:target :value])}))
      }])

(defn input-number-view
  "Returns an input view that converts to/from a number"
  [{v :value min :min max :max step :step title :title path :path}]
  [:input.input
   {
    :type  :range
    :title title
    :min   min
    :max   max
    :step  step
    :value v
    :on-change
           (fn [e]
             (actions/handle-message! {:path path :value (js/parseFloat (oget e [:target :value]))}))
    }])

(defn input-unit-view
  "Returns an input view that converts to/from em units"
  [{u :unit v :value min :min max :max step :step title :title path :path}]
  [:input.input
   {
    :type  :range
    :title (or title (str path))
    :min   min
    :max   max
    :step  step
    :value (get v :magnitude)
    :on-change
           (fn [e]
             (actions/handle-message! {:path path :value (u (js/parseFloat (oget e [:target :value])))}))
    }])

(defn css-things-view
  "Some dynamic CSS rules for the table-of-things view"
  ([{colour-index :colour-index} colours]
   [css-view
    [
     ".things"
     {
      :display               :grid
      :grid-area             :content
      :grid-template-columns [[(percent 40) (percent 60)]]
      :grid-column-gap       (em 1)
      :grid-auto-rows        :auto
      :grid-row-gap          (em 2.5)
      :background            (rgb 70 70 70)
      }
     ".thing" {:align-self :start :justify-self :stretch}
     ".input" {:align-self :start}
     ".text-input"
     {
      :font-size  (em 1)
      :color      (rgb 255 252 250)
      :background (rgb 90 90 90)
      :padding    (em 1)
      :border     :none
      }
     ".sample" {
                :background   (colours colour-index)
                :width        (px 64)
                :height       (px 64)
                :border-width (px 1)
                :border-color (rgb 200 200 200)
                :border-style :solid
                }
     ".number" {
                :font-size (em 4)
                }
     ".a-circle"
     {
      :fill   (rgb 20 255 100)
      :stroke :none
      }
     ".circles"
     {
      :background :black
      }
     ".list" {
               :display :flex
               :flex-flow [:row :wrap]
             }
     ".little-layouts"
     {
      :padding (em 1)
      :display               :grid
      :grid-template-columns [(repeat 4 (fr 1))]
      :grid-template-rows    [(repeat 4 (fr 1))]
      :grid-column-gap       (em 2)
      :grid-row-gap          (em 2)
      :background            (rgb 50 50 50)
      }
     ".little-layout-content" {:background (rgb 20 20 20) :justify-self :center :grid-area :content :font-size (em 1) :color (rgb 255 250 240)}
     ".l" {:background (rgb 255 0 0)    :grid-area :l}
     ".r" {:background (rgb 255 250 0)  :grid-area :r}
     ".t" {:background (rgb 20 140 255) :grid-area :t}
     ".b" {:background (rgb 30 255 100) :grid-area :b}
     ".tl" {:background (rgb 250 30 200) :grid-area :tl}
     ".tr" {:background (rgb 250 30 200) :grid-area :tr}
     ".bl" {:background (rgb 250 30 200) :grid-area :bl}
     ".br" {:background (rgb 250 30 200) :grid-area :br}
     ]]))

(defn css-grid-view
  "A tiny dynamic CSS rule just for the little table"
  [rule]
  [css-view
    [
      ".my-columns" rule
    ]])

(defn listy-view
  "Returns a view to display a list of things"
  [a-list]
  (into [:div.list]
    (map
      (fn [v]
        [:div
         (cond
           (:magnitude v) (str "(" (name (:unit v)) " " (:magnitude v) ")")
           (:hue v)       (str "(hsl " (string/join " " [(:hue v) (:saturation v) (:lightness v)]) ")")
           (seqable? v)   [listy-view v]
           :otherwise     (str v))])
      a-list)))

(defn table-view
  "Returns a view to display a table
   of the given map's key-value pairs"
  [a-map]
  (into [:div.my-columns]
    (mapcat
      (fn [[k v]]
       [[:div (str k)]
        [:div
          (cond
            (:magnitude v) (str "(" (name (:unit v))  " " (:magnitude v) ")")
            (:hue v)       (str "(hsl " (string/join " " [(:hue v) (:saturation v) (:lightness v)]) ")")
            (seqable? v)   [listy-view v]
            :otherwise     (str v))]
        ])
      a-map)))

(defn circles-view
  "Returns a view of some circles"
  [circles]
  [:svg.thing.circles {:viewBox "-1 -1 2 2" :height 64 :width "100%"}
    (into [:g]
      (map
        (fn [i]
          [:circle.a-circle {:cx i :cy 0 :r (/ 0.9 circles)}])
        (range -1 1 (/ 2 circles))))])

(defn little-layouts-view
  "Returns a view of lots of variants of the given layout rule"
  ([little-layout-rule i n]
    (into
      [:div.thing.little-layouts
        [css-view (into [".little-layout" little-layout-rule]
          (mapcat
            (fn [x]
              [(str ".little-layout-" x)
               {:grid-template-columns
                (update-in (get little-layout-rule :grid-template-columns) [0 1]
                  (fn [{p :magnitude}] (percent (min 100 (+ p (* 0.3 (Math/pow x 2)))))))}])
            (range 0 n)))]]
     (map
       (fn [x]
         [:div {:class (str "little-layout little-layout-" x)}
          [:div.little-layout-content (str (+ i x))]
          [:div.l] [:div.r] [:div.t] [:div.b]
          [:div.tl] [:div.bl] [:div.tr] [:div.br]])
       (range 0 n)))))

(defn root-view
  "
   Returns a view component for
   the root of the whole UI

   We only pass the data each view needs

   Each component has its own CSS
  "
  ([] (root-view @actions/app-state))
  ([{{i :x :as numbers}                                      :numbers
     colours                                                 :colours
     {grid-css :grid little-layout :little-layout :as rules} :css}]
     [:div.root
       [css-root-view rules]
       [:div.main
        [css-things-view numbers colours]
        [:div.button {:title "reinitialize everything!" :on-click (fn [e] (actions/handle-message! {:clicked :reinitialize}))} "🌅"]
        [:div.things
          [:div.input
            [input-unit-view {:unit px :min 4 :max 128 :step 1 :path [:css :grid :border-radius] :value (get-in grid-css [:border-radius])}]
            [input-unit-view {:unit percent :min 5 :max 50 :step 1 :path [:css :grid :grid-template-columns 0 0] :value (get-in grid-css [:grid-template-columns 0 0])}]
            [input-unit-view {:unit em :min 0.3 :max 4 :step 0.1 :path [:css :grid :grid-row-gap] :value (:grid-row-gap grid-css)}]
            [input-number-view {:min 0 :max 255 :step 1 :title "Hue" :path [:css :grid :background :hue] :value (get-in grid-css [:background :hue])}]
            ]
            [:div.thing.grid-demo
              [css-grid-view grid-css]
              [table-view grid-css]]
          [input-unit-view {:unit percent :min 3 :max 20 :step 1 :path [:css :little-layout :grid-template-columns 0 1] :value (get-in rules [:little-layout :grid-template-columns 0 1])}]
          [little-layouts-view little-layout i 16]]
          ]]))