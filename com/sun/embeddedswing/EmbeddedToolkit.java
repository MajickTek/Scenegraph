package com.sun.embeddedswing;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.SwingUtilities;

public abstract class EmbeddedToolkit<T extends EmbeddedPeer> {
   static final Object EmbeddedToolkitKey = new StringBuilder("EmbeddedToolkitKey");
   private static final Runnable nopRunnable = new Runnable() {
      public void run() {
      }
   };

   public abstract Popup getPopup(EmbeddedPeer var1, Component var2, int var3, int var4);

   protected abstract T createEmbeddedPeer(JComponent var1, Component var2, Object... var3);

   protected abstract CoordinateHandler createCoordinateHandler(JComponent var1, Point2D var2, MouseEvent var3);

   protected Runnable processMouseEvent(MouseEvent e, Point2D point, EmbeddedPeer embeddedPeer) {
      return nopRunnable;
   }

   public final T embed(JComponent parent, Component embedded, Object... args) {
      this.registerEmbeddedToolkit(parent);
      return this.createEmbeddedPeer(parent, embedded, args);
   }

   public final T getEmbeddedPeer(Component comp) {
      EmbeddedPeer.Shell shell = null;
      if (comp instanceof EmbeddedPeer.ShellContainer) {
         comp = ((EmbeddedPeer.ShellContainer)comp).getComponent(0);
      } else if (!(comp instanceof EmbeddedPeer.Shell)) {
         comp = SwingUtilities.getAncestorOfClass(EmbeddedPeer.Shell.class, (Component)comp);
      }

      if (comp != null) {
         shell = (EmbeddedPeer.Shell)comp;
         EmbeddedPeer embeddedPeer = shell.getEmbeddedPeer();
         if (getEmbeddedToolkit(embeddedPeer.getParentComponent()) == this) {
            return embeddedPeer;
         }
      }

      return null;
   }

   public static final boolean isEmbedded(Component comp) {
      return SwingGlueLayer.getAncestorWithClientProperty(comp, EmbeddedToolkitKey) != null;
   }

   public final void registerEmbeddedToolkit(JComponent parent) {
      if (parent != null) {
         parent.putClientProperty(EmbeddedToolkitKey, this);
      }

   }

   public static final EmbeddedToolkit<?> getEmbeddedToolkit(JComponent parent) {
      return (EmbeddedToolkit)((EmbeddedToolkit)parent.getClientProperty(EmbeddedToolkitKey));
   }

   public interface CoordinateHandler {
      EmbeddedPeer getEmbeddedPeer();

      Point2D transform(Point2D var1, Point2D var2);
   }
}
