package com.sun.scenario.effect.impl;

import com.sun.scenario.effect.Effect;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;

public class ImageData {
   private ImageData sharedOwner;
   private GraphicsConfiguration config;
   private int refcount;
   private Image image;
   private final Rectangle bounds;

   public ImageData(GraphicsConfiguration config, Image image, Rectangle bounds) {
      this.config = config;
      this.refcount = 1;
      this.image = image;
      this.bounds = bounds;
   }

   public ImageData(ImageData original, Rectangle bounds) {
      this((GraphicsConfiguration)null, original.image, bounds);
      this.sharedOwner = original;
      this.sharedOwner.addref();
   }

   public GraphicsConfiguration getGraphicsConfig() {
      return this.config;
   }

   public Image getImage() {
      return this.image;
   }

   public Rectangle getBounds() {
      return this.bounds;
   }

   public int getReferenceCount() {
      return this.refcount;
   }

   public void addref() {
      ++this.refcount;
   }

   public void unref() {
      if (--this.refcount == 0) {
         if (this.sharedOwner != null) {
            this.sharedOwner.unref();
            this.sharedOwner = null;
         } else if (this.config != null) {
            Effect.releaseCompatibleImage(this.config, this.image);
            this.config = null;
         }

         this.image = null;
      }

   }
}
