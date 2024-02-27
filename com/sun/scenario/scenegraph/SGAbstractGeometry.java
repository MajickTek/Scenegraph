package com.sun.scenario.scenegraph;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;

public abstract class SGAbstractGeometry extends SGAbstractShape {
   private Object antialiasingHint;
   Shape cachedStrokeShape;

   public SGAbstractGeometry() {
      this.antialiasingHint = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
   }

   public Object getAntialiasingHint() {
      return this.antialiasingHint;
   }

   public void setAntialiasingHint(Object hint) {
      if (!RenderingHints.KEY_ANTIALIASING.isCompatibleValue(hint)) {
         throw new IllegalArgumentException("invalid hint");
      } else {
         if (this.antialiasingHint != hint) {
            this.antialiasingHint = hint;
            this.repaint(false);
         }

      }
   }

   public void paint(Graphics2D g) {
      Shape shape = this.getShape();
      if (shape != null && this.mode != SGAbstractShape.Mode.EMPTY) {
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, this.antialiasingHint);
         if (this.mode != SGAbstractShape.Mode.STROKE && this.fillPaint != null) {
            g.setPaint(this.fillPaint);
            if (DO_PAINT) {
               g.fill(shape);
            }
         }

         if (this.mode != SGAbstractShape.Mode.FILL && this.drawPaint != null) {
            g.setPaint(this.drawPaint);
            g.setStroke(this.drawStroke);
            if (DO_PAINT) {
               try {
                  g.draw(shape);
               } catch (Throwable var4) {
               }
            }
         }

      }
   }

   public abstract boolean contains(Point2D var1);

   public void setDrawStroke(Stroke drawStroke) {
      super.setDrawStroke(drawStroke);
      this.cachedStrokeShape = null;
   }

   void invalidateStrokeShape() {
      this.cachedStrokeShape = null;
      this.repaint(true);
   }

   Shape getStrokeShape() {
      if (this.cachedStrokeShape == null) {
         this.cachedStrokeShape = this.getDrawStroke().createStrokedShape(this.getShape());
      }

      return this.cachedStrokeShape;
   }
}
