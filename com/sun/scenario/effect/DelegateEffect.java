package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class DelegateEffect extends Effect {
   protected DelegateEffect(Effect input) {
      super(input);
   }

   protected DelegateEffect(Effect input1, Effect input2) {
      super(input1, input2);
   }

   protected abstract Effect getDelegate();

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      return this.getDelegate().getBounds(transform, defaultInput);
   }

   public ImageData filter(GraphicsConfiguration config, AffineTransform transform, Effect defaultInput) {
      return this.getDelegate().filter(config, transform, defaultInput);
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      return this.getDelegate().untransform(p, defaultInput);
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      return this.getDelegate().transform(p, defaultInput);
   }

   public Effect.AccelType getAccelType(GraphicsConfiguration config) {
      return this.getDelegate().getAccelType(config);
   }
}
