package com.sun.scenario.effect;

import java.awt.geom.Point2D;

public class Blend extends CoreEffect {
   private Mode mode;
   private float opacity;

   public Blend(Mode mode, Effect bottomInput, Effect topInput) {
      super(bottomInput, topInput);
      this.setMode(mode);
      this.setOpacity(1.0F);
   }

   public final Effect getBottomInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setBottomInput(Effect bottomInput) {
      this.setInput(0, bottomInput);
   }

   public final Effect getTopInput() {
      return (Effect)this.getInputs().get(1);
   }

   public void setTopInput(Effect topInput) {
      this.setInput(1, topInput);
   }

   public Mode getMode() {
      return this.mode;
   }

   public void setMode(Mode mode) {
      if (mode == null) {
         throw new IllegalArgumentException("Mode must be non-null");
      } else {
         Mode old = this.mode;
         this.mode = mode;
         this.updatePeerKey("Blend_" + mode.name());
         this.firePropertyChange("mode", old, mode);
      }
   }

   public float getOpacity() {
      return this.opacity;
   }

   public void setOpacity(float opacity) {
      if (!(opacity < 0.0F) && !(opacity > 1.0F)) {
         float old = this.opacity;
         this.opacity = opacity;
         this.firePropertyChange("opacity", old, opacity);
      } else {
         throw new IllegalArgumentException("Opacity must be in the range [0,1]");
      }
   }

   public Point2D transform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(1, defaultInput).transform(p, defaultInput);
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      return this.getDefaultedInput(1, defaultInput).untransform(p, defaultInput);
   }

   public static enum Mode {
      SRC_OVER,
      SRC_IN,
      SRC_OUT,
      SRC_ATOP,
      ADD,
      MULTIPLY,
      SCREEN,
      OVERLAY,
      DARKEN,
      LIGHTEN,
      COLOR_DODGE,
      COLOR_BURN,
      HARD_LIGHT,
      SOFT_LIGHT,
      DIFFERENCE,
      EXCLUSION,
      RED,
      GREEN,
      BLUE;
   }
}
