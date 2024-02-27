package com.sun.scenario.scenegraph;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

class DesktopSGTransformFactory extends SGTransform.Factory {
   public DesktopSGTransformFactory() {
   }

   SGTransform.Translate makeTranslate(double tx, double ty) {
      return new Translate(tx, ty);
   }

   SGTransform.Scale makeScale(double sx, double sy) {
      return new Scale(sx, sy);
   }

   SGTransform.Rotate makeRotate(double theta) {
      return new Rotate(theta);
   }

   SGTransform.Shear makeShear(double shx, double shy) {
      return new Shear(shx, shy);
   }

   SGTransform.Affine makeAffine(AffineTransform at) {
      return new Affine(at);
   }

   static Point2D setPoint(Point2D dst, double x, double y) {
      if (dst == null) {
         dst = new Point2D.Float();
      }

      ((Point2D)dst).setLocation(x, y);
      return (Point2D)dst;
   }

   public static final class Affine extends SGTransform.Affine {
      // $FF: renamed from: at java.awt.geom.AffineTransform
      private AffineTransform field_13;

      public Affine(AffineTransform at) {
         if (at == null) {
            this.field_13 = new AffineTransform();
         } else {
            this.field_13 = new AffineTransform(at);
         }

      }

      public Point2D transform(Point2D src, Point2D dst) {
         return this.field_13.transform(src, dst);
      }

      public Point2D inverseTransform(Point2D src, Point2D dst) {
         try {
            return this.field_13.inverseTransform(src, dst);
         } catch (NoninvertibleTransformException var4) {
            return DesktopSGTransformFactory.setPoint(dst, src.getX(), src.getY());
         }
      }

      public void concatenateInto(AffineTransform at) {
         at.concatenate(this.field_13);
      }

      public void concatenateInto(Graphics2D g2d) {
         g2d.transform(this.field_13);
      }

      public void getTransform(AffineTransform at) {
         at.setTransform(this.field_13);
      }

      public AffineTransform getAffine() {
         return new AffineTransform(this.field_13);
      }

      public void setAffine(AffineTransform at) {
         if (!this.field_13.equals(at)) {
            this.field_13.setTransform(at);
            this.invalidateTransform();
         }

      }

      public void transformBy(AffineTransform at) {
         if (!at.isIdentity()) {
            this.field_13.concatenate(at);
            this.invalidateTransform();
         }

      }

      public void reset() {
         this.field_13.setToIdentity();
         this.invalidateTransform();
      }
   }

   public static final class Shear extends SGTransform.Shear {
      private double shx;
      private double shy;

      public Shear(double shx, double shy) {
         this.shx = shx;
         this.shy = shy;
      }

      public Point2D transform(Point2D src, Point2D dst) {
         double x = src.getX();
         double y = src.getY();
         double retx = x + this.shx * y;
         double rety = y + this.shy * x;
         return DesktopSGTransformFactory.setPoint(dst, retx, rety);
      }

      public Point2D inverseTransform(Point2D src, Point2D dst) {
         double x = src.getX();
         double y = src.getY();
         double det = 1.0 - this.shx * this.shy;
         double retx = x;
         double rety = y;
         if (det != 0.0) {
            retx = x - this.shx * y;
            rety = y - this.shy * x;
            retx /= det;
            rety /= det;
         }

         return DesktopSGTransformFactory.setPoint(dst, retx, rety);
      }

      public void concatenateInto(AffineTransform at) {
         at.shear(this.shx, this.shy);
      }

      public void concatenateInto(Graphics2D g2d) {
         g2d.shear(this.shx, this.shy);
      }

      public void getTransform(AffineTransform at) {
         at.setToShear(this.shx, this.shy);
      }

      public double getShearX() {
         return this.shx;
      }

      public double getShearY() {
         return this.shy;
      }

      public void setShearX(double shx) {
         if (this.shx != shx) {
            this.shx = shx;
            this.invalidateTransform();
         }

      }

      public void setShearY(double shy) {
         if (this.shy != shy) {
            this.shy = shy;
            this.invalidateTransform();
         }

      }

      public void setShear(double shx, double shy) {
         if (this.shx != shx || this.shy != shy) {
            this.shx = shx;
            this.shy = shy;
            this.invalidateTransform();
         }

      }

      public void shearBy(double shx, double shy) {
         this.shx *= shx;
         this.shy *= shy;
         this.invalidateTransform();
      }
   }

   public static final class Rotate extends SGTransform.Rotate {
      private double theta;

      public Rotate(double theta) {
         this.theta = theta;
      }

      static Point2D transform(Point2D src, Point2D dst, double theta) {
         double sin = Math.sin(theta);
         double cos = Math.cos(theta);
         double x = src.getX();
         double y = src.getY();
         double retx = x * cos - y * sin;
         double rety = x * sin + y * cos;
         return DesktopSGTransformFactory.setPoint(dst, retx, rety);
      }

