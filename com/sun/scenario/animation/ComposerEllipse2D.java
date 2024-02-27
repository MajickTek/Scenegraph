package com.sun.scenario.animation;

import java.awt.geom.Ellipse2D;

class ComposerEllipse2D extends Composer<Ellipse2D> {
   public ComposerEllipse2D() {
      super(4);
   }

   public double[] decompose(Ellipse2D o, double[] v) {
      v[0] = o.getX();
      v[1] = o.getY();
      v[2] = o.getWidth();
      v[3] = o.getHeight();
      return v;
   }

   public Ellipse2D compose(double[] v) {
      return new Ellipse2D.Float((float)v[0], (float)v[1], (float)v[2], (float)v[3]);
   }
}
