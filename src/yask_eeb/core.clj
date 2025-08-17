(ns yask-eeb.core
  (:require [scad-clj.scad :as scad] [scad-clj.model :as model]))

(def mx-nib
  (model/difference
    (model/cylinder (/ 5.7 2) 3)
    (model/union
      (model/cube 1.15 4.0 3)
      (model/cube 4.0 1.15 3)
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
      [0 0 -3]
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
        [(* index 16) 0 0]
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

(spit "scads/housing.scad"
  (scad/write-scad housing)
)

(spit "scads/kcap.scad"
  (scad/write-scad 
    (model/union mx-nib cap-profile)
  )
)

(spit "scads/test.scad"
  (scad/write-scad 
    switch-cutter
  )
)

(defn -main [dum] 1)
