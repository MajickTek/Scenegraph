package com.sun.scenario.animation;

class ComposerShort extends Composer<Short> {
   public ComposerShort() {
      super(1);
   }

   public double[] decompose(Short o, double[] v) {
      v[0] = (double)o;
      return v;
   }

   public Short compose(double[] v) {
      return (short)((int)v[0]);
   }
}
