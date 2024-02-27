package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.EffectPeer;
import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public abstract class AbstractGaussian extends CoreEffect {
   private float radius;
   private final String prefix;

   AbstractGaussian(String prefix, float radius, Effect input) {
      super(input);
      this.prefix = prefix;
      this.setRadius(radius);
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
   }

   public float getRadius() {
      return this.radius;
   }

   public void setRadius(float radius) {
      if (!(radius < 1.0F) && !(radius > 63.0F)) {
         float old = this.radius;
         this.radius = radius;
         this.firePropertyChange("radius", old, radius);
         this.updatePeer();
      } else {
         throw new IllegalArgumentException("Radius must be in the range [1,63]");
      }
   }

   private int getPad() {
      return (int)Math.ceil((double)this.getRadius());
   }

   private int getKernelSize() {
      int r = this.getPad();
      return r * 2 + 1;
   }

   private void updatePeer() {
      int ksize = this.getKernelSize();
      int psize = ksize + (10 - ksize % 10);
      this.updatePeerKey(this.prefix, psize);
   }

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      Rectangle2D r = super.getBounds((AffineTransform)null, defaultInput);
      int pad = this.getPad();
      int pad2 = 2 * pad;
      Rectangle2D ret = new Rectangle2D.Float();
      ret.setFrame(r.getX() - (double)pad, r.getY() - (double)pad, r.getWidth() + (double)pad2, r.getHeight() + (double)pad2);
      return this.transformBounds(transform, ret);
   }

   public Rectangle getResultBounds(AffineTransform transform, ImageData... inputDatas) {
      Rectangle r = super.getResultBounds(transform, inputDatas);
      int pad = this.getPad();
      Rectangle ret = new Rectangle(r);
      ret.grow(pad, pad);
      return ret;
   }

   public ImageData filterImageDatas(GraphicsConfiguration config, AffineTransform transform, ImageData... inputs) {
      EffectPeer peer = this.getPeer(config);
      peer.setPass(0);
      ImageData res0 = peer.filter(this, transform, inputs);
      peer.setPass(1);
      ImageData res1 = peer.filter(this, transform, res0);
      res0.unref();
      return res1;
   }

   public boolean operatesInUserSpace() {
      return true;
   }
}
