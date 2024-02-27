package com.sun.scenario.animation;

class Path {
   private final int valsPerPoint;
   private final float flatness;
   private double[] coords;
   private int numPoints;
   private boolean normalized;
   private static final int INIT_SIZE = 4;
   private static final int EXPAND_MAX = 100;
   private double[] src;

   protected Path(int valsPerPoint) {
      this(valsPerPoint, 0.5F);
   }

   protected Path(int valsPerPoint, float flatness) {
      this.valsPerPoint = valsPerPoint + 1;
      this.coords = new double[valsPerPoint * 4];
      this.flatness = flatness;
   }

   void moveTo(double... pt) {
      if (this.numPoints > 0) {
         throw new IllegalStateException("Only one moveTo() allowed per path");
      } else if (pt.length < this.valsPerPoint - 1) {
         throw new IllegalArgumentException("Not enough elements in parameter list");
      } else {
         this.maybeExpand();
         int n = this.numPoints * this.valsPerPoint;

         for(int i = 0; i < this.valsPerPoint - 1; ++i) {
            this.coords[n + i] = pt[i];
         }

         ++this.numPoints;
      }
   }

   void linearTo(double... pt) {
      if (this.numPoints == 0) {
         throw new IllegalStateException("Missing initial moveTo()");
      } else if (pt.length < this.valsPerPoint - 1) {
         throw new IllegalArgumentException("Not enough elements in parameter list");
      } else {
         this.maybeExpand();
         int n = this.numPoints * this.valsPerPoint;

         for(int i = 0; i < this.valsPerPoint - 1; ++i) {
            this.coords[n + i] = pt[i];
         }

         ++this.numPoints;
      }
   }

   void cubicTo(double... pts) {
      if (this.numPoints == 0) {
         throw new IllegalStateException("Missing initial moveTo()");
      } else if (pts.length < (this.valsPerPoint - 1) * 3) {
         throw new IllegalArgumentException("Not enough elements in parameter list");
      } else {
         int prevIndex;
         if (this.src == null) {
            prevIndex = (this.valsPerPoint - 1) * 4;
            this.src = new double[prevIndex];
         }

         prevIndex = (this.numPoints - 1) * this.valsPerPoint;
         int n = 0;

         int i;
         for(i = 0; i < this.valsPerPoint - 1; ++n) {
            this.src[n] = this.coords[prevIndex + i];
            ++i;
         }

         for(i = 0; i < (this.valsPerPoint - 1) * 3; ++n) {
            this.src[n] = pts[i];
            ++i;
         }

         this.flattenCurve(this.src);
      }
   }

   private void flattenCurve(double[] c) {
      int offset;
      if (this.isFlat(c, this.flatness)) {
         offset = (this.valsPerPoint - 1) * 3;

         for(int i = 0; i < this.valsPerPoint - 1; ++i) {
            c[i] = c[offset + i];
         }

         this.linearTo(c);
      } else {
         offset = (this.valsPerPoint - 1) * 4;
         double[] l = new double[offset];
         double[] r = new double[offset];
         this.subdivide(c, l, r);
         this.flattenCurve(l);
         this.flattenCurve(r);
      }

   }

   private void subdivide(double[] c, double[] left, double[] right) {
      int n = this.valsPerPoint - 1;

      for(int i = 0; i < n; ++i) {
         double x1 = c[n * 0 + i];
         double ctrlx1 = c[n * 1 + i];
         double ctrlx2 = c[n * 2 + i];
         double x2 = c[n * 3 + i];
         left[n * 0 + i] = x1;
         right[n * 3 + i] = x2;
         x1 = (x1 + ctrlx1) / 2.0;
         x2 = (x2 + ctrlx2) / 2.0;
         double centerx = (ctrlx1 + ctrlx2) / 2.0;
         ctrlx1 = (x1 + centerx) / 2.0;
         ctrlx2 = (x2 + centerx) / 2.0;
         centerx = (ctrlx1 + ctrlx2) / 2.0;
         left[n * 1 + i] = x1;
         left[n * 2 + i] = ctrlx1;
         left[n * 3 + i] = centerx;
         right[n * 0 + i] = centerx;
         right[n * 1 + i] = ctrlx2;
         right[n * 2 + i] = x2;
      }

   }

   private boolean isFlat(double[] c, float flatness) {
      int n = this.valsPerPoint - 1;
      double sum = 0.0;

      for(int i = 0; i < n; ++i) {
         double u = 3.0 * c[n * 1 + i] - 2.0 * c[n * 0 + i] - c[n * 3 + i];
         double v = 3.0 * c[n * 2 + i] - 2.0 * c[n * 3 + i] - c[n * 0 + i];
         u *= u;
         v *= v;
         if (u < v) {
            u = v;
         }

         sum += u;
      }

      return sum <= (double)flatness;
   }

