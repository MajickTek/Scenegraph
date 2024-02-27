package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;

public class Merge extends FilterEffect {
   public Merge(Effect bottomInput, Effect topInput) {
      super(bottomInput, topInput);
   }

   public final Effect getBottomInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setBottomInput(Effect bottomInput) {
      this.setInput(0, bottomInput);
   }

   public final Effect getTopInput() {
      return (Effect)this.getInputs().get(1);
   }

   public void setTopInput(Effect topInput) {
      this.setInput(1, topInput);
   }

   public ImageData filterImageDatas(GraphicsConfiguration config, AffineTransform transform, ImageData... inputDatas) {
      Rectangle unionbounds = this.getResultBounds(transform, inputDatas);
      Image dst = getCompatibleImage(config, unionbounds.width, unionbounds.height);
      Graphics2D gdst = (Graphics2D)dst.getGraphics();
      gdst.translate(-unionbounds.x, -unionbounds.y);
      ImageData[] arr$ = inputDatas;
      int len$ = inputDatas.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ImageData input = arr$[i$];
         Rectangle inputbounds = input.getBounds();
         gdst.drawImage(input.getImage(), inputbounds.x, inputbounds.y, (ImageObserver)null);
      }

      gdst.dispose();
      return new ImageData(config, dst, unionbounds);
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(1, defaultInput).transform(p, defaultInput);
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(1, defaultInput).untransform(p, defaultInput);
   }

   public Effect.AccelType getAccelType(GraphicsConfiguration config) {
      return Effect.AccelType.INTRINSIC;
   }
}
