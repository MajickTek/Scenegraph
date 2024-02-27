package com.sun.scenario.effect;

import java.awt.Color;

public class Shadow extends AbstractGaussian {
   private float spread;
   private Color color;

   public Shadow() {
      this(10.0F);
   }

   public Shadow(float radius) {
      this(radius, Color.BLACK);
   }

   public Shadow(float radius, Color color) {
      this(radius, color, DefaultInput);
   }

   public Shadow(float radius, Color color, Effect input) {
      super("Shadow", radius, input);
      this.setColor(color);
   }

   public float getSpread() {
      return this.spread;
   }

   public void setSpread(float spread) {
      if (!(spread < 0.0F) && !(spread > 1.0F)) {
         float old = this.spread;
         this.spread = spread;
         this.firePropertyChange("spread", old, spread);
      } else {
         throw new IllegalArgumentException("Spread must be in the range [0,1]");
      }
   }

   public Color getColor() {
      return this.color;
   }

   public void setColor(Color color) {
      if (color == null) {
         throw new IllegalArgumentException("Color must be non-null");
      } else {
         Color old = this.color;
         this.color = color;
         this.firePropertyChange("color", old, color);
      }
   }
}