   double[] getValue(float t, double[] ret) {
      if (!this.normalized) {
         this.normalize();
         this.normalized = true;
      }

      int lastIndex;
      if (t <= 0.0F) {
         for(lastIndex = 0; lastIndex < this.valsPerPoint - 1; ++lastIndex) {
            ret[lastIndex] = this.coords[lastIndex];
         }

         return ret;
      } else {
         int i;
         if (t >= 1.0F) {
            lastIndex = (this.numPoints - 1) * this.valsPerPoint;

            for(i = 0; i < this.valsPerPoint - 1; ++i) {
               ret[i] = this.coords[lastIndex + i];
            }

            return ret;
         } else {
            lastIndex = (this.numPoints - 1) * this.valsPerPoint;

            for(i = 0; i < lastIndex; i += this.valsPerPoint) {
               double segT = this.coords[i + this.valsPerPoint - 1];
               if ((double)t <= segT) {
                  double prevT = i > 0 ? this.coords[i - 1] : 0.0;
                  double relT = ((double)t - prevT) / (segT - prevT);

                  for(int j = 0; j < this.valsPerPoint - 1; ++j) {
                     double v0 = this.coords[i + j];
                     double v1 = this.coords[i + j + this.valsPerPoint];
                     ret[j] = v0 + (v1 - v0) * relT;
                  }

                  return ret;
               }
            }

            throw new InternalError();
         }
      }
   }

   double[] getRotationVector(float t, double[] ret) {
      if (!this.normalized) {
         this.normalize();
         this.normalized = true;
      }

      int lastIndex;
      if (t <= 0.0F) {
         for(lastIndex = 0; lastIndex < this.valsPerPoint - 1; ++lastIndex) {
            ret[lastIndex] = this.coords[lastIndex + this.valsPerPoint] - this.coords[lastIndex];
         }

         return ret;
      } else {
         int i;
         if (t >= 1.0F) {
            lastIndex = (this.numPoints - 1) * this.valsPerPoint;

            for(i = 0; i < this.valsPerPoint - 1; ++i) {
               ret[i] = this.coords[lastIndex + i] - this.coords[lastIndex + i - this.valsPerPoint];
            }

            return ret;
         } else {
            lastIndex = (this.numPoints - 1) * this.valsPerPoint;

            for(i = 0; i < lastIndex; i += this.valsPerPoint) {
               double segT = this.coords[i + this.valsPerPoint - 1];
               if ((double)t <= segT) {
                  double prevT = i > 0 ? this.coords[i - 1] : 0.0;
                  double relT = ((double)t - prevT) / (segT - prevT);

                  for(int j = 0; j < this.valsPerPoint - 1; ++j) {
                     double v0 = this.coords[i + j];
                     double v1 = this.coords[i + j + this.valsPerPoint];
                     ret[j] = v1 - v0;
                  }

                  return ret;
               }
            }

            throw new InternalError();
         }
      }
   }

   private void normalize() {
      int max = (this.numPoints - 1) * this.valsPerPoint;
      double totalLength = 0.0;

      double normLength;
      for(int i = 0; i < max; i += this.valsPerPoint) {
         double sum = 0.0;

         for(int j = 0; j < this.valsPerPoint - 1; ++j) {
            double v0 = this.coords[i + j];
            double v1 = this.coords[i + j + this.valsPerPoint];
            double d = v1 - v0;
            sum += d * d;
         }

         normLength = sum == 0.0 ? 0.0 : Math.sqrt(sum);
         this.coords[i + this.valsPerPoint - 1] = normLength;
         totalLength += normLength;
      }

      if (totalLength == 0.0) {
         this.coords[this.valsPerPoint - 1] = 1.0;
      } else {
         double accumNormLength = 0.0;

         for(int i = this.valsPerPoint - 1; i < max; i += this.valsPerPoint) {
            normLength = this.coords[i] / totalLength;
            accumNormLength += normLength;
            this.coords[i] = accumNormLength;
         }

      }
   }

   private static double[] copyOf(double[] original, int newLength) {
      double[] copy = new double[newLength];
      System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
      return copy;
   }

   private void maybeExpand() {
      int size = this.coords.length;
      if ((this.numPoints + 1) * this.valsPerPoint > size) {
         int grow = size;
         if (size > 100 * this.valsPerPoint) {
            grow = 100 * this.valsPerPoint;
         }

         if (grow < this.valsPerPoint) {
            grow = this.valsPerPoint;
         }

         this.coords = copyOf(this.coords, size + grow);
      }

   }
}
