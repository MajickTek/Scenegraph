package com.sun.scenario.scenegraph;

import com.sun.scenario.effect.Effect;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SGEffect extends SGFilter {
   private final EffectListener listener = new EffectListener();
   private Effect effect;

   public Effect getEffect() {
      return this.effect;
   }

   public void setEffect(Effect effect) {
      if (this.effect != null) {
         this.effect.removePropertyChangeListener(this.listener);
      }

      this.effect = effect;
      if (this.effect != null) {
         this.effect.addPropertyChangeListener(this.listener);
      }

      this.boundsChanged();
      this.dispatchAllPendingEvents();
   }

   public Rectangle2D getBounds(AffineTransform xform) {
      SGNode child = this.getChild();
      if (child == null) {
         return new Rectangle2D.Float();
      } else if (this.effect == null) {
         return child.getBounds(xform);
      } else {
         NodeEffectInput nodeinput = new NodeEffectInput(child);
         return this.effect.getBounds(xform, nodeinput);
      }
   }

   Rectangle2D calculateAccumBounds() {
      return this.getBounds(this.getCumulativeTransform());
   }

   public boolean canExpandBounds() {
      return this.effect != null;
   }

   void markDirty(int state) {
      super.markDirty(state);
      if ((state & 32) != 0) {
         this.boundsChanged();
      }

      if ((state & 16) != 0) {
         this.visualChanged();
      }

   }

   void render(Graphics2D g, Rectangle dirtyRegion, boolean clearDirty) {
      if (this.isVisible()) {
         if (dirtyRegion != null) {
            Rectangle2D bounds = this.getTransformedBounds();
            if (bounds == null || !bounds.intersects(dirtyRegion)) {
               return;
            }
         }

         SGNode child = this.getChild();
         if (child != null) {
            if (this.effect == null) {
               child.render(g, dirtyRegion, clearDirty);
            } else {
               NodeEffectInput nodeinput = new NodeEffectInput(child);
               this.effect.render(g, 0.0F, 0.0F, nodeinput);
               nodeinput.flush();
            }
         }

         if (clearDirty) {
            this.clearDirty();
         }

      }
   }

   boolean hasOverlappingContents() {
      return this.effect != null ? false : super.hasOverlappingContents();
   }

   private class EffectListener implements PropertyChangeListener {
      private EffectListener() {
      }

      public void propertyChange(PropertyChangeEvent evt) {
         SGEffect.this.boundsChanged();
         SGEffect.this.dispatchAllPendingEvents();
      }
   }
}
