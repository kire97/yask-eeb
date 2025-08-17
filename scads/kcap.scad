union () {
  difference () {
    cylinder (h=3, r=2.85, center=true);
    union () {
      cube ([1.15, 4.0, 3], center=true);
      cube ([4.0, 1.15, 3], center=true);
    }
  }
  linear_extrude (height=15, center=true){
    polygon (points=[[-7.5, 0], [-7.5, 1.5], [-6, 2], [6, 2], [7.5, 1.5], [7.5, 0]]);
  }
}
