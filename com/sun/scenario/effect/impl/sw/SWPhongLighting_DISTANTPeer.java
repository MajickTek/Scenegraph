package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.PhongLighting;
import com.sun.scenario.effect.impl.BufferUtil;
import com.sun.scenario.effect.impl.ImageData;
import com.sun.scenario.effect.light.PointLight;
import com.sun.scenario.effect.light.SpotLight;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.FloatBuffer;

public class SWPhongLighting_DISTANTPeer extends SWEffectPeer {
   private FloatBuffer kvals;

   public SWPhongLighting_DISTANTPeer(GraphicsConfiguration gc) {
      super(gc);
   }

   protected final PhongLighting getEffect() {
      return (PhongLighting)super.getEffect();
   }

   private float getSurfaceScale() {
      return this.getEffect().getSurfaceScale();
   }

   private float getDiffuseConstant() {
      return this.getEffect().getDiffuseConstant();
   }

   private float getSpecularConstant() {
      return this.getEffect().getSpecularConstant();
   }

   private float getSpecularExponent() {
      return this.getEffect().getSpecularExponent();
   }

   private float[] getNormalizedLightPosition() {
      return this.getEffect().getLight().getNormalizedLightPosition();
   }

   private float[] getLightPosition() {
      PointLight plight = (PointLight)this.getEffect().getLight();
      return new float[]{plight.getX(), plight.getY(), plight.getZ()};
   }

   private float[] getLightColor() {
      return this.getEffect().getLight().getColor().getComponents((float[])null);
   }

   private float getLightSpecularExponent() {
      return ((SpotLight)this.getEffect().getLight()).getSpecularExponent();
   }

   private float[] getNormalizedLightDirection() {
      return ((SpotLight)this.getEffect().getLight()).getNormalizedLightDirection();
   }

   private FloatBuffer getKvals() {
      Rectangle bumpImgBounds = this.getInputNativeBounds(0);
      float xoff = 1.0F / (float)bumpImgBounds.width;
      float yoff = 1.0F / (float)bumpImgBounds.height;
      float[] kx = new float[]{-1.0F, 0.0F, 1.0F, -2.0F, 0.0F, 2.0F, -1.0F, 0.0F, 1.0F};
      float[] ky = new float[]{-1.0F, -2.0F, -1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F};
      if (this.kvals == null) {
         this.kvals = BufferUtil.newFloatBuffer(36);
      }

      this.kvals.clear();
      int kidx = 0;

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            this.kvals.put((float)j * xoff);
            this.kvals.put((float)i * yoff);
            this.kvals.put(kx[kidx]);
            this.kvals.put(ky[kidx]);
            ++kidx;
         }
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
      int[] bumpImg = ((DataBufferInt)src0.getRaster().getDataBuffer()).getData();
      Rectangle src0Bounds = new Rectangle(src0x, src0y, src0w, src0h);
      this.setInputBounds(0, inputs[0].getBounds());
      this.setInputNativeBounds(0, src0Bounds);
      BufferedImage src1 = (BufferedImage)inputs[1].getImage();
      int src1x = 0;
      int src1y = 0;
      int src1w = src1.getWidth();
      int src1h = src1.getHeight();
      int src1scan = src1.getWidth();
      int[] origImg = ((DataBufferInt)src1.getRaster().getDataBuffer()).getData();
      Rectangle src1Bounds = new Rectangle(src1x, src1y, src1w, src1h);
      this.setInputBounds(1, inputs[1].getBounds());
      this.setInputNativeBounds(1, src1Bounds);
      float[] src0Rect = this.getSourceRegion(0);
      float[] src1Rect = this.getSourceRegion(1);
      Rectangle dstBounds = this.getDestBounds();
      int dstx = false;
      int dsty = false;
      int dstw = dstBounds.width;
      int dsth = dstBounds.height;
      BufferedImage dst = this.getDestImageFromPool(dstw, dsth);
      this.setDestNativeBounds(dst.getWidth(), dst.getHeight());
      int dstscan = dst.getWidth();
      int[] dstPixels = ((DataBufferInt)dst.getRaster().getDataBuffer()).getData();
      float specularConstant = this.getSpecularConstant();
      float diffuseConstant = this.getDiffuseConstant();
      FloatBuffer kvals_buf = this.getKvals();
      float[] kvals_arr = new float[kvals_buf.capacity()];
      kvals_buf.get(kvals_arr);
      float[] normalizedLightPosition_arr = this.getNormalizedLightPosition();
      float normalizedLightPosition_x = normalizedLightPosition_arr[0];
      float normalizedLightPosition_y = normalizedLightPosition_arr[1];
      float normalizedLightPosition_z = normalizedLightPosition_arr[2];
      float[] lightColor_arr = this.getLightColor();
      float lightColor_x = lightColor_arr[0];
      float lightColor_y = lightColor_arr[1];
      float lightColor_z = lightColor_arr[2];
      float surfaceScale = this.getSurfaceScale();
      float specularExponent = this.getSpecularExponent();
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
            int origImg_tmp;
            int i;
            if (pos1_x >= 0.0F && pos1_y >= 0.0F) {
               int iloc_tmp_x = (int)(pos1_x * (float)src1w);
               i = (int)(pos1_y * (float)src1h);
               boolean out = iloc_tmp_x >= src1w || i >= src1h;
               origImg_tmp = out ? 0 : origImg[i * src1scan + iloc_tmp_x];
            } else {
               origImg_tmp = 0;
            }

