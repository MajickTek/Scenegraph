package com.sun.scenario.scenegraph;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.im.InputMethodRequests;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.Timer;

class FocusHandler {
   private static final Logger logger = Logger.getLogger(FocusHandler.class.getName());
   private static final SGFocusTraversalPolicy focusTraversalPolicy = new SGFocusTraversalPolicy();
   private static final Set<SGNode> focusRequestPostponed = new HashSet();

   static FocusTraversalPolicy getFocusTraversalPolicy() {
      return focusTraversalPolicy;
   }

   static void requestFocus(SGNode toFocus) {
      if (toFocus == null) {
         throw new IllegalArgumentException("toFocus  should not be null");
      } else {
         Component toFocusComponent = null;
         JSGPanel jsgPanel = toFocus.getPanel();
         if (jsgPanel != null) {
            if (toFocus instanceof SGComponent) {
               toFocusComponent = ((SGComponent)toFocus).getComponent();
            } else {
               toFocusComponent = createFocusOwnerContainer(toFocus);
            }
         }

         if (toFocusComponent != null) {
            ((Component)toFocusComponent).requestFocusInWindow();
         } else {
            focusRequestPostponed.add(toFocus);
         }

      }
   }

   private static SGNode getLastLeaf(SGNode top) {
      SGNode node = top;
      if (top instanceof SGParent) {
         SGParent group = (SGParent)top;
         List<SGNode> children = group.getChildren();
         if (!children.isEmpty()) {
            int childIndex = children.size() - 1;
            node = getLastLeaf((SGNode)children.get(childIndex));
         }
      }

      return node;
   }

