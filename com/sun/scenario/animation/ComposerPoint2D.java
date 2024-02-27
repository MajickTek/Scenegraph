package com.sun.scenario.animation;

import java.awt.geom.Point2D;

class ComposerPoint2D extends Composer<Point2D> {
   public ComposerPoint2D() {
      super(2);
   }

   public double[] decompose(Point2D o, double[] v) {
      v[0] = o.getX();
      v[1] = o.getY();
      return v;
   }

   public Point2D compose(double[] v) {
      return new Point2D.Float((float)v[0], (float)v[1]);
   }
}
