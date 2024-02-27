package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

public class Reflection extends FilterEffect {
   private float topOffset;
   private float topOpacity;
   private float bottomOpacity;
   private float fraction;

   public Reflection() {
      this(DefaultInput);
   }

   public Reflection(Effect input) {
      super(input);
      this.topOffset = 0.0F;
      this.topOpacity = 0.5F;
      this.bottomOpacity = 0.0F;
      this.fraction = 0.75F;
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
   }

   public float getTopOffset() {
      return this.topOffset;
   }

   public void setTopOffset(float topOffset) {
      float old = this.topOffset;
      this.topOffset = topOffset;
      this.firePropertyChange("topOffset", old, topOffset);
   }

   public float getTopOpacity() {
      return this.topOpacity;
   }

   public void setTopOpacity(float topOpacity) {
      if (!(topOpacity < 0.0F) && !(topOpacity > 1.0F)) {
         float old = this.topOpacity;
         this.topOpacity = topOpacity;
         this.firePropertyChange("topOpacity", old, topOpacity);
      } else {
         throw new IllegalArgumentException("Top opacity must be in the range [0,1]");
      }
   }

   public float getBottomOpacity() {
      return this.bottomOpacity;
   }

   public void setBottomOpacity(float bottomOpacity) {
      if (!(bottomOpacity < 0.0F) && !(bottomOpacity > 1.0F)) {
         float old = this.bottomOpacity;
         this.bottomOpacity = bottomOpacity;
         this.firePropertyChange("bottomOpacity", old, bottomOpacity);
      } else {
         throw new IllegalArgumentException("Bottom opacity must be in the range [0,1]");
      }
   }

   public float getFraction() {
      return this.fraction;
   }

   public void setFraction(float fraction) {
      if (!(fraction < 0.0F) && !(fraction > 1.0F)) {
         float old = this.fraction;
         this.fraction = fraction;
         this.firePropertyChange("fraction", old, fraction);
      } else {
         throw new IllegalArgumentException("Fraction must be in the range [0,1]");
      }
   }

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      Effect input = this.getDefaultedInput(0, defaultInput);
      Rectangle2D r = input.getBounds((AffineTransform)null, defaultInput).getBounds();
      double refX = r.getX();
      double refY = r.getY() + r.getHeight() + (double)this.topOffset;
      double refW = r.getWidth();
      double refH = (double)this.fraction * r.getHeight();
      Rectangle2D ret = new Rectangle2D.Double(refX, refY, refW, refH);
      ret.add(r);
      return this.transformBounds(transform, ret);
   }

   public ImageData filterImageDatas(GraphicsConfiguration config, AffineTransform transform, ImageData... inputDatas) {
      Rectangle inputbounds = inputDatas[0].getBounds();
      int srcW = inputbounds.width;
      int srcH = inputbounds.height;
      float refY = (float)srcH + this.topOffset;
      float refH = this.fraction * (float)srcH;
      int irefY1 = (int)Math.floor((double)refY);
      int irefY2 = (int)Math.ceil((double)(refY + refH));
      int irefH = irefY2 - irefY1;
      Image dst = getCompatibleImage(config, srcW, irefY2);
      Image src = inputDatas[0].getImage();
      Graphics2D gdst = (Graphics2D)dst.getGraphics();
      Color c1 = new Color(1.0F, 1.0F, 1.0F, this.topOpacity);
      Color c2 = new Color(1.0F, 1.0F, 1.0F, this.bottomOpacity);
      gdst.setPaint(new GradientPaint(0.0F, refY, c1, 0.0F, refY + refH, c2, true));
      gdst.setComposite(AlphaComposite.Src);
      gdst.fillRect(0, irefY1, srcW, irefH);
      gdst.setComposite(AlphaComposite.SrcIn);
      gdst.drawImage(src, 0, irefY2, srcW, irefY1, 0, srcH - irefH, srcW, srcH, (ImageObserver)null);
      gdst.setComposite(AlphaComposite.SrcOver);
      gdst.drawImage(src, 0, 0, (ImageObserver)null);
      gdst.dispose();
      Rectangle newbounds = new Rectangle(inputbounds.x, inputbounds.y, srcW, irefY2);
      return new ImageData(config, dst, newbounds);
   }

   public boolean operatesInUserSpace() {
      return true;
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
