package com.sun.scenario.scenegraph;

import com.sun.scenario.effect.Effect;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;

public class SGClip extends SGFilter {
   private static final boolean aaAvailable;
   private boolean rectOptimization;
   private SGNode clipNode;
   private ClippingMode currentMode;
   private AffineTransform filterXform;
   private Rectangle2D TEMP_RECT;
   private Rectangle2D TEMP_RECT2;

   public SGClip() {
      this.currentMode = SGClip.ClippingMode.NO_CLIP;
   }

   public void setClipNode(SGNode newClipNode) {
      if (newClipNode != this.clipNode) {
         if (this.clipNode != null) {
            this.remove(this.clipNode);
            this.clipNode.setParent((Object)null);
         }

         if (newClipNode != null) {
            SGParent oldParent = newClipNode.getParent();
            if (oldParent != null) {
               oldParent.remove(newClipNode);
            }

            newClipNode.setParent(this);
         }

         this.clipNode = newClipNode;
         this.updateMode();
         this.boundsChanged();
      } else {
         this.updateMode();
      }

      this.dispatchAllPendingEvents();
   }

   final void doTransformChanged() {
      super.doTransformChanged();
      if (this.clipNode != null) {
         this.clipNode.transformChanged();
      }

   }

   void dispatchPendingEvents() {
      super.dispatchPendingEvents();
      if (this.clipNode != null) {
         this.clipNode.dispatchPendingEvents();
      }

   }

   Rectangle2D accumulateDirtyChildren(Rectangle2D r, Rectangle2D clip) {
      if (this.getChild() == null) {
         return r;
      } else if (this.clipNode == null) {
         return super.accumulateDirtyChildren(r, clip);
      } else {
         Rectangle2D clipDirtyRect = this.clipNode.accumulateDirty(r, clip);
         Rectangle2D clipRect = this.clipNode.getTransformedBounds();
         Rectangle2D childDirtyRect = this.getChild().accumulateDirty((Rectangle2D)null, clip);
         if (childDirtyRect != null && !clipRect.isEmpty()) {
            Rectangle2D.intersect(childDirtyRect, clipRect, childDirtyRect);
            if (clipDirtyRect == null) {
               r = childDirtyRect;
            } else {
               Rectangle2D.union(clipDirtyRect, childDirtyRect, childDirtyRect);
               r = childDirtyRect;
            }
         } else {
            r = clipDirtyRect;
         }

         return r;
      }
   }

   void markDirty(int state) {
      super.markDirty(state);
      this.updateMode();
   }

   void updateMode() {
      ClippingMode newMode = SGClip.ClippingMode.NO_CLIP;
      SGNode clipNode = this.getRealClipNode();
      if (clipNode instanceof SGAbstractGeometry) {
         SGAbstractGeometry sgShape = (SGAbstractGeometry)clipNode;
         Shape shape = sgShape.getShape();
         if (shape == null) {
            newMode = SGClip.ClippingMode.EMPTY_CLIP;
         } else {
            Object aahint = sgShape.getAntialiasingHint();
            if (aahint == RenderingHints.VALUE_ANTIALIAS_ON) {
               if (sgShape instanceof SGRectangle) {
                  newMode = SGClip.ClippingMode.SOFT_RECT_SHAPE;
               } else {
                  newMode = SGClip.ClippingMode.SOFT_SHAPE;
               }
            } else if (shape instanceof Rectangle2D) {
               newMode = SGClip.ClippingMode.RECT_CLIP;
            } else {
               newMode = SGClip.ClippingMode.HARD_SHAPE;
            }
         }
      } else if (clipNode instanceof SGImage) {
         Image im = ((SGImage)clipNode).getImage();
         newMode = SGClip.ClippingMode.BITMASK;
         if (im instanceof BufferedImage) {
            if (!((BufferedImage)im).getColorModel().hasAlpha()) {
               newMode = SGClip.ClippingMode.RECT_CLIP;
            }
         } else if (im instanceof VolatileImage) {
            if (((VolatileImage)im).getTransparency() == 1) {
               newMode = SGClip.ClippingMode.RECT_CLIP;
            }
         } else if (im == null) {
            newMode = SGClip.ClippingMode.EMPTY_CLIP;
         }
      } else if (clipNode != null) {
         newMode = SGClip.ClippingMode.NODE_CLIP;
      } else {
         assert clipNode == null;

         newMode = SGClip.ClippingMode.NO_CLIP;
      }

      this.currentMode = newMode;
   }

   public Rectangle2D getBounds(AffineTransform xform) {
      SGNode child = this.getChild();
      if (child != null && this.currentMode != SGClip.ClippingMode.EMPTY_CLIP) {
         Rectangle2D childXformBounds = child.getBounds(xform);
         if (this.currentMode == SGClip.ClippingMode.NO_CLIP) {
            return childXformBounds;
         } else {
            Rectangle2D nodeBounds = this.clipNode.getBounds();
            if (xform != null) {
               nodeBounds = xform.createTransformedShape(nodeBounds).getBounds2D();
            }

            return nodeBounds.createIntersection(childXformBounds);
         }
      } else {
         return new Rectangle2D.Float();
      }
   }

