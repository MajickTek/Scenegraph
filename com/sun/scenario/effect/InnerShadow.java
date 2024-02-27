package com.sun.scenario.effect;

import java.awt.Color;
import java.awt.geom.Point2D;

public class InnerShadow extends DelegateEffect {
   private final InvertMask invert;
   private final Shadow shadow;
   private final Offset offset;
   private final Blend blend;

   public InnerShadow() {
      this(DefaultInput, DefaultInput);
   }

   public InnerShadow(Effect input) {
      this(input, input);
   }

   public InnerShadow(Effect shadowSourceInput, Effect contentInput) {
      super(shadowSourceInput, contentInput);
      this.invert = new InvertMask(10, shadowSourceInput);
      this.shadow = new Shadow(10.0F, Color.BLACK, this.invert);
      this.offset = new Offset(0, 0, this.shadow);
      this.blend = new Blend(Blend.Mode.SRC_ATOP, contentInput, this.offset);
   }

   protected Effect getDelegate() {
      return this.blend;
   }

   public final Effect getShadowSourceInput() {
      return this.shadow.getInput();
   }

   public void setShadowSourceInput(Effect shadowSourceInput) {
      this.shadow.setInput(shadowSourceInput);
   }

   public final Effect getContentInput() {
      return this.blend.getBottomInput();
   }

   public void setContentInput(Effect contentInput) {
      this.blend.setBottomInput(contentInput);
   }

   public float getRadius() {
      return this.shadow.getRadius();
   }

   public void setRadius(float radius) {
      float old = this.shadow.getRadius();
      this.invert.setPad((int)Math.ceil((double)radius));
      this.shadow.setRadius(radius);
      this.firePropertyChange("radius", old, radius);
   }

   public float getChoke() {
      return this.shadow.getSpread();
   }

   public void setChoke(float choke) {
      float old = this.shadow.getSpread();
      this.shadow.setSpread(choke);
      this.firePropertyChange("choke", old, choke);
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

   public Point2D transform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(1, defaultInput).transform(p, defaultInput);
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(1, defaultInput).untransform(p, defaultInput);
   }
}
