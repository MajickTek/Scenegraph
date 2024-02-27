package com.sun.scenario.effect;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class DisplacementMap extends CoreEffect {
   private FloatMap mapData;
   private float scaleX;
   private float scaleY;
   private float offsetX;
   private float offsetY;
   private boolean wrap;

   public DisplacementMap(FloatMap mapData) {
      this(mapData, DefaultInput);
   }

   public DisplacementMap(FloatMap mapData, Effect contentInput) {
      super(contentInput);
      this.scaleX = 1.0F;
      this.scaleY = 1.0F;
      this.offsetX = -0.5F;
      this.offsetY = -0.5F;
      this.setMapData(mapData);
      this.updatePeerKey("DisplacementMap");
   }

   public final FloatMap getMapData() {
      return this.mapData;
   }

   public void setMapData(FloatMap mapData) {
      if (mapData == null) {
         throw new IllegalArgumentException("Map data must be non-null");
      } else {
         FloatMap old = this.mapData;
         this.mapData = mapData;
         this.firePropertyChange("mapData", old, mapData);
      }
   }

   public final Effect getContentInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setContentInput(Effect contentInput) {
      this.setInput(0, contentInput);
   }

   public float getScaleX() {
      return this.scaleX;
   }

   public void setScaleX(float scaleX) {
      float old = this.scaleX;
      this.scaleX = scaleX;
      this.firePropertyChange("scaleX", old, scaleX);
   }

   public float getScaleY() {
      return this.scaleY;
   }

   public void setScaleY(float scaleY) {
      float old = this.scaleY;
      this.scaleY = scaleY;
      this.firePropertyChange("scaleY", old, scaleY);
   }

   public float getOffsetX() {
      return this.offsetX;
   }

   public void setOffsetX(float offsetX) {
      float old = this.offsetX;
      this.offsetX = offsetX;
      this.firePropertyChange("offsetX", old, offsetX);
   }

   public float getOffsetY() {
      return this.offsetY;
   }

   public void setOffsetY(float offsetY) {
      float old = this.offsetY;
      this.offsetY = offsetY;
      this.firePropertyChange("offsetY", old, offsetY);
   }

   public boolean getWrap() {
      return this.wrap;
   }

   public void setWrap(boolean wrap) {
      boolean old = this.wrap;
      this.wrap = wrap;
      this.firePropertyChange("wrap", old, wrap);
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      return new Point2D.Float(Float.NaN, Float.NaN);
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      Rectangle2D r = this.getBounds(new AffineTransform(), defaultInput);
      double x = (((Point2D)p).getX() - r.getX()) / r.getWidth();
      double y = (((Point2D)p).getY() - r.getY()) / r.getHeight();
      if (x >= 0.0 && y >= 0.0 && x < 1.0 && y < 1.0) {
         int mx = (int)(x * (double)this.mapData.getWidth());
         int my = (int)(y * (double)this.mapData.getHeight());
         float dx = this.mapData.getSample(mx, my, 0);
         float dy = this.mapData.getSample(mx, my, 1);
         x += (double)(this.scaleX * (dx + this.offsetX));
         y += (double)(this.scaleY * (dy + this.offsetY));
         if (this.wrap) {
            x -= Math.floor(x);
            y -= Math.floor(y);
         }

         p = new Point2D.Float();
         ((Point2D)p).setLocation(x * r.getWidth() + r.getX(), y * r.getHeight() + r.getY());
      }

      return this.getDefaultedInput(0, defaultInput).untransform((Point2D)p, defaultInput);
   }
}
