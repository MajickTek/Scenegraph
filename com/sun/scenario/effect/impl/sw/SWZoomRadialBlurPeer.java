package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.ZoomRadialBlur;
import com.sun.scenario.effect.impl.ImageData;
import com.sun.scenario.effect.impl.state.AccessHelper;
import com.sun.scenario.effect.impl.state.ZoomRadialBlurState;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class SWZoomRadialBlurPeer extends SWEffectPeer {
   private float[] centerTmp = new float[2];
   private float[] deltaTmp = new float[2];

   public SWZoomRadialBlurPeer(GraphicsConfiguration gc) {
      super(gc);
   }

   protected final ZoomRadialBlur getEffect() {
      return (ZoomRadialBlur)super.getEffect();
   }

   private ZoomRadialBlurState getState() {
      return (ZoomRadialBlurState)AccessHelper.getState(this.getEffect());
   }

   private float[] getCenter() {
      ZoomRadialBlurState state = this.getState();
      this.centerTmp[0] = this.getEffect().getCenterX() * state.getDx();
      this.centerTmp[1] = this.getEffect().getCenterY() * state.getDy();
      return this.centerTmp;
   }

   private float getAlpha() {
      return this.getState().getAlpha();
   }

   private int getNumSteps() {
      return this.getState().getNumSteps();
   }

   private float[] getDelta() {
      ZoomRadialBlurState state = this.getState();
      this.deltaTmp[0] = state.getDx();
      this.deltaTmp[1] = state.getDy();
      return this.deltaTmp;
   }

   private float getRadius() {
      return (float)this.getEffect().getRadius();
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
      int[] img = ((DataBufferInt)src0.getRaster().getDataBuffer()).getData();
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
      float radius = this.getRadius();
      int numSteps = this.getNumSteps();
      float[] delta_arr = this.getDelta();
      float delta_x = delta_arr[0];
      float delta_y = delta_arr[1];
      float[] center_arr = this.getCenter();
      float center_x = center_arr[0];
      float center_y = center_arr[1];
      float alpha = this.getAlpha();
      float inc0_x = (src0Rect[2] - src0Rect[0]) / (float)dstw;
      float inc0_y = (src0Rect[3] - src0Rect[1]) / (float)dsth;
      float pos0_y = src0Rect[1] + inc0_y * 0.5F;

      for(int dy = 0; dy < 0 + dsth; ++dy) {
         float pixcoord_y = (float)dy;
         int dyi = dy * dstscan;
         float pos0_x = src0Rect[0] + inc0_x * 0.5F;

         for(int dx = 0; dx < 0 + dstw; ++dx) {
            float pixcoord_x = (float)dx;
            float color_x = 0.0F;
            float color_y = 0.0F;
            float color_z = 0.0F;
            float color_w = 1.0F;
            float d_x = pos0_x - center_x;
            float d_y = pos0_y - center_y;
            float p_x = (float)Math.sqrt((double)(d_x * d_x + d_y * d_y));
            float normalize_res_y = d_y / p_x;
            float normalize_res_x = d_x / p_x;
            d_x = normalize_res_x * delta_x;
            d_y = normalize_res_y * delta_y;
            p_x = -radius * d_x;
            float p_y = -radius * d_y;

            for(int i = 0; i < numSteps; ++i) {
               p_x += d_x;
               p_y += d_y;
               float loc_tmp_x = pos0_x + p_x;
               float loc_tmp_y = pos0_y + p_y;
               int img_tmp;
               if (loc_tmp_x >= 0.0F && loc_tmp_y >= 0.0F) {
                  int iloc_tmp_x = (int)(loc_tmp_x * (float)src0w);
                  int iloc_tmp_y = (int)(loc_tmp_y * (float)src0h);
                  boolean out = iloc_tmp_x >= src0w || iloc_tmp_y >= src0h;
                  img_tmp = out ? 0 : img[iloc_tmp_y * src0scan + iloc_tmp_x];
               } else {
                  img_tmp = 0;
               }

               float sample_res_z = (float)(img_tmp & 255) / 255.0F;
               float sample_res_y = (float)(img_tmp >> 8 & 255) / 255.0F;
               float sample_res_x = (float)(img_tmp >> 16 & 255) / 255.0F;
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
            pos0_x += inc0_x;
         }

         pos0_y += inc0_y;
      }

      return new ImageData(this.getGraphicsConfig(), dst, dstBounds);
   }
}
