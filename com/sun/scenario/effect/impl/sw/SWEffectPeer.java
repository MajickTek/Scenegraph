package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.impl.EffectPeer;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;

public abstract class SWEffectPeer extends EffectPeer {
   protected SWEffectPeer(GraphicsConfiguration gc) {
      super(gc);
   }

   protected BufferedImage getDestImageFromPool(int w, int h) {
      return (BufferedImage)this.getRenderer().getCompatibleImage(w, h);
   }

   public Effect.AccelType getAccelType() {
      return Effect.AccelType.NONE;
   }

   protected final void accum(int pixel, float mul, float[] fvals) {
      mul /= 255.0F;
      fvals[0] += (float)(pixel >> 16 & 255) * mul;
      fvals[1] += (float)(pixel >> 8 & 255) * mul;
      fvals[2] += (float)(pixel & 255) * mul;
      fvals[3] += (float)(pixel >>> 24) * mul;
   }

   protected final void lsample(int[] img, float floc_x, float floc_y, int w, int h, int scan, float[] fvals) {
      fvals[0] = 0.0F;
      fvals[1] = 0.0F;
      fvals[2] = 0.0F;
      fvals[3] = 0.0F;
      if (floc_x >= 0.0F && floc_y >= 0.0F && floc_x < 1.0F && floc_y < 1.0F) {
         floc_x = floc_x * (float)w + 0.5F;
         floc_y = floc_y * (float)h + 0.5F;
         int iloc_x = (int)floc_x;
         int iloc_y = (int)floc_y;
         floc_x -= (float)iloc_x;
         floc_y -= (float)iloc_y;
         int offset = iloc_y * scan + iloc_x;
         float fract = floc_x * floc_y;
         if (iloc_y < h) {
            if (iloc_x < w) {
               this.accum(img[offset], fract, fvals);
            }

            if (iloc_x > 0) {
               this.accum(img[offset - 1], floc_y - fract, fvals);
            }
         }

         if (iloc_y > 0) {
            if (iloc_x < w) {
               this.accum(img[offset - scan], floc_x - fract, fvals);
            }

            if (iloc_x > 0) {
               this.accum(img[offset - scan - 1], 1.0F - floc_x - floc_y + fract, fvals);
            }
         }
      }

   }
}
