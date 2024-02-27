package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class FilterEffect extends Effect {
   protected FilterEffect() {
   }

   protected FilterEffect(Effect input) {
      super(input);
   }

   protected FilterEffect(Effect input1, Effect input2) {
      super(input1, input2);
   }

   public boolean operatesInUserSpace() {
      return false;
   }

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      int numinputs = this.getNumInputs();
      AffineTransform inputtx = this.operatesInUserSpace() ? null : transform;
      Rectangle2D ret;
      if (numinputs == 1) {
         Effect input = this.getDefaultedInput(0, defaultInput);
         ret = input.getBounds(inputtx, defaultInput);
      } else {
         Rectangle2D[] inputBounds = new Rectangle2D[numinputs];

         for(int i = 0; i < numinputs; ++i) {
            Effect input = this.getDefaultedInput(i, defaultInput);
            inputBounds[i] = input.getBounds(inputtx, defaultInput);
         }

         ret = this.combineBounds(inputBounds);
      }

      if (inputtx != transform) {
         ret = this.transformBounds(transform, ret);
      }

      return ret;
   }

   public ImageData filter(GraphicsConfiguration config, AffineTransform transform, Effect defaultInput) {
      int numinputs = this.getNumInputs();
      ImageData[] inputDatas = new ImageData[numinputs];
      AffineTransform inputtx = this.operatesInUserSpace() ? null : transform;

      for(int i = 0; i < numinputs; ++i) {
         Effect input = this.getDefaultedInput(i, defaultInput);
         inputDatas[i] = input.filter(config, inputtx, defaultInput);
      }

      ImageData ret = this.filterImageDatas(config, transform, inputDatas);

      for(int i = 0; i < numinputs; ++i) {
         inputDatas[i].unref();
      }

      if (inputtx != transform) {
         ret = this.ensureTransform(config, ret, transform);
      }

      return ret;
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(0, defaultInput).transform(p, defaultInput);
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(0, defaultInput).untransform(p, defaultInput);
   }

   protected abstract ImageData filterImageDatas(GraphicsConfiguration var1, AffineTransform var2, ImageData... var3);
}
