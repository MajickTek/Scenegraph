package com.sun.scenario.effect.light;

import java.awt.Color;

public class DistantLight extends Light {
   private float azimuth;
   private float elevation;

   public DistantLight() {
      this(0.0F, 0.0F, Color.WHITE);
   }

   public DistantLight(float azimuth, float elevation, Color color) {
      super(Light.Type.DISTANT, color);
      this.azimuth = azimuth;
      this.elevation = elevation;
   }

   public float getAzimuth() {
      return this.azimuth;
   }

   public void setAzimuth(float azimuth) {
      float old = this.azimuth;
      this.azimuth = azimuth;
      this.firePropertyChange("azimuth", old, azimuth);
   }

   public float getElevation() {
      return this.elevation;
   }

   public void setElevation(float elevation) {
      float old = this.elevation;
      this.elevation = elevation;
      this.firePropertyChange("elevation", old, elevation);
   }

   public float[] getNormalizedLightPosition() {
      double a = Math.toRadians((double)this.azimuth);
      double e = Math.toRadians((double)this.elevation);
      float x = (float)(Math.cos(a) * Math.cos(e));
      float y = (float)(Math.sin(a) * Math.cos(e));
      float z = (float)Math.sin(e);
      float len = (float)Math.sqrt((double)(x * x + y * y + z * z));
      if (len == 0.0F) {
         len = 1.0F;
      }

      float[] pos = new float[]{x / len, y / len, z / len};
      return pos;
   }
}
