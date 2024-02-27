package com.sun.scenario.scenegraph.fx;

import com.sun.scenario.scenegraph.SGAbstractShape;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

public class FXAbstractShape extends FXNode {
   private SGAbstractShape shapeNode;

   FXAbstractShape(SGAbstractShape shape) {
      super(shape);
      this.shapeNode = shape;
   }

   public final Shape getShape() {
      return this.shapeNode.getShape();
   }

   public final SGAbstractShape.Mode getMode() {
      return this.shapeNode.getMode();
   }

   public final void setMode(SGAbstractShape.Mode mode) {
      this.shapeNode.setMode(mode);
   }

   public final Paint getDrawPaint() {
      return this.shapeNode.getDrawPaint();
   }

   public void setDrawPaint(Paint drawPaint) {
      this.shapeNode.setDrawPaint(drawPaint);
   }

   public final Paint getFillPaint() {
      return this.shapeNode.getFillPaint();
   }

   public void setFillPaint(Paint fillPaint) {
      this.shapeNode.setFillPaint(fillPaint);
   }

   public final Stroke getDrawStroke() {
      return this.shapeNode.getDrawStroke();
   }

   public void setDrawStroke(Stroke drawStroke) {
      this.shapeNode.setDrawStroke(drawStroke);
   }
}
