package com.sun.scenario.effect;

public class SepiaTone extends CoreEffect {
   private float level;

   public SepiaTone() {
      this(DefaultInput);
   }

   public SepiaTone(Effect input) {
      super(input);
      this.setLevel(1.0F);
      this.updatePeerKey("SepiaTone");
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
   }

   public float getLevel() {
      return this.level;
   }

   public void setLevel(float level) {
      if (!(level < 0.0F) && !(level > 1.0F)) {
         float old = this.level;
         this.level = level;
         this.firePropertyChange("level", old, level);
      } else {
         throw new IllegalArgumentException("Level must be in the range [0,1]");
      }
   }
}