      public Point2D transform(Point2D src, Point2D dst) {
         return transform(src, dst, this.theta);
      }

      public Point2D inverseTransform(Point2D src, Point2D dst) {
         return transform(src, dst, -this.theta);
      }

      public void concatenateInto(AffineTransform at) {
         at.rotate(this.theta);
      }

      public void concatenateInto(Graphics2D g2d) {
         g2d.rotate(this.theta);
      }

      public void getTransform(AffineTransform at) {
         at.setToRotation(this.theta);
      }

      public double getRotation() {
         return this.theta;
      }

      public void setRotation(double theta) {
         if (this.theta != theta) {
            this.theta = theta;
            this.invalidateTransform();
         }

      }

      public void rotateBy(double theta) {
         this.theta += theta;
         this.invalidateTransform();
      }
   }

   public static final class Scale extends SGTransform.Scale {
      // $FF: renamed from: sx double
      private double field_14;
      // $FF: renamed from: sy double
      private double field_15;

      public Scale(double sx, double sy) {
         this.field_14 = sx;
         this.field_15 = sy;
      }

      public Point2D transform(Point2D src, Point2D dst) {
         double retx = src.getX() * this.field_14;
         double rety = src.getY() * this.field_15;
         return DesktopSGTransformFactory.setPoint(dst, retx, rety);
      }

      public Point2D inverseTransform(Point2D src, Point2D dst) {
         double retx = src.getX() / (this.field_14 == 0.0 ? 1.0 : this.field_14);
         double rety = src.getY() / (this.field_15 == 0.0 ? 1.0 : this.field_15);
         return DesktopSGTransformFactory.setPoint(dst, retx, rety);
      }

      public void concatenateInto(AffineTransform at) {
         at.scale(this.field_14, this.field_15);
      }

      public void concatenateInto(Graphics2D g2d) {
         g2d.scale(this.field_14, this.field_15);
      }

      public void getTransform(AffineTransform at) {
         at.setToScale(this.field_14, this.field_15);
      }

      public double getScaleX() {
         return this.field_14;
      }

      public double getScaleY() {
         return this.field_15;
      }

      public void setScaleX(double sx) {
         if (this.field_14 != sx) {
            this.field_14 = sx;
            this.invalidateTransform();
         }

      }

      public void setScaleY(double sy) {
         if (this.field_15 != sy) {
            this.field_15 = sy;
            this.invalidateTransform();
         }

      }

      public void setScale(double sx, double sy) {
         if (this.field_14 != sx || this.field_15 != sy) {
            this.field_14 = sx;
            this.field_15 = sy;
            this.invalidateTransform();
         }

      }

      public void scaleBy(double sx, double sy) {
         this.field_14 *= sx;
         this.field_15 *= sy;
         this.invalidateTransform();
      }
   }

   public static final class Translate extends SGTransform.Translate {
      // $FF: renamed from: tx double
      private double field_16;
      // $FF: renamed from: ty double
      private double field_17;

      public Translate(double tx, double ty) {
         this.field_16 = tx;
         this.field_17 = ty;
      }

      public Point2D transform(Point2D src, Point2D dst) {
         return DesktopSGTransformFactory.setPoint(dst, src.getX() + this.field_16, src.getY() + this.field_17);
      }

      public Point2D inverseTransform(Point2D src, Point2D dst) {
         return DesktopSGTransformFactory.setPoint(dst, src.getX() - this.field_16, src.getY() - this.field_17);
      }

      public void concatenateInto(AffineTransform at) {
         at.translate(this.field_16, this.field_17);
      }

      public void concatenateInto(Graphics2D g2d) {
         g2d.translate(this.field_16, this.field_17);
      }

      public void getTransform(AffineTransform at) {
         at.setToTranslation(this.field_16, this.field_17);
      }

      public double getTranslateX() {
         return this.field_16;
      }

      public double getTranslateY() {
         return this.field_17;
      }

      public void setTranslateX(double tx) {
         if (this.field_16 != tx) {
            this.field_16 = tx;
            this.invalidateTransform();
         }

      }

      public void setTranslateY(double ty) {
         if (this.field_17 != ty) {
            this.field_17 = ty;
            this.invalidateTransform();
         }

      }

      public void setTranslation(double tx, double ty) {
         if (this.field_16 != tx || this.field_17 != ty) {
            this.field_16 = tx;
            this.field_17 = ty;
            this.invalidateTransform();
         }

      }

      public void translateBy(double tx, double ty) {
         this.field_16 += tx;
         this.field_17 += ty;
         this.invalidateTransform();
      }
   }
}
