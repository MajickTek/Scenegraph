package com.sun.scenario.scenegraph;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class SGCubicCurve extends SGAbstractGeometry {
   private CubicCurve2D.Float curve = new CubicCurve2D.Float();

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

   public final float getCtrlX1() {
      return this.curve.ctrlx1;
   }

   public void setCtrlX1(float ctrlx1) {
      if (this.curve.ctrlx1 != ctrlx1) {
         this.curve.ctrlx1 = ctrlx1;
         this.invalidateStrokeShape();
      }

   }

   public final float getCtrlY1() {
      return this.curve.ctrly1;
   }

   public void setCtrlY1(float ctrly1) {
      if (this.curve.ctrly1 != ctrly1) {
         this.curve.ctrly1 = ctrly1;
         this.invalidateStrokeShape();
      }

   }

   public final float getCtrlX2() {
      return this.curve.ctrlx2;
   }

   public void setCtrlX2(float ctrlx2) {
      if (this.curve.ctrlx2 != ctrlx2) {
         this.curve.ctrlx2 = ctrlx2;
         this.invalidateStrokeShape();
      }

   }

   public final float getCtrlY2() {
      return this.curve.ctrly2;
   }

   public void setCtrlY2(float ctrly2) {
      if (this.curve.ctrly2 != ctrly2) {
         this.curve.ctrly2 = ctrly2;
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
