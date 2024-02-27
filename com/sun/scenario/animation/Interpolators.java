package com.sun.scenario.animation;

public class Interpolators {
   private static final Interpolator DISCRETE_INSTANCE = new Discrete();
   private static final Interpolator LINEAR_INSTANCE = new Linear();
   private static final Interpolator EASING_INSTANCE = new Easing(0.2F, 0.2F);

   private Interpolators() {
   }

   public static Interpolator getDiscreteInstance() {
      return DISCRETE_INSTANCE;
   }

   public static Interpolator getLinearInstance() {
      return LINEAR_INSTANCE;
   }

   public static Interpolator getEasingInstance() {
      return EASING_INSTANCE;
   }

   public static Interpolator getEasingInstance(float acceleration, float deceleration) {
      return new Easing(acceleration, deceleration);
   }

   public static Interpolator getSplineInstance(float x1, float y1, float x2, float y2) {
      return new SplineInterpolator(x1, y1, x2, y2);
   }

   private static class Easing implements Interpolator {
      private float acceleration;
      private float deceleration;

      Easing(float acceleration, float deceleration) {
         if (!(acceleration < 0.0F) && !(acceleration > 1.0F)) {
            if (acceleration > 1.0F - deceleration) {
               throw new IllegalArgumentException("Acceleration value cannot be greater than (1 - deceleration)");
            } else if (!(deceleration < 0.0F) && !(deceleration > 1.0F)) {
               if (deceleration > 1.0F - acceleration) {
                  throw new IllegalArgumentException("Deceleration value cannot be greater than (1 - acceleration)");
               } else {
                  this.acceleration = acceleration;
                  this.deceleration = deceleration;
               }
            } else {
               throw new IllegalArgumentException("Deceleration value cannot lie outside [0,1] range");
            }
         } else {
            throw new IllegalArgumentException("Acceleration value cannot lie outside [0,1] range");
         }
      }

      public float interpolate(float fraction) {
         if (this.acceleration != 0.0F || this.deceleration != 0.0F) {
            float runRate = 1.0F / (1.0F - this.acceleration / 2.0F - this.deceleration / 2.0F);
            float tdec;
            if (fraction < this.acceleration) {
               tdec = runRate * (fraction / this.acceleration) / 2.0F;
               fraction *= tdec;
            } else if (fraction > 1.0F - this.deceleration) {
               tdec = fraction - (1.0F - this.deceleration);
               float pdec = tdec / this.deceleration;
               fraction = runRate * (1.0F - this.acceleration / 2.0F - this.deceleration + tdec * (2.0F - pdec) / 2.0F);
            } else {
               fraction = runRate * (fraction - this.acceleration / 2.0F);
            }

            if (fraction < 0.0F) {
               fraction = 0.0F;
            } else if (fraction > 1.0F) {
               fraction = 1.0F;
            }
         }

         return fraction;
      }
   }

   private static class Linear implements Interpolator {
      private Linear() {
      }

      public float interpolate(float t) {
         return t;
      }
   }

   private static class Discrete implements Interpolator {
      private Discrete() {
      }

      public float interpolate(float t) {
         return t < 1.0F ? 0.0F : 1.0F;
      }
   }
}
