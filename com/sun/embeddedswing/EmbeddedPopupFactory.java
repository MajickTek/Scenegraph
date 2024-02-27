package com.sun.embeddedswing;

import java.awt.Component;
import java.awt.Point;
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

public class EmbeddedPopupFactory extends PopupFactory {
   public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
      JComponent host = SwingGlueLayer.getAncestorWithClientProperty(owner, EmbeddedToolkit.EmbeddedToolkitKey);
      if (host != null) {
         EmbeddedToolkit<?> embeddedToolkit = EmbeddedToolkit.getEmbeddedToolkit(host);
         EmbeddedPeer embeddedPeer = embeddedToolkit.getEmbeddedPeer(owner);
         if (embeddedPeer != null) {
            Point point = new Point(x, y);
            SwingUtilities.convertPointFromScreen(point, embeddedPeer.getEmbeddedComponent());
            return embeddedToolkit.getPopup(embeddedPeer, contents, point.x, point.y);
         }
      }

      return super.getPopup(owner, contents, x, y);
   }
}
