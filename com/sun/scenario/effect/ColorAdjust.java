package com.sun.scenario.effect;

public class ColorAdjust extends CoreEffect {
   private float hue;
   private float saturation;
   private float brightness;
   private float contrast;

   public ColorAdjust() {
      this(DefaultInput);
   }

   public ColorAdjust(Effect input) {
      super(input);
      this.hue = 0.0F;
      this.saturation = 0.0F;
      this.brightness = 0.0F;
      this.contrast = 1.0F;
      this.updatePeerKey("ColorAdjust");
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
   }

   public float getHue() {
      return this.hue;
   }

   public void setHue(float hue) {
      if (!(hue < -1.0F) && !(hue > 1.0F)) {
         float old = this.hue;
         this.hue = hue;
         this.firePropertyChange("hue", old, hue);
      } else {
         throw new IllegalArgumentException("Hue must be in the range [-1, 1]");
      }
   }

   public float getSaturation() {
      return this.saturation;
   }

   public void setSaturation(float saturation) {
      if (!(saturation < -1.0F) && !(saturation > 1.0F)) {
         float old = this.saturation;
         this.saturation = saturation;
         this.firePropertyChange("saturation", old, saturation);
      } else {
         throw new IllegalArgumentException("Saturation must be in the range [-1, 1]");
      }
   }

   public float getBrightness() {
      return this.brightness;
   }

   public void setBrightness(float brightness) {
      if (!(brightness < -1.0F) && !(brightness > 1.0F)) {
         float old = this.brightness;
         this.brightness = brightness;
         this.firePropertyChange("brightness", old, brightness);
      } else {
         throw new IllegalArgumentException("Brightness must be in the range [-1, 1]");
      }
   }

   public float getContrast() {
      return this.contrast;
   }

   public void setContrast(float contrast) {
      if (!(contrast < 0.25F) && !(contrast > 4.0F)) {
         float old = this.contrast;
         this.contrast = contrast;
         this.firePropertyChange("contrast", old, contrast);
      } else {
         throw new IllegalArgumentException("Contrast must be in the range [0.25, 4]");
      }
   }
}
