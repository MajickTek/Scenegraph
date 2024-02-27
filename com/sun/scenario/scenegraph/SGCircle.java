package com.sun.scenario.scenegraph;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class SGCircle extends SGAbstractGeometry {
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

   public final float getRadius() {
      return this.ellipse.width / 2.0F;
   }

   public void setRadius(float r) {
      if (this.getRadius() != r) {
         float cx = this.getCenterX();
         float cy = this.getCenterY();
         this.ellipse.x = cx - r;
         this.ellipse.y = cy - r;
         this.ellipse.width = r * 2.0F;
         this.ellipse.height = r * 2.0F;
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
