package com.sun.scenario.animation;

import java.awt.geom.QuadCurve2D;

class ComposerQuadCurve2D extends Composer<QuadCurve2D> {
   public ComposerQuadCurve2D() {
      super(6);
   }

   public double[] decompose(QuadCurve2D o, double[] v) {
      v[0] = o.getX1();
      v[1] = o.getY1();
      v[2] = o.getCtrlX();
      v[3] = o.getCtrlY();
      v[4] = o.getX2();
      v[5] = o.getY2();
      return v;
   }

   public QuadCurve2D compose(double[] v) {
      return new QuadCurve2D.Float((float)v[0], (float)v[1], (float)v[2], (float)v[3], (float)v[4], (float)v[5]);
   }
}
