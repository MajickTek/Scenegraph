package com.sun.scenario.animation;

class ComposerFloat extends Composer<Float> {
   public ComposerFloat() {
      super(1);
   }

   public double[] decompose(Float o, double[] v) {
      v[0] = (double)o;
      return v;
   }

   public Float compose(double[] v) {
      return (float)v[0];
   }
}
