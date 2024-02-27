package com.sun.scenario.animation;

import java.awt.geom.Rectangle2D;

class ComposerRectangle2D extends Composer<Rectangle2D> {
   public ComposerRectangle2D() {
      super(4);
   }

   public double[] decompose(Rectangle2D o, double[] v) {
      v[0] = o.getX();
      v[1] = o.getY();
      v[2] = o.getWidth();
      v[3] = o.getHeight();
      return v;
   }

   public Rectangle2D compose(double[] v) {
      return new Rectangle2D.Float((float)v[0], (float)v[1], (float)v[2], (float)v[3]);
   }
}
