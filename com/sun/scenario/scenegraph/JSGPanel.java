package com.sun.scenario.scenegraph;

import com.sun.embeddedswing.EmbeddedToolkit;
import com.sun.scenario.Settings;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class JSGPanel extends JComponent {
   private static final boolean incrementalRepaintOpt;
   private static final Logger fpsLog = Logger.getLogger("com.sun.scenario.animation.fps");
   private static final String TRACKFPS_PROP = "com.sun.scenario.animation.trackfps";
   private static boolean trackFPS = Settings.getBoolean("com.sun.scenario.animation.trackfps", false);
   private FPSData fpsData;
   private static PropertyChangeListener pcl = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent event) {
         if (event.getPropertyName().equals("com.sun.scenario.animation.trackfps")) {
            JSGPanel.trackFPS = Settings.getBoolean("com.sun.scenario.animation.trackfps");
         }

      }
   };
   private static final boolean hiliteDirty = false;
   private SGNode scene = null;
   private boolean sceneIsNew = false;
   private SGGroup sceneGroup = null;
   private Dimension validatedPreferredSize = null;
   private final MouseInputDispatcher dispatchMouseEvents;
   private Cursor defaultCursor = null;
   private Point2D mousePoint = null;
   private SGNode focusOwner = null;
   private Rectangle dmgrect;
   public static final Object VSYNC_ENABLED_KEY;
   private final HierarchyListener hierarchyListener = new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
         Component root = null;
         if ((e.getChangeFlags() & 1L) != 0L && (Boolean.TRUE.equals(JSGPanel.this.getClientProperty(JSGPanel.VSYNC_ENABLED_KEY)) || Settings.getBoolean("com.sun.scenario.animation.vsync")) && !Settings.getBoolean("com.sun.scenario.animation.fullspeed") && !EmbeddedToolkit.isEmbedded(JSGPanel.this) && (root = SwingUtilities.getRoot(JSGPanel.this)) != null) {
            try {
               JSGPanel.setVsyncRequested_METHOD.invoke(JSGPanel.SwingUtilities3_CLASS, root, Boolean.TRUE);
            } catch (IllegalArgumentException var4) {
            } catch (IllegalAccessException var5) {
            } catch (InvocationTargetException var6) {
            }
         }

      }
   };
   private static final Class<?> SwingUtilities3_CLASS;
   private static final Method setVsyncRequested_METHOD;

   public JSGPanel() {
      this.setOpaque(true);
      this.dispatchMouseEvents = new MouseInputDispatcher();
      this.addMouseListener(this.dispatchMouseEvents);
      this.addMouseMotionListener(this.dispatchMouseEvents);
      this.addMouseWheelListener(this.dispatchMouseEvents);

      try {
         this.setFocusTraversalPolicyProvider(true);
      } catch (NoSuchMethodError var2) {
         this.setFocusCycleRoot(true);
      }

      this.setFocusTraversalPolicy(FocusHandler.getFocusTraversalPolicy());
      this.initVsyncedPainting();
   }

   SGGroup getSceneGroup() {
      return this.sceneGroup;
   }

   public final SGNode getScene() {
      return this.scene;
   }

   void removeScene() {
      this.scene = null;
      this.sceneGroup = null;
      this.fpsData = null;
      this.markDirty();
   }

   public void setScene(SGNode scene) {
      JSGPanel oldpanel = scene.getPanel();
      if (oldpanel != null && oldpanel.getScene() == scene) {
         oldpanel.removeScene();
      }

      SGParent oldParent = scene.getParent();
      if (oldParent != null) {
         oldParent.remove(scene);
      }

      if (trackFPS || fpsLog.isEnabled(Logger.Level.MESSAGE)) {
         this.fpsData = new FPSData();
      }

      this.scene = scene;
      this.sceneIsNew = true;
      this.sceneGroup = new SGGroup();
      this.sceneGroup.add(scene);
      this.sceneGroup.setParent(this);
      this.sceneGroup.dispatchAllPendingEvents();
      this.updateCursor();
      FocusHandler.addNotify(scene);
      this.markDirty();
   }

   public Dimension getPreferredSize() {
      if (this.isPreferredSizeSet()) {
         return super.getPreferredSize();
      } else {
         Insets insets = this.getInsets();
         int dx = insets.left + insets.right;
         int dy = insets.top + insets.bottom;
         SGNode root = this.getScene();
         if (root == null) {
            return new Dimension(dx + 640, dy + 480);
         } else {
            Rectangle r = root.getTransformedBounds().getBounds();
            return new Dimension(dx + r.x + r.width, dy + r.y + r.height);
         }
      }
   }

   MouseInputDispatcher getMouseInputDispatcher() {
      return this.dispatchMouseEvents;
   }

   public void setCursor(Cursor cursor) {
      this.setCursor(cursor, true);
   }

   private void setCursor(Cursor cursor, boolean isDefault) {
      if (isDefault) {
         this.defaultCursor = cursor;
      }

      super.setCursor(cursor);
   }

   void updateCursor() {
      if (this.mousePoint != null && this.sceneGroup != null) {
         List<SGNode> nodes = this.sceneGroup.pick(this.mousePoint);
         this.updateCursor(nodes);
      }

   }

   void updateCursor(List<SGNode> nodes) {
      if (this.mousePoint != null) {
         Cursor cursor = null;
         Iterator i$ = nodes.iterator();

         while(i$.hasNext()) {
            SGNode node = (SGNode)i$.next();
            cursor = node.getCursor();
            if (cursor != null || node.isMouseBlocker()) {
               break;
            }
         }

         cursor = cursor == null ? this.defaultCursor : cursor;
         this.setCursor(cursor, false);
      }
   }

   final void setFocusOwner(SGNode newFocusOwner) {
      SGNode oldFocusOwner = this.focusOwner;
      this.focusOwner = newFocusOwner;
      this.firePropertyChange("focusOwner", oldFocusOwner, this.focusOwner);
   }

   public final SGNode getFocusOwner() {
      return this.focusOwner;
   }

   protected void paintBackground(Graphics g) {
      if (this.isOpaque()) {
         Graphics g1 = g.create();
         g1.setColor(this.getBackground());
         Rectangle r = g.getClipBounds();
         if (r == null) {
            r = new Rectangle(0, 0, this.getWidth(), this.getHeight());
         }

         g1.fillRect(r.x, r.y, r.width, r.height);
         g1.dispose();
      }

   }

   protected void paintComponent(Graphics g) {
      Rectangle dirtyRegion = g.getClipBounds();
      if (dirtyRegion == null) {
         dirtyRegion = new Rectangle(0, 0, this.getWidth(), this.getHeight());
      }

      if (!dirtyRegion.isEmpty()) {
         Graphics2D g2 = (Graphics2D)g.create();
         this.paintBackground(g2);
         SGNode root = this.getSceneGroup();
         if (root != null) {
            root.render(g2, incrementalRepaintOpt ? dirtyRegion : null, true);
         }

         g2.dispose();
      }

      if (this.dmgrect != null) {
         Graphics g2 = g.create();
         g2.setXORMode(this.getBackground());
         g2.setColor(Color.red);
         g2.fillRect(this.dmgrect.x, this.dmgrect.y, this.dmgrect.width, this.dmgrect.height);
         g2.dispose();
      }

   }

   final void repaintDirtyRegions(boolean immediately) {
      if (this.getSceneGroup() != null && this.getSceneGroup().isDirty()) {
         Rectangle clip = new Rectangle(0, 0, this.getWidth(), this.getHeight());
         Rectangle dirtyRegion = null;
         if (incrementalRepaintOpt) {
            dirtyRegion = this.accumulateDirtyRegions(clip);
            if (this.sceneIsNew) {
               dirtyRegion = clip;
            }
         } else {
            dirtyRegion = clip;
         }

         this.sceneIsNew = false;
         if (dirtyRegion != null) {
            if (immediately) {
               this.paintImmediately(dirtyRegion);
            } else {
               this.repaint(dirtyRegion);
            }

            if (trackFPS || fpsLog.isEnabled(Logger.Level.MESSAGE)) {
               if (this.fpsData == null) {
                  this.fpsData = new FPSData();
               }

               this.fpsData.nextFrame();
            }
         }

         if (!this.isPreferredSizeSet()) {
            Dimension d = this.getPreferredSize();
            if (!d.equals(this.validatedPreferredSize)) {
               this.validatedPreferredSize = d;
               this.revalidate();
            }
         }
      }

   }

   final void markDirty() {
      JSGPanelRepainter.getJSGPanelRepainter().addDirtyPanel(this);
   }

   private Rectangle accumulateDirtyRegions(Rectangle clip) {
      assert this.getSceneGroup() != null;

      Rectangle2D dirtyRegion = this.getSceneGroup().accumulateDirty((Rectangle2D)null, clip);
      return dirtyRegion != null && !dirtyRegion.isEmpty() ? dirtyRegion.getBounds() : null;
   }

   public Icon toIcon() {
      return null;
   }

   public BufferedImage getIconImage() {
      return null;
   }

   private void initVsyncedPainting() {
      if (setVsyncRequested_METHOD != null) {
         this.addHierarchyListener(this.hierarchyListener);
      }

   }

   static {
      String pkg = JSGPanel.class.getPackage().getName();
      incrementalRepaintOpt = !Settings.getBoolean(pkg + ".fullrepaint");
      Settings.addPropertyChangeListener("com.sun.scenario.animation.trackfps", pcl);
      VSYNC_ENABLED_KEY = new StringBuilder("VSYNC_ENABLED_KEY");
      Class<?> tmpClass = null;
      Method tmpMethod = null;

      try {
         tmpClass = Class.forName("com.sun.java.swing.SwingUtilities3");
         tmpMethod = tmpClass.getMethod("setVsyncRequested", Container.class, Boolean.TYPE);
      } catch (ClassNotFoundException var3) {
      } catch (NoSuchMethodException var4) {
      }

      if (tmpMethod == null) {
         tmpClass = null;
      }

      SwingUtilities3_CLASS = tmpClass;
      setVsyncRequested_METHOD = tmpMethod;
   }

   private class FPSData {
      private static final long PRINT_FREQ = 3000L;
      private long prevMillis = System.currentTimeMillis();
      private long frames = 0L;
      private float fps;

      public FPSData() {
      }

      public void nextFrame() {
         long now = System.currentTimeMillis();
         long dur = now - this.prevMillis;
         if (dur > 3000L) {
            float oldFps = this.fps;
            this.fps = 1000.0F * (float)this.frames / (float)dur;
            if (JSGPanel.fpsLog.isEnabled(Logger.Level.MESSAGE)) {
               JSGPanel.fpsLog.message("%.2f FPS", this.fps);
            }

            if (JSGPanel.trackFPS) {
               JSGPanel.this.firePropertyChange("fpsData", oldFps, this.fps);
            }

            this.prevMillis = now;
            this.frames = 0L;
         } else {
            ++this.frames;
         }

      }
   }

   class MouseInputDispatcher implements MouseListener, MouseMotionListener, MouseWheelListener {
      private int buttonsPressed = 0;
      private List<SGNode> pdrNodes = null;
      private List<SGNode> enterNodes = Collections.emptyList();

      private List<SGNode> uptoMouseBlocker(List<SGNode> nodes) {
         List<SGNode> rv = nodes;

         for(int i = 0; i < nodes.size() - 1; ++i) {
            if (((SGNode)nodes.get(i)).isMouseBlocker()) {
               rv = nodes.subList(0, i + 1);
               break;
            }
         }

         return rv;
      }

      private void deliver(MouseEvent e, List<SGNode> nodes) {
         this.deliver(e, nodes, (SGComponent)null);
      }

      private Runnable deliver(final MouseEvent e, List<SGNode> nodes, SGComponent sgComponent) {
         Runnable rv = null;
         if (nodes != null) {
            for(int i = 0; i < nodes.size(); ++i) {
               SGNode node = (SGNode)nodes.get(i);
               if (sgComponent != null && node == sgComponent) {
                  final List<SGNode> tail = nodes.subList(i, nodes.size());
                  rv = new Runnable() {
                     public void run() {
                        MouseInputDispatcher.this.deliver(e, tail, (SGComponent)null);
                     }
                  };
                  break;
               }

               node.processMouseEvent(e);
            }
         }

         return rv;
      }

      private MouseEvent createEvent(MouseEvent e, int id) {
         return new MouseEvent(JSGPanel.this, id, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
      }

      private void deliverEnterExit(MouseEvent e, List<SGNode> nodes) {
         HashSet<SGNode> nodesHash = new HashSet(nodes);
         Iterator i$ = this.enterNodes.iterator();

         SGNode n;
         while(i$.hasNext()) {
            n = (SGNode)i$.next();
            if (!nodesHash.contains(n)) {
               n.processMouseEvent(this.createEvent(e, 505));
            }
         }

         nodesHash.clear();
         nodesHash.addAll(this.enterNodes);
         i$ = nodes.iterator();

         while(i$.hasNext()) {
            n = (SGNode)i$.next();
            if (!nodesHash.contains(n)) {
               n.processMouseEvent(this.createEvent(e, 504));
            }
         }

         this.enterNodes = nodes;
      }

      public void mousePressed(MouseEvent e) {
         this.mousePressed(e, (Point2D)null, (SGComponent)null);
      }

      private Runnable mousePressed(final MouseEvent e, Point2D _point, SGComponent sgComponent) {
         final Runnable rv = null;
         SGGroup sceneGroup = JSGPanel.this.getSceneGroup();
         if (sceneGroup != null) {
            Point2D point = _point == null ? e.getPoint() : _point;
            final List<SGNode> nodes = this.uptoMouseBlocker(sceneGroup.pick((Point2D)point));
            if (this.buttonsPressed == 0) {
               this.pdrNodes = nodes;
            }

            this.buttonsPressed |= 1 << e.getButton();
            rv = this.deliver(e, this.pdrNodes, sgComponent);
            if (rv != null) {
               rv = new Runnable() {
                  public void run() {
                     rv.run();
                     MouseInputDispatcher.this.deliverEnterExit(e, nodes);
                  }
               };
            } else {
               this.deliverEnterExit(e, nodes);
            }
         }

         return rv;
      }

      public void mouseDragged(MouseEvent e) {
         this.mouseDragged(e, (Point2D)null, (SGComponent)null);
      }

      private Runnable mouseDragged(final MouseEvent e, Point2D _point, SGComponent sgComponent) {
         final Runnable rv = null;
         SGGroup sceneGroup = JSGPanel.this.getSceneGroup();
         if (sceneGroup != null) {
            Point2D point = _point == null ? e.getPoint() : _point;
            final List<SGNode> nodes = this.uptoMouseBlocker(sceneGroup.pick((Point2D)point));
            if (this.pdrNodes != null) {
               rv = this.deliver(e, this.pdrNodes, sgComponent);
               if (rv != null) {
                  rv = new Runnable() {
                     public void run() {
                        rv.run();
                        MouseInputDispatcher.this.deliverEnterExit(e, nodes);
                     }
                  };
               }
            }

            if (rv == null) {
               this.deliverEnterExit(e, nodes);
            }
         }

         return rv;
      }

      public void mouseReleased(MouseEvent e) {
         this.mouseReleased(e, (Point2D)null, (SGComponent)null);
      }

      private Runnable mouseReleased(final MouseEvent e, Point2D _point, SGComponent sgComponent) {
         final Runnable rv = null;
         SGGroup sceneGroup = JSGPanel.this.getSceneGroup();
         if (sceneGroup != null) {
            Point2D point = _point == null ? e.getPoint() : _point;
            this.buttonsPressed &= ~(1 << e.getButton());
            rv = this.deliver(e, this.pdrNodes, sgComponent);
            final List<SGNode> nodes = this.uptoMouseBlocker(sceneGroup.pick((Point2D)point));
            if (rv != null) {
               rv = new Runnable() {
                  public void run() {
                     rv.run();
                     MouseInputDispatcher.this.deliverEnterExit(e, nodes);
                  }
               };
            } else {
               this.deliverEnterExit(e, nodes);
            }
         }

         return rv;
      }

      public void mouseClicked(MouseEvent e) {
         this.mouseClicked(e, (Point2D)null, (SGComponent)null);
      }

      private Runnable mouseClicked(MouseEvent e, Point2D point, SGComponent sgComponent) {
         return this.deliver(e, this.pdrNodes, sgComponent);
      }

      public void mouseMoved(MouseEvent e) {
         this.mouseMoved(e, (Point2D)null, (SGComponent)null);
      }

      private Runnable mouseMoved(MouseEvent e, Point2D point, SGComponent sgComponent) {
         Runnable rv = null;
         SGGroup sceneGroup = JSGPanel.this.getSceneGroup();
         JSGPanel.this.mousePoint = (Point2D)(point == null ? e.getPoint() : point);
         if (sceneGroup != null) {
            List<SGNode> nodes = this.uptoMouseBlocker(sceneGroup.pick(JSGPanel.this.mousePoint));
            JSGPanel.this.updateCursor(nodes);
            this.deliverEnterExit(e, nodes);
            rv = this.deliver(e, nodes, sgComponent);
         }

         return rv;
      }

      public void mouseEntered(MouseEvent e) {
         this.mouseEntered(e, (Point2D)null, (SGComponent)null);
      }

      private Runnable mouseEntered(MouseEvent e, Point2D point, SGComponent sgComponent) {
         return null;
      }

      public void mouseExited(MouseEvent e) {
         this.mouseExited(e, (Point2D)null, (SGComponent)null);
      }

      private Runnable mouseExited(MouseEvent e, Point2D _point, SGComponent sgCompnent) {
         SGGroup sceneGroup = JSGPanel.this.getSceneGroup();
         if (sceneGroup != null) {
            Point2D point = _point == null ? e.getPoint() : _point;
            if (!sceneGroup.pick((Point2D)point).isEmpty()) {
               return null;
            }
         }

         List<SGNode> emptyList = Collections.emptyList();
         this.deliverEnterExit(e, emptyList);
         return null;
      }

      public void mouseWheelMoved(MouseWheelEvent e) {
         this.mouseWheelMoved(e, (Point2D)null, (SGComponent)null);
      }

      private Runnable mouseWheelMoved(MouseWheelEvent e, Point2D _point, SGComponent sgComponent) {
         Runnable rv = null;
         SGGroup sceneGroup = JSGPanel.this.getSceneGroup();
         if (sceneGroup != null) {
            Point2D point = _point == null ? e.getPoint() : _point;
            List<SGNode> nodes = this.uptoMouseBlocker(sceneGroup.pick((Point2D)point));
            this.deliverEnterExit(e, nodes);
            rv = this.deliver(e, nodes, sgComponent);
         }

         return rv;
      }

      Runnable processMouseEvent(MouseEvent e, Point2D point, SGComponent sgComponent) {
         Runnable rv = null;
         switch (e.getID()) {
            case 500:
               rv = this.mouseClicked(e, point, sgComponent);
               break;
            case 501:
               rv = this.mousePressed(e, point, sgComponent);
               break;
            case 502:
               rv = this.mouseReleased(e, point, sgComponent);
               break;
            case 503:
               rv = this.mouseMoved(e, point, sgComponent);
               break;
            case 504:
               rv = this.mouseEntered(e, point, sgComponent);
               break;
            case 505:
               rv = this.mouseExited(e, point, sgComponent);
               break;
            case 506:
               rv = this.mouseDragged(e, point, sgComponent);
               break;
            case 507:
               rv = this.mouseWheelMoved((MouseWheelEvent)e, point, sgComponent);
         }

         return rv;
      }
   }
}
