package com.sun.scenario.effect.impl.sw;

import com.sun.scenario.effect.impl.Renderer;
import java.awt.image.BufferedImage;

public class SWRenderer extends Renderer {
   private static SWRenderer theInstance;

   private SWRenderer() {
   }

   public static synchronized SWRenderer getInstance() {
      if (theInstance == null) {
         theInstance = new SWRenderer();
      }

      return theInstance;
   }

   public final BufferedImage createCompatibleImage(int w, int h) {
      return new BufferedImage(w, h, 3);
   }
}
