package com.sun.scenario.animation;

class ComposerDouble extends Composer<Double> {
   public ComposerDouble() {
      super(1);
   }

   public double[] decompose(Double o, double[] v) {
      v[0] = o;
      return v;
   }

   public Double compose(double[] v) {
      return v[0];
   }
}
