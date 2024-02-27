package com.sun.scenario.animation;

class ComposerLong extends Composer<Long> {
   public ComposerLong() {
      super(1);
   }

   public double[] decompose(Long o, double[] v) {
      v[0] = (double)o;
      return v;
   }

   public Long compose(double[] v) {
      return (long)v[0];
   }
}
