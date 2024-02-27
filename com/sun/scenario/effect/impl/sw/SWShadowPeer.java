package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.Shadow;
import com.sun.scenario.effect.impl.BufferUtil;
import com.sun.scenario.effect.impl.ImageData;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.FloatBuffer;

public class SWShadowPeer extends SWEffectPeer {
   private FloatBuffer kvals;

   public SWShadowPeer(GraphicsConfiguration gc) {
      super(gc);
   }

   protected final Shadow getEffect() {
      return (Shadow)super.getEffect();
   }

   private static int getPad(float radius) {
      return (int)Math.ceil((double)radius);
   }

   private int getKernelSize() {
      int r = getPad(this.getEffect().getRadius());
      return r * 2 + 1;
   }

   private FloatBuffer getKvals() {
      float radius = this.getEffect().getRadius();
      float spread = this.getEffect().getSpread();
      int r = getPad(radius);
      int klen = r * 2 + 1;
      float xoff;
      float yoff;
      if (this.getPass() == 0) {
         xoff = 1.0F / (float)this.getInputNativeBounds(0).width;
         yoff = 0.0F;
      } else {
         xoff = 0.0F;
         yoff = 1.0F / (float)this.getInputNativeBounds(0).height;
      }

      if (this.kvals == null) {
         this.kvals = BufferUtil.newFloatBuffer(508);
      }

      this.kvals.clear();
      float sigma = radius / 3.0F;
      float sigma22 = 2.0F * sigma * sigma;
      float sigmaPi2 = 6.2831855F * sigma;
      float sqrtSigmaPi2 = (float)Math.sqrt((double)sigmaPi2);
      float radius2 = radius * radius;
      float total = 0.0F;

      for(int row = -r; row <= r; ++row) {
         float distance = (float)row * (float)row;
         float kval;
         if (distance > radius2) {
            kval = 0.0F;
         } else {
            kval = (float)Math.exp((double)(-distance / sigma22)) / sqrtSigmaPi2;
         }

         this.kvals.put((float)row * xoff);
         this.kvals.put((float)row * yoff);
         this.kvals.put(kval);
         this.kvals.put(0.0F);
         total += kval;
      }

      float min = (float)Math.exp((double)(-radius2 / sigma22)) / sqrtSigmaPi2;
      total += (min - total) * spread;

      for(int i = 2; i < klen * 4; i += 4) {
         this.kvals.put(i, this.kvals.get(i) / total);
      }

      this.kvals.rewind();
      return this.kvals;
   }

   private float[] getShadowColor() {
      Color shadowcolor = this.getPass() == 0 ? Color.BLACK : this.getEffect().getColor();
      return getPremultipliedComponents(shadowcolor);
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
      int kernelSize = this.getKernelSize();
      FloatBuffer kvals_buf = this.getKvals();
      float[] kvals_arr = new float[kvals_buf.capacity()];
      kvals_buf.get(kvals_arr);
      float[] shadowColor_arr = this.getShadowColor();
      float shadowColor_x = shadowColor_arr[0];
      float shadowColor_y = shadowColor_arr[1];
      float shadowColor_z = shadowColor_arr[2];
      float shadowColor_w = shadowColor_arr[3];
      int MAX_KERNEL_SIZE = true;
      float inc0_x = (src0Rect[2] - src0Rect[0]) / (float)dstw;
      float inc0_y = (src0Rect[3] - src0Rect[1]) / (float)dsth;
      float pos0_y = src0Rect[1] + inc0_y * 0.5F;

      for(int dy = 0; dy < 0 + dsth; ++dy) {
         float pixcoord_y = (float)dy;
         int dyi = dy * dstscan;
         float pos0_x = src0Rect[0] + inc0_x * 0.5F;

         for(int dx = 0; dx < 0 + dstw; ++dx) {
            float pixcoord_x = (float)dx;
            float sum = 0.0F;

            float sample_res_w;
            float loc_tmp_y;
            for(int i = 0; i < kernelSize; ++i) {
               float loc_tmp_x = pos0_x + kvals_arr[i * 4 + 0];
               loc_tmp_y = pos0_y + kvals_arr[i * 4 + 1];
               int baseImg_tmp;
               if (loc_tmp_x >= 0.0F && loc_tmp_y >= 0.0F) {
                  int iloc_tmp_x = (int)(loc_tmp_x * (float)src0w);
                  int iloc_tmp_y = (int)(loc_tmp_y * (float)src0h);
                  boolean out = iloc_tmp_x >= src0w || iloc_tmp_y >= src0h;
                  baseImg_tmp = out ? 0 : baseImg[iloc_tmp_y * src0scan + iloc_tmp_x];
               } else {
                  baseImg_tmp = 0;
               }

               sample_res_w = (float)(baseImg_tmp >>> 24) / 255.0F;
               sum += kvals_arr[i * 4 + 2] * sample_res_w;
            }

            loc_tmp_y = 0.0F;
            float max_tmp = 1.0F;
            sample_res_w = sum < loc_tmp_y ? loc_tmp_y : (sum > max_tmp ? max_tmp : sum);
            float color_x = sample_res_w * shadowColor_x;
            float color_y = sample_res_w * shadowColor_y;
            float color_z = sample_res_w * shadowColor_z;
            float color_w = sample_res_w * shadowColor_w;
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
