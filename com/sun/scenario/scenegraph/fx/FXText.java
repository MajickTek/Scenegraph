package com.sun.scenario.scenegraph.fx;

import com.sun.scenario.scenegraph.SGText;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

public class FXText extends FXAbstractShape {
   private SGText textNode = (SGText)this.getLeaf();

   public FXText() {
      super(new SGText());
      this.textNode.setAntialiasingHint(RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
   }

   public final String getText() {
      return this.textNode.getText();
   }

   public void setText(String text) {
      this.textNode.setText(text);
   }

   public final Font getFont() {
      return this.textNode.getFont();
   }

   public void setFont(Font font) {
      this.textNode.setFont(font);
   }

   public final Point2D getLocation(Point2D rv) {
      return this.textNode.getLocation(rv);
   }

   public final Point2D getLocation() {
      return this.textNode.getLocation((Point2D)null);
   }

   public void setLocation(Point2D location) {
      this.textNode.setLocation(location);
   }

   public final SGText.VAlign getVerticalTextAlignment() {
      return this.textNode.getVerticalAlignment();
   }

   public void setVerticalTextAlignment(SGText.VAlign verticalAlignment) {
      this.textNode.setVerticalAlignment(verticalAlignment);
   }

   public final Object getAntialiasingHint() {
      return this.textNode.getAntialiasingHint();
   }

   public void setAntialiasingHint(Object hint) {
      this.textNode.setAntialiasingHint(hint);
   }
}
