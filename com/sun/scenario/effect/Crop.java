package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

class Crop extends Effect {
   public Crop(Effect source) {
      this(source, DefaultInput);
   }

   public Crop(Effect source, Effect boundsInput) {
      super(source, boundsInput);
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
   }

   public final Effect getBoundsInput() {
      return (Effect)this.getInputs().get(1);
   }

   public void setBoundsInput(Effect input) {
      this.setInput(1, input);
   }

   private Effect getBoundsInput(Effect defaultInput) {
      return this.getDefaultedInput(1, defaultInput);
   }

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      return this.getBoundsInput(defaultInput).getBounds(transform, defaultInput);
   }

   public ImageData filter(GraphicsConfiguration config, AffineTransform transform, Effect defaultInput) {
      Rectangle clipBounds = this.getBounds(transform, defaultInput).getBounds();
      int w = clipBounds.width;
      int h = clipBounds.height;
      Image dst = getCompatibleImage(config, w, h);
      Effect input = (Effect)this.getInputs().get(0);
      ImageData srcData = input.filter(config, transform, defaultInput);
      Image src = srcData.getImage();
      Rectangle inputBounds = srcData.getBounds();
      int sx = clipBounds.x - inputBounds.x;
      int sy = clipBounds.y - inputBounds.y;
      Graphics2D gdst = (Graphics2D)dst.getGraphics();
      gdst.drawImage(src, 0, 0, w, h, sx, sy, sx + w, sy + h, (ImageObserver)null);
      gdst.dispose();
      srcData.unref();
      return new ImageData(config, dst, clipBounds);
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(0, defaultInput).transform(p, defaultInput);
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(0, defaultInput).untransform(p, defaultInput);
   }

   public Effect.AccelType getAccelType(GraphicsConfiguration config) {
      return Effect.AccelType.INTRINSIC;
   }
}
