package com.sun.scenario.scenegraph;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class SGEllipse extends SGAbstractGeometry {
   private Ellipse2D.Float ellipse = new Ellipse2D.Float();

   public final Shape getShape() {
      return this.ellipse;
   }

   public final float getCenterX() {
      return this.ellipse.x + this.ellipse.width / 2.0F;
   }

   public void setCenterX(float cx) {
      if (this.getCenterX() != cx) {
         this.ellipse.x = cx - this.ellipse.width / 2.0F;
         this.invalidateStrokeShape();
      }

   }

   public final float getCenterY() {
      return this.ellipse.y + this.ellipse.height / 2.0F;
   }

   public void setCenterY(float cy) {
      if (this.getCenterY() != cy) {
         this.ellipse.y = cy - this.ellipse.height / 2.0F;
         this.invalidateStrokeShape();
      }

   }

   public final float getRadiusX() {
      return this.ellipse.width / 2.0F;
   }

   public void setRadiusX(float rx) {
      if (this.getRadiusX() != rx) {
         float cx = this.getCenterX();
         this.ellipse.x = cx - rx;
         this.ellipse.width = rx * 2.0F;
         this.invalidateStrokeShape();
      }

   }

   public final float getRadiusY() {
      return this.ellipse.height / 2.0F;
   }

   public void setRadiusY(float ry) {
      if (this.getRadiusY() != ry) {
         float cy = this.getCenterY();
         this.ellipse.y = cy - ry;
         this.ellipse.height = ry * 2.0F;
         this.invalidateStrokeShape();
      }

   }

   public final boolean contains(Point2D point) {
      return SGShape.contains(point.getX(), point.getY(), this, (Ellipse2D)this.ellipse);
   }

   public final Rectangle2D getBounds(AffineTransform at) {
      return SGShape.getBounds(at, this, (Ellipse2D)this.ellipse);
   }
}
