package com.sun.scenario.animation;

class ComposerInteger extends Composer<Integer> {
   public ComposerInteger() {
      super(1);
   }

   public double[] decompose(Integer o, double[] v) {
      v[0] = (double)o;
      return v;
   }

   public Integer compose(double[] v) {
      return (int)v[0];
   }
}
