package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Flood extends Effect {
   private Paint paint;
   private Rectangle2D bounds;

   public Flood(Paint paint) {
      this.bounds = new Rectangle2D.Float();
      if (paint == null) {
         throw new IllegalArgumentException("Paint must be non-null");
      } else {
         this.paint = paint;
      }
   }

   public Flood(Paint paint, Rectangle2D bounds) {
      this(paint);
      if (bounds == null) {
         throw new IllegalArgumentException("Bounds must be non-null");
      } else {
         this.bounds.setRect(bounds);
      }
   }

   public Paint getPaint() {
      return this.paint;
   }

   public void setPaint(Paint paint) {
      if (paint == null) {
         throw new IllegalArgumentException("Paint must be non-null");
      } else {
         Paint old = this.paint;
         this.paint = paint;
         this.firePropertyChange("paint", old, paint);
      }
   }

   public Rectangle2D getFloodBounds() {
      return (Rectangle2D)this.bounds.clone();
   }

   public void setFloodBounds(Rectangle2D bounds) {
      if (bounds == null) {
         throw new IllegalArgumentException("Bounds must be non-null");
      } else {
         Rectangle2D old = new Rectangle2D.Float();
         old.setRect(bounds);
         this.bounds.setRect(bounds);
         this.firePropertyChange("flood bounds", old, bounds);
      }
   }

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      return this.transformBounds(transform, this.bounds);
   }

   public ImageData filter(GraphicsConfiguration config, AffineTransform transform, Effect defaultInput) {
      Rectangle2D fullBounds = this.getBounds(transform, defaultInput);
      Rectangle tmp = fullBounds.getBounds();
      int w = tmp.width;
      int h = tmp.height;
      Image dst = getCompatibleImage(config, w, h);
      Graphics2D gdst = (Graphics2D)dst.getGraphics();
      gdst.setComposite(AlphaComposite.Src);
      gdst.translate(-tmp.x, -tmp.y);
      if (transform != null && !transform.isIdentity()) {
         gdst.transform(transform);
      }

      gdst.setPaint(this.paint);
      gdst.fill(this.bounds);
      gdst.dispose();
      return new ImageData(config, dst, tmp);
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      return new Point2D.Float(Float.NaN, Float.NaN);
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      return new Point2D.Float(Float.NaN, Float.NaN);
   }

   public Effect.AccelType getAccelType(GraphicsConfiguration config) {
      return Effect.AccelType.INTRINSIC;
   }
}
