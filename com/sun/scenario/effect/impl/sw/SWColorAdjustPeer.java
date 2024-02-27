package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.ColorAdjust;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class SWColorAdjustPeer extends SWEffectPeer {
   public SWColorAdjustPeer(GraphicsConfiguration gc) {
      super(gc);
   }

   protected final ColorAdjust getEffect() {
      return (ColorAdjust)super.getEffect();
   }

   private float getHue() {
      return this.getEffect().getHue() / 2.0F;
   }

   private float getSaturation() {
      return this.getEffect().getSaturation() + 1.0F;
   }

   private float getBrightness() {
      return this.getEffect().getBrightness() + 1.0F;
   }

   private float getContrast() {
      return this.getEffect().getContrast();
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
      float hue = this.getHue();
      float contrast = this.getContrast();
      float saturation = this.getSaturation();
      float brightness = this.getBrightness();
      float inc0_x = (src0Rect[2] - src0Rect[0]) / (float)dstw;
      float inc0_y = (src0Rect[3] - src0Rect[1]) / (float)dsth;
      float pos0_y = src0Rect[1] + inc0_y * 0.5F;

      for(int dy = 0; dy < 0 + dsth; ++dy) {
         float pixcoord_y = (float)dy;
         int dyi = dy * dstscan;
         float pos0_x = src0Rect[0] + inc0_x * 0.5F;

         for(int dx = 0; dx < 0 + dstw; ++dx) {
            float pixcoord_x = (float)dx;
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
            float sample_res_w = (float)(baseImg_tmp >>> 24) / 255.0F;
            float sample_res_x = (float)(baseImg_tmp >> 16 & 255) / 255.0F;
            float src_x = (sample_res_x - 0.5F) * contrast + 0.5F;
            float src_y = (sample_res_y - 0.5F) * contrast + 0.5F;
            float src_z = (sample_res_z - 0.5F) * contrast + 0.5F;
            float max_res = src_x > src_y ? src_x : src_y;
            max_res = max_res > src_z ? max_res : src_z;
            float max_tmp = src_x < src_y ? src_x : src_y;
            max_tmp = max_tmp < src_z ? max_tmp : src_z;
            float clamp_res_y;
            float clamp_res_x;
            float res_x;
            float res_y;
            if (max_res > max_tmp) {
               float c_x = (max_res - src_x) / (max_res - max_tmp);
               res_x = (max_res - src_y) / (max_res - max_tmp);
               res_y = (max_res - src_z) / (max_res - max_tmp);
               if (src_x == max_res) {
                  clamp_res_y = res_y - res_x;
               } else if (src_y == max_res) {
                  clamp_res_y = 2.0F + c_x - res_y;
               } else {
                  clamp_res_y = 4.0F + res_x - c_x;
               }

               clamp_res_y /= 6.0F;
               if (clamp_res_y < 0.0F) {
                  ++clamp_res_y;
               }

               clamp_res_x = (max_res - max_tmp) / max_res;
            } else {
               clamp_res_y = 0.0F;
               clamp_res_x = 0.0F;
            }

            float hsb_x = clamp_res_y + hue;
            if (hsb_x < 0.0F) {
               ++hsb_x;
            } else if (hsb_x > 1.0F) {
               --hsb_x;
            }

            float hsb_y;
            if (saturation > 1.0F) {
               clamp_res_y = saturation - 1.0F;
               hsb_y = clamp_res_x + (1.0F - clamp_res_x) * clamp_res_y;
            } else {
               hsb_y = clamp_res_x * saturation;
            }

            float hsb_z;
            if (brightness > 1.0F) {
               clamp_res_y = brightness - 1.0F;
               hsb_y *= 1.0F - clamp_res_y;
               hsb_z = max_res + (1.0F - max_res) * clamp_res_y;
            } else {
               hsb_z = max_res * brightness;
            }

            float min_tmp = 0.0F;
            max_tmp = 1.0F;
            clamp_res_y = hsb_z < min_tmp ? min_tmp : (hsb_z > max_tmp ? max_tmp : hsb_z);
            clamp_res_x = hsb_y < min_tmp ? min_tmp : (hsb_y > max_tmp ? max_tmp : hsb_y);
            res_x = 0.0F;
            res_y = 0.0F;
            float res_z = 0.0F;
            float floor_res = (float)Math.floor((double)hsb_x);
            float h = (hsb_x - floor_res) * 6.0F;
            floor_res = (float)Math.floor((double)h);
            float f = h - floor_res;
            float p = clamp_res_y * (1.0F - clamp_res_x);
            float q = clamp_res_y * (1.0F - clamp_res_x * f);
            float t = clamp_res_y * (1.0F - clamp_res_x * (1.0F - f));
            floor_res = (float)Math.floor((double)h);
            if (floor_res < 1.0F) {
               res_x = clamp_res_y;
               res_y = t;
               res_z = p;
            } else if (floor_res < 2.0F) {
               res_x = q;
               res_y = clamp_res_y;
               res_z = p;
            } else if (floor_res < 3.0F) {
               res_x = p;
               res_y = clamp_res_y;
               res_z = t;
            } else if (floor_res < 4.0F) {
               res_x = p;
               res_y = q;
               res_z = clamp_res_y;
            } else if (floor_res < 5.0F) {
               res_x = t;
               res_y = p;
               res_z = clamp_res_y;
            } else {
               res_x = clamp_res_y;
               res_y = p;
               res_z = q;
            }

            float color_x = res_x;
            float color_y = res_y;
            float color_z = res_z;
            float color_w = sample_res_w;
            if (res_x < 0.0F) {
               color_x = 0.0F;
            } else if (res_x > 1.0F) {
               color_x = 1.0F;
            }

            if (res_y < 0.0F) {
               color_y = 0.0F;
            } else if (res_y > 1.0F) {
               color_y = 1.0F;
            }

            if (res_z < 0.0F) {
               color_z = 0.0F;
            } else if (res_z > 1.0F) {
               color_z = 1.0F;
            }

            if (sample_res_w < 0.0F) {
               color_w = 0.0F;
            } else if (sample_res_w > 1.0F) {
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
