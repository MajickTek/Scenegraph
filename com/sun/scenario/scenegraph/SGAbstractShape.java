package com.sun.scenario.scenegraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

public abstract class SGAbstractShape extends SGLeaf {
   private static final Stroke defaultStroke = new BasicStroke(1.0F);
   Mode mode;
   Paint drawPaint;
   Paint fillPaint;
   Stroke drawStroke;

   public SGAbstractShape() {
      this.mode = SGAbstractShape.Mode.FILL;
      this.drawPaint = Color.BLACK;
      this.fillPaint = Color.BLACK;
      this.drawStroke = defaultStroke;
   }

   public abstract Shape getShape();

   public final Mode getMode() {
      return this.mode;
   }

   public void setMode(Mode mode) {
      if (mode == null) {
         throw new IllegalArgumentException("null mode");
      } else {
         if (this.mode != mode) {
            this.mode = mode;
            this.repaint(true);
         }

      }
   }

   public final Paint getDrawPaint() {
      return this.drawPaint;
   }

   public void setDrawPaint(Paint drawPaint) {
      this.drawPaint = drawPaint;
      this.repaint(false);
   }

   public final Paint getFillPaint() {
      return this.fillPaint;
   }

   public void setFillPaint(Paint fillPaint) {
      this.fillPaint = fillPaint;
      this.repaint(false);
   }

   public final Stroke getDrawStroke() {
      return this.drawStroke;
   }

   public void setDrawStroke(Stroke drawStroke) {
      if (drawStroke == null) {
         throw new IllegalArgumentException("null drawStroke");
      } else {
         this.drawStroke = drawStroke;
         this.repaint(true);
      }
   }

   boolean hasOverlappingContents() {
      return this.getMode() == SGAbstractShape.Mode.STROKE_FILL;
   }

   public static enum Mode {
      STROKE,
      FILL,
      STROKE_FILL,
      EMPTY;
   }
}
