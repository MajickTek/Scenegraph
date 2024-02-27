package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.state.DefocusState;

public class Defocus extends BoxBlur {
   public Defocus() {
      super(1);
   }

   public Defocus(int radius) {
      super(radius, DefaultInput);
   }

   public Defocus(int radius, Effect input) {
      super(radius, input);
   }

   DefocusState createState() {
      return new DefocusState(this);
   }
}