   Rectangle2D calculateAccumBounds() {
      return this.getBounds(this.getCumulativeTransform());
   }

   public boolean canSkipRendering() {
      return this.currentMode == SGClip.ClippingMode.EMPTY_CLIP;
   }

   public boolean canSkipChildren() {
      return this.currentMode == SGClip.ClippingMode.EMPTY_CLIP ? true : super.canSkipChildren();
   }

   public int needsSourceContent(Graphics2D g) {
      return this.currentMode != SGClip.ClippingMode.HARD_SHAPE && this.currentMode != SGClip.ClippingMode.RECT_CLIP && this.currentMode != SGClip.ClippingMode.NO_CLIP && !this.checkIfRectOpt(g) ? 10 : 0;
   }

   public void setupRenderGraphics(Graphics2D g) {
      if (this.currentMode == SGClip.ClippingMode.HARD_SHAPE) {
         g.clip(((SGAbstractGeometry)this.getRealClipNode()).getShape());
      } else if (this.currentMode != SGClip.ClippingMode.RECT_CLIP && !this.rectOptimization) {
         this.filterXform = g.getTransform();
      } else {
         Rectangle b = this.clipNode.getBounds().getBounds();
         g.clipRect(b.x, b.y, b.width, b.height);
      }

      this.rectOptimization = false;
   }

   public void renderFinalImage(Graphics2D g, SGSourceContent srcContent) {
      assert this.clipNode != null;

      Image src = srcContent.getTransformedImage();
      int srcw = src.getWidth((ImageObserver)null);
      int srch = src.getHeight((ImageObserver)null);
      GraphicsConfiguration gc = g.getDeviceConfiguration();
      Image tmp = Effect.getCompatibleImage(gc, srcw, srch);
      Graphics2D gtmp = (Graphics2D)tmp.getGraphics();
      gtmp.setComposite(AlphaComposite.Clear);
      gtmp.fillRect(0, 0, tmp.getWidth((ImageObserver)null), tmp.getHeight((ImageObserver)null));
      gtmp.clipRect(0, 0, srcw, srch);
      gtmp.setComposite(AlphaComposite.SrcOver);
      gtmp.setTransform(this.filterXform);
      this.clipNode.render(gtmp);
      gtmp.setTransform(new AffineTransform());
      gtmp.setComposite(AlphaComposite.SrcIn);
      gtmp.drawImage(src, 0, 0, (ImageObserver)null);
      gtmp.dispose();
      g.drawImage(tmp, 0, 0, (ImageObserver)null);
      Effect.releaseCompatibleImage(gc, tmp);
   }

   private boolean checkIfRectOpt(Graphics2D g) {
      boolean opt = false;
      if (this.currentMode == SGClip.ClippingMode.SOFT_RECT_SHAPE) {
         SGRectangle rect = (SGRectangle)this.getRealClipNode();
         opt = !rect.isAARequired(g.getTransform());
      }

      this.rectOptimization = opt;
      return opt;
   }

   private SGNode getRealClipNode() {
      return this.clipNode instanceof SGWrapper ? ((SGWrapper)this.clipNode).getChild() : this.clipNode;
   }

   public boolean contains(Point2D point) {
      SGNode child = this.getChild();
      boolean childContains = child == null ? true : child.contains(point);
      switch (this.currentMode) {
         case NO_CLIP:
            return childContains && super.contains(point);
         case EMPTY_CLIP:
            return false;
         case RECT_CLIP:
         case SOFT_SHAPE:
         case SOFT_RECT_SHAPE:
         case BITMASK:
         case HARD_SHAPE:
            return childContains && this.clipNode.contains(point);
         case NODE_CLIP:
            Rectangle2D r = this.clipNode.getBounds();
            if (childContains && r.contains(point)) {
               BufferedImage bi = new BufferedImage(1, 1, 2);
               Graphics2D g = bi.createGraphics();
               int x = (int)point.getX();
               int y = (int)point.getY();
               g.translate(-x, -y);
               this.clipNode.render(g, new Rectangle(x, y, 1, 1), false);
               return (bi.getRGB(0, 0) >> 24 & 255) > 0;
            }
         default:
            return false;
      }
   }

   boolean hasOverlappingContents() {
      return this.currentMode == SGClip.ClippingMode.HARD_SHAPE ? true : super.hasOverlappingContents();
   }

   void clearDirty() {
      super.clearDirty();
      if (this.clipNode != null) {
         this.clipNode.clearDirty();
      }

   }

   public SGNode lookup(String id) {
      SGNode node = super.lookup(id);
      if (node == null && this.clipNode != null) {
         node = this.clipNode.lookup(id);
      }

      return node;
   }

   static {
      boolean tmpBoolean = false;

      try {
         tmpBoolean = AlphaComposite.SrcIn != AlphaComposite.Src;
      } catch (NoSuchFieldError var2) {
      }

      aaAvailable = tmpBoolean;
   }

   static enum ClippingMode {
      NO_CLIP,
      EMPTY_CLIP,
      BITMASK,
      SOFT_SHAPE,
      SOFT_RECT_SHAPE,
      HARD_SHAPE,
      NODE_CLIP,
      RECT_CLIP;
   }
}
