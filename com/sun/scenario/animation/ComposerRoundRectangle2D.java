package com.sun.scenario.animation;

import java.awt.geom.RoundRectangle2D;

class ComposerRoundRectangle2D extends Composer<RoundRectangle2D> {
   public ComposerRoundRectangle2D() {
      super(6);
   }

   public double[] decompose(RoundRectangle2D o, double[] v) {
      v[0] = o.getX();
      v[1] = o.getY();
      v[2] = o.getWidth();
      v[3] = o.getHeight();
      v[4] = o.getArcWidth();
      v[5] = o.getArcHeight();
      return v;
   }

   public RoundRectangle2D compose(double[] v) {
      return new RoundRectangle2D.Float((float)v[0], (float)v[1], (float)v[2], (float)v[3], (float)v[4], (float)v[5]);
   }
}
