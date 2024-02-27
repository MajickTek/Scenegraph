package com.sun.scenario.scenegraph;

import com.sun.scenario.effect.Blend;
import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.impl.ImageData;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SGGroup extends SGParent {
   private List<SGNode> children;
   private List<SGNode> childrenUnmodifiable;
   private Blend.Mode blendMode;

   public SGGroup() {
      this.blendMode = Blend.Mode.SRC_OVER;
   }

   public final List<SGNode> getChildren() {
      if (this.children == null) {
         return Collections.emptyList();
      } else {
         if (this.childrenUnmodifiable == null) {
            this.childrenUnmodifiable = Collections.unmodifiableList(this.children);
         }

         return this.childrenUnmodifiable;
      }
   }

   public void add(int index, SGNode child) {
      if (child == null) {
         throw new IllegalArgumentException("null child");
      } else {
         if (this.children == null) {
            this.children = new ArrayList(1);
         }

         if (index >= -1 && index <= this.children.size()) {
            SGParent oldParent = child.getParent();
            if (oldParent == this) {
               this.children.remove(child);
               this.boundsChanged();
            } else if (oldParent != null) {
               oldParent.remove(child);
            }

            if (index == -1) {
               this.children.add(child);
            } else {
               this.children.add(index, child);
            }

            child.setParent(this);
            this.dispatchAllPendingEvents();
            FocusHandler.addNotify(child);
            this.updateCursor();
         } else {
            throw new IndexOutOfBoundsException("invalid index");
         }
      }
   }

   public final void add(SGNode child) {
      this.add(-1, child);
   }

   public void remove(SGNode child) {
      if (child == null) {
         throw new IllegalArgumentException("null child");
      } else {
         if (this.children != null) {
            int index = this.children.indexOf(child);
            if (index >= 0) {
               this.remove(index);
            }
         }

      }
   }

   public final void remove(int index) {
      if (this.children != null) {
         SGNode child = (SGNode)this.children.get(index);
         FocusHandler.removeNotify(child);
         child.setParent((Object)null);
         this.children.remove(index);
         child.dispatchAllPendingEvents();
         this.dispatchAllPendingEvents();
         this.updateCursor();
      }

   }

   public Blend.Mode getBlendMode() {
      return this.blendMode;
   }

   public void setBlendMode(Blend.Mode blendMode) {
      if (blendMode == null) {
         throw new IllegalArgumentException("Mode must be non-null");
      } else {
         if (this.blendMode != blendMode) {
            this.blendMode = blendMode;
            this.visualChanged();
         }

      }
   }

   Rectangle2D calculateAccumBounds() {
      Rectangle2D bounds = null;
      if (this.isVisible() && this.children != null && !this.children.isEmpty()) {
         for(int i = 0; i < this.children.size(); ++i) {
            SGNode child = (SGNode)this.children.get(i);
            if (child.isVisible()) {
               Rectangle2D rc = child.getTransformedBounds();
               bounds = accumulate((Rectangle2D)bounds, rc, false);
            }
         }
      }

      if (bounds == null) {
         bounds = new Rectangle2D.Float();
      }

      return (Rectangle2D)bounds;
   }

   public final Rectangle2D getBounds(AffineTransform transform) {
      Rectangle2D bounds = null;
      if (this.isVisible() && this.children != null && !this.children.isEmpty()) {
         for(int i = 0; i < this.children.size(); ++i) {
            SGNode child = (SGNode)this.children.get(i);
            if (child.isVisible()) {
               Rectangle2D rc = child.getBounds(transform);
               bounds = accumulate((Rectangle2D)bounds, rc, true);
            }
         }
      }

      if (bounds == null) {
         bounds = new Rectangle2D.Float();
      }

      return (Rectangle2D)bounds;
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

         if (this.blendMode == Blend.Mode.SRC_OVER) {
            for(int i = 0; i < this.children.size(); ++i) {
               SGNode child = (SGNode)this.children.get(i);
               child.render(g, dirtyRegion, clearDirty);
            }
         } else {
            Blend b = new Blend(this.blendMode, (Effect)null, (Effect)null);
            GraphicsConfiguration config = g.getDeviceConfiguration();
            AffineTransform transform = g.getTransform();
            ImageData bot = null;

            for(int i = 0; i < this.children.size(); ++i) {
               SGNode child = (SGNode)this.children.get(i);
               ImageData top = NodeEffectInput.getImageDataForNode(config, child, transform);
               if (bot == null) {
                  bot = top;
               } else {
                  ImageData newbot = b.filterImageDatas(config, transform, new ImageData[]{bot, top});
                  bot.unref();
                  top.unref();
                  bot = newbot;
               }
            }

            if (bot != null) {
               g.setTransform(new AffineTransform());
               Rectangle r = bot.getBounds();
               g.drawImage(bot.getImage(), r.x, r.y, (ImageObserver)null);
               g.setTransform(transform);
               bot.unref();
            }
         }

         if (clearDirty) {
            this.clearDirty();
         }

      }
   }

   final void doTransformChanged() {
      super.doTransformChanged();
      if (this.children != null) {
         for(int i = 0; i < this.children.size(); ++i) {
            SGNode child = (SGNode)this.children.get(i);
            child.transformChanged();
         }
      }

   }

   Rectangle2D accumulateDirtyChildren(Rectangle2D r, Rectangle2D clip) {
      if (this.children != null) {
         for(int i = 0; i < this.children.size(); ++i) {
            SGNode child = (SGNode)this.children.get(i);
            r = child.accumulateDirty(r, clip);
         }
      }

      return r;
   }

   void dispatchPendingEvents() {
      super.dispatchPendingEvents();
      if (this.children != null) {
         for(int i = 0; i < this.children.size(); ++i) {
            SGNode child = (SGNode)this.children.get(i);
            if (child.getEventState() != 0) {
               child.dispatchPendingEvents();
            }
         }
      }

   }

   boolean hasOverlappingContents() {
      if (this.blendMode != Blend.Mode.SRC_OVER) {
         return false;
      } else {
         int n = this.children == null ? 0 : this.children.size();
         if (n == 1) {
            return ((SGNode)this.children.get(0)).hasOverlappingContents();
         } else {
            return n != 0;
         }
      }
   }
}
