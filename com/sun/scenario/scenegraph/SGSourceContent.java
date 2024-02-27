package com.sun.scenario.scenegraph;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

class SGSourceContent {
   private final AffineTransform xform;
   private final Image unxformImage;
   private final Rectangle2D unxformBounds;
   private final Image xformImage;
   private final Rectangle2D xformBounds;

   public SGSourceContent(Image image) {
      this(image, new Rectangle(0, 0, image.getWidth((ImageObserver)null), image.getHeight((ImageObserver)null)));
   }

   public SGSourceContent(Image image, Rectangle2D bounds) {
      this(new AffineTransform(), image, bounds, image, bounds);
   }

   public SGSourceContent(AffineTransform xform, Image unxformImage, Rectangle2D unxformBounds, Image xformImage, Rectangle2D xformBounds) {
      this.xform = xform;
      this.unxformImage = unxformImage;
      this.unxformBounds = unxformBounds;
      this.xformImage = xformImage;
      this.xformBounds = xformBounds;
   }

   public AffineTransform getTransform() {
      return this.xform;
   }

   public Image getUntransformedImage() {
      return this.unxformImage;
   }

   public Rectangle2D getUntransformedBounds() {
      return this.unxformBounds;
   }

   public Image getTransformedImage() {
      return this.xformImage;
   }

   public Rectangle2D getTransformedBounds() {
      return this.xformBounds;
   }
}
