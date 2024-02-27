package com.sun.scenario.scenegraph;

import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.impl.ImageData;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

class NodeEffectInput extends Effect {
   private SGNode node;
   private ImageData cachedIdentityImageData;
   private ImageData cachedTransformedImageData;
   private AffineTransform cachedTransform;

   public NodeEffectInput() {
      this((SGNode)null);
   }

   public NodeEffectInput(SGNode node) {
      this.setNode(node);
   }

   public SGNode getNode() {
      return this.node;
   }

   public void setNode(SGNode node) {
      if (this.node != node) {
         this.node = node;
         this.flush();
      }

   }

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      return transform != null && !transform.isIdentity() ? this.node.getBounds(transform) : this.node.getBounds();
   }

   public ImageData filter(GraphicsConfiguration config, AffineTransform transform, Effect defaultInput) {
      if (transform != null && !transform.isIdentity()) {
         if (this.cachedTransformedImageData != null && this.cachedTransform.equals(transform)) {
            this.cachedTransformedImageData.addref();
            return this.cachedTransformedImageData;
         }
      } else {
         if (this.cachedIdentityImageData != null && this.cachedIdentityImageData.getGraphicsConfig() == config) {
            this.cachedIdentityImageData.addref();
            return this.cachedIdentityImageData;
         }

         transform = null;
      }

      ImageData retData = getImageDataForNode(config, this.node, transform);
      if (transform == null) {
         this.flushIdentityImage();
         this.cachedIdentityImageData = retData;
         this.cachedIdentityImageData.addref();
      } else {
         this.flushTransformedImage();
         this.cachedTransform = new AffineTransform(transform);
         this.cachedTransformedImageData = retData;
         this.cachedTransformedImageData.addref();
      }

      return retData;
   }

   public Effect.AccelType getAccelType(GraphicsConfiguration config) {
      return Effect.AccelType.INTRINSIC;
   }

   public void flushIdentityImage() {
      if (this.cachedIdentityImageData != null) {
         this.cachedIdentityImageData.unref();
         this.cachedIdentityImageData = null;
      }

   }

   public void flushTransformedImage() {
      if (this.cachedTransformedImageData != null) {
         this.cachedTransformedImageData.unref();
         this.cachedTransformedImageData = null;
      }

      this.cachedTransform = null;
   }

   public void flush() {
      this.flushIdentityImage();
      this.flushTransformedImage();
   }

   static ImageData getImageDataForNode(GraphicsConfiguration config, SGNode node, AffineTransform transform) {
      Rectangle bounds = node.getBounds(transform).getBounds();
      Image ret = Effect.getCompatibleImage(config, bounds.width, bounds.height);
      Graphics2D g2d = (Graphics2D)ret.getGraphics();
      g2d.translate(-bounds.x, -bounds.y);
      if (transform != null) {
         g2d.transform(transform);
      }

      node.render(g2d);
      g2d.dispose();
      return new ImageData(config, ret, bounds);
   }
}
