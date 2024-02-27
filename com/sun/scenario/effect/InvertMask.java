package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

public class InvertMask extends FilterEffect {
   private int pad;

   public InvertMask() {
      this(10);
   }

   public InvertMask(Effect input) {
      this(10, input);
   }

   public InvertMask(int pad) {
      this(pad, DefaultInput);
   }

   public InvertMask(int pad, Effect input) {
      super(input);
      this.setPad(pad);
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
   }

   public int getPad() {
      return this.pad;
   }

   public void setPad(int pad) {
      if (pad < 0) {
         throw new IllegalArgumentException("Pad value must be non-negative");
      } else {
         int old = this.pad;
         this.pad = pad;
         this.firePropertyChange("pad", old, pad);
      }
   }

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      Rectangle2D r = super.getBounds((AffineTransform)null, defaultInput);
      Rectangle2D ret = new Rectangle2D.Float();
      int pad2 = 2 * this.pad;
      ((Rectangle2D)ret).setFrame(r.getX() - (double)this.pad, r.getY() - (double)this.pad, r.getWidth() + (double)pad2, r.getHeight() + (double)pad2);
      if (transform != null && !transform.isIdentity()) {
         ret = this.transformBounds(transform, (Rectangle2D)ret);
      }

      return (Rectangle2D)ret;
   }

   public ImageData filterImageDatas(GraphicsConfiguration config, AffineTransform transform, ImageData... inputDatas) {
      Rectangle inputBounds = inputDatas[0].getBounds();
      Rectangle dstBounds = new Rectangle(inputBounds);
      dstBounds.grow(this.pad, this.pad);
      int w = dstBounds.width;
      int h = dstBounds.height;
      Image dst = getCompatibleImage(config, w, h);
      Image src = inputDatas[0].getImage();
      int iw = inputBounds.width;
      int ih = inputBounds.height;
      int dx = this.pad;
      int dy = this.pad;
      int sx = 0;
      int sy = 0;
      Graphics2D gdst = (Graphics2D)dst.getGraphics();
      gdst.setColor(Color.WHITE);
      gdst.fillRect(0, 0, w, h);
      gdst.setComposite(AlphaComposite.DstOut);
      gdst.drawImage(src, dx, dy, dx + iw, dy + ih, sx, sy, sx + iw, sx + ih, (ImageObserver)null);
      gdst.dispose();
      return new ImageData(config, dst, dstBounds);
   }

   public Effect.AccelType getAccelType(GraphicsConfiguration config) {
      return Effect.AccelType.INTRINSIC;
   }
}
