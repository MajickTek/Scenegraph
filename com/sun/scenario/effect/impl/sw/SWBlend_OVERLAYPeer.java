package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.Blend;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class SWBlend_OVERLAYPeer extends SWEffectPeer {
   public SWBlend_OVERLAYPeer(GraphicsConfiguration gc) {
      super(gc);
   }

   protected final Blend getEffect() {
      return (Blend)super.getEffect();
   }

   private float getOpacity() {
      return this.getEffect().getOpacity();
   }

   public ImageData filter(Effect effect, AffineTransform transform, ImageData... inputs) {
      this.setEffect(effect);
      this.setDestBounds(effect.getResultBounds(transform, inputs));
      BufferedImage src1 = (BufferedImage)inputs[1].getImage();
      int src1x = 0;
      int src1y = 0;
      int src1w = src1.getWidth();
      int src1h = src1.getHeight();
      int src1scan = src1.getWidth();
      int[] topImg = ((DataBufferInt)src1.getRaster().getDataBuffer()).getData();
      Rectangle src1Bounds = new Rectangle(src1x, src1y, src1w, src1h);
      this.setInputBounds(1, inputs[1].getBounds());
      this.setInputNativeBounds(1, src1Bounds);
      BufferedImage src0 = (BufferedImage)inputs[0].getImage();
      int src0x = 0;
      int src0y = 0;
      int src0w = src0.getWidth();
      int src0h = src0.getHeight();
      int src0scan = src0.getWidth();
      int[] botImg = ((DataBufferInt)src0.getRaster().getDataBuffer()).getData();
      Rectangle src0Bounds = new Rectangle(src0x, src0y, src0w, src0h);
      this.setInputBounds(0, inputs[0].getBounds());
      this.setInputNativeBounds(0, src0Bounds);
      float[] src1Rect = this.getSourceRegion(1);
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
      float opacity = this.getOpacity();
      float inc1_x = (src1Rect[2] - src1Rect[0]) / (float)dstw;
      float inc1_y = (src1Rect[3] - src1Rect[1]) / (float)dsth;
      float inc0_x = (src0Rect[2] - src0Rect[0]) / (float)dstw;
      float inc0_y = (src0Rect[3] - src0Rect[1]) / (float)dsth;
      float pos1_y = src1Rect[1] + inc1_y * 0.5F;
      float pos0_y = src0Rect[1] + inc0_y * 0.5F;

      for(int dy = 0; dy < 0 + dsth; ++dy) {
         float pixcoord_y = (float)dy;
         int dyi = dy * dstscan;
         float pos1_x = src1Rect[0] + inc1_x * 0.5F;
         float pos0_x = src0Rect[0] + inc0_x * 0.5F;

         for(int dx = 0; dx < 0 + dstw; ++dx) {
            float pixcoord_x = (float)dx;
            int botImg_tmp;
            if (pos0_x >= 0.0F && pos0_y >= 0.0F) {
               int iloc_tmp_x = (int)(pos0_x * (float)src0w);
               int iloc_tmp_y = (int)(pos0_y * (float)src0h);
               boolean out = iloc_tmp_x >= src0w || iloc_tmp_y >= src0h;
               botImg_tmp = out ? 0 : botImg[iloc_tmp_y * src0scan + iloc_tmp_x];
            } else {
               botImg_tmp = 0;
            }

            float sample_res_z = (float)(botImg_tmp & 255) / 255.0F;
            float sample_res_y = (float)(botImg_tmp >> 8 & 255) / 255.0F;
            float sample_res_w = (float)(botImg_tmp >>> 24) / 255.0F;
            float sample_res_x = (float)(botImg_tmp >> 16 & 255) / 255.0F;
            float bot_x = sample_res_x;
            float bot_y = sample_res_y;
            float bot_z = sample_res_z;
            float bot_w = sample_res_w;
            int topImg_tmp;
            if (pos1_x >= 0.0F && pos1_y >= 0.0F) {
               int iloc_tmp_x = (int)(pos1_x * (float)src1w);
               int iloc_tmp_y = (int)(pos1_y * (float)src1h);
               boolean out = iloc_tmp_x >= src1w || iloc_tmp_y >= src1h;
               topImg_tmp = out ? 0 : topImg[iloc_tmp_y * src1scan + iloc_tmp_x];
            } else {
               topImg_tmp = 0;
            }

            sample_res_z = (float)(topImg_tmp & 255) / 255.0F;
            sample_res_y = (float)(topImg_tmp >> 8 & 255) / 255.0F;
            sample_res_w = (float)(topImg_tmp >>> 24) / 255.0F;
            sample_res_x = (float)(topImg_tmp >> 16 & 255) / 255.0F;
            float top_x = sample_res_x * opacity;
            float top_y = sample_res_y * opacity;
            float top_z = sample_res_z * opacity;
            float top_w = sample_res_w * opacity;
            float res_w = bot_w + top_w - bot_w * top_w;
            float x_tmp_x = bot_x - bot_w * 0.5F;
            float x_tmp_y = bot_y - bot_w * 0.5F;
            float x_tmp_z = bot_z - bot_w * 0.5F;
            float ceil_res_z = (float)Math.ceil((double)x_tmp_z);
            float ceil_res_y = (float)Math.ceil((double)x_tmp_y);
            float ceil_res_x = (float)Math.ceil((double)x_tmp_x);
            float adjbot_x = bot_x - ceil_res_x * bot_w;
            float adjbot_y = bot_y - ceil_res_y * bot_w;
            float adjbot_z = bot_z - ceil_res_z * bot_w;
            float abs_res_z = Math.abs(adjbot_z);
            float abs_res_y = Math.abs(adjbot_y);
            float abs_res_x = Math.abs(adjbot_x);
            adjbot_x = abs_res_x;
            adjbot_y = abs_res_y;
            adjbot_z = abs_res_z;
            float x_tmp_x = top_x - ceil_res_x * top_w;
            float x_tmp_y = top_y - ceil_res_y * top_w;
            float x_tmp_z = top_z - ceil_res_z * top_w;
            abs_res_z = Math.abs(x_tmp_z);
            abs_res_y = Math.abs(x_tmp_y);
            abs_res_x = Math.abs(x_tmp_x);
            float res_x = (2.0F * adjbot_x + 1.0F - bot_w) * abs_res_x + (1.0F - top_w) * adjbot_x;
            float res_y = (2.0F * adjbot_y + 1.0F - bot_w) * abs_res_y + (1.0F - top_w) * adjbot_y;
            float res_z = (2.0F * adjbot_z + 1.0F - bot_w) * abs_res_z + (1.0F - top_w) * adjbot_z;
            float x_tmp_x = res_x - ceil_res_x * res_w;
            float x_tmp_y = res_y - ceil_res_y * res_w;
            float x_tmp_z = res_z - ceil_res_z * res_w;
            abs_res_z = Math.abs(x_tmp_z);
            abs_res_y = Math.abs(x_tmp_y);
            abs_res_x = Math.abs(x_tmp_x);
            float color_x = abs_res_x;
            float color_y = abs_res_y;
            float color_z = abs_res_z;
            float color_w = res_w;
            if (abs_res_x < 0.0F) {
               color_x = 0.0F;
            } else if (abs_res_x > 1.0F) {
               color_x = 1.0F;
            }

            if (abs_res_y < 0.0F) {
               color_y = 0.0F;
            } else if (abs_res_y > 1.0F) {
               color_y = 1.0F;
            }

            if (abs_res_z < 0.0F) {
               color_z = 0.0F;
            } else if (abs_res_z > 1.0F) {
               color_z = 1.0F;
            }

            if (res_w < 0.0F) {
               color_w = 0.0F;
            } else if (res_w > 1.0F) {
               color_w = 1.0F;
            }

            dstPixels[dyi + dx] = (int)(color_x * 255.0F) << 16 | (int)(color_y * 255.0F) << 8 | (int)(color_z * 255.0F) << 0 | (int)(color_w * 255.0F + 0.5F) << 24;
            pos1_x += inc1_x;
            pos0_x += inc0_x;
         }

         pos1_y += inc1_y;
         pos0_y += inc0_y;
      }

      return new ImageData(this.getGraphicsConfig(), dst, dstBounds);
   }
}