   private static boolean checkPostponedFocusRequest(final SGNode node) {
      boolean rv = false;
      if (focusRequestPostponed.contains(node)) {
         focusRequestPostponed.remove(node);
         Timer timer = new Timer(333, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               FocusHandler.requestFocus(node);
            }
         });
         timer.setRepeats(false);
         timer.start();
         rv = true;
      } else if (node instanceof SGParent) {
         Iterator i$ = ((SGParent)node).getChildren().iterator();

         while(i$.hasNext()) {
            SGNode child = (SGNode)i$.next();
            rv = checkPostponedFocusRequest(child);
            if (rv) {
               break;
            }
         }
      }

      return rv;
   }

   static void addNotify(SGNode node) {
      if (!focusRequestPostponed.isEmpty() && node.getPanel() != null) {
         checkPostponedFocusRequest(node);
      }

   }

   static void removeNotify(SGNode node) {
      JSGPanel panel = node.getPanel();
      if (panel != null) {
         for(int i = 0; i < panel.getComponentCount(); ++i) {
            Component child = panel.getComponent(i);
            SGNode childNode = getNode(child);

            for(SGNode leafNode = childNode; childNode != null; childNode = ((SGNode)childNode).getParent()) {
               if (childNode == node) {
                  if (panel.getFocusOwner() == childNode) {
                     panel.setFocusOwner((SGNode)null);
                  }

                  if (leafNode instanceof SGComponent) {
                     child.transferFocus();
                  } else {
                     panel.remove(child);
                  }
                  break;
               }
            }
         }

      }
   }

   private static FocusOwnerContainer createFocusOwnerContainer(SGNode node) {
      if (logger.isEnabled(Logger.Level.MESSAGE)) {
         logger.message("createFocusOwnerContainer for " + node);
      }

      JSGPanel panel = node.getPanel();
      return panel == null ? null : FocusHandler.FocusOwnerContainer.getFocusOwnerContainer(panel, node);
   }

   static void purgeAllExcept(SGNode node) {
      JSGPanel panel = node.getPanel();
      if (panel != null) {
         for(int i = 0; i < panel.getComponentCount(); ++i) {
            Component child = panel.getComponent(i);
            if (child instanceof FocusOwnerContainer && !((FocusOwnerContainer)child).isWaitingForFocus() && ((FocusOwnerContainer)child).peer != node) {
               if (logger.isEnabled(Logger.Level.MESSAGE)) {
                  logger.message("cleaning " + child);
               }

               panel.remove(child);
            }
         }

      }
   }

   private static SGNode getNode(Component component) {
      SGNode rv = null;
      if (component instanceof FocusOwnerContainer) {
         rv = ((FocusOwnerContainer)component).peer;
      } else {
         SGComponent.SGEmbeddedPeer embeddedPeer = (SGComponent.SGEmbeddedPeer)SGEmbeddedToolkit.getSGEmbeddedToolkit().getEmbeddedPeer(component);
         if (embeddedPeer != null) {
            rv = embeddedPeer.getNode();
         }
      }

      return (SGNode)rv;
   }

   private static class FocusOwnerContainer_InputMethod extends FocusOwnerContainer {
      private FocusOwnerContainer_InputMethod(SGNode peer) {
         super(peer, null);

         assert peer instanceof InputMethodHelper;

         this.addInputMethodListener((InputMethodHelper)peer);
      }

      public InputMethodRequests getInputMethodRequests() {
         return ((InputMethodHelper)this.peer).getInputMethodRequests();
      }
   }

   private static class FocusOwnerContainer extends Container {
      private static final long serialVersionUID = 1L;
      protected final SGNode peer;
      private boolean waitingForFocus;

      private static FocusOwnerContainer getFocusOwnerContainer(Container container, SGNode peer) {
         for(int i = container.getComponentCount() - 1; i >= 0; --i) {
            Component child = container.getComponent(i);
            if (child instanceof FocusOwnerContainer && ((FocusOwnerContainer)child).peer == peer) {
               return (FocusOwnerContainer)child;
            }
         }

         FocusOwnerContainer focusOwner = peer instanceof InputMethodHelper ? new FocusOwnerContainer_InputMethod(peer) : new FocusOwnerContainer(peer);
         container.add((Component)focusOwner);
         return (FocusOwnerContainer)focusOwner;
      }

      private FocusOwnerContainer(SGNode peer) {
         this.waitingForFocus = false;
         this.peer = peer;
         this.enableEvents(12L);
      }

      boolean isWaitingForFocus() {
         return this.waitingForFocus;
      }

      public boolean requestFocusInWindow() {
         this.waitingForFocus = true;
         return super.requestFocusInWindow();
      }

      protected void processKeyEvent(KeyEvent e) {
         if (FocusHandler.logger.isEnabled(Logger.Level.MESSAGE)) {
            FocusHandler.logger.message("processKeyEvent " + e + " for peer " + this.peer);
         }

         KeyEvent event = new KeyEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar(), e.getKeyLocation());
         this.peer.processKeyEvent(event);
      }

      protected void processFocusEvent(FocusEvent e) {
         if (FocusHandler.logger.isEnabled(Logger.Level.MESSAGE)) {
            String str = "";
            switch (e.getID()) {
               case 1004:
                  str = "FOCUS_GAINED";
                  break;
               case 1005:
                  str = "FOCUS_LOST";
            }

            FocusHandler.logger.message(str + " on peer " + this.peer);
         }

         JSGPanel jsgpanel;
         switch (e.getID()) {
            case 1004:
               this.waitingForFocus = false;
               FocusHandler.purgeAllExcept(this.peer);
               jsgpanel = this.peer.getPanel();
               if (jsgpanel != null) {
                  jsgpanel.setFocusOwner(this.peer);
               }
               break;
            case 1005:
               jsgpanel = this.peer.getPanel();
               if (jsgpanel != null) {
                  jsgpanel.setFocusOwner((SGNode)null);
               }
         }

         FocusEvent event = new FocusEvent(e.getComponent(), e.getID(), e.isTemporary(), e.getOppositeComponent());
         this.peer.processFocusEvent(event);
      }

      public String toString() {
         String className = this.getClass().getName();
         int i = className.lastIndexOf(36);
         return className.substring(i + 1) + "[" + this.peer + "]";
      }
   }

   private static class SGFocusTraversalPolicy extends FocusTraversalPolicy {
      private static FocusTraversalPolicy shellFocusTraversalPolicy = new LayoutFocusTraversalPolicy();

      private SGFocusTraversalPolicy() {
      }

      private Component getComponent(Container container, Component component, boolean isAfter) {
         Component rv = null;
         if (FocusHandler.logger.isEnabled(Logger.Level.MESSAGE)) {
            FocusHandler.logger.message("container " + container + "\ncomponent " + component + "\nisAfter " + isAfter);
         }

         if (!(component instanceof FocusOwnerContainer)) {
            Component toFocusComponent;
            if (isAfter) {
               toFocusComponent = shellFocusTraversalPolicy.getComponentAfter(container, component);
               if (toFocusComponent != shellFocusTraversalPolicy.getFirstComponent(container)) {
                  rv = toFocusComponent;
               }
            } else {
               toFocusComponent = shellFocusTraversalPolicy.getComponentBefore(container, component);
               if (toFocusComponent != shellFocusTraversalPolicy.getLastComponent(container)) {
                  rv = toFocusComponent;
               }
            }
         }

         if (rv == null) {
            SGNode focused = FocusHandler.getNode(component);
            SGTreeIterator iterator = new SGTreeIterator(focused, isAfter);
            SGNode toFocus = null;

            while(iterator.hasNext()) {
               SGNode next = iterator.next();
               if (next.isFocusable()) {
                  rv = this.getComponent(next, isAfter);
                  if (rv != null) {
                     break;
                  }
               }
            }

            JSGPanel jsgpanel;
            if (rv == null && (jsgpanel = focused.getPanel()) != null) {
               if (isAfter) {
                  rv = this.getFirstComponent(jsgpanel);
               } else {
                  rv = this.getLastComponent(jsgpanel);
               }
            }
         }

         if (FocusHandler.logger.isEnabled(Logger.Level.MESSAGE)) {
            FocusHandler.logger.message("container " + container + "\ncomponent " + component + "\nisAfter " + isAfter + "\nresult " + rv);
         }

         return rv;
      }

      public Component getComponentAfter(Container container, Component component) {
         return this.getComponent(container, component, true);
      }

      public Component getComponentBefore(Container container, Component component) {
         return this.getComponent(container, component, false);
      }

      public Component getDefaultComponent(Container container) {
         return SGEmbeddedToolkit.getSGEmbeddedToolkit().getEmbeddedPeer(container) != null ? shellFocusTraversalPolicy.getDefaultComponent(container) : this.getFirstComponent(container);
      }

      private Component getComponent(SGNode node, boolean isFirst) {
         Component component = null;
         if (node != null && node.isFocusable()) {
            if (node instanceof SGComponent) {
               Container focusComponentParent = ((SGComponent)node).getComponent().getParent();
               if (isFirst) {
                  component = shellFocusTraversalPolicy.getFirstComponent(focusComponentParent);
               } else {
                  component = shellFocusTraversalPolicy.getLastComponent(focusComponentParent);
               }
            } else {
               component = FocusHandler.createFocusOwnerContainer(node);
            }
         }

         return (Component)component;
      }

      private Component getComponent(Container container, boolean isFirst) {
         if (container instanceof JSGPanel) {
            Component rv = null;
            SGNode node = ((JSGPanel)container).getSceneGroup();
            SGNode toFocus = isFirst ? node : FocusHandler.getLastLeaf(node);
            SGNode toFocus;
            if (toFocus != null && (rv = this.getComponent((SGNode)toFocus, isFirst)) == null) {
               for(SGTreeIterator iterator = new SGTreeIterator((SGNode)toFocus, isFirst); rv == null && iterator.hasNext(); rv = this.getComponent(toFocus, isFirst)) {
                  toFocus = iterator.next();
               }
            }

            return rv;
         } else {
            return isFirst ? shellFocusTraversalPolicy.getFirstComponent(container) : shellFocusTraversalPolicy.getLastComponent(container);
         }
      }

      public Component getFirstComponent(Container container) {
         return this.getComponent(container, true);
      }

      public Component getLastComponent(Container container) {
         return this.getComponent(container, false);
      }
   }

   private static class SGTreeIterator implements Iterator<SGNode> {
      private final boolean isForward;
      private SGNode current;
      private boolean gotNext = false;
      private SGNode next;

      SGTreeIterator(SGNode node, boolean isForward) {
         this.isForward = isForward;
         this.current = node;
      }

      public boolean hasNext() {
         if (!this.gotNext) {
            this.doNext();
         }

         return this.next != null;
      }

      public SGNode next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            this.current = this.next;
            this.gotNext = false;
            return this.current;
         }
      }

      public void remove() {
         if (!this.gotNext) {
            this.doNext();
         }

         SGParent parent = this.current.getParent();
         if (parent != null) {
            parent.remove(this.current);
         }

      }

      private void doNext() {
         SGNode node = null;
         if (this.isForward && this.current instanceof SGParent) {
            List<SGNode> children = ((SGParent)this.current).getChildren();
            if (children.size() > 0) {
               node = (SGNode)children.get(0);
            }
         }

         if (node == null) {
            node = this.getNextNode(this.current.getParent(), this.current);
         }

         this.next = node;
         this.gotNext = true;
      }

      private SGNode getNextNode(SGNode current, SGNode child) {
         if (current == null) {
            return null;
         } else {
            SGNode node = null;
            if (current instanceof SGParent) {
               List<SGNode> children = ((SGParent)current).getChildren();
               int childIndex = -1;

               int nextIndex;
               for(nextIndex = 0; nextIndex < children.size(); ++nextIndex) {
                  if (children.get(nextIndex) == child) {
                     childIndex = nextIndex;
                     break;
                  }
               }

               if (childIndex == -1) {
                  throw new AssertionError("child " + child + " should be in parent " + current);
               }

               nextIndex = this.isForward ? childIndex + 1 : childIndex - 1;
               if (nextIndex < 0) {
                  node = current;
               } else if (nextIndex >= children.size()) {
                  node = this.getNextNode(current.getParent(), current);
               } else {
                  node = (SGNode)children.get(nextIndex);
                  if (!this.isForward) {
                     node = FocusHandler.getLastLeaf(node);
                  }
               }
            }

            return node;
         }
      }
   }
}
