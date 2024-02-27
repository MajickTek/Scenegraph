package com.sun.scenario.effect;

import java.awt.geom.Point2D;

public class Glow extends DelegateEffect {
   private final GaussianBlur blur;
   private final Blend blend;

   public Glow() {
      this(DefaultInput);
   }

   public Glow(Effect input) {
      super(input);
      this.blur = new GaussianBlur(10.0F, input);
      Crop crop = new Crop(this.blur, input);
      this.blend = new Blend(Blend.Mode.ADD, input, crop);
      this.blend.setOpacity(0.3F);
   }

   protected Effect getDelegate() {
      return this.blend;
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
      this.blur.setInput(input);
      this.blend.setBottomInput(input);
   }

   public float getLevel() {
      return this.blend.getOpacity();
   }

   public void setLevel(float level) {
      float old = this.blend.getOpacity();
      this.blend.setOpacity(level);
      this.firePropertyChange("level", old, level);
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(0, defaultInput).transform(p, defaultInput);
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(0, defaultInput).untransform(p, defaultInput);
   }
}
