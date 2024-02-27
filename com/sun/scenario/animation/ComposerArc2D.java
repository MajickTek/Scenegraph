package com.sun.scenario.animation;

import java.awt.geom.Arc2D;

class ComposerArc2D extends Composer<Arc2D> {
   public ComposerArc2D() {
      super(6);
   }

   public double[] decompose(Arc2D o, double[] v) {
      v[0] = o.getX();
      v[1] = o.getY();
      v[2] = o.getWidth();
      v[3] = o.getHeight();
      v[4] = o.getAngleStart();
      v[5] = o.getAngleExtent();
      return v;
   }

   public Arc2D compose(double[] v) {
      return new Arc2D.Float((float)v[0], (float)v[1], (float)v[2], (float)v[3], (float)v[4], (float)v[5], 0);
   }
}
