package com.sun.scenario.scenegraph;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

public abstract class SGWrapper extends SGParent {
   private SGNode rootNode;
   private List<SGNode> singletonList;

   protected void setRoot(SGNode newroot) {
      if (newroot == null) {
         throw new IllegalArgumentException("null child");
      } else {
         if (this.rootNode != newroot) {
            this.singletonList = null;
            SGNode r = this.rootNode;
            if (r != null) {
               r.visualChanged();
               this.addDirtyRegion(r.getTransformedBounds(), false);
               r.dispatchAllPendingEvents();
            }

            this.rootNode = newroot;
            newroot.setParent(this);
            this.boundsChanged();
            this.dispatchAllPendingEvents();
            this.updateCursor();
         }

      }
   }

   public SGNode getChild() {
      return this.rootNode;
   }

   public List<SGNode> getChildren() {
      if (this.rootNode == null) {
         return Collections.emptyList();
      } else {
         if (this.singletonList == null) {
            this.singletonList = Collections.singletonList(this.rootNode);
         }

         return this.singletonList;
      }
   }

   final void doTransformChanged() {
      super.doTransformChanged();
      if (this.rootNode != null) {
         this.rootNode.transformChanged();
      }

   }

   void dispatchPendingEvents() {
      super.dispatchPendingEvents();
      if (this.rootNode != null) {
         this.rootNode.dispatchPendingEvents();
      }

   }

   Rectangle2D accumulateDirtyChildren(Rectangle2D r, Rectangle2D clip) {
      if (this.rootNode != null) {
         r = this.rootNode.accumulateDirty(r, clip);
      }

      return r;
   }

   void render(Graphics2D g, Rectangle dirtyRegion, boolean clearDirty) {
      if (!this.isVisible()) {
         if (clearDirty) {
            this.clearDirty();
         }

      } else {
         if (this.rootNode != null) {
            this.rootNode.render(g, dirtyRegion, clearDirty);
         }

         if (clearDirty) {
            this.clearDirty();
         }

      }
   }

   public boolean contains(Point2D point) {
      return this.rootNode != null ? this.rootNode.contains(point) : super.contains(point);
   }

   public Rectangle2D getBounds(AffineTransform transform) {
      return (Rectangle2D)(this.rootNode == null ? new Rectangle2D.Float() : this.rootNode.getBounds(transform));
   }

   boolean hasOverlappingContents() {
      return this.rootNode == null ? false : this.rootNode.hasOverlappingContents();
   }
}
