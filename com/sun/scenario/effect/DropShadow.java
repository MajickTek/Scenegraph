package com.sun.scenario.effect;

import java.awt.Color;
import java.awt.GraphicsConfiguration;

public class DropShadow extends DelegateEffect {
   private final Shadow shadow;
   private final Offset offset;
   private final Merge merge;

   public DropShadow() {
      this(DefaultInput, DefaultInput);
   }

   public DropShadow(Effect input) {
      this(input, input);
   }

   public DropShadow(Effect shadowSourceInput, Effect contentInput) {
      super(shadowSourceInput, contentInput);
      this.shadow = new Shadow(10.0F, Color.BLACK, shadowSourceInput);
      this.offset = new Offset(0, 0, this.shadow);
      this.merge = new Merge(this.offset, contentInput);
   }

   protected Effect getDelegate() {
      return this.merge;
   }

   public final Effect getShadowSourceInput() {
      return this.shadow.getInput();
   }

   public void setShadowSourceInput(Effect shadowSourceInput) {
      this.shadow.setInput(shadowSourceInput);
   }

   public final Effect getContentInput() {
      return this.merge.getTopInput();
   }

   public void setContentInput(Effect contentInput) {
      this.merge.setTopInput(contentInput);
   }

   public float getRadius() {
      return this.shadow.getRadius();
   }

   public void setRadius(float radius) {
      float old = this.shadow.getRadius();
      this.shadow.setRadius(radius);
      this.firePropertyChange("radius", old, radius);
   }

   public float getSpread() {
      return this.shadow.getSpread();
   }

   public void setSpread(float spread) {
      float old = this.shadow.getSpread();
      this.shadow.setSpread(spread);
      this.firePropertyChange("spread", old, spread);
   }

   public Color getColor() {
      return this.shadow.getColor();
   }

   public void setColor(Color color) {
      Color old = this.shadow.getColor();
      this.shadow.setColor(color);
      this.firePropertyChange("color", old, color);
   }

   public int getOffsetX() {
      return this.offset.getX();
   }

   public void setOffsetX(int xoff) {
      int old = this.offset.getX();
      this.offset.setX(xoff);
      this.firePropertyChange("offsetX", old, xoff);
   }

   public int getOffsetY() {
      return this.offset.getY();
   }

   public void setOffsetY(int yoff) {
      int old = this.offset.getY();
      this.offset.setY(yoff);
      this.firePropertyChange("offsetY", old, yoff);
   }

   public Effect.AccelType getAccelType(GraphicsConfiguration config) {
      return this.shadow.getAccelType(config);
   }
}
