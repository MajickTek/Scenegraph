package com.sun.scenario.animation;

class ComposerBoolean extends Composer<Boolean> {
   public ComposerBoolean() {
      super(1);
   }

   public double[] decompose(Boolean o, double[] v) {
      v[0] = o ? 1.0 : 0.0;
      return v;
   }

   public Boolean compose(double[] v) {
      return v[0] == 1.0;
   }
}
