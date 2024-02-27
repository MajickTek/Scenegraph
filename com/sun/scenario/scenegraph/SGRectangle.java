package com.sun.scenario.scenegraph;

import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class SGRectangle extends SGAbstractGeometry {
   private RoundRectangle2D.Float rrect = new RoundRectangle2D.Float();
   private Rectangle2D.Float rect = new Rectangle2D.Float();
   private boolean aaRequired;

   public final Shape getShape() {
      return (Shape)(this.rrect.archeight != 0.0F && this.rrect.arcwidth != 0.0F ? this.rrect : this.rect);
   }

   public final float getX() {
      return this.rect.x;
   }

   public void setX(float x) {
      if (this.rect.x != x) {
         this.rrect.x = this.rect.x = x;
         this.invalidateStrokeShape();
         this.checkAARequired();
      }

   }

   public final float getY() {
      return this.rect.y;
   }

   public void setY(float y) {
      if (this.rect.y != y) {
         this.rrect.y = this.rect.y = y;
         this.invalidateStrokeShape();
         this.checkAARequired();
      }

   }

   public final float getWidth() {
      return this.rect.width;
   }

   public void setWidth(float width) {
      if (this.rect.width != width) {
         this.rrect.width = this.rect.width = width;
         this.invalidateStrokeShape();
         this.checkAARequired();
      }

   }

   public final float getHeight() {
      return this.rect.height;
   }

   public void setHeight(float height) {
      if (this.rect.height != height) {
         this.rrect.height = this.rect.height = height;
         this.invalidateStrokeShape();
         this.checkAARequired();
      }

   }

   public final float getArcWidth() {
      return this.rrect.arcwidth;
   }

   public void setArcWidth(float arcwidth) {
      if (this.rrect.arcwidth != arcwidth) {
         this.rrect.arcwidth = arcwidth;
         this.invalidateStrokeShape();
         this.checkAARequired();
      }

   }

   public final float getArcHeight() {
      return this.rrect.archeight;
   }

   public void setArcHeight(float archeight) {
      if (this.rrect.archeight != archeight) {
         this.rrect.archeight = archeight;
         this.invalidateStrokeShape();
         this.checkAARequired();
      }

   }

   public boolean contains(Point2D point) {
      return SGShape.contains(point.getX(), point.getY(), this, (RoundRectangle2D)this.rrect);
   }

   public final Rectangle2D getBounds(AffineTransform at) {
      return SGShape.getBounds(at, this, (RoundRectangle2D)this.rrect);
   }

   public void setAntialiasingHint(Object hint) {
      super.setAntialiasingHint(hint);
      this.checkAARequired();
   }

   boolean isAARequired(AffineTransform xform) {
      if (!this.aaRequired && this.mode != SGAbstractShape.Mode.STROKE && this.mode != SGAbstractShape.Mode.STROKE_FILL) {
         if (!xform.isIdentity() && this.mode != SGAbstractShape.Mode.EMPTY) {
            double tx = xform.getTranslateX();
            double ty = xform.getTranslateY();
            return xform.getType() >= 15 || tx != (double)((int)tx) || ty != (double)((int)ty);
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   private void checkAARequired() {
      this.aaRequired = this.getAntialiasingHint() == RenderingHints.VALUE_ANTIALIAS_ON && (this.rrect.arcwidth != 0.0F || this.rrect.archeight != 0.0F || this.rect.x != (float)((int)this.rect.x) || this.rect.y != (float)((int)this.rect.y) || this.rect.width != (float)((int)this.rect.width) || this.rect.height != (float)((int)this.rect.height));
   }
}
