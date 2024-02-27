package com.sun.scenario.scenegraph.fx;

import com.sun.scenario.scenegraph.SGShape;
import java.awt.RenderingHints;
import java.awt.Shape;

public class FXShape extends FXAbstractShape {
   private SGShape shapeNode = (SGShape)this.getLeaf();

   public FXShape() {
      super(new SGShape());
      this.shapeNode.setAntialiasingHint(RenderingHints.VALUE_ANTIALIAS_ON);
   }

   public void setShape(Shape shape) {
      this.shapeNode.setShape(shape);
   }

   public final Object getAntialiasingHint() {
      return this.shapeNode.getAntialiasingHint();
   }

   public void setAntialiasingHint(Object hint) {
      this.shapeNode.setAntialiasingHint(hint);
   }
}
