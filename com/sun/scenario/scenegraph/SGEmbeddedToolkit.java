package com.sun.scenario.scenegraph;

import com.sun.embeddedswing.EmbeddedPeer;
import com.sun.embeddedswing.EmbeddedToolkit;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.SwingUtilities;

public class SGEmbeddedToolkit extends EmbeddedToolkit<SGComponent.SGEmbeddedPeer> {
   private static final SGEmbeddedToolkit sgEmbeddedToolkit = new SGEmbeddedToolkit();
   private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

   public static SGEmbeddedToolkit getSGEmbeddedToolkit() {
      return sgEmbeddedToolkit;
   }

   protected EmbeddedToolkit.CoordinateHandler createCoordinateHandler(JComponent parent, Point2D point, MouseEvent mouseEvent) {
      EmbeddedToolkit.CoordinateHandler coordinateHandler = null;
      JSGPanel jsgPanel = null;
      if (parent instanceof JSGPanel) {
         jsgPanel = (JSGPanel)parent;
         SGNode node = jsgPanel.getSceneGroup();
         if (node != null) {
            List<SGNode> list = node.pick(point);
            Iterator i$ = list.iterator();

            while(i$.hasNext()) {
               SGNode leaf = (SGNode)i$.next();
               final SGComponent.SGEmbeddedPeer embeddedPeer = null;
               if (leaf instanceof SGComponent && (embeddedPeer = ((SGComponent)leaf).getEmbeddedPeer()) != null) {
                  final AffineTransform leafTransformInverse = null;

                  try {
                     leafTransformInverse = leaf.getCumulativeTransform().createInverse();
                  } catch (NoninvertibleTransformException var14) {
                     leafTransformInverse = IDENTITY_TRANSFORM;
                  }

                  coordinateHandler = new EmbeddedToolkit.CoordinateHandler() {
                     public EmbeddedPeer getEmbeddedPeer() {
                        return embeddedPeer;
                     }

                     public Point2D transform(Point2D src, Point2D dst) {
                        return leafTransformInverse.transform(src, dst);
                     }
                  };
                  break;
               }
            }
         }
      }

      return coordinateHandler;
   }

   protected SGComponent.SGEmbeddedPeer createEmbeddedPeer(JComponent parent, Component embedded, Object... args) {
      SGComponent sgComponent = (SGComponent)args[0];
      return sgComponent.new SGEmbeddedPeer(parent, embedded);
   }

   public Popup getPopup(EmbeddedPeer peer, Component contents, int x, int y) {
      SGComponent.SGEmbeddedPeer sgEmbeddedPeer = (SGComponent.SGEmbeddedPeer)peer;
      SGGroup topSGGroup = null;
      AffineTransform accTransform = new AffineTransform();

      final SGComponent sgComponent;
      SGComponent.SGEmbeddedPeer parentPeer;
      for(Point offset = new Point(x, y); sgEmbeddedPeer != null; sgEmbeddedPeer = parentPeer) {
         accTransform.preConcatenate(AffineTransform.getTranslateInstance(offset.getX(), offset.getY()));
         sgComponent = sgEmbeddedPeer.getNode();
         accTransform.preConcatenate(sgComponent.getCumulativeTransform());
         JSGPanel jsgPanel = sgComponent.getPanel();
         topSGGroup = jsgPanel.getSceneGroup();
         parentPeer = (SGComponent.SGEmbeddedPeer)getSGEmbeddedToolkit().getEmbeddedPeer(jsgPanel);
         if (parentPeer != null) {
            offset = SwingUtilities.convertPoint(sgEmbeddedPeer.getEmbeddedComponent(), 0, 0, parentPeer.getEmbeddedComponent());
         }
      }

      sgComponent = new SGComponent();
      sgComponent.setComponent(contents);
      final SGTransform.Affine sgTransform = SGTransform.createAffine(accTransform, sgComponent);
      sgTransform.setVisible(false);
      topSGGroup.add(sgTransform);
      return new Popup() {
         public void show() {
            sgTransform.setVisible(true);
         }

         public void hide() {
            SGParent parent = sgTransform.getParent();
            if (parent != null) {
               parent.remove(sgTransform);
            }

            sgComponent.setComponent((Component)null);
         }
      };
   }

   protected Runnable processMouseEvent(MouseEvent e, Point2D point, EmbeddedPeer embeddedPeer) {
      SGComponent.SGEmbeddedPeer sgEmbeddedPeer = (SGComponent.SGEmbeddedPeer)embeddedPeer;
      return sgEmbeddedPeer.getNode().getPanel().getMouseInputDispatcher().processMouseEvent(e, point, sgEmbeddedPeer.getNode());
   }
}
