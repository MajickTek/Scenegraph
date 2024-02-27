package com.sun.scenario.scenegraph;

import com.sun.scenario.effect.Effect;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

public class SGFilter extends SGParent {
   public static final int NONE = 0;
   public static final int UNTRANSFORMED = 1;
   public static final int TRANSFORMED = 2;
   public static final int BOTH = 3;
   public static final int CACHED = 4;
   public static final int CLIPPED = 8;
   private SGNode child;
   private List<SGNode> singletonList;

   public final List<SGNode> getChildren() {
      if (this.child == null) {
         return Collections.emptyList();
      } else {
         if (this.singletonList == null) {
            this.singletonList = Collections.singletonList(this.child);
         }

         return this.singletonList;
      }
   }

   public final SGNode getChild() {
      return this.child;
   }

   public void setChild(SGNode child) {
      if (child == null) {
         throw new IllegalArgumentException("null child");
      } else {
         if (child != this.child) {
            SGParent oldParent = child.getParent();
            if (oldParent != null) {
               oldParent.remove(child);
            }

            if (this.child != null) {
               this.child.setParent((Object)null);
            }

            this.singletonList = null;
            this.child = child;
            child.setParent(this);
            this.dispatchAllPendingEvents();
            this.updateCursor();
         }

      }
   }

   public void remove(SGNode node) {
      if (node == this.child) {
         this.remove();
      }

   }

   public void remove() {
      FocusHandler.removeNotify(this.child);
      SGNode c = this.child;
      if (c != null) {
         c.setParent((Object)null);
         c.dispatchAllPendingEvents();
      }

      this.child = null;
      this.singletonList = null;
      this.dispatchAllPendingEvents();
      this.updateCursor();
   }

   public void renderFromCache(Graphics2D g) {
   }

   public boolean canSkipRendering() {
      return false;
   }

   public boolean canSkipChildren() {
      return false;
   }

   public boolean canExpandBounds() {
      return false;
   }

   void doTransformChanged() {
      super.doTransformChanged();
      if (this.child != null) {
         this.child.transformChanged();
      }

   }

   void dispatchPendingEvents() {
      super.dispatchPendingEvents();
      if (this.child != null) {
         this.child.dispatchPendingEvents();
      }

   }

   Rectangle2D accumulateDirtyChildren(Rectangle2D r, Rectangle2D clip) {
      if (this.canExpandBounds()) {
         r = accumulate(r, this.getTransformedBounds(), false);
      }

      if (this.child != null) {
         r = this.child.accumulateDirty(r, clip);
      }

      return r;
   }

   public int needsSourceContent(Graphics2D g) {
      return 0;
   }

   public void setupRenderGraphics(Graphics2D g) {
   }

   public void renderFinalImage(Graphics2D g, SGSourceContent srcContent) {
   }

