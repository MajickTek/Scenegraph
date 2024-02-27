package com.sun.scenario.animation;

import java.awt.Color;

class ComposerColor extends Composer<Color> {
   private float[] comps = new float[4];

   public ComposerColor() {
      super(4);
   }

   public double[] decompose(Color o, double[] v) {
      this.comps = o.getComponents(this.comps);

      for(int i = 0; i < 4; ++i) {
         v[i] = (double)this.comps[i];
      }

      return v;
   }

   public Color compose(double[] v) {
      return new Color((float)v[0], (float)v[1], (float)v[2], (float)v[3]);
   }
}
