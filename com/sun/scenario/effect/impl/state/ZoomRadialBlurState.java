package com.sun.scenario.effect.impl.state;

import com.sun.scenario.effect.ZoomRadialBlur;

public class ZoomRadialBlurState {
   // $FF: renamed from: dx float
   private float field_25 = -1.0F;
   // $FF: renamed from: dy float
   private float field_26 = -1.0F;
   private final ZoomRadialBlur effect;

   public ZoomRadialBlurState(ZoomRadialBlur effect) {
      this.effect = effect;
   }

   public int getRadius() {
      return this.effect.getRadius();
   }

   public void updateDeltas(float dx, float dy) {
      this.field_25 = dx;
      this.field_26 = dy;
   }

   public void invalidateDeltas() {
      this.field_25 = -1.0F;
      this.field_26 = -1.0F;
   }

   public float getDx() {
      return this.field_25;
   }

   public float getDy() {
      return this.field_26;
   }

   public int getNumSteps() {
      int r = this.getRadius();
      return r * 2 + 1;
   }

   public float getAlpha() {
      float r = (float)this.getRadius();
      return 1.0F / (2.0F * r + 1.0F);
   }
}
