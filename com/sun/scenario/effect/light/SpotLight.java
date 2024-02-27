package com.sun.scenario.effect.light;

import java.awt.Color;

public class SpotLight extends PointLight {
   private float pointsAtX;
   private float pointsAtY;
   private float pointsAtZ;
   private float specularExponent;

   public SpotLight() {
      this(0.0F, 0.0F, 0.0F, Color.WHITE);
   }

   public SpotLight(float x, float y, float z, Color color) {
      super(Light.Type.SPOT, x, y, z, color);
      this.pointsAtX = 0.0F;
      this.pointsAtY = 0.0F;
      this.pointsAtZ = 0.0F;
      this.specularExponent = 1.0F;
   }

   public float getPointsAtX() {
      return this.pointsAtX;
   }

   public void setPointsAtX(float pointsAtX) {
      float old = this.pointsAtX;
      this.pointsAtX = pointsAtX;
      this.firePropertyChange("pointsAtX", old, pointsAtX);
   }

   public float getPointsAtY() {
      return this.pointsAtY;
   }

   public void setPointsAtY(float pointsAtY) {
      float old = this.pointsAtY;
      this.pointsAtY = pointsAtY;
      this.firePropertyChange("pointsAtY", old, pointsAtY);
   }

   public float getPointsAtZ() {
      return this.pointsAtZ;
   }

   public void setPointsAtZ(float pointsAtZ) {
      float old = this.pointsAtZ;
      this.pointsAtZ = pointsAtZ;
      this.firePropertyChange("pointsAtZ", old, pointsAtZ);
   }

   public float getSpecularExponent() {
      return this.specularExponent;
   }

   public void setSpecularExponent(float specularExponent) {
      if (!(specularExponent < 0.0F) && !(specularExponent > 4.0F)) {
         float old = this.specularExponent;
         this.specularExponent = specularExponent;
         this.firePropertyChange("specularExponent", old, specularExponent);
      } else {
         throw new IllegalArgumentException("Specular exponent must be in the range [0,4]");
      }
   }

   public float[] getNormalizedLightPosition() {
      float x = this.getX();
      float y = this.getY();
      float z = this.getZ();
      float len = (float)Math.sqrt((double)(x * x + y * y + z * z));
      if (len == 0.0F) {
         len = 1.0F;
      }

      float[] pos = new float[]{x / len, y / len, z / len};
      return pos;
   }

   public float[] getNormalizedLightDirection() {
      float sx = this.pointsAtX - this.getX();
      float sy = this.pointsAtY - this.getY();
      float sz = this.pointsAtZ - this.getZ();
      float len = (float)Math.sqrt((double)(sx * sx + sy * sy + sz * sz));
      if (len == 0.0F) {
         len = 1.0F;
      }

      float[] vec = new float[]{sx / len, sy / len, sz / len};
      return vec;
   }
}
