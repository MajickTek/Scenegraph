package com.sun.scenario.animation;

import java.awt.geom.CubicCurve2D;

class ComposerCubicCurve2D extends Composer<CubicCurve2D> {
   public ComposerCubicCurve2D() {
      super(8);
   }

   public double[] decompose(CubicCurve2D o, double[] v) {
      v[0] = o.getX1();
      v[1] = o.getY1();
      v[2] = o.getCtrlX1();
      v[3] = o.getCtrlY1();
      v[4] = o.getCtrlX2();
      v[5] = o.getCtrlY2();
      v[6] = o.getX2();
      v[7] = o.getY2();
      return v;
   }

   public CubicCurve2D compose(double[] v) {
      return new CubicCurve2D.Float((float)v[0], (float)v[1], (float)v[2], (float)v[3], (float)v[4], (float)v[5], (float)v[6], (float)v[7]);
   }
}
