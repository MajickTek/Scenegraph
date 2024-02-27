package com.sun.scenario.scenegraph;

import com.sun.scenario.Settings;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public abstract class SGLeaf extends SGNode {
   private static final boolean debugBounds;
   static final boolean DO_PAINT;

   public abstract void paint(Graphics2D var1);

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

         this.paint(g);
         if (debugBounds) {
            AffineTransform gtx = g.getTransform();
            Rectangle leafBounds = this.getBounds(gtx).getBounds();
            g.setTransform(new AffineTransform());
            g.setColor(Color.RED);
            g.drawRect(leafBounds.x, leafBounds.y, leafBounds.width - 1, leafBounds.height - 1);
            g.setTransform(gtx);
         }

         if (clearDirty) {
            this.clearDirty();
         }

      }
   }

   protected final void repaint(boolean boundsChanged) {
      if (boundsChanged) {
         this.boundsChanged();
         this.dispatchAllPendingEvents();
      } else {
         this.visualChanged();
      }

   }

   protected final void repaint(Rectangle2D subregionBounds) {
      if (subregionBounds == null) {
         throw new IllegalArgumentException("subregion bounds must be non-null");
      } else {
         if ((this.getDirtyState() & 2) == 0 && !subregionBounds.isEmpty()) {
            AffineTransform accumXform = this.getCumulativeTransform();
            subregionBounds = accumXform.createTransformedShape(subregionBounds).getBounds2D();
            this.markSubregionDirty(subregionBounds);
         }

      }
   }

   static {
      String pkg = SGLeaf.class.getPackage().getName();
      DO_PAINT = !Settings.getBoolean(pkg + ".skippaint");
      if (!DO_PAINT) {
         System.err.println("SGLEAF PAINTING DISABLED");
      }

      debugBounds = Settings.getBoolean(pkg + ".debugbounds");
   }
}