   void render(Graphics2D g, Rectangle dirtyRegion, boolean clearDirty) {
      if (!this.isVisible()) {
         if (clearDirty) {
            this.clearDirty();
         }

      } else {
         if (dirtyRegion != null) {
            Rectangle2D bounds = this.getTransformedBounds();
            if (bounds == null || !bounds.intersects(dirtyRegion)) {
               if (clearDirty) {
                  this.clearDirty();
               }

               return;
            }
         }

         if (this.child != null) {
            Graphics2D gOrig = (Graphics2D)g.create();
            if (this.canSkipRendering()) {
               if (!this.canSkipChildren()) {
                  this.child.render(gOrig, dirtyRegion, clearDirty);
               }
            } else {
               int sourceType = this.needsSourceContent(gOrig);
               if (sourceType == 0) {
                  this.setupRenderGraphics(gOrig);
                  this.child.render(gOrig, dirtyRegion, clearDirty);
               } else if (sourceType == 4) {
                  this.renderFromCache(gOrig);
               } else {
                  Image xformImage = null;
                  Rectangle xformBounds = null;
                  Image unxformImage = null;
                  Rectangle unxformBounds = this.child.getBounds().getBounds();
                  Rectangle childDirty = dirtyRegion == null ? null : this.child.getTransformedBounds().getBounds();
                  if (unxformBounds.isEmpty()) {
                     if (clearDirty) {
                        this.clearDirty();
                     }

                     return;
                  }

                  GraphicsConfiguration gc = gOrig.getDeviceConfiguration();
                  AffineTransform gtx = gOrig.getTransform();
                  int nodeX;
                  int nodeY;
                  int nodeW;
                  if ((sourceType & 2) != 0) {
                     xformBounds = this.child.getBounds(gtx).getBounds();
                     gOrig.setTransform(new AffineTransform());
                     Rectangle destRect;
                     if ((sourceType & 8) != 0) {
                        destRect = gOrig.getClipBounds();
                        if (destRect != null) {
                           Rectangle.intersect(xformBounds, destRect, destRect);
                           if (destRect.isEmpty()) {
                              if (clearDirty) {
                                 this.clearDirty();
                              }

                              return;
                           }
                        } else {
                           destRect = xformBounds;
                        }
                     } else {
                        destRect = xformBounds;
                     }

                     nodeX = destRect.x;
                     nodeY = destRect.y;
                     nodeW = destRect.width;
                     int nodeH = destRect.height;
                     if (this instanceof SGRenderCache) {
                        xformImage = Effect.createCompatibleImage(gc, nodeW, nodeH);
                     } else {
                        xformImage = Effect.getCompatibleImage(gc, nodeW, nodeH);
                     }

                     Graphics2D gFilter = (Graphics2D)xformImage.getGraphics();
                     AffineTransform filterXform = AffineTransform.getTranslateInstance((double)(-nodeX), (double)(-nodeY));
                     filterXform.concatenate(gtx);
                     gFilter.setTransform(filterXform);
                     this.setupRenderGraphics(gFilter);
                     this.child.render(gFilter, childDirty, clearDirty);
                     gOrig.translate(nodeX, nodeY);
                  }

                  if ((sourceType & 1) != 0) {
                     if (xformImage != null && gtx.isIdentity()) {
                        unxformImage = xformImage;
                     } else {
                        int nodeX = unxformBounds.x;
                        nodeX = unxformBounds.y;
                        nodeY = unxformBounds.width;
                        nodeW = unxformBounds.height;
                        unxformImage = Effect.getCompatibleImage(gc, nodeY, nodeW);
                        Graphics2D gFilter = (Graphics2D)unxformImage.getGraphics();
                        AffineTransform filterXform = AffineTransform.getTranslateInstance((double)(-nodeX), (double)(-nodeX));
                        gFilter.setTransform(filterXform);
                        this.setupRenderGraphics(gFilter);
                        this.child.render(gFilter, childDirty, clearDirty);
                     }
                  }

                  SGSourceContent sourceContent = new SGSourceContent(gtx, unxformImage, unxformBounds, xformImage, xformBounds);
                  this.renderFinalImage(gOrig, sourceContent);
                  if (unxformImage != null) {
                     Effect.releaseCompatibleImage(gc, unxformImage);
                  }

                  if (xformImage != null && xformImage != unxformImage) {
                     Effect.releaseCompatibleImage(gc, xformImage);
                  }
               }
            }
         }

         if (clearDirty) {
            this.clearDirty();
         }

      }
   }

   public Rectangle2D getBounds(AffineTransform transform) {
      return (Rectangle2D)(this.child == null ? new Rectangle2D.Float() : this.child.getBounds(transform));
   }

   Rectangle2D calculateAccumBounds() {
      return (Rectangle2D)(this.child == null ? new Rectangle2D.Float() : this.child.getTransformedBounds());
   }

   boolean hasOverlappingContents() {
      return this.child.hasOverlappingContents();
   }

   public SGNode lookup(String id) {
      if (id.equals(this.getID())) {
         return this;
      } else {
         return this.child != null ? this.child.lookup(id) : null;
      }
   }
}
