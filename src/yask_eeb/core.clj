(ns yask-eeb.core
  (:require [scad-clj.scad :as scad] [scad-clj.model :as model]))

(def key-spacing 18)

(def mx-nib
  (model/translate
    [0 0 -1.5]
    (model/difference
      (model/cylinder (/ 5.7 2) 3)
      (model/union
        (model/cube 1.15 4.0 3)
        (model/cube 4.0 1.15 3)
      )
    )
  )
)

(def top-face
  (model/translate 
    [0 0 2.5] 
    (model/cube 15 15 2)
  )
)

(def cap-profile
  (model/extrude-linear
    {:height 15}
    (model/polygon [[-7.5 0][-7.5 1.5][-6 2][6 2][7.5 1.5][7.5 0]])
  )
)

(def bevel-cap
  (model/difference
    (model/intersection
      (model/rotate
        [(/ Math/PI 2) 0 0] 
        cap-profile
      )
      (model/rotate
        [(/ Math/PI 2) 0 (/ Math/PI 2)] 
        cap-profile
      )
    )
    (model/translate
      [0 0 41.5]
      (model/sphere 40)
    )
  )
)

(def switch-cutter
  (model/union
    (model/translate 
      [0 0 -0.5]
      (model/cube 14 14 1)
    )
    (model/translate
      [0 0 -3.5]
      (model/cube 15 14 5)
    )
    (model/translate
      [0 0 5]
      (model/cube 15 15 10)
    )
  )
)

(def switch-row
  (model/union
    (for 
      [index (range 0 6)] 
      (model/translate
        [(* index key-spacing) 0 0]
        switch-cutter
      ) 
    )
  )
)

(def housing
  (model/difference
    (model/cube 300 150 3)
    switch-row
  )
)

(defn slope 
  [axis offset steepness] 
  (Math/clamp
    (Math/cbrt (* (- axis offset) steepness)) 
    -1.0 
    1.0
  )
)

(defn point-displace 
  [x y z] 
  [x y (+ 
    (* (Math/pow (* (+ x -50) 0.05) 2) -1)
    (Math/pow (* (+ y -25) 0.05) 2)
    ; (slope (+ x (* y 0.2)) 5 0.25)
    ; (- 1 (slope x 20 0.25))
    z
  )]
)

(defn get-point-rotation ;Sample neigbouring points to calculate normal angle.
  [func x y z w]
  (let [[x0 y0 z0] (func x y z) [x1 y1 z1] (func (+ x w) y z) [x2 y2 z2] (func x (+ y w) z)]
    [
      (Math/atan(/ (- z2 z0) w))
      (Math/atan(/ (- z0 z1) w))
      0
    ]
  )
)


(defn face-looper ;Creates quad face loop.
  [idx off-1 off-2] 
  [idx (+ idx off-1) (+ idx (+ off-1 off-2)) (+ idx off-2)]
)

(defn mirror-looper ;Creates mirrored pair of quad loops.
  [idx off-1 off-2 off-3]
  [
    (face-looper idx off-1 off-2)
    (face-looper (+ idx off-3) off-2 off-1)
  ]
)

(defn create-surface 
  [x-start y-start width length height]
  (model/polyhedron
    (for ;Create points.
      [z [0 height] y (range 0 length) x (range 0 width)]
      (point-displace (+ x x-start) (+ y y-start) z)
    )
    (into [] cat (concat ;Feels like there is a better method for this.
      (for ;Create z axis faces.
        [y (range 0 (- length 1)) x (range 0 (- width 1))]
        (mirror-looper (+(+ x (* y width))) 1 width (* length width)) 
      )
      (for ;Create y axis faces.
        [x (range 0 (- width 1))]
        (mirror-looper x (* length width) 1 (- (* width length) width))
      )
      (for ;Create x axis faces.
        [y (range 0 (- length 1))]
        (mirror-looper (* y width) width (* length width) (- width 1))
      )
    ))
  )
)

(defn finger-section 
  [col-cnt row-cnt] 
  (model/union
    (for 
      [col (range 0 col-cnt) row (range 0 row-cnt)]
      (model/translate
        [(* col key-spacing) (* row key-spacing) 0]
        switch-cutter
      )
    )
  )
)

(defn get-switch-positions
  [[x-off y-off z-off] columns]
  (into [] cat (concat
    (for 
      [[i [row-cnt row-off]] (map-indexed vector columns)]
      (for 
        [row (range 0 row-cnt)]
        [(+(* i key-spacing) x-off) (+(+(* row key-spacing) row-off) y-off) z-off]
      )
    )    
  ))
)

(defn create-switches 
  [positions]
  (for 
    [[x y z] positions]
    (model/translate
      (point-displace x y z)
      (model/rotate
        (get-point-rotation point-displace x y z 0.1)
        switch-cutter
      )
    )
  )    
)

(defn create-outline
  [positions]
  (model/extrude-linear
    {:height 20}
    (model/offset
      5
      (model/hull
        (for 
          [[x y z] positions]
          (model/translate [x y] 
            (let [w (- key-spacing 5)]
              (model/square w w)
            )
          )
        )
      )
    )
  )
)

(def section-ifinger (finger-section 2 3))
(def section-lfinger (finger-section 1 4))
(def section-rfinger (finger-section 1 4))
(def section-pfinger (finger-section 2 3))

(spit "scads/housing.scad"
  (scad/write-scad housing)
)

(spit "scads/kcap.scad"
  (scad/write-scad 
    (model/fs! 0.5)
    (model/fa! 2)
    (model/union mx-nib bevel-cap)
  )
)

(spit "scads/test.scad"
  (scad/write-scad 
    (model/fs! 0.5)
    (model/fa! 2)
    (let [positions (get-switch-positions [0 0 3] [[4 0] [4 0] [4 5] [4 2] [3 0] [2 0]])]
      (model/difference
        (model/intersection
          (create-surface -16 -20 120 90 3)
          (create-outline positions)
        )
        (create-switches positions)
      )
    )
  )
)

(defn -main [] 1)