            float sample_res_z = (float)(origImg_tmp & 255) / 255.0F;
            float sample_res_y = (float)(origImg_tmp >> 8 & 255) / 255.0F;
            float sample_res_w = (float)(origImg_tmp >>> 24) / 255.0F;
            float sample_res_x = (float)(origImg_tmp >> 16 & 255) / 255.0F;
            float sum_x = 0.0F;
            float sum_y = 0.0F;

            float loc_tmp_x;
            float loc_tmp_y;
            for(i = 0; i < 9; ++i) {
               loc_tmp_x = pos0_x + kvals_arr[i * 4 + 0];
               loc_tmp_y = pos0_y + kvals_arr[i * 4 + 1];
               int bumpImg_tmp;
               if (loc_tmp_x >= 0.0F && loc_tmp_y >= 0.0F) {
                  int iloc_tmp_x = (int)(loc_tmp_x * (float)src0w);
                  int iloc_tmp_y = (int)(loc_tmp_y * (float)src0h);
                  boolean out = iloc_tmp_x >= src0w || iloc_tmp_y >= src0h;
                  bumpImg_tmp = out ? 0 : bumpImg[iloc_tmp_y * src0scan + iloc_tmp_x];
               } else {
                  bumpImg_tmp = 0;
               }

               sample_res_w = (float)(bumpImg_tmp >>> 24) / 255.0F;
               sum_x += kvals_arr[i * 4 + 2] * sample_res_w;
               sum_y += kvals_arr[i * 4 + 3] * sample_res_w;
            }

            loc_tmp_x = 0.25F;
            sum_x *= -surfaceScale * loc_tmp_x;
            sum_y *= -surfaceScale * loc_tmp_x;
            float N_z = 1.0F;
            float denom = (float)Math.sqrt((double)(sum_x * sum_x + sum_y * sum_y + N_z * N_z));
            loc_tmp_y = N_z / denom;
            float normalize_res_y = sum_y / denom;
            float normalize_res_x = sum_x / denom;
            float N_x = normalize_res_x;
            float N_y = normalize_res_y;
            N_z = loc_tmp_y;
            float E_x = 0.0F;
            float E_y = 0.0F;
            float E_z = 1.0F;
            float x_tmp_x = normalizedLightPosition_x + E_x;
            float x_tmp_y = normalizedLightPosition_y + E_y;
            float x_tmp_z = normalizedLightPosition_z + E_z;
            float D_x = (float)Math.sqrt((double)(x_tmp_x * x_tmp_x + x_tmp_y * x_tmp_y + x_tmp_z * x_tmp_z));
            loc_tmp_y = x_tmp_z / D_x;
            normalize_res_y = x_tmp_y / D_x;
            normalize_res_x = x_tmp_x / D_x;
            float dot_res = N_x * normalizedLightPosition_x + N_y * normalizedLightPosition_y + N_z * normalizedLightPosition_z;
            D_x = diffuseConstant * dot_res * lightColor_x;
            float D_y = diffuseConstant * dot_res * lightColor_y;
            float D_z = diffuseConstant * dot_res * lightColor_z;
            float D_w = 1.0F;
            dot_res = N_x * normalize_res_x + N_y * normalize_res_y + N_z * loc_tmp_y;
            float pow_res = (float)Math.pow((double)dot_res, (double)specularExponent);
            float S_x = specularConstant * pow_res * lightColor_x;
            float S_y = specularConstant * pow_res * lightColor_y;
            float S_z = specularConstant * pow_res * lightColor_z;
            float max_res = S_x > S_y ? S_x : S_y;
            max_res = max_res > S_z ? max_res : S_z;
            float orig_x = sample_res_x * D_x;
            float orig_y = sample_res_y * D_y;
            float orig_z = sample_res_z * D_z;
            float orig_w = sample_res_w * D_w;
            S_x *= orig_w;
            S_y *= orig_w;
            S_z *= orig_w;
            float S_w = max_res * orig_w;
            float color_x = S_x + orig_x * (1.0F - S_w);
            float color_y = S_y + orig_y * (1.0F - S_w);
            float color_z = S_z + orig_z * (1.0F - S_w);
            float color_w = orig_w;
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

            if (orig_w < 0.0F) {
               color_w = 0.0F;
            } else if (orig_w > 1.0F) {
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
