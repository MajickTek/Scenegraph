package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.PerspectiveTransform;
import com.sun.scenario.effect.impl.ImageData;
import com.sun.scenario.effect.impl.state.AccessHelper;
import com.sun.scenario.effect.impl.state.PerspectiveTransformState;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class SWPerspectiveTransformPeer extends SWEffectPeer {
   public SWPerspectiveTransformPeer(GraphicsConfiguration gc) {
      super(gc);
   }

   protected final PerspectiveTransform getEffect() {
      return (PerspectiveTransform)super.getEffect();
   }

   private float[][] getITX() {
      PerspectiveTransformState state = (PerspectiveTransformState)AccessHelper.getState(this.getEffect());
      return state.getITX();
   }

   private float[] getTx0() {
      Rectangle ib = this.getInputBounds(0);
      Rectangle nb = this.getInputNativeBounds(0);
      float scalex = (float)(ib.getWidth() / nb.getWidth());
      float[] itx0 = this.getITX()[0];
      return new float[]{itx0[0] * scalex, itx0[1] * scalex, itx0[2] * scalex};
   }

   private float[] getTx1() {
      Rectangle ib = this.getInputBounds(0);
      Rectangle nb = this.getInputNativeBounds(0);
      float scaley = (float)(ib.getHeight() / nb.getHeight());
      float[] itx1 = this.getITX()[1];
      return new float[]{itx1[0] * scaley, itx1[1] * scaley, itx1[2] * scaley};
   }

   private float[] getTx2() {
      return this.getITX()[2];
   }

   public float[] getSourceRegion(int i) {
      Rectangle2D ib = this.getInputBounds(0);
      Rectangle2D fb = this.getDestBounds();
      float txmin = (float)((fb.getMinX() - ib.getX()) / ib.getWidth());
      float tymin = (float)((fb.getMinY() - ib.getY()) / ib.getHeight());
      float txmax = (float)((fb.getMaxX() - ib.getX()) / ib.getWidth());
      float tymax = (float)((fb.getMaxY() - ib.getY()) / ib.getHeight());
      return new float[]{txmin, tymin, txmax, tymax};
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
      float[] baseImg_vals = new float[4];
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
      float[] tx2_arr = this.getTx2();
      float tx2_x = tx2_arr[0];
      float tx2_y = tx2_arr[1];
      float tx2_z = tx2_arr[2];
      float[] tx0_arr = this.getTx0();
      float tx0_x = tx0_arr[0];
      float tx0_y = tx0_arr[1];
      float tx0_z = tx0_arr[2];
      float[] tx1_arr = this.getTx1();
      float tx1_x = tx1_arr[0];
      float tx1_y = tx1_arr[1];
      float tx1_z = tx1_arr[2];
      float inc0_x = (src0Rect[2] - src0Rect[0]) / (float)dstw;
      float inc0_y = (src0Rect[3] - src0Rect[1]) / (float)dsth;
      float pos0_y = src0Rect[1] + inc0_y * 0.5F;

      for(int dy = 0; dy < 0 + dsth; ++dy) {
         float pixcoord_y = (float)dy;
         int dyi = dy * dstscan;
         float pos0_x = src0Rect[0] + inc0_x * 0.5F;

         for(int dx = 0; dx < 0 + dstw; ++dx) {
            float pixcoord_x = (float)dx;
            float p1_z = 1.0F;
            float dot_res = pos0_x * tx2_x + pos0_y * tx2_y + p1_z * tx2_z;
            float p2_z = dot_res;
            dot_res = pos0_x * tx0_x + pos0_y * tx0_y + p1_z * tx0_z;
            float p2_x = dot_res / p2_z;
            dot_res = pos0_x * tx1_x + pos0_y * tx1_y + p1_z * tx1_z;
            float p2_y = dot_res / p2_z;
            this.lsample(baseImg, p2_x, p2_y, src0w, src0h, src0scan, baseImg_vals);
            float sample_res_z = baseImg_vals[2];
            float sample_res_y = baseImg_vals[1];
            float sample_res_w = baseImg_vals[3];
            float sample_res_x = baseImg_vals[0];
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
         }

         pos0_y += inc0_y;
      }

      return new ImageData(this.getGraphicsConfig(), dst, dstBounds);
   }
}
