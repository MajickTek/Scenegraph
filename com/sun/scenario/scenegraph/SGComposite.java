package com.sun.scenario.scenegraph;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;

public class SGComposite extends SGFilter {
   private float opacity = 1.0F;
   private Mode mode;
   private OverlapBehavior overlapbehavior;

   public SGComposite() {
      this.mode = SGComposite.Mode.SRC_OVER;
      this.overlapbehavior = SGComposite.OverlapBehavior.AUTOMATIC;
   }

   public SGComposite(float opacity, SGNode child) {
      this.mode = SGComposite.Mode.SRC_OVER;
      this.overlapbehavior = SGComposite.OverlapBehavior.AUTOMATIC;
      this.setOpacity(opacity);
      this.setChild(child);
   }

   public float getOpacity() {
      return this.opacity;
   }

   public void setOpacity(float opacity) {
      if (this.opacity != opacity) {
         this.opacity = opacity;
         this.visualChanged();
      }

   }

   public Mode getMode() {
      return this.mode;
   }

   public void setMode(Mode mode) {
      if (this.mode != mode) {
         this.mode = mode;
         this.visualChanged();
      }

   }

   public OverlapBehavior getOverlapBehavior() {
      return this.overlapbehavior;
   }

   public void setOverlapBehavior(OverlapBehavior overlapbehavior) {
      if (this.overlapbehavior != overlapbehavior) {
         this.overlapbehavior = overlapbehavior;
         this.visualChanged();
      }

   }

   public boolean canSkipRendering() {
      return this.mode == SGComposite.Mode.SRC_OVER && this.opacity == 0.0F;
   }

   public boolean canSkipChildren() {
      return this.canSkipRendering();
   }

   public int needsSourceContent(Graphics2D g) {
      SGNode child = this.getChild();
      boolean needsSource;
      if (child == null) {
         needsSource = false;
      } else if (this.opacity < 1.0F) {
         switch (this.overlapbehavior) {
            case AUTOMATIC:
               needsSource = child.hasOverlappingContents();
               break;
            case FLATTEN:
               needsSource = true;
               break;
            case LAYER:
               needsSource = false;
               break;
            default:
               needsSource = true;
         }
      } else {
         needsSource = false;
      }

      return needsSource ? 2 : 0;
   }

   public void setupRenderGraphics(Graphics2D g) {
      if (this.needsSourceContent(g) == 0) {
         g.setComposite(this.makeComposite(g));
      }

   }

   public void renderFinalImage(Graphics2D g, SGSourceContent srcContent) {
      if (this.opacity < 1.0F) {
         g.setComposite(this.makeComposite(g));
      }

      g.drawImage(srcContent.getTransformedImage(), 0, 0, (ImageObserver)null);
   }

   private Composite makeComposite(Graphics2D g) {
      switch (this.mode) {
         case SRC_OVER:
            int rule = 3;
            AlphaComposite ac = (AlphaComposite)g.getComposite();
            if (ac.getRule() != rule) {
               throw new InternalError("mixed AlphaComposite modes");
            }

            float alpha = this.opacity * ac.getAlpha();
            return AlphaComposite.getInstance(rule, alpha);
         default:
            throw new InternalError("unknown Mode: " + this.mode);
      }
   }

   boolean hasOverlappingContents() {
      return this.mode != SGComposite.Mode.SRC_OVER ? true : super.hasOverlappingContents();
   }

   public static enum OverlapBehavior {
      AUTOMATIC,
      FLATTEN,
      LAYER;
   }

   public static enum Mode {
      SRC_OVER;
   }
}
