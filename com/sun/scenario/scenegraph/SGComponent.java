package com.sun.scenario.scenegraph;

import com.sun.embeddedswing.EmbeddedPeer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;

public class SGComponent extends SGLeaf {
   private static final Logger logger = Logger.getLogger(SGComponent.class.getName());
   private static final Logger focusLogger = Logger.getLogger(SGComponent.class.getName());
   private SGEmbeddedPeer embeddedPeer = null;
   private Dimension size = null;

   public final Component getComponent() {
      return this.embeddedPeer == null ? null : this.embeddedPeer.getEmbeddedComponent();
   }

   void setParent(Object parent) {
      if (this.embeddedPeer != null && parent == null) {
         this.embeddedPeer.setParentComponent((JComponent)null);
      }

      super.setParent(parent);
   }

   public void setComponent(Component component) {
      if (this.embeddedPeer != null) {
         this.embeddedPeer.dispose();
         this.embeddedPeer = null;
      }

      if (component != null) {
         this.embeddedPeer = (SGEmbeddedPeer)SGEmbeddedToolkit.getSGEmbeddedToolkit().embed(this.getPanel(), component, new Object[]{this});
         this.embeddedPeer.setSize(this.getSize());
         this.embeddedPeer.getShellPanel().setFocusCycleRoot(true);
         this.embeddedPeer.getShellPanel().setFocusTraversalPolicy(FocusHandler.getFocusTraversalPolicy());
      }

      this.repaint(true);
   }

   public Dimension getSize() {
      return this.size;
   }

   public void setSize(Dimension size) {
      this.size = size;
      if (this.embeddedPeer != null) {
         this.embeddedPeer.setSize(size);
         this.embeddedPeer.validate();
      }

   }

   public void setSize(int width, int height) {
      this.setSize(new Dimension(width, height));
   }

   public void paint(Graphics2D g) {
      if (DO_PAINT) {
         try {
            this.embeddedPeer.paint(g);
         } catch (NullPointerException var3) {
            if (g.getTransform().getDeterminant() != 0.0) {
               throw var3;
            }
         }
      }

   }

   public final Rectangle2D getBounds(AffineTransform transform) {
      if (this.embeddedPeer == null) {
         return new Rectangle2D.Float();
      } else {
         JSGPanel panel = this.getPanel();
         if (panel != null) {
            this.embeddedPeer.setParentComponent(panel);
         }

         this.embeddedPeer.setSizeUpdateEnabled(false);
         this.embeddedPeer.validate();
         this.embeddedPeer.setSizeUpdateEnabled(true);
         Rectangle2D bounds = new Rectangle(0, 0, this.embeddedPeer.getShellPanel().getWidth(), this.embeddedPeer.getShellPanel().getHeight());
         if (transform != null && !transform.isIdentity()) {
            bounds = transform.createTransformedShape((Shape)bounds).getBounds2D();
         }

         return (Rectangle2D)bounds;
      }
   }

   boolean isFocusable() {
      return this.isVisible() && this.getComponent() != null && this.isFocusEnabled();
   }

   boolean hasOverlappingContents() {
      return true;
   }

   SGEmbeddedPeer getEmbeddedPeer() {
      return this.embeddedPeer;
   }

   class SGEmbeddedPeer extends EmbeddedPeer {
      private boolean sizeUpdateEnabled = true;

      SGEmbeddedPeer(JComponent parent, Component embedded) {
         super(parent, embedded);
      }

      public void repaint(final int x, final int y, final int width, final int height) {
         if (EventQueue.isDispatchThread()) {
            this.repaintOnEDT(x, y, width, height);
         } else {
            EventQueue.invokeLater(new Runnable() {
               public void run() {
                  SGEmbeddedPeer.this.repaintOnEDT(x, y, width, height);
               }
            });
         }

      }

      private void repaintOnEDT(int x, int y, int width, int height) {
         if (SGComponent.this.getComponent() == null) {
            SGComponent.this.repaint(false);
         } else {
            int compw = SGComponent.this.getComponent().getWidth();
            int comph = SGComponent.this.getComponent().getHeight();
            int x0 = Math.max(x, 0);
            int y0 = Math.max(y, 0);
            int x1 = Math.min(x + width, compw);
            int y1 = Math.min(y + height, comph);
            if (x0 == 0 && y0 == 0 && x1 == compw && y1 == comph) {
               SGComponent.this.repaint(false);
            } else if (x1 > x0 && y1 > y0) {
               Rectangle2D rectangle = new Rectangle2D.Float((float)x0, (float)y0, (float)(x1 - x0), (float)(y1 - y0));
               SGComponent.this.repaint(rectangle);
            }

         }
      }

      void setSizeUpdateEnabled(boolean isEnabled) {
         this.sizeUpdateEnabled = isEnabled;
      }

      protected void sizeChanged(Dimension oldSize, Dimension newSize) {
         if (this.sizeUpdateEnabled) {
            if (EventQueue.isDispatchThread()) {
               SGComponent.this.repaint(true);
            } else {
               EventQueue.invokeLater(new Runnable() {
                  public void run() {
                     SGComponent.this.repaint(true);
                  }
               });
            }

         }
      }

      protected SGEmbeddedToolkit getEmbeddedToolkit() {
         return SGEmbeddedToolkit.getSGEmbeddedToolkit();
      }

      SGComponent getNode() {
         return SGComponent.this;
      }
   }
}
