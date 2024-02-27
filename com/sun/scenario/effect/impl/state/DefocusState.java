package com.sun.scenario.effect.impl.state;

import com.sun.scenario.effect.Defocus;

public class DefocusState extends BoxBlurState {
   public DefocusState(Defocus effect) {
      super(effect);
   }

   public float getColOffset() {
      float r = (float)this.getRadius();
      float row = (float)this.getRow();
      return (float)(Math.sqrt((double)(r * r - row * row)) * (double)this.getDx());
   }

   public int getRowCnt() {
      float r = (float)this.getRadius();
      float row = (float)this.getRow();
      return (int)(Math.sqrt((double)(r * r - row * row)) * 2.0);
   }

   public float getAlpha() {
      float r = (float)this.getRadius();
      return (float)(1.0 / (Math.PI * (double)r * (double)r));
   }
}
