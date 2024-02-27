package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Offset extends Effect {
   private int xoff;
   private int yoff;

   public Offset(int xoff, int yoff, Effect input) {
      super(input);
      this.xoff = xoff;
      this.yoff = yoff;
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
   }

   public int getX() {
      return this.xoff;
   }

   public void setX(int xoff) {
      int old = this.xoff;
      this.xoff = xoff;
      this.firePropertyChange("x", old, xoff);
   }

   public int getY() {
      return this.yoff;
   }

   public void setY(int yoff) {
      float old = (float)this.yoff;
      this.yoff = yoff;
      this.firePropertyChange("y", old, yoff);
   }

   static AffineTransform getOffsetTransform(AffineTransform transform, double xoff, double yoff) {
      if (transform != null && !transform.isIdentity()) {
         AffineTransform at = new AffineTransform(transform);
         at.translate(xoff, yoff);
         return at;
      } else {
         return xoff == 0.0 && yoff == 0.0 ? null : AffineTransform.getTranslateInstance(xoff, yoff);
      }
   }

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      AffineTransform at = getOffsetTransform(transform, (double)this.xoff, (double)this.yoff);
      Effect input = this.getDefaultedInput(0, defaultInput);
      return input.getBounds(at, defaultInput);
   }

   public ImageData filter(GraphicsConfiguration config, AffineTransform transform, Effect defaultInput) {
      AffineTransform at = getOffsetTransform(transform, (double)this.xoff, (double)this.yoff);
      Effect input = this.getDefaultedInput(0, defaultInput);
      return input.filter(config, at, defaultInput);
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      p = this.getDefaultedInput(0, defaultInput).transform(p, defaultInput);
      double x = p.getX() + (double)this.xoff;
      double y = p.getY() + (double)this.yoff;
      Point2D p = new Point2D.Float();
      p.setLocation(x, y);
      return p;
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      double x = p.getX() - (double)this.xoff;
      double y = p.getY() - (double)this.yoff;
      Point2D p = new Point2D.Float();
      p.setLocation(x, y);
      p = this.getDefaultedInput(0, defaultInput).untransform(p, defaultInput);
      return p;
   }

   public Effect.AccelType getAccelType(GraphicsConfiguration config) {
      return ((Effect)this.getInputs().get(0)).getAccelType(config);
   }
}
