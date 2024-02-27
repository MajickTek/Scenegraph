package com.sun.scenario.effect;

public class Brightpass extends CoreEffect {
   private float threshold;

   public Brightpass() {
      this(DefaultInput);
   }

   public Brightpass(Effect input) {
      super(input);
      this.setThreshold(0.3F);
      this.updatePeerKey("Brightpass");
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
   }

   public float getThreshold() {
      return this.threshold;
   }

   public void setThreshold(float threshold) {
      if (!(threshold < 0.0F) && !(threshold > 1.0F)) {
         float old = this.threshold;
         this.threshold = threshold;
         this.firePropertyChange("threshold", old, threshold);
      } else {
         throw new IllegalArgumentException("Threshold must be in the range [0,1]");
      }
   }
}
