package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.BoxBlur;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.impl.ImageData;
import com.sun.scenario.effect.impl.state.AccessHelper;
import com.sun.scenario.effect.impl.state.BoxBlurState;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class SWBoxBlurPeer extends SWEffectPeer {
   public SWBoxBlurPeer(GraphicsConfiguration gc) {
      super(gc);
   }

   protected final BoxBlur getEffect() {
      return (BoxBlur)super.getEffect();
   }

   private BoxBlurState getState() {
      return (BoxBlurState)AccessHelper.getState(this.getEffect());
   }

   private float getRowOffset() {
      return this.getState().getRowOffset();
   }

   private float getColOffset() {
      return this.getState().getColOffset();
   }

   private float getAlpha() {
      return this.getState().getAlpha();
   }

   private int getRowCnt() {
      return this.getState().getRowCnt();
   }

   private float getDeltaX() {
      return this.getState().getDx();
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
      int[] tmp = ((DataBufferInt)src1.getRaster().getDataBuffer()).getData();
      Rectangle src1Bounds = new Rectangle(src1x, src1y, src1w, src1h);
      this.setInputBounds(1, inputs[1].getBounds());
      this.setInputNativeBounds(1, src1Bounds);
      BufferedImage src0 = (BufferedImage)inputs[0].getImage();
      int src0x = 0;
      int src0y = 0;
      int src0w = src0.getWidth();
      int src0h = src0.getHeight();
      int src0scan = src0.getWidth();
      int[] img = ((DataBufferInt)src0.getRaster().getDataBuffer()).getData();
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
      float deltaX = this.getDeltaX();
      float rowOffset = this.getRowOffset();
      int rowCnt = this.getRowCnt();
      float colOffset = this.getColOffset();
      float alpha = this.getAlpha();
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
            float color_x = 0.0F;
            float color_y = 0.0F;
            float color_z = 0.0F;
            float color_w = 1.0F;
            float p_x = -colOffset;
            float p_y = rowOffset;
            int img_tmp;
            int iloc_tmp_x;
            int iloc_tmp_y;
            boolean out;
            if (pos1_x >= 0.0F && pos1_y >= 0.0F) {
               iloc_tmp_x = (int)(pos1_x * (float)src1w);
               iloc_tmp_y = (int)(pos1_y * (float)src1h);
               out = iloc_tmp_x >= src1w || iloc_tmp_y >= src1h;
               img_tmp = out ? 0 : tmp[iloc_tmp_y * src1scan + iloc_tmp_x];
            } else {
               img_tmp = 0;
            }

            float sample_res_z = (float)(img_tmp & 255) / 255.0F;
            float sample_res_y = (float)(img_tmp >> 8 & 255) / 255.0F;
            float sample_res_x = (float)(img_tmp >> 16 & 255) / 255.0F;
            color_x += sample_res_x;
            color_y += sample_res_y;
            color_z += sample_res_z;

            for(int i = 0; i < rowCnt; ++i) {
               p_x += deltaX;
               float loc_tmp_x = pos0_x + p_x;
               float loc_tmp_y = pos0_y + p_y;
               if (loc_tmp_x >= 0.0F && loc_tmp_y >= 0.0F) {
                  iloc_tmp_x = (int)(loc_tmp_x * (float)src0w);
                  iloc_tmp_y = (int)(loc_tmp_y * (float)src0h);
                  out = iloc_tmp_x >= src0w || iloc_tmp_y >= src0h;
                  img_tmp = out ? 0 : img[iloc_tmp_y * src0scan + iloc_tmp_x];
               } else {
                  img_tmp = 0;
               }

               sample_res_z = (float)(img_tmp & 255) / 255.0F;
               sample_res_y = (float)(img_tmp >> 8 & 255) / 255.0F;
               sample_res_x = (float)(img_tmp >> 16 & 255) / 255.0F;
               color_x += alpha * sample_res_x;
               color_y += alpha * sample_res_y;
               color_z += alpha * sample_res_z;
            }

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
            pos1_x += inc1_x;
            pos0_x += inc0_x;
         }

         pos1_y += inc1_y;
         pos0_y += inc0_y;
      }

      return new ImageData(this.getGraphicsConfig(), dst, dstBounds);
   }
}
