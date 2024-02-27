package com.sun.scenario.scenegraph;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class SGLine extends SGAbstractGeometry {
   private Line2D.Float line = new Line2D.Float();

   public final Shape getShape() {
      return this.line;
   }

   public final float getX1() {
      return this.line.x1;
   }

   public void setX1(float x1) {
      if (this.line.x1 != x1) {
         this.line.x1 = x1;
         this.invalidateStrokeShape();
      }

   }

   public final float getY1() {
      return this.line.y1;
   }

   public void setY1(float y1) {
      if (this.line.y1 != y1) {
         this.line.y1 = y1;
         this.invalidateStrokeShape();
      }

   }

   public final float getX2() {
      return this.line.x2;
   }

   public void setX2(float x2) {
      if (this.line.x2 != x2) {
         this.line.x2 = x2;
         this.invalidateStrokeShape();
      }

   }

   public final float getY2() {
      return this.line.y2;
   }

   public void setY2(float y2) {
      if (this.line.y2 != y2) {
         this.line.y2 = y2;
         this.invalidateStrokeShape();
      }

   }

   public final boolean contains(Point2D point) {
      return SGShape.contains(point.getX(), point.getY(), this, (Line2D)this.line);
   }

   public final Rectangle2D getBounds(AffineTransform at) {
      return SGShape.getBounds(at, this, (Line2D)this.line);
   }
}
