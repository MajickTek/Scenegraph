package com.sun.scenario.animation;

class ComposerByte extends Composer<Byte> {
   public ComposerByte() {
      super(1);
   }

   public double[] decompose(Byte o, double[] v) {
      v[0] = (double)o;
      return v;
   }

   public Byte compose(double[] v) {
      return (byte)((int)v[0]);
   }
}
