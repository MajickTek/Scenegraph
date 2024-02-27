package com.sun.scenario.animation;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;

class ComposerDimension2D extends Composer<Dimension2D> {
   public ComposerDimension2D() {
      super(2);
   }

   public double[] decompose(Dimension2D o, double[] v) {
      v[0] = o.getWidth();
      v[1] = o.getHeight();
      return v;
   }

   public Dimension2D compose(double[] v) {
      return new Dimension((int)v[0], (int)v[1]);
   }
}
