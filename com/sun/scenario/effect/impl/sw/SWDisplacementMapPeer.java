package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.DisplacementMap;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.FloatMap;
import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class SWDisplacementMapPeer extends SWEffectPeer {
   public SWDisplacementMapPeer(GraphicsConfiguration gc) {
      super(gc);
   }

   protected final DisplacementMap getEffect() {
      return (DisplacementMap)super.getEffect();
   }

   private float getScalex() {
      return this.getEffect().getScaleX();
   }

   private float getScaley() {
      return this.getEffect().getScaleY();
   }

   private float getOffsetx() {
      return this.getEffect().getOffsetX();
   }

   private float getOffsety() {
      return this.getEffect().getOffsetY();
   }

   private float getWrap() {
      return this.getEffect().getWrap() ? 1.0F : 0.0F;
   }

   protected Object getSamplerData(int i) {
      return this.getEffect().getMapData();
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
      int[] origImg = ((DataBufferInt)src0.getRaster().getDataBuffer()).getData();
      Rectangle src0Bounds = new Rectangle(src0x, src0y, src0w, src0h);
      this.setInputBounds(0, inputs[0].getBounds());
      this.setInputNativeBounds(0, src0Bounds);
      float[] origImg_vals = new float[4];
      FloatMap src1 = (FloatMap)this.getSamplerData(1);
      int src1x = false;
      int src1y = false;
      int src1w = src1.getWidth();
      int src1h = src1.getHeight();
      int src1scan = src1.getWidth();
      float[] mapImg = src1.getData();
      float[] src0Rect = this.getSourceRegion(0);
      float[] src1Rect = new float[]{0.0F, 0.0F, 1.0F, 1.0F};
      Rectangle dstBounds = this.getDestBounds();
      int dstx = false;
      int dsty = false;
      int dstw = dstBounds.width;
      int dsth = dstBounds.height;
      BufferedImage dst = this.getDestImageFromPool(dstw, dsth);
      this.setDestNativeBounds(dst.getWidth(), dst.getHeight());
      int dstscan = dst.getWidth();
      int[] dstPixels = ((DataBufferInt)dst.getRaster().getDataBuffer()).getData();
      float scalex = this.getScalex();
      float offsety = this.getOffsety();
      float scaley = this.getScaley();
      float wrap = this.getWrap();
      float offsetx = this.getOffsetx();
      float inc0_x = (src0Rect[2] - src0Rect[0]) / (float)dstw;
      float inc0_y = (src0Rect[3] - src0Rect[1]) / (float)dsth;
      float inc1_x = (src1Rect[2] - src1Rect[0]) / (float)dstw;
      float inc1_y = (src1Rect[3] - src1Rect[1]) / (float)dsth;
      float pos0_y = src0Rect[1] + inc0_y * 0.5F;
      float pos1_y = src1Rect[1] + inc1_y * 0.5F;

      for(int dy = 0; dy < 0 + dsth; ++dy) {
         float pixcoord_y = (float)dy;
         int dyi = dy * dstscan;
         float pos0_x = src0Rect[0] + inc0_x * 0.5F;
         float pos1_x = src1Rect[0] + inc1_x * 0.5F;

         for(int dx = 0; dx < 0 + dstw; ++dx) {
            float pixcoord_x = (float)dx;
            float[] mapImg_arr_tmp = null;
            int iloc_tmp = 0;
            if (pos0_x >= 0.0F && pos0_y >= 0.0F) {
               int iloc_tmp_x = (int)(pos0_x * (float)src1w);
               int iloc_tmp_y = (int)(pos0_y * (float)src1h);
               boolean out = iloc_tmp_x >= src1w || iloc_tmp_y >= src1h;
               if (!out) {
                  mapImg_arr_tmp = mapImg;
                  iloc_tmp = 4 * (iloc_tmp_y * src1scan + iloc_tmp_x);
               }
            }

            float sample_res_z = mapImg_arr_tmp == null ? 0.0F : mapImg_arr_tmp[iloc_tmp + 2];
            float sample_res_y = mapImg_arr_tmp == null ? 0.0F : mapImg_arr_tmp[iloc_tmp + 1];
            float var10000 = mapImg_arr_tmp == null ? 0.0F : mapImg_arr_tmp[iloc_tmp + 3];
            float sample_res_x = mapImg_arr_tmp == null ? 0.0F : mapImg_arr_tmp[iloc_tmp];
            float loc_x = pos1_x + scalex * (sample_res_x + offsetx);
            float loc_y = pos1_y + scaley * (sample_res_y + offsety);
            float floor_res = (float)Math.floor((double)loc_x);
            loc_x -= wrap * floor_res;
            floor_res = (float)Math.floor((double)loc_y);
            loc_y -= wrap * floor_res;
            this.lsample(origImg, loc_x, loc_y, src0w, src0h, src0scan, origImg_vals);
            sample_res_z = origImg_vals[2];
            sample_res_y = origImg_vals[1];
            float sample_res_w = origImg_vals[3];
            sample_res_x = origImg_vals[0];
            float color_x = sample_res_x;
            float color_y = sample_res_y;
            float color_z = sample_res_z;
            float color_w = sample_res_w;
            if (sample_res_x < 0.0F) {
               color_x = 0.0F;
            } else if (sample_res_x > 1.0F) {
               color_x = 1.0F;
            }

            if (sample_res_y < 0.0F) {
               color_y = 0.0F;
            } else if (sample_res_y > 1.0F) {
               color_y = 1.0F;
            }

            if (sample_res_z < 0.0F) {
               color_z = 0.0F;
            } else if (sample_res_z > 1.0F) {
               color_z = 1.0F;
            }

            if (sample_res_w < 0.0F) {
               color_w = 0.0F;
            } else if (sample_res_w > 1.0F) {
               color_w = 1.0F;
            }

            dstPixels[dyi + dx] = (int)(color_x * 255.0F) << 16 | (int)(color_y * 255.0F) << 8 | (int)(color_z * 255.0F) << 0 | (int)(color_w * 255.0F + 0.5F) << 24;
            pos0_x += inc0_x;
            pos1_x += inc1_x;
         }

         pos0_y += inc0_y;
         pos1_y += inc1_y;
      }

      return new ImageData(this.getGraphicsConfig(), dst, dstBounds);
   }
}
