package com.sun.scenario.scenegraph;

import java.awt.ComponentOrientation;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

public class SGAlignment extends SGTransform {
   private static final boolean ltr;
   private int halign = 10;
   private int valign = 1;
   // $FF: renamed from: tx double
   private double field_18;
   // $FF: renamed from: ty double
   private double field_19;
   private boolean transformValid;

   public int getHorizontalAlignment() {
      return this.halign;
   }

   public void setHorizontalAlignment(int halign) {
      if (this.halign != halign) {
         if (halign != 2 && halign != 10 && halign != 0 && halign != 4 && halign != 11) {
            throw new IllegalArgumentException("invalid halign value");
         }

         this.halign = halign;
         this.invalidateTransform();
      }

   }

   public int getVerticalAlignment() {
      return this.valign;
   }

   public void setVerticalAlignment(int valign) {
      if (this.valign != valign) {
         if (valign != 1 && valign != 0 && valign != 3) {
            throw new IllegalArgumentException("invalid valign value");
         }

         this.valign = valign;
         this.invalidateTransform();
      }

   }

   private boolean isLeftAligned() {
      return this.halign == 2 || this.halign == 10 && ltr || this.halign == 11 && !ltr;
   }

   private boolean isRightAligned() {
      return this.halign == 4 || this.halign == 11 && ltr || this.halign == 10 && !ltr;
   }

   private void updateTransform() {
      if (this.isLeftAligned() && this.valign == 1) {
         this.field_18 = this.field_19 = 0.0;
      } else {
         SGNode child = this.getChild();
         if (child == null) {
            this.field_18 = this.field_19 = 0.0;
         } else {
            Rectangle2D bounds;
            if (child instanceof SGText) {
               bounds = ((SGText)child).getLogicalBounds((AffineTransform)null);
            } else {
               bounds = child.getBounds();
            }

            if (this.isRightAligned()) {
               this.field_18 = -bounds.getWidth();
            } else if (this.halign == 0) {
               this.field_18 = -bounds.getWidth() / 2.0;
            } else {
               this.field_18 = 0.0;
            }

            if (this.valign == 3) {
               this.field_19 = -bounds.getHeight();
            } else if (this.valign == 0) {
               this.field_19 = -bounds.getHeight() / 2.0;
            } else {
               this.field_19 = 0.0;
            }

         }
      }
   }

   private void validateTransform() {
      if (!this.transformValid) {
         if ((this.getDirtyState() & 40) == 0) {
            this.transformValid = true;
         }

         this.updateTransform();
      }

   }

   private static Point2D setPoint(Point2D dst, double x, double y) {
      if (dst == null) {
         dst = new Point2D.Float();
      }

      ((Point2D)dst).setLocation(x, y);
      return (Point2D)dst;
   }

   public Point2D transform(Point2D src, Point2D dst) {
      this.validateTransform();
      return setPoint(dst, src.getX() + this.field_18, src.getY() + this.field_19);
   }

   public Point2D inverseTransform(Point2D src, Point2D dst) {
      this.validateTransform();
      return setPoint(dst, src.getX() - this.field_18, src.getY() - this.field_19);
   }

   public void concatenateInto(AffineTransform at) {
      this.validateTransform();
      at.translate(this.field_18, this.field_19);
   }

   public void concatenateInto(Graphics2D g2d) {
      this.validateTransform();
      g2d.translate(this.field_18, this.field_19);
   }

   public void getTransform(AffineTransform at) {
      this.validateTransform();
      at.setToTranslation(this.field_18, this.field_19);
   }

   public void reset() {
      this.setHorizontalAlignment(10);
      this.setVerticalAlignment(1);
   }

   void render(Graphics2D g, Rectangle dirtyRegion, boolean clearDirty) {
      if (!this.isVisible()) {
         if (clearDirty) {
            this.clearDirty();
         }

      } else {
         if (dirtyRegion != null) {
            Rectangle2D bounds = this.getTransformedBounds();
            if (bounds == null || !bounds.intersects(dirtyRegion)) {
               if (clearDirty) {
                  this.clearDirty();
               }

               return;
            }
         }

         SGNode child = this.getChild();
         if (child != null) {
            this.validateTransform();
            g.translate(this.field_18, this.field_19);
            child.render(g, dirtyRegion, clearDirty);
            g.translate(-this.field_18, -this.field_19);
         }

         if (clearDirty) {
            this.clearDirty();
         }

      }
   }

   static {
      ltr = ComponentOrientation.getOrientation(Locale.getDefault()) == ComponentOrientation.LEFT_TO_RIGHT;
   }
}
