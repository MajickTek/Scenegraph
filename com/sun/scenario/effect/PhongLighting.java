package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import com.sun.scenario.effect.light.Light;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PhongLighting extends CoreEffect {
   private float surfaceScale;
   private float diffuseConstant;
   private float specularConstant;
   private float specularExponent;
   private Light light;
   private final PropertyChangeListener lightListener;

   public PhongLighting(Light light) {
      this(light, new Shadow(10.0F), DefaultInput);
   }

   public PhongLighting(Light light, Effect bumpInput, Effect contentInput) {
      super(bumpInput, contentInput);
      this.lightListener = new LightChangeListener();
      this.surfaceScale = 1.0F;
      this.diffuseConstant = 1.0F;
      this.specularConstant = 1.0F;
      this.specularExponent = 1.0F;
      this.setLight(light);
   }

   public final Effect getBumpInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setBumpInput(Effect bumpInput) {
      this.setInput(0, bumpInput);
   }

   public final Effect getContentInput() {
      return (Effect)this.getInputs().get(1);
   }

   private Effect getContentInput(Effect defaultInput) {
      return this.getDefaultedInput(1, defaultInput);
   }

   public void setContentInput(Effect contentInput) {
      this.setInput(1, contentInput);
   }

   public Light getLight() {
      return this.light;
   }

   public void setLight(Light light) {
      if (light == null) {
         throw new IllegalArgumentException("Light must be non-null");
      } else {
         Light old = this.light;
         if (old != null) {
            old.removePropertyChangeListener(this.lightListener);
         }

         this.light = light;
         this.light.addPropertyChangeListener(this.lightListener);
         this.updatePeerKey("PhongLighting_" + light.getType().name());
         this.firePropertyChange("light", old, light);
      }
   }

   public float getDiffuseConstant() {
      return this.diffuseConstant;
   }

   public void setDiffuseConstant(float diffuseConstant) {
      if (!(diffuseConstant < 0.0F) && !(diffuseConstant > 2.0F)) {
         float old = this.diffuseConstant;
         this.diffuseConstant = diffuseConstant;
         this.firePropertyChange("diffuseConstant", old, diffuseConstant);
      } else {
         throw new IllegalArgumentException("Diffuse constant must be in the range [0,2]");
      }
   }

   public float getSpecularConstant() {
      return this.specularConstant;
   }

   public void setSpecularConstant(float specularConstant) {
      if (!(specularConstant < 0.0F) && !(specularConstant > 2.0F)) {
         float old = this.specularConstant;
         this.specularConstant = specularConstant;
         this.firePropertyChange("specularConstant", old, specularConstant);
      } else {
         throw new IllegalArgumentException("Specular constant must be in the range [0,2]");
      }
   }

   public float getSpecularExponent() {
      return this.specularExponent;
   }

   public void setSpecularExponent(float specularExponent) {
      if (!(specularExponent < 0.0F) && !(specularExponent > 40.0F)) {
         float old = this.specularExponent;
         this.specularExponent = specularExponent;
         this.firePropertyChange("specularExponent", old, specularExponent);
      } else {
         throw new IllegalArgumentException("Specular exponent must be in the range [0,40]");
      }
   }

   public float getSurfaceScale() {
      return this.surfaceScale;
   }

   public void setSurfaceScale(float surfaceScale) {
      if (!(surfaceScale < 0.0F) && !(surfaceScale > 10.0F)) {
         float old = this.surfaceScale;
         this.surfaceScale = surfaceScale;
         this.firePropertyChange("surfaceScale", old, surfaceScale);
      } else {
         throw new IllegalArgumentException("Surface scale must be in the range [0,10]");
      }
   }

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      return this.getContentInput(defaultInput).getBounds(transform, defaultInput);
   }

   public Rectangle getResultBounds(AffineTransform transform, ImageData... inputDatas) {
      return super.getResultBounds(transform, new ImageData[]{inputDatas[1]});
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      return this.getContentInput(defaultInput).transform(p, defaultInput);
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      return this.getContentInput(defaultInput).untransform(p, defaultInput);
   }

   private class LightChangeListener implements PropertyChangeListener {
      private LightChangeListener() {
      }

      public void propertyChange(PropertyChangeEvent e) {
         PhongLighting.this.firePropertyChange("light", (Object)null, PhongLighting.this.light);
      }
   }
}
