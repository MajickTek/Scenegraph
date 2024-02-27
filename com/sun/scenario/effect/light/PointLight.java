package com.sun.scenario.effect.light;

import java.awt.Color;

public class PointLight extends Light {
   // $FF: renamed from: x float
   private float field_5;
   // $FF: renamed from: y float
   private float field_6;
   // $FF: renamed from: z float
   private float field_7;

   public PointLight() {
      this(0.0F, 0.0F, 0.0F, Color.WHITE);
   }

   public PointLight(float x, float y, float z, Color color) {
      this(Light.Type.POINT, x, y, z, color);
   }

   PointLight(Light.Type type, float x, float y, float z, Color color) {
      super(type, color);
      this.field_5 = x;
      this.field_6 = y;
      this.field_7 = z;
   }

   public float getX() {
      return this.field_5;
   }

   public void setX(float x) {
      float old = this.field_5;
      this.field_5 = x;
      this.firePropertyChange("x", old, x);
   }

   public float getY() {
      return this.field_6;
   }

   public void setY(float y) {
      float old = this.field_6;
      this.field_6 = y;
      this.firePropertyChange("y", old, y);
   }

   public float getZ() {
      return this.field_7;
   }

   public void setZ(float z) {
      float old = this.field_7;
      this.field_7 = z;
      this.firePropertyChange("z", old, z);
   }

   public float[] getNormalizedLightPosition() {
      float len = (float)Math.sqrt((double)(this.field_5 * this.field_5 + this.field_6 * this.field_6 + this.field_7 * this.field_7));
      if (len == 0.0F) {
         len = 1.0F;
      }

      float[] pos = new float[]{this.field_5 / len, this.field_6 / len, this.field_7 / len};
      return pos;
   }
}
