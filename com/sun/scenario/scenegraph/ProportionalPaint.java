package com.sun.scenario.scenegraph;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

public class ProportionalPaint implements Paint {
   private Paint origPaint;

   public ProportionalPaint(Paint origPaint) {
      this.origPaint = origPaint;
   }

   public int getTransparency() {
      return this.origPaint.getTransparency();
   }

   public PaintContext createContext(ColorModel cm, Rectangle devBounds, Rectangle2D usrBounds, AffineTransform at, RenderingHints hints) {
      AffineTransform at2 = new AffineTransform(at);
      at2.translate(usrBounds.getX(), usrBounds.getY());
      at2.scale(usrBounds.getWidth(), usrBounds.getHeight());
      return this.origPaint.createContext(cm, devBounds, new Rectangle(0, 0, 1, 1), at2, hints);
   }
}
