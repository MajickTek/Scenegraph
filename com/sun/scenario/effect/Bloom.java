package com.sun.scenario.effect;

import java.awt.geom.Point2D;

public class Bloom extends DelegateEffect {
   private final Brightpass brightpass;
   private final GaussianBlur blur;
   private final Blend blend;

   public Bloom() {
      this(DefaultInput);
   }

   public Bloom(Effect input) {
      super(input);
      this.brightpass = new Brightpass(input);
      this.blur = new GaussianBlur(10.0F, this.brightpass);
      Crop crop = new Crop(this.blur, input);
      this.blend = new Blend(Blend.Mode.ADD, input, crop);
   }

   protected Effect getDelegate() {
      return this.blend;
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
      this.brightpass.setInput(input);
      this.blend.setBottomInput(input);
   }

   public float getThreshold() {
      return this.brightpass.getThreshold();
   }

   public void setThreshold(float threshold) {
      float old = this.brightpass.getThreshold();
      this.brightpass.setThreshold(threshold);
      this.firePropertyChange("threshold", old, threshold);
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(0, defaultInput).transform(p, defaultInput);
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(0, defaultInput).untransform(p, defaultInput);
   }
}
