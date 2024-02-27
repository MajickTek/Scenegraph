package com.sun.scenario.scenegraph;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class SGArc extends SGAbstractGeometry {
   private Arc2D.Float arc = new Arc2D.Float();

   public final Shape getShape() {
      return this.arc;
   }

   public final float getCenterX() {
      return this.arc.x + this.arc.width / 2.0F;
   }

   public void setCenterX(float cx) {
      if (this.getCenterX() != cx) {
         this.arc.x = cx - this.arc.width / 2.0F;
         this.invalidateStrokeShape();
      }

   }

   public final float getCenterY() {
      return this.arc.y + this.arc.height / 2.0F;
   }

   public void setCenterY(float cy) {
      if (this.getCenterY() != cy) {
         this.arc.y = cy - this.arc.height / 2.0F;
         this.invalidateStrokeShape();
      }

   }

   public final float getRadiusX() {
      return this.arc.width / 2.0F;
   }

   public void setRadiusX(float rx) {
      if (this.getRadiusX() != rx) {
         float cx = this.getCenterX();
         this.arc.x = cx - rx;
         this.arc.width = rx * 2.0F;
         this.invalidateStrokeShape();
      }

   }

   public final float getRadiusY() {
      return this.arc.height / 2.0F;
   }

   public void setRadiusY(float ry) {
      if (this.getRadiusY() != ry) {
         float cy = this.getCenterY();
         this.arc.y = cy - ry;
         this.arc.height = ry * 2.0F;
         this.invalidateStrokeShape();
      }

   }

   public final float getAngleStart() {
      return this.arc.start;
   }

   public void setAngleStart(float start) {
      if (this.arc.start != start) {
         this.arc.start = start;
         this.invalidateStrokeShape();
      }

   }

   public final float getAngleExtent() {
      return this.arc.extent;
   }

   public void setAngleExtent(float extent) {
      if (this.arc.extent != extent) {
         this.arc.extent = extent;
         this.invalidateStrokeShape();
      }

   }

   public final int getArcType() {
      return this.arc.getArcType();
   }

   public void setArcType(int type) {
      if (this.getArcType() != type) {
         this.arc.setArcType(type);
         this.invalidateStrokeShape();
      }

   }

   public final boolean contains(Point2D point) {
      return SGShape.contains(point.getX(), point.getY(), this, (Arc2D)this.arc);
   }

   public final Rectangle2D getBounds(AffineTransform at) {
      return SGShape.getBounds(at, this, (Arc2D)this.arc);
   }
}
