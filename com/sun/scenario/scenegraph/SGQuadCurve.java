package com.sun.scenario.scenegraph;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

public class SGQuadCurve extends SGAbstractGeometry {
   private QuadCurve2D.Float curve = new QuadCurve2D.Float();

   public final Shape getShape() {
      return this.curve;
   }

   public final float getX1() {
      return this.curve.x1;
   }

   public void setX1(float x1) {
      if (this.curve.x1 != x1) {
         this.curve.x1 = x1;
         this.invalidateStrokeShape();
      }

   }

   public final float getY1() {
      return this.curve.y1;
   }

   public void setY1(float y1) {
      if (this.curve.y1 != y1) {
         this.curve.y1 = y1;
         this.invalidateStrokeShape();
      }

   }

   public final float getX2() {
      return this.curve.x2;
   }

   public void setX2(float x2) {
      if (this.curve.x2 != x2) {
         this.curve.x2 = x2;
         this.invalidateStrokeShape();
      }

   }

   public final float getY2() {
      return this.curve.y2;
   }

   public void setY2(float y2) {
      if (this.curve.y2 != y2) {
         this.curve.y2 = y2;
         this.invalidateStrokeShape();
      }

   }

   public final float getCtrlX() {
      return this.curve.ctrlx;
   }

   public void setCtrlX(float ctrlx) {
      if (this.curve.ctrlx != ctrlx) {
         this.curve.ctrlx = ctrlx;
         this.invalidateStrokeShape();
      }

   }

   public final float getCtrlY() {
      return this.curve.ctrly;
   }

   public void setCtrlY(float ctrly) {
      if (this.curve.ctrly != ctrly) {
         this.curve.ctrly = ctrly;
         this.invalidateStrokeShape();
      }

   }

   public final boolean contains(Point2D point) {
      return SGShape.shapeContains(point.getX(), point.getY(), this, this.curve);
   }

   public final Rectangle2D getBounds(AffineTransform at) {
      return SGShape.getShapeBounds(at, this, this.curve);
   }
}
