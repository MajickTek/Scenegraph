package com.sun.scenario.scenegraph;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

public class SGRenderCache extends SGFilter {
   private Image cachedImage;
   private double cachedX;
   private double cachedY;
   private AffineTransform cachedXform;
   private boolean checkXform;
   private boolean enabled = true;
   private boolean subpixelaccurate;
   private Object filterHint;

   public SGRenderCache() {
      this.filterHint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
   }

   public static SGRenderCache createCache(SGNode n) {
      SGRenderCache cache = new SGRenderCache();
      if (n != null) {
         cache.setChild(n);
      }

      return cache;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      if (this.enabled != enabled) {
         this.enabled = enabled;
         this.cachedImage = null;
         this.visualChanged();
      }

   }

   public boolean isSubpixelAccurate() {
      return this.subpixelaccurate;
   }

   public void setSubpixelAccurate(boolean accurate) {
      if (this.subpixelaccurate != accurate) {
         this.subpixelaccurate = accurate;
         this.cachedImage = null;
         this.visualChanged();
      }

   }

   public Object getInterpolationHint() {
      return this.filterHint;
   }

   public void setInterpolationHint(Object hint) {
      if (this.filterHint != hint) {
         if (!RenderingHints.KEY_INTERPOLATION.isCompatibleValue(hint)) {
            throw new IllegalArgumentException("invalid hint");
         }

         this.filterHint = hint;
         this.visualChanged();
      }

   }

   void markDirty(int state) {
      if ((state & 8) != 0) {
         this.checkXform = true;
      } else {
         this.cachedImage = null;
      }

      super.markDirty(state);
   }

   private void checkAccumTransform() {
      AffineTransform at = this.getCumulativeTransform();
      if (this.cachedXform != null && at.getScaleX() == this.cachedXform.getScaleX() && at.getScaleY() == this.cachedXform.getScaleY() && at.getShearX() == this.cachedXform.getShearX() && at.getShearY() == this.cachedXform.getShearY()) {
         if (this.subpixelaccurate && (fract(at.getTranslateX()) != fract(this.cachedXform.getTranslateX()) || fract(at.getTranslateY()) != fract(this.cachedXform.getTranslateY()))) {
            this.cachedImage = null;
         }
      } else {
         this.cachedImage = null;
      }

   }

   private static double fract(double v) {
      return v - Math.floor(v);
   }

   public int needsSourceContent(Graphics2D g) {
      if (this.enabled) {
         if (this.checkXform) {
            this.checkXform = false;
            this.checkAccumTransform();
         }

         return this.cachedImage == null ? 2 : 4;
      } else {
         return 0;
      }
   }

   public boolean canExpandBounds() {
      return this.enabled;
   }

   public Rectangle2D getBounds(AffineTransform xform) {
      SGNode child = this.getChild();
      if (child == null) {
         return new Rectangle2D.Float();
      } else {
         Rectangle2D cb = child.getBounds(xform);
         if (this.enabled) {
            cb.setRect(cb.getX() - 1.0, cb.getY() - 1.0, cb.getWidth() + 2.0, cb.getHeight() + 2.0);
         }

         return cb;
      }
   }

   Rectangle2D calculateAccumBounds() {
      return this.getBounds(this.getCumulativeTransform());
   }

   public void renderFromCache(Graphics2D g) {
      AffineTransform at = g.getTransform();
      double x = at.getTranslateX() + this.cachedX;
      double y = at.getTranslateY() + this.cachedY;
      g.setTransform(AffineTransform.getTranslateInstance(x, y));
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, this.filterHint);
      g.drawImage(this.cachedImage, 0, 0, (ImageObserver)null);
   }

   public void renderFinalImage(Graphics2D g, SGSourceContent srcContent) {
      this.cachedImage = srcContent.getTransformedImage();
      Rectangle2D srcBounds = srcContent.getTransformedBounds();
      AffineTransform curTx = srcContent.getTransform();
      this.cachedXform = new AffineTransform(curTx);
      this.cachedX = srcBounds.getX() - curTx.getTranslateX();
      this.cachedY = srcBounds.getY() - curTx.getTranslateY();
      g.drawImage(this.cachedImage, 0, 0, (ImageObserver)null);
   }

   boolean hasOverlappingContents() {
      return this.enabled ? false : super.hasOverlappingContents();
   }
}
