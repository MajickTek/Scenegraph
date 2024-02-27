package com.sun.scenario.scenegraph;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class SGTransform extends SGFilter {
   static Factory theFactory = new DesktopSGTransformFactory();

   public static Translate createTranslation(double tx, double ty, SGNode child) {
      Translate t = theFactory.makeTranslate(tx, ty);
      if (child != null) {
         t.setChild(child);
      }

      return t;
   }

   public static Scale createScale(double sx, double sy, SGNode child) {
      Scale s = theFactory.makeScale(sx, sy);
      if (child != null) {
         s.setChild(child);
      }

      return s;
   }

   public static Rotate createRotation(double theta, SGNode child) {
      Rotate r = theFactory.makeRotate(theta);
      if (child != null) {
         r.setChild(child);
      }

      return r;
   }

   public static Shear createShear(double shx, double shy, SGNode child) {
      Shear s = theFactory.makeShear(shx, shy);
      if (child != null) {
         s.setChild(child);
      }

      return s;
   }

   public static Affine createAffine(AffineTransform at, SGNode child) {
      Affine a = theFactory.makeAffine(at);
      if (child != null) {
         a.setChild(child);
      }

      return a;
   }

   SGTransform() {
   }

   public abstract Point2D transform(Point2D var1, Point2D var2);

   public abstract Point2D inverseTransform(Point2D var1, Point2D var2);

   public abstract void concatenateInto(AffineTransform var1);

   public abstract void concatenateInto(Graphics2D var1);

   public abstract void getTransform(AffineTransform var1);

   public AffineTransform createAffineTransform() {
      AffineTransform at = new AffineTransform();
      this.getTransform(at);
      return at;
   }

   public abstract void reset();

   protected void invalidateTransform() {
      if (this.getChild() != null) {
         this.boundsChanged();
         this.transformChanged();
         this.dispatchAllPendingEvents();
      }

   }

   public boolean canSkipRendering() {
      return true;
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
            AffineTransform oldAT = g.getTransform();
            this.concatenateInto(g);
            child.render(g, dirtyRegion, clearDirty);
            g.setTransform(oldAT);
         }

         if (clearDirty) {
            this.clearDirty();
         }

      }
   }

   final AffineTransform updateCumulativeTransform(AffineTransform oldAccumXform) {
      if (oldAccumXform == null) {
         oldAccumXform = new AffineTransform();
      }

      SGParent parent = this.getParent();
      if (parent != null) {
         oldAccumXform.setTransform(parent.getCumulativeTransform());
      } else {
         oldAccumXform.setToIdentity();
      }

      this.concatenateInto(oldAccumXform);
      return oldAccumXform;
   }

   public Rectangle2D getBounds(AffineTransform transform) {
      SGNode child = this.getChild();
      if (child == null) {
         return new Rectangle2D.Float();
      } else {
         AffineTransform childTx = this.createAffineTransform();
         if (childTx != null && !childTx.isIdentity()) {
            if (transform != null && !transform.isIdentity()) {
               childTx.preConcatenate(transform);
            }

            transform = childTx;
         }

         return child.getBounds(transform);
      }
   }

   public abstract static class Affine extends SGTransform {
      Affine() {
      }

      public abstract AffineTransform getAffine();

      public abstract void setAffine(AffineTransform var1);

      public abstract void transformBy(AffineTransform var1);
   }

   public abstract static class Shear extends SGTransform {
      Shear() {
      }

      public abstract double getShearX();

      public abstract double getShearY();

      public abstract void setShearX(double var1);

      public abstract void setShearY(double var1);

      public abstract void setShear(double var1, double var3);

      public abstract void shearBy(double var1, double var3);

      public void reset() {
         this.setShear(0.0, 0.0);
      }
   }

   public abstract static class Rotate extends SGTransform {
      Rotate() {
      }

      public abstract double getRotation();

      public abstract void setRotation(double var1);

      public abstract void rotateBy(double var1);

      public void reset() {
         this.setRotation(0.0);
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
               double theta = this.getRotation();
               g.rotate(theta);
               child.render(g, dirtyRegion, clearDirty);
               g.rotate(-theta);
            }

            if (clearDirty) {
               this.clearDirty();
            }

         }
      }
   }

   public abstract static class Scale extends SGTransform {
      Scale() {
      }

      public abstract double getScaleX();

      public abstract double getScaleY();

      public abstract void setScaleX(double var1);

      public abstract void setScaleY(double var1);

      public abstract void setScale(double var1, double var3);

      public abstract void scaleBy(double var1, double var3);

      public void reset() {
         this.setScale(1.0, 1.0);
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
               double sx = this.getScaleX();
               double sy = this.getScaleY();
               if (sx != 0.0 && sy != 0.0) {
                  g.scale(sx, sy);
                  child.render(g, dirtyRegion, clearDirty);
                  g.scale(1.0 / sx, 1.0 / sy);
               }
            }

            if (clearDirty) {
               this.clearDirty();
            }

         }
      }
   }

   public abstract static class Translate extends SGTransform {
      Translate() {
      }

      public abstract double getTranslateX();

      public abstract double getTranslateY();

      public abstract void setTranslateX(double var1);

      public abstract void setTranslateY(double var1);

      public abstract void setTranslation(double var1, double var3);

      public abstract void translateBy(double var1, double var3);

      public void reset() {
         this.setTranslation(0.0, 0.0);
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
               double tx = this.getTranslateX();
               double ty = this.getTranslateY();
               g.translate(tx, ty);
               child.render(g, dirtyRegion, clearDirty);
               g.translate(-tx, -ty);
            }

            if (clearDirty) {
               this.clearDirty();
            }

         }
      }
   }

   abstract static class Factory {
      abstract Translate makeTranslate(double var1, double var3);

      abstract Scale makeScale(double var1, double var3);

      abstract Rotate makeRotate(double var1);

      abstract Shear makeShear(double var1, double var3);

      abstract Affine makeAffine(AffineTransform var1);
   }
}
