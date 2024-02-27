package com.sun.scenario.scenegraph;

import com.sun.scenario.scenegraph.event.SGFocusListener;
import com.sun.scenario.scenegraph.event.SGKeyListener;
import com.sun.scenario.scenegraph.event.SGMouseListener;
import com.sun.scenario.scenegraph.event.SGNodeListener;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class SGNode {
   private Object parent;
   private Map<String, Object> attributeMap;
   private List<SGNodeListener> nodeListeners = null;
   private List<SGMouseListener> mouseListeners = null;
   private List<SGKeyListener> keyListeners;
   private List<SGFocusListener> focusListeners;
   private boolean visible;
   // $FF: renamed from: id java.lang.String
   private String field_12;
   private Rectangle2D bounds;
   private Rectangle2D transformedBounds;
   private AffineTransform cachedAccumXform;
   private Rectangle2D dirtyRegion;
   private Cursor cursor;
   private boolean isMouseBlocker;
   private boolean isFocusEnabled;
   static final int DIRTY_NONE = 0;
   static final int DIRTY_SUBREGION = 1;
   static final int DIRTY_VISUAL = 2;
   static final int DIRTY_BOUNDS = 4;
   static final int DIRTY_TRANSFORM = 8;
   static final int DIRTY_CHILDREN_VISUAL = 16;
   static final int DIRTY_CHILDREN_BOUNDS = 32;
   private int dirtyState;
   private boolean paused;
   private int cachedFlags;
   static final int BOUNDS_EVENT = 2;
   static final int TRANSFORMED_BOUNDS_EVENT = 4;
   private int eventFlags;

   public SGNode() {
      this.keyListeners = Collections.EMPTY_LIST;
      this.focusListeners = Collections.EMPTY_LIST;
      this.visible = true;
      this.cursor = null;
      this.isMouseBlocker = false;
      this.isFocusEnabled = true;
      this.dirtyState = 2;
      this.eventFlags = 0;
   }

   public final boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      if (this.visible != visible) {
         if (visible) {
            this.visible = true;
            if (this.parent instanceof SGParent) {
               ((SGParent)this.parent).boundsChanged();
            }

            this.markDirty(2);
         } else {
            if (this.parent instanceof SGParent) {
               ((SGParent)this.parent).boundsChanged();
            }

            this.markDirty(2);
            this.visible = false;
            FocusHandler.removeNotify(this);
         }

         this.updateCursor();
      }

   }

   public final boolean isMouseBlocker() {
      return this.isMouseBlocker;
   }

   public final void setMouseBlocker(boolean value) {
      if (this.isMouseBlocker != value) {
         this.isMouseBlocker = value;
         this.updateCursor();
      }

   }

   public final boolean isFocusEnabled() {
      return this.isFocusEnabled;
   }

   public final void setFocusEnabled(boolean enabled) {
      if (this.isFocusEnabled != enabled) {
         this.isFocusEnabled = enabled;
         if (!this.isFocusEnabled) {
            FocusHandler.removeNotify(this);
         }
      }

   }

   public final String getID() {
      return this.field_12;
   }

   public final void setID(String id) {
      this.field_12 = id;
   }

   public SGNode lookup(String id) {
      return id.equals(this.getID()) ? this : null;
   }

   public String toString() {
      return this.field_12 + " " + super.toString();
   }

   public SGParent getParent() {
      return this.parent instanceof JSGPanel ? null : (SGParent)this.parent;
   }

   void setParent(Object parent) {
      if (this.parent != parent) {
         if (this.parent instanceof SGParent) {
            SGParent p = (SGParent)this.parent;
            p.boundsChanged();
         }

         assert parent == null || parent instanceof SGParent || parent instanceof JSGPanel;

         this.parent = parent;
         if (this.parent != null) {
            this.boundsChanged();
         }

         this.transformChanged();
      }
   }

   public JSGPanel getPanel() {
      for(Object node = this.parent; node != null; node = ((SGNode)node).parent) {
         if (node instanceof JSGPanel) {
            return (JSGPanel)node;
         }
      }

      return null;
   }

   public Rectangle2D getBounds() {
      if (this.bounds == null) {
         this.bounds = this.getBounds((AffineTransform)null);
         this.dirtyState &= -37;
      }

      return this.bounds;
   }

   public abstract Rectangle2D getBounds(AffineTransform var1);

   public final Rectangle2D getTransformedBounds() {
      if (this.transformedBounds == null || (this.dirtyState & 4 | 32 | 8) != 0) {
         this.transformedBounds = this.calculateAccumBounds();
      }

      return this.transformedBounds;
   }

   Rectangle2D calculateAccumBounds() {
      return this.getBounds(this.getCumulativeTransform());
   }

   final AffineTransform getCumulativeTransform() {
      if (this.cachedAccumXform == null) {
         this.cachedAccumXform = this.updateCumulativeTransform(this.cachedAccumXform);
         this.dirtyState &= -9;
      }

      return this.cachedAccumXform;
   }

   AffineTransform updateCumulativeTransform(AffineTransform oldAccumXform) {
      return this.parent instanceof SGNode ? ((SGNode)this.parent).getCumulativeTransform() : new AffineTransform();
   }

   public Point2D globalToLocal(Point2D global, Point2D local) {
      try {
         return this.getCumulativeTransform().inverseTransform(global, (Point2D)local);
      } catch (NoninvertibleTransformException var5) {
         for(SGNode cur = this; cur != null; cur = ((SGNode)cur).getParent()) {
            if (cur instanceof SGTransform) {
               global = ((SGTransform)this).inverseTransform(global, (Point2D)local);
               local = global;
            }
         }

         if (local != global) {
            if (local == null) {
               local = new Point2D.Float();
            }

            ((Point2D)local).setLocation(global);
         }

         return (Point2D)local;
      }
   }

   public Point2D localToGlobal(Point2D local, Point2D global) {
      return this.getCumulativeTransform().transform(global, local);
   }

   public boolean contains(Point2D point) {
      if (point == null) {
         throw new IllegalArgumentException("null point");
      } else {
         Rectangle2D bounds = this.getBounds();
         return bounds.contains(point);
      }
   }

   private boolean maybeAppend(boolean b, SGNode n, List<SGNode> l) {
      if (b) {
         l.add(n);
      }

      return b;
   }

   private boolean pickRecursive(SGNode node, Point2D p, List<SGNode> rv) {
      if (node.isVisible()) {
         if (node instanceof SGLeaf) {
            return this.maybeAppend(node.contains(p), node, rv);
         }

         if (node instanceof SGParent) {
            if (node instanceof SGTransform) {
               p = ((SGTransform)node).inverseTransform(p, (Point2D)null);
            } else if (node instanceof SGPerspective) {
               p = ((SGPerspective)node).inverseTransform(p, (Point2D)null);
            } else if (node instanceof SGClip && !node.contains(p)) {
               return false;
            }

            List<SGNode> children = ((SGParent)node).getChildren();
            boolean descendantPicked = false;

            for(int i = children.size() - 1; i >= 0; --i) {
               SGNode child = (SGNode)children.get(i);
               if (this.pickRecursive(child, p, rv)) {
                  descendantPicked = true;
               }
            }

            return this.maybeAppend(descendantPicked, node, rv);
         }
      }

      return false;
   }

   public List<SGNode> pick(Point2D p) {
      List<SGNode> rv = new ArrayList();
      return (List)(this.pickRecursive(this, p, rv) ? rv : Collections.emptyList());
   }

   final void processMouseEvent(MouseEvent e) {
      if (this.mouseListeners != null && this.mouseListeners.size() > 0) {
         Iterator i$ = this.mouseListeners.iterator();

         while(i$.hasNext()) {
            SGMouseListener ml = (SGMouseListener)i$.next();
            switch (e.getID()) {
               case 500:
                  ml.mouseClicked(e, this);
                  break;
               case 501:
                  ml.mousePressed(e, this);
                  break;
               case 502:
                  ml.mouseReleased(e, this);
                  break;
               case 503:
                  ml.mouseMoved(e, this);
                  break;
               case 504:
                  ml.mouseEntered(e, this);
                  break;
               case 505:
                  ml.mouseExited(e, this);
                  break;
               case 506:
                  ml.mouseDragged(e, this);
                  break;
               case 507:
                  ml.mouseWheelMoved((MouseWheelEvent)e, this);
            }
         }
      }

   }

   public void addMouseListener(SGMouseListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("null listener");
      } else {
         if (this.mouseListeners == null) {
            this.mouseListeners = new ArrayList(1);
         }

         this.mouseListeners.add(listener);
      }
   }

   public void removeMouseListener(SGMouseListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("null listener");
      } else {
         if (this.mouseListeners != null) {
            this.mouseListeners.remove(listener);
         }

      }
   }

   public final Object getAttribute(String key) {
      if (key == null) {
         throw new IllegalArgumentException("null key");
      } else {
         return this.attributeMap == null ? null : this.attributeMap.get(key);
      }
   }

   public final void putAttribute(String key, Object value) {
      if (this.attributeMap == null) {
         this.attributeMap = new HashMap(1);
      }

      this.attributeMap.put(key, value);
   }

   void addDirtyRegion(Rectangle2D r, boolean shareable) {
      this.dirtyRegion = accumulate(this.dirtyRegion, r, shareable);
   }

   public void beginChanges() {
      this.paused = true;
   }

   public void finishChanges() {
      if (this.paused) {
         this.paused = false;
         this.markDirty(this.cachedFlags);
         if ((this.cachedFlags & 8) != 0) {
            this.transformChanged();
         }

         if ((~this.cachedFlags & 6) == 0) {
            this.markParentsDirty(48);
         } else if ((this.cachedFlags & 4) != 0) {
            this.markParentsDirty(32);
         } else if ((this.cachedFlags & 2) != 0) {
            this.markParentsDirty(16);
         }

         this.dispatchAllPendingEvents();
      }

   }

   void markDirty(int state) {
      if (this.paused) {
         this.cachedFlags |= state;
      } else {
         if ((state & 12) != 0) {
            state |= 2;
            if (this.transformedBounds != null) {
               this.addDirtyRegion(this.transformedBounds, false);
            }

            if ((state & 8) != 0) {
               this.cachedAccumXform = null;
               this.eventFlags |= 4;
            } else {
               this.bounds = null;
               this.eventFlags |= 2;
               this.eventFlags |= 4;
            }
         } else if ((state & 32) != 0) {
            state |= 16;
            this.bounds = null;
            this.eventFlags |= 2;
            this.eventFlags |= 4;
         }

         this.dirtyState |= state;
      }

   }

   final void transformChanged() {
      if (this.paused) {
         this.cachedFlags |= 8;
      } else {
         this.doTransformChanged();
      }

   }

   void doTransformChanged() {
      this.markDirty(8);
   }

   private final void markParentsDirty(int state) {
      if (!this.paused) {
         SGNode n = this;

         while(n.parent instanceof SGNode) {
            n = (SGNode)n.parent;
            if ((n.dirtyState & 16) != 0 && state == 16) {
               break;
            }

            n.markDirty(state);
         }

         if (n.parent instanceof JSGPanel && n.isVisible()) {
            ((JSGPanel)n.parent).markDirty();
         }
      }

   }

   final void boundsChanged() {
      this.markDirty(4);
      this.markParentsDirty(32);
   }

   final void visualChanged() {
      this.markDirty(2);
      this.markParentsDirty(16);
   }

   final void markSubregionDirty(Rectangle2D subregion) {
      this.addDirtyRegion(subregion, false);
      this.markDirty(1);
      this.markParentsDirty(16);
   }

   void clearDirty() {
      this.dirtyState = 0;
      this.dirtyRegion = null;
   }

   final boolean isDirty() {
      return this.dirtyState != 0;
   }

   final int getDirtyState() {
      return this.dirtyState;
   }

   final int getEventState() {
      return this.eventFlags;
   }

   static Rectangle2D accumulate(Rectangle2D accumulator, Rectangle2D newrect) {
      return accumulate(accumulator, newrect, false);
   }

   static Rectangle2D accumulate(Rectangle2D accumulator, Rectangle2D newrect, boolean newrectshareable) {
      if (newrect != null && newrect.getWidth() > 0.0 && newrect.getHeight() > 0.0) {
         if (accumulator == null) {
            accumulator = newrectshareable ? newrect : (Rectangle2D)newrect.clone();
         } else {
            accumulator.add(newrect);
         }
      }

      return accumulator;
   }

   final Rectangle2D accumulateDirty(Rectangle2D r, Rectangle2D clip) {
      if (this.dirtyRegion != null) {
         r = accumulate(r, this.dirtyRegion, false);
      }

      if ((this.dirtyState & 2) != 0) {
         r = accumulate(r, this.getTransformedBounds(), false);
      } else if ((this.dirtyState & 16) != 0) {
         r = this.accumulateDirtyChildren(r, clip);
      }

      return r;
   }

   Rectangle2D accumulateDirtyChildren(Rectangle2D r, Rectangle2D clip) {
      return r;
   }

   public final void render(Graphics2D g) {
      this.render(g, (Rectangle)null, true);
   }

   abstract void render(Graphics2D var1, Rectangle var2, boolean var3);

   public final void addNodeListener(SGNodeListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("null listener");
      } else {
         if (this.nodeListeners == null) {
            this.nodeListeners = new ArrayList(1);
         }

         this.nodeListeners.add(listener);
      }
   }

   public final void removeNodeListener(SGNodeListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("null listener");
      } else {
         if (this.nodeListeners != null) {
            this.nodeListeners.remove(listener);
         }

      }
   }

   final void dispatchAllPendingEvents() {
      if (!this.paused) {
         SGNode root = this;

         for(SGParent p = this.getParent(); p != null; p = p.getParent()) {
            root = p;
         }

         ((SGNode)root).dispatchPendingEvents();
      }

   }

   void dispatchPendingEvents() {
      if ((this.eventFlags & 4) != 0) {
         this.eventFlags &= -5;
         this.dispatchTransformEvent();
      }

      if ((this.eventFlags & 2) != 0) {
         this.eventFlags &= -3;
         this.dispatchBoundsEvent();
      }

   }

   final void dispatchBoundsEvent() {
      if (this.nodeListeners != null && this.nodeListeners.size() > 0) {
         for(int i = 0; i < this.nodeListeners.size(); ++i) {
            ((SGNodeListener)this.nodeListeners.get(i)).boundsChanged(this);
         }
      }

   }

   final void dispatchTransformEvent() {
      if (this.nodeListeners != null && this.nodeListeners.size() > 0) {
         for(int i = 0; i < this.nodeListeners.size(); ++i) {
            ((SGNodeListener)this.nodeListeners.get(i)).transformedBoundsChanged(this);
         }
      }

   }

   public final void addKeyListener(SGKeyListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("null listener");
      } else {
         if (this.keyListeners == Collections.EMPTY_LIST) {
            this.keyListeners = new ArrayList(1);
         }

         this.keyListeners.add(listener);
      }
   }

   public final void removeKeyListener(SGKeyListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("null listener");
      } else {
         this.keyListeners.remove(listener);
      }
   }

   public final void addFocusListener(SGFocusListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("null listener");
      } else {
         if (this.focusListeners == Collections.EMPTY_LIST) {
            this.focusListeners = new ArrayList(1);
         }

         this.focusListeners.add(listener);
      }
   }

   public final void removeFocusListener(SGFocusListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("null listener");
      } else {
         this.focusListeners.remove(listener);
      }
   }

   final void processKeyEvent(KeyEvent e) {
      int evid = e.getID();
      Iterator i$ = this.keyListeners.iterator();

      while(i$.hasNext()) {
         SGKeyListener listener = (SGKeyListener)i$.next();
         switch (evid) {
            case 400:
               listener.keyTyped(e, this);
               break;
            case 401:
               listener.keyPressed(e, this);
               break;
            case 402:
               listener.keyReleased(e, this);
         }
      }

   }

   final void processFocusEvent(FocusEvent e) {
      int evid = e.getID();
      Iterator i$ = this.focusListeners.iterator();

      while(i$.hasNext()) {
         SGFocusListener listener = (SGFocusListener)i$.next();
         switch (evid) {
            case 1004:
               listener.focusGained(e, this);
               break;
            case 1005:
               listener.focusLost(e, this);
         }
      }

   }

   boolean isFocusable() {
      return this.isVisible() && this.isFocusEnabled() && !this.keyListeners.isEmpty();
   }

   public final void requestFocus() {
      FocusHandler.requestFocus(this);
   }

   public final void setCursor(Cursor cursor) {
      this.cursor = cursor;
      this.updateCursor();
   }

   public final Cursor getCursor() {
      return this.cursor;
   }

   void updateCursor() {
      JSGPanel panel = this.getPanel();
      if (panel != null) {
         panel.updateCursor();
      }

   }

   boolean hasOverlappingContents() {
      return true;
   }
}
