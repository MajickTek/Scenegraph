package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.Brightpass;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class SWBrightpassPeer extends SWEffectPeer {
   public SWBrightpassPeer(GraphicsConfiguration gc) {
      super(gc);
   }

   protected final Brightpass getEffect() {
      return (Brightpass)super.getEffect();
   }

   private float getThreshold() {
      return this.getEffect().getThreshold();
   }

   public ImageData filter(Effect effect, AffineTransform transform, ImageData... inputs) {
      this.setEffect(effect);
      this.setDestBounds(effect.getResultBounds(transform, inputs));
      BufferedImage src0 = (BufferedImage)inputs[0].getImage();
      int src0x = 0;
      int src0y = 0;
      int src0w = src0.getWidth();
      int src0h = src0.getHeight();
      int src0scan = src0.getWidth();
      int[] baseImg = ((DataBufferInt)src0.getRaster().getDataBuffer()).getData();
      Rectangle src0Bounds = new Rectangle(src0x, src0y, src0w, src0h);
      this.setInputBounds(0, inputs[0].getBounds());
      this.setInputNativeBounds(0, src0Bounds);
      float[] src0Rect = this.getSourceRegion(0);
      Rectangle dstBounds = this.getDestBounds();
      int dstx = false;
      int dsty = false;
      int dstw = dstBounds.width;
      int dsth = dstBounds.height;
      BufferedImage dst = this.getDestImageFromPool(dstw, dsth);
      this.setDestNativeBounds(dst.getWidth(), dst.getHeight());
      int dstscan = dst.getWidth();
      int[] dstPixels = ((DataBufferInt)dst.getRaster().getDataBuffer()).getData();
      float threshold = this.getThreshold();
      float inc0_x = (src0Rect[2] - src0Rect[0]) / (float)dstw;
      float inc0_y = (src0Rect[3] - src0Rect[1]) / (float)dsth;
      float pos0_y = src0Rect[1] + inc0_y * 0.5F;

      for(int dy = 0; dy < 0 + dsth; ++dy) {
         float pixcoord_y = (float)dy;
         int dyi = dy * dstscan;
         float pos0_x = src0Rect[0] + inc0_x * 0.5F;

         for(int dx = 0; dx < 0 + dstw; ++dx) {
            float pixcoord_x = (float)dx;
            float luminanceVector_x = 0.2125F;
            float luminanceVector_y = 0.7154F;
            float luminanceVector_z = 0.0721F;
            int baseImg_tmp;
            if (pos0_x >= 0.0F && pos0_y >= 0.0F) {
               int iloc_tmp_x = (int)(pos0_x * (float)src0w);
               int iloc_tmp_y = (int)(pos0_y * (float)src0h);
               boolean out = iloc_tmp_x >= src0w || iloc_tmp_y >= src0h;
               baseImg_tmp = out ? 0 : baseImg[iloc_tmp_y * src0scan + iloc_tmp_x];
            } else {
               baseImg_tmp = 0;
            }

            float sample_res_z = (float)(baseImg_tmp & 255) / 255.0F;
            float sample_res_y = (float)(baseImg_tmp >> 8 & 255) / 255.0F;
            float sample_res_x = (float)(baseImg_tmp >> 16 & 255) / 255.0F;
            float dot_res = luminanceVector_x * sample_res_x + luminanceVector_y * sample_res_y + luminanceVector_z * sample_res_z;
            float sign_res = 0.0F;
            float y_tmp = dot_res - threshold;
            float max_res = sign_res > y_tmp ? sign_res : y_tmp;
            sign_res = Math.signum(max_res);
            float color_x = sample_res_x * sign_res;
            float color_y = sample_res_y * sign_res;
            float color_z = sample_res_z * sign_res;
            float color_w = 1.0F;
            if (color_x < 0.0F) {
               color_x = 0.0F;
            } else if (color_x > 1.0F) {
               color_x = 1.0F;
            }

            if (color_y < 0.0F) {
               color_y = 0.0F;
            } else if (color_y > 1.0F) {
               color_y = 1.0F;
            }

            if (color_z < 0.0F) {
               color_z = 0.0F;
            } else if (color_z > 1.0F) {
               color_z = 1.0F;
            }

            if (color_w < 0.0F) {
               color_w = 0.0F;
            } else if (color_w > 1.0F) {
               color_w = 1.0F;
            }

            dstPixels[dyi + dx] = (int)(color_x * 255.0F) << 16 | (int)(color_y * 255.0F) << 8 | (int)(color_z * 255.0F) << 0 | (int)(color_w * 255.0F + 0.5F) << 24;
            pos0_x += inc0_x;
         }

         pos0_y += inc0_y;
      }

      return new ImageData(this.getGraphicsConfig(), dst, dstBounds);
   }
}
