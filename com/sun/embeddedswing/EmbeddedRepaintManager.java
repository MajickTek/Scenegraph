package com.sun.embeddedswing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

class EmbeddedRepaintManager extends RepaintManager {
   private RepaintManager delegate;
   private static final Rectangle NULL_RECTANGLE = new Rectangle();
   private static final Dimension NULL_DIMENSION = new Dimension();

   public EmbeddedRepaintManager(RepaintManager repaintManager) {
      this.delegate = repaintManager;
   }

   void setDelegate(RepaintManager manager) {
      this.delegate = manager;
   }

   public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
      EmbeddedPeer.Shell shell = (EmbeddedPeer.Shell)SwingUtilities.getAncestorOfClass(EmbeddedPeer.Shell.class, c);
      if (shell != null) {
         Point p = SwingUtilities.convertPoint(c, new Point(x, y), shell);
         shell.repaint(p.x, p.y, w, h);
      } else if (this.delegate != null) {
         this.delegate.addDirtyRegion(c, x, y, w, h);
      }

   }

   public void addInvalidComponent(JComponent invalidComponent) {
      if (this.delegate != null && SwingUtilities.getAncestorOfClass(EmbeddedPeer.Shell.class, invalidComponent) == null) {
         if (this.delegate != null) {
            this.delegate.addInvalidComponent(invalidComponent);
         }

      }
   }

   public Rectangle getDirtyRegion(JComponent component) {
      return this.delegate != null && SwingUtilities.getAncestorOfClass(EmbeddedPeer.Shell.class, component) == null ? this.delegate.getDirtyRegion(component) : NULL_RECTANGLE;
   }

   public Dimension getDoubleBufferMaximumSize() {
      return this.delegate != null ? this.delegate.getDoubleBufferMaximumSize() : NULL_DIMENSION;
   }

   public Image getOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
      return this.delegate != null && SwingUtilities.getAncestorOfClass(EmbeddedPeer.Shell.class, c) == null ? this.delegate.getOffscreenBuffer(c, proposedWidth, proposedHeight) : null;
   }

   public Image getVolatileOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {
      return this.delegate != null && SwingUtilities.getAncestorOfClass(EmbeddedPeer.Shell.class, c) == null ? this.delegate.getVolatileOffscreenBuffer(c, proposedWidth, proposedHeight) : null;
   }

   public boolean isCompletelyDirty(JComponent component) {
      return this.delegate != null && SwingUtilities.getAncestorOfClass(EmbeddedPeer.Shell.class, component) == null ? this.delegate.isCompletelyDirty(component) : false;
   }

   public boolean isDoubleBufferingEnabled() {
      return this.delegate != null ? this.delegate.isDoubleBufferingEnabled() : false;
   }

   public void markCompletelyClean(JComponent component) {
      if (this.delegate != null && SwingUtilities.getAncestorOfClass(EmbeddedPeer.Shell.class, component) == null) {
         this.delegate.markCompletelyClean(component);
      }

   }

   public void markCompletelyDirty(JComponent component) {
      EmbeddedPeer.Shell shell = (EmbeddedPeer.Shell)SwingUtilities.getAncestorOfClass(EmbeddedPeer.Shell.class, component);
      if (shell != null) {
         Point p = SwingUtilities.convertPoint(component, new Point(0, 0), shell);
         shell.repaint(p.x, p.y, component.getWidth(), component.getHeight());
      } else {
         if (this.delegate != null) {
            this.delegate.markCompletelyDirty(component);
         }

      }
   }

   public void paintDirtyRegions() {
      if (this.delegate != null) {
         this.delegate.paintDirtyRegions();
      }

   }

   public void removeInvalidComponent(JComponent component) {
      if (this.delegate != null && SwingUtilities.getAncestorOfClass(EmbeddedPeer.Shell.class, component) == null) {
         this.delegate.removeInvalidComponent(component);
      }

   }

   public void setDoubleBufferingEnabled(boolean flag) {
      if (this.delegate != null) {
         this.delegate.setDoubleBufferingEnabled(flag);
      }

   }

   public void setDoubleBufferMaximumSize(Dimension d) {
      if (this.delegate != null) {
         this.delegate.setDoubleBufferMaximumSize(d);
      }

   }

   public void validateInvalidComponents() {
      if (this.delegate != null) {
         this.delegate.validateInvalidComponents();
      }

   }
}
