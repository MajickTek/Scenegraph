package com.sun.scenario.effect.impl.state;

import com.sun.scenario.effect.BoxBlur;

public class BoxBlurState {
   // $FF: renamed from: dx float
   private float field_3 = -1.0F;
   // $FF: renamed from: dy float
   private float field_4 = -1.0F;
   private int row = 0;
   private final BoxBlur effect;

   public BoxBlurState(BoxBlur effect) {
      this.effect = effect;
   }

   public int getRadius() {
      return this.effect.getRadius();
   }

   public void updateDeltas(float dx, float dy) {
      this.field_3 = dx;
      this.field_4 = dy;
   }

   public void invalidateDeltas() {
      this.field_3 = -1.0F;
      this.field_4 = -1.0F;
   }

   public float getDx() {
      return this.field_3;
   }

   public float getDy() {
      return this.field_4;
   }

   public int getRow() {
      return this.row;
   }

   public void setRow(int row) {
      this.row = row;
   }

   public float getRowOffset() {
      return (float)this.row * this.field_4;
   }

   public float getColOffset() {
      float r = (float)this.getRadius();
      return r * this.field_3;
   }

   public int getRowCnt() {
      int r = this.getRadius();
      return r * 2 + 1;
   }

   public float getAlpha() {
      float r = (float)this.getRadius();
      return 1.0F / ((2.0F * r + 1.0F) * (2.0F * r + 1.0F));
   }
}
