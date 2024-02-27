package com.sun.scenario.effect;

public class GaussianBlur extends AbstractGaussian {
   public GaussianBlur() {
      this(10.0F, DefaultInput);
   }

   public GaussianBlur(float radius) {
      this(radius, DefaultInput);
   }

   public GaussianBlur(float radius, Effect input) {
      super("GaussianBlur", radius, input);
   }
}
