package com.sun.scenario.animation;

class SplineInterpolator implements Interpolator {
   // $FF: renamed from: x1 float
   private final float field_8;
   // $FF: renamed from: y1 float
   private final float field_9;
   // $FF: renamed from: x2 float
   private final float field_10;
   // $FF: renamed from: y2 float
   private final float field_11;
   private final boolean isCurveLinear;
   private static final int SAMPLE_SIZE = 16;
   private static final float SAMPLE_INCREMENT = 0.0625F;
   private final float[] xSamples = new float[17];

   SplineInterpolator(float px1, float py1, float px2, float py2) {
      if (!(px1 < 0.0F) && !(px1 > 1.0F) && !(py1 < 0.0F) && !(py1 > 1.0F) && !(px2 < 0.0F) && !(px2 > 1.0F) && !(py2 < 0.0F) && !(py2 > 1.0F)) {
         this.field_8 = px1;
         this.field_9 = py1;
         this.field_10 = px2;
         this.field_11 = py2;
         this.isCurveLinear = this.field_8 == this.field_9 && this.field_10 == this.field_11;
         if (!this.isCurveLinear) {
            for(int i = 0; i < 17; ++i) {
               this.xSamples[i] = this.eval((float)i * 0.0625F, this.field_8, this.field_10);
            }
         }

      } else {
         throw new IllegalArgumentException("Control point coordinates must all be in range [0,1]");
      }
   }

   public float interpolate(float x) {
      if (!(x < 0.0F) && !(x > 1.0F)) {
         return !this.isCurveLinear && x != 0.0F && x != 1.0F ? this.eval(this.findTForX(x), this.field_9, this.field_11) : x;
      } else {
         throw new IllegalArgumentException("x must be in range [0,1]");
      }
   }

   private float eval(float t, float p1, float p2) {
      float compT = 1.0F - t;
      return t * (3.0F * compT * (compT * p1 + t * p2) + t * t);
   }

   private float evalDerivative(float t, float p1, float p2) {
      float compT = 1.0F - t;
      return 3.0F * (compT * (compT * p1 + 2.0F * t * (p2 - p1)) + t * t * (1.0F - p2));
   }

   private float getInitialGuessForT(float x) {
      for(int i = 1; i < 17; ++i) {
         if (this.xSamples[i] >= x) {
            float xRange = this.xSamples[i] - this.xSamples[i - 1];
            if (xRange == 0.0F) {
               return (float)(i - 1) * 0.0625F;
            }

            return ((float)(i - 1) + (x - this.xSamples[i - 1]) / xRange) * 0.0625F;
         }
      }

      return 1.0F;
   }

   private float findTForX(float x) {
      float t = this.getInitialGuessForT(x);
      int numIterations = true;

      for(int i = 0; i < 4; ++i) {
         float xT = this.eval(t, this.field_8, this.field_10) - x;
         if (xT == 0.0F) {
            break;
         }

         float dXdT = this.evalDerivative(t, this.field_8, this.field_10);
         if (dXdT == 0.0F) {
            break;
         }

         t -= xT / dXdT;
      }

      return t;
   }
}
