package com.sun.scenario.effect.impl;

import com.sun.scenario.effect.FloatMap;
import java.awt.Image;

public abstract class Renderer {
   private final ImagePool imagePool = new ImagePool();

   protected Renderer() {
   }

   public abstract Image createCompatibleImage(int var1, int var2);

   public Image getCompatibleImage(int w, int h) {
      return this.imagePool.checkOut(this, w, h);
   }

   public void releaseCompatibleImage(Image image) {
      this.imagePool.checkIn(image);
   }

   public Object createFloatTexture(int w, int h) {
      throw new InternalError();
   }

   public void updateFloatTexture(Object texture, FloatMap map) {
      throw new InternalError();
   }
}
