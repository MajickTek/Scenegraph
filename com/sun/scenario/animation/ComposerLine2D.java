package com.sun.scenario.animation;

import java.awt.geom.Line2D;

class ComposerLine2D extends Composer<Line2D> {
   public ComposerLine2D() {
      super(4);
   }

   public double[] decompose(Line2D o, double[] v) {
      v[0] = o.getX1();
      v[1] = o.getY1();
      v[2] = o.getX2();
      v[3] = o.getY2();
      return v;
   }

   public Line2D compose(double[] v) {
      return new Line2D.Float((float)v[0], (float)v[1], (float)v[2], (float)v[3]);
   }
}
