package com.sun.scenario.scenegraph;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;

public class SGImageOp extends SGFilter {
   private BufferedImageOp[] imageOps;

   public BufferedImageOp[] getImageOps() {
      return this.imageOps;
   }

   public void setImageOps(BufferedImageOp... imageOps) {
      this.imageOps = imageOps;
      this.boundsChanged();
   }

   public boolean canSkipRendering() {
      return this.imageOps == null;
   }

   public boolean canExpandBounds() {
      return this.imageOps != null;
   }

   public int needsSourceContent(Graphics2D g) {
      return 2;
   }

   public void setupRenderGraphics(Graphics2D g) {
   }

   public void renderFinalImage(Graphics2D g, SGSourceContent srcContent) {
      BufferedImage src = (BufferedImage)srcContent.getTransformedImage();
      BufferedImage dst = null;

      for(int i = 0; i < this.imageOps.length; ++i) {
         BufferedImageOp op = this.imageOps[i];
         src = op.filter(src, (BufferedImage)dst);
      }

      g.drawImage(src, 0, 0, (ImageObserver)null);
   }

   boolean hasOverlappingContents() {
      return this.imageOps != null ? false : super.hasOverlappingContents();
   }
}
