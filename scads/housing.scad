difference () {
  cube ([300, 150, 3], center=true);
  union () {
    translate ([0, 0, 0]) {
      union () {
        translate ([0, 0, -0.5]) {
          cube ([14, 14, 1], center=true);
        }
        translate ([0, 0, -3]) {
          cube ([15, 14, 5], center=true);
        }
        translate ([0, 0, 5]) {
          cube ([15, 15, 10], center=true);
        }
      }
    }
    translate ([16, 0, 0]) {
      union () {
        translate ([0, 0, -0.5]) {
          cube ([14, 14, 1], center=true);
        }
        translate ([0, 0, -3]) {
          cube ([15, 14, 5], center=true);
        }
        translate ([0, 0, 5]) {
          cube ([15, 15, 10], center=true);
        }
      }
    }
    translate ([32, 0, 0]) {
      union () {
        translate ([0, 0, -0.5]) {
          cube ([14, 14, 1], center=true);
        }
        translate ([0, 0, -3]) {
          cube ([15, 14, 5], center=true);
        }
        translate ([0, 0, 5]) {
          cube ([15, 15, 10], center=true);
        }
      }
    }
    translate ([48, 0, 0]) {
      union () {
        translate ([0, 0, -0.5]) {
          cube ([14, 14, 1], center=true);
        }
        translate ([0, 0, -3]) {
          cube ([15, 14, 5], center=true);
        }
        translate ([0, 0, 5]) {
          cube ([15, 15, 10], center=true);
        }
      }
    }
    translate ([64, 0, 0]) {
      union () {
        translate ([0, 0, -0.5]) {
          cube ([14, 14, 1], center=true);
        }
        translate ([0, 0, -3]) {
          cube ([15, 14, 5], center=true);
        }
        translate ([0, 0, 5]) {
          cube ([15, 15, 10], center=true);
        }
      }
    }
    translate ([80, 0, 0]) {
      union () {
        translate ([0, 0, -0.5]) {
          cube ([14, 14, 1], center=true);
        }
        translate ([0, 0, -3]) {
          cube ([15, 14, 5], center=true);
        }
        translate ([0, 0, 5]) {
          cube ([15, 15, 10], center=true);
        }
      }
    }
  }
}
