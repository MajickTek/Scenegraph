package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.MotionBlur;
import com.sun.scenario.effect.impl.BufferUtil;
import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.FloatBuffer;

public class SWMotionBlurPeer extends SWEffectPeer {
   private FloatBuffer kvals;

   public SWMotionBlurPeer(GraphicsConfiguration gc) {
      super(gc);
   }

   protected final MotionBlur getEffect() {
      return (MotionBlur)super.getEffect();
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
      int r = getPad(radius);
      int klen = r * 2 + 1;
      Rectangle nb = this.getInputNativeBounds(0);
      float xoff = 1.0F / (float)nb.width;
      float yoff = 1.0F / (float)nb.height;
      if (this.kvals == null) {
         this.kvals = BufferUtil.newFloatBuffer(508);
      }

      this.kvals.clear();
      float angle = this.getEffect().getAngle();
      float sina = (float)Math.sin((double)angle);
      float cosa = (float)Math.cos((double)angle);
      float sigma = radius / 3.0F;
      float sigma22 = 2.0F * sigma * sigma;
      float sigmaPi2 = 6.2831855F * sigma;
      float sqrtSigmaPi2 = (float)Math.sqrt((double)sigmaPi2);
      float radius2 = radius * radius;
      float total = 0.0F;

      int row;
      for(row = -r; row <= r; ++row) {
         float distance = (float)row * (float)row;
         float kval;
         if (distance > radius2) {
            kval = 0.0F;
         } else {
            kval = (float)Math.exp((double)(-distance / sigma22)) / sqrtSigmaPi2;
         }

         float dx = cosa * (float)row;
         this.kvals.put(dx * xoff);
         float dy = sina * (float)row;
         this.kvals.put(dy * yoff);
         this.kvals.put(kval);
         this.kvals.put(0.0F);
         total += kval;
      }

      for(row = 2; row < klen * 4; row += 4) {
         this.kvals.put(row, this.kvals.get(row) / total);
      }

      this.kvals.rewind();
      return this.kvals;
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
            float sum_x = 0.0F;
            float sum_y = 0.0F;
            float sum_z = 0.0F;
            float sum_w = 0.0F;

            for(int i = 0; i < kernelSize; ++i) {
               float loc_tmp_x = pos0_x + kvals_arr[i * 4 + 0];
               float loc_tmp_y = pos0_y + kvals_arr[i * 4 + 1];
               int baseImg_tmp;
               if (loc_tmp_x >= 0.0F && loc_tmp_y >= 0.0F) {
                  int iloc_tmp_x = (int)(loc_tmp_x * (float)src0w);
                  int iloc_tmp_y = (int)(loc_tmp_y * (float)src0h);
                  boolean out = iloc_tmp_x >= src0w || iloc_tmp_y >= src0h;
                  baseImg_tmp = out ? 0 : baseImg[iloc_tmp_y * src0scan + iloc_tmp_x];
               } else {
                  baseImg_tmp = 0;
               }

               float sample_res_z = (float)(baseImg_tmp & 255) / 255.0F;
               float sample_res_y = (float)(baseImg_tmp >> 8 & 255) / 255.0F;
               float sample_res_w = (float)(baseImg_tmp >>> 24) / 255.0F;
               float sample_res_x = (float)(baseImg_tmp >> 16 & 255) / 255.0F;
               sum_x += kvals_arr[i * 4 + 2] * sample_res_x;
               sum_y += kvals_arr[i * 4 + 2] * sample_res_y;
               sum_z += kvals_arr[i * 4 + 2] * sample_res_z;
               sum_w += kvals_arr[i * 4 + 2] * sample_res_w;
            }

            float color_x = sum_x;
            float color_y = sum_y;
            float color_z = sum_z;
            float color_w = sum_w;
            if (sum_x < 0.0F) {
               color_x = 0.0F;
            } else if (sum_x > 1.0F) {
               color_x = 1.0F;
            }

            if (sum_y < 0.0F) {
               color_y = 0.0F;
            } else if (sum_y > 1.0F) {
               color_y = 1.0F;
            }

            if (sum_z < 0.0F) {
               color_z = 0.0F;
            } else if (sum_z > 1.0F) {
               color_z = 1.0F;
            }

            if (sum_w < 0.0F) {
               color_w = 0.0F;
            } else if (sum_w > 1.0F) {
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
