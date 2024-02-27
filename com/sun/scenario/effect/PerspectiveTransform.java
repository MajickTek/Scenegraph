package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import com.sun.scenario.effect.impl.state.PerspectiveTransformState;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class PerspectiveTransform extends CoreEffect {
   // $FF: renamed from: tx float[][]
   private float[][] field_0;
   private float ulx;
   private float uly;
   private float urx;
   private float ury;
   private float lrx;
   private float lry;
   private float llx;
   private float lly;
   private float[] devcoords;
   private final PerspectiveTransformState state;

   public PerspectiveTransform() {
      this(DefaultInput);
   }

   public PerspectiveTransform(Effect input) {
      super(input);
      this.field_0 = new float[3][3];
      this.devcoords = new float[8];
      this.state = new PerspectiveTransformState();
      this.setQuadMapping(0.0F, 0.0F, 100.0F, 0.0F, 100.0F, 100.0F, 0.0F, 100.0F);
      this.updatePeerKey("PerspectiveTransform");
   }

   Object getState() {
      return this.state;
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
   }

   private void setUnitQuadMapping(float ulx, float uly, float urx, float ury, float lrx, float lry, float llx, float lly) {
      float dx3 = ulx - urx + lrx - llx;
      float dy3 = uly - ury + lry - lly;
      this.field_0 = new float[3][3];
      this.field_0[2][2] = 1.0F;
      if (dx3 == 0.0F && dy3 == 0.0F) {
         this.field_0[0][0] = urx - ulx;
         this.field_0[0][1] = lrx - urx;
         this.field_0[0][2] = ulx;
         this.field_0[1][0] = ury - uly;
         this.field_0[1][1] = lry - ury;
         this.field_0[1][2] = uly;
         this.field_0[2][0] = 0.0F;
         this.field_0[2][1] = 0.0F;
      } else {
         float dx1 = urx - lrx;
         float dy1 = ury - lry;
         float dx2 = llx - lrx;
         float dy2 = lly - lry;
         float invdet = 1.0F / (dx1 * dy2 - dx2 * dy1);
         this.field_0[2][0] = (dx3 * dy2 - dx2 * dy3) * invdet;
         this.field_0[2][1] = (dx1 * dy3 - dx3 * dy1) * invdet;
         this.field_0[0][0] = urx - ulx + this.field_0[2][0] * urx;
         this.field_0[0][1] = llx - ulx + this.field_0[2][1] * llx;
         this.field_0[0][2] = ulx;
         this.field_0[1][0] = ury - uly + this.field_0[2][0] * ury;
         this.field_0[1][1] = lly - uly + this.field_0[2][1] * lly;
         this.field_0[1][2] = uly;
      }

      this.state.updateTx(this.field_0);
   }

   public void setQuadMapping(float ulx, float uly, float urx, float ury, float lrx, float lry, float llx, float lly) {
      this.ulx = ulx;
      this.uly = uly;
      this.urx = urx;
      this.ury = ury;
      this.lrx = lrx;
      this.lry = lry;
      this.llx = llx;
      this.lly = lly;
      this.firePropertyChange("quadMapping", (Object)null, lly);
   }

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      this.devcoords[0] = this.ulx;
      this.devcoords[1] = this.uly;
      this.devcoords[2] = this.urx;
      this.devcoords[3] = this.ury;
      this.devcoords[4] = this.lrx;
      this.devcoords[5] = this.lry;
      this.devcoords[6] = this.llx;
      this.devcoords[7] = this.lly;
      if (transform != null && !transform.isIdentity()) {
         transform.transform(this.devcoords, 0, this.devcoords, 0, 4);
      }

      float maxx;
      float minx = maxx = this.devcoords[0];
      float maxy;
      float miny = maxy = this.devcoords[1];

      for(int i = 2; i < this.devcoords.length; i += 2) {
         if (minx > this.devcoords[i]) {
            minx = this.devcoords[i];
         } else if (maxx < this.devcoords[i]) {
            maxx = this.devcoords[i];
         }

         if (miny > this.devcoords[i + 1]) {
            miny = this.devcoords[i + 1];
         } else if (maxy < this.devcoords[i + 1]) {
            maxy = this.devcoords[i + 1];
         }
      }

      return new Rectangle2D.Float(minx, miny, maxx - minx, maxy - miny);
   }

   public boolean operatesInUserSpace() {
      return true;
   }

   public ImageData filter(GraphicsConfiguration config, AffineTransform transform, Effect defaultInput) {
      Effect input = this.getDefaultedInput(0, defaultInput);
      ImageData inputData = input.filter(config, (AffineTransform)null, defaultInput);
      ImageData ret = this.filterImageDatas(config, transform, new ImageData[]{inputData});
      inputData.unref();
      return ret;
   }

   public Rectangle getResultBounds(AffineTransform transform, ImageData... inputDatas) {
      Rectangle ob = this.getBounds(transform, (Effect)null).getBounds();
      this.setupTransforms(inputDatas[0].getBounds());
      return ob;
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      p = this.getDefaultedInput(0, defaultInput).transform(p, defaultInput);
      this.getBounds((AffineTransform)null, defaultInput).getBounds();
      Effect input = this.getDefaultedInput(0, defaultInput);
      this.setupTransforms(input.getBounds((AffineTransform)null, defaultInput).getBounds());
      double sx = p.getX();
      double sy = p.getY();
      double dx = (double)this.field_0[0][0] * sx + (double)this.field_0[0][1] * sy + (double)this.field_0[0][2];
      double dy = (double)this.field_0[1][0] * sx + (double)this.field_0[1][1] * sy + (double)this.field_0[1][2];
      double dw = (double)this.field_0[2][0] * sx + (double)this.field_0[2][1] * sy + (double)this.field_0[2][2];
      Point2D p = new Point2D.Float();
      p.setLocation(dx / dw, dy / dw);
      return p;
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      this.getBounds((AffineTransform)null, defaultInput).getBounds();
      Effect input = this.getDefaultedInput(0, defaultInput);
      this.setupTransforms(input.getBounds((AffineTransform)null, defaultInput).getBounds());
      double dx = p.getX();
      double dy = p.getY();
      float[][] itx = this.state.getITX();
      double sx = (double)itx[0][0] * dx + (double)itx[0][1] * dy + (double)itx[0][2];
      double sy = (double)itx[1][0] * dx + (double)itx[1][1] * dy + (double)itx[1][2];
      double sw = (double)itx[2][0] * dx + (double)itx[2][1] * dy + (double)itx[2][2];
      Point2D p = new Point2D.Float();
      p.setLocation(sx / sw, sy / sw);
      p = this.getDefaultedInput(0, defaultInput).untransform(p, defaultInput);
      return p;
   }

   private void setupTransforms(Rectangle ib) {
      float x = (float)ib.x;
      float y = (float)ib.y;
      float w = (float)ib.width;
      float h = (float)ib.height;
      this.setUnitQuadMapping((this.devcoords[0] - x) / w, (this.devcoords[1] - y) / h, (this.devcoords[2] - x) / w, (this.devcoords[3] - y) / h, (this.devcoords[4] - x) / w, (this.devcoords[5] - y) / h, (this.devcoords[6] - x) / w, (this.devcoords[7] - y) / h);
   }
}
