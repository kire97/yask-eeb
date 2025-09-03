(ns yask-eeb.core
  (:require [scad-clj.scad :as scad] [scad-clj.model :as model]))

(def key-spacing 19.05)

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

(defn main-displacement 
  [x y z] 
  [x y (+ 
    (* (Math/pow (* (+ x -50) 0.05) 2) 1)
    (* (Math/pow (* (+ y -25) 0.05) 2) 1)
    ; (slope (+ x (* y 0.2)) 5 0.25)
    ; (- 1 (slope x 20 0.25))
    z
  )]
)

(defn thumb-displacement 
  [x y z] 
  [x y (+ 
    (* (Math/pow (* (+ x -50) 0.05) 2) 1)
    (* (Math/pow (* (+ y -25) 0.05) 2) 1)
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
  [displace x-start y-start width length height]
  (model/polyhedron
    (for ;Create points.
      [z [0 height] y (range 0 length) x (range 0 width)]
      (displace (+ x x-start) (+ y y-start) z)
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

(defn get-switch-positions-x-curved
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
  [displace positions]
  (for 
    [[x y z] positions]
    (model/translate
      (displace x y z)
      (model/rotate
        (get-point-rotation displace x y z 0.1)
        switch-cutter
      )
    )
  )    
)

(defn create-outline
  [positions radius]
  (model/extrude-linear
    {:height 50}
    (model/offset
      radius
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

(spit "scads/kcap.scad"
  (scad/write-scad 
    (model/fs! 0.5)
    (model/fa! 2)
    (model/union mx-nib bevel-cap)
  )
)

(def switch-positions (get-switch-positions [0 0 3] [[4 0] [4 0] [4 5] [4 2] [3 0] [2 0]]))

(spit "scads/test.scad"
  (scad/write-scad 
    (model/fs! 0.5)
    (model/fa! 2)
    (model/difference
      (model/intersection
        (create-surface main-displacement -16 -20 150 120 3)
        (create-outline switch-positions 5)
      )
      (create-switches main-displacement switch-positions)
    )
  )
)

(spit "scads/test_housing.scad"
  (scad/write-scad
    (let [surface (create-surface main-displacement -20 -20 150 120 30)]
      (model/difference
        (create-outline switch-positions 10)
        surface
        (model/intersection
          (model/union
            (create-outline switch-positions 3)
            (model/translate
              [0 0 -3]
              surface
            )
          )
          (create-outline switch-positions 5)
        )
      )
    )
  )
)

(spit "scads/test2.scad"
  (scad/write-scad 
    (model/fs! 0.5)
    (model/fa! 2)
    (let [positions (get-switch-positions [0 0 3] [[1 8] [2 0]])]
      (model/difference
        (model/intersection
          (create-surface thumb-displacement -16 -20 150 120 3)
          (create-outline positions 5)
        )
        (create-switches thumb-displacement positions)
      )
    )
  )
)

(defn -main [] 1)
