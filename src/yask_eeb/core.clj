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

(defn surface-deform 
  [x y] 
  (+ 
    (Math/sin (* x 0.1))
    (Math/cos (* y 0.1))
  )
)

(defn create-surface 
  [width length height]
  (model/polyhedron
    (for ;Create points.
      [z [0 height] y (range 0 length) x (range 0 width)]
      [x y (+ (surface-deform x y) z)]
    )
    (for ;Create top and bottom faces.
      [z [0 1] y (range 0 (- length 1)) x (range 0 (- width 1))]
      (let [idx (+(+ x (* y length)) (* (* width length) z))]
      [idx (+ idx 1) (+ idx (+ length 1)) (+ idx length)])
    )
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

(def section-ifinger (finger-section 2 3))
(def section-lfinger)
(def section-rfinger)
(def section-pfinger)

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
    (model/difference
      (model/translate
        [(* key-spacing 0.5) key-spacing  0]
        (model/cube 
          (+(* key-spacing 2) 8) 
          (+(* key-spacing 3) 8) 
          2
        )
      )
      section-ifinger
    )
  )
)

(defn -main [] 1)
