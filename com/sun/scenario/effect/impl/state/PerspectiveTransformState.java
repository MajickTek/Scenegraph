package com.sun.scenario.effect.impl.state;

public class PerspectiveTransformState {
   private float[][] itx = new float[3][3];

   public float[][] getITX() {
      return this.itx;
   }

   public void updateTx(float[][] tx) {
      float det = get3x3Determinant(tx);
      if ((double)Math.abs(det) < 1.0E-10) {
         this.itx[0][0] = this.itx[1][0] = this.itx[2][0] = 0.0F;
         this.itx[0][1] = this.itx[1][1] = this.itx[2][1] = 0.0F;
         this.itx[0][2] = this.itx[1][2] = -1.0F;
         this.itx[2][2] = 1.0F;
      } else {
         float invdet = 1.0F / det;
         this.itx[0][0] = invdet * (tx[1][1] * tx[2][2] - tx[1][2] * tx[2][1]);
         this.itx[1][0] = invdet * (tx[1][2] * tx[2][0] - tx[1][0] * tx[2][2]);
         this.itx[2][0] = invdet * (tx[1][0] * tx[2][1] - tx[1][1] * tx[2][0]);
         this.itx[0][1] = invdet * (tx[0][2] * tx[2][1] - tx[0][1] * tx[2][2]);
         this.itx[1][1] = invdet * (tx[0][0] * tx[2][2] - tx[0][2] * tx[2][0]);
         this.itx[2][1] = invdet * (tx[0][1] * tx[2][0] - tx[0][0] * tx[2][1]);
         this.itx[0][2] = invdet * (tx[0][1] * tx[1][2] - tx[0][2] * tx[1][1]);
         this.itx[1][2] = invdet * (tx[0][2] * tx[1][0] - tx[0][0] * tx[1][2]);
         this.itx[2][2] = invdet * (tx[0][0] * tx[1][1] - tx[0][1] * tx[1][0]);
      }

   }

   private static float get3x3Determinant(float[][] m) {
      return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
   }
}
