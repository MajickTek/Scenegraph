package com.sun.embeddedswing;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.PopupFactory;
import javax.swing.RepaintManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class SwingGlueLayer {
   private static final Class<?> repaintManagerClass;
   private final Object repaintManager;
   private final EmbeddedEventQueue eventQueue;
   private static final Object SWING_GLUELAYER_KEY;
   private final Map<Object, Object> contextMap = Collections.synchronizedMap(new HashMap());

   public SwingGlueLayer() {
      this.repaintManager = repaintManagerClass != null ? SwingGlueLayer.RepaintManagerRegister.createRepaintManager() : null;
      this.eventQueue = new EmbeddedEventQueue();
      EventQueue.invokeLater(new Runnable() {
         public void run() {
            new JButton();
            PopupFactory.setSharedInstance(new EmbeddedPopupFactory());
            SwingGlueLayer.this.eventQueue.register();
         }
      });
   }

   public static synchronized SwingGlueLayer getSwingGlueLayer() {
      UIDefaults uiDefaults = UIManager.getDefaults();
      SwingGlueLayer instance = (SwingGlueLayer)uiDefaults.get(SWING_GLUELAYER_KEY);
      if (instance == null) {
         instance = new SwingGlueLayer();
         uiDefaults.put(SWING_GLUELAYER_KEY, instance);
      }

      return instance;
   }

   public static Map<Object, Object> getContextMap() {
      return getSwingGlueLayer().contextMap;
   }

   void registerRepaintManager(JComponent c) {
      if (this.repaintManager != null) {
         SwingGlueLayer.RepaintManagerRegister.registerRepaintManager(c, this.repaintManager);
      }
   }

   public void setAnimationRunnable(DelayedRunnable animationRunnable) {
      this.eventQueue.setAnimationRunnable(animationRunnable);
   }

   static JComponent getAncestorWithClientProperty(Component comp, Object clientKey) {
      if (comp == null) {
         return null;
      } else {
         Container parent;
         for(parent = comp.getParent(); parent != null && (!(parent instanceof JComponent) || ((JComponent)parent).getClientProperty(clientKey) == null); parent = parent.getParent()) {
         }

         return (JComponent)parent;
      }
   }

   static JComponent getFirstComponentWithClientProperty(Component parent, Object clientKey, int x, int y, Point offset) {
      if (!parent.contains(x, y)) {
         return null;
      } else if (parent instanceof JComponent && ((JComponent)parent).getClientProperty(clientKey) != null) {
         return (JComponent)parent;
      } else {
         if (parent instanceof Container) {
            Component[] components = ((Container)parent).getComponents();

            for(int i = 0; i < components.length; ++i) {
               Component comp = components[i];
               if (comp != null && comp.isVisible()) {
                  Point loc = comp.getLocation();
                  if (comp.contains(x - loc.x, y - loc.y)) {
                     JComponent t = getFirstComponentWithClientProperty(comp, clientKey, x - loc.x, y - loc.y, loc);
                     if (t != null) {
                        offset.x += loc.x;
                        offset.y += loc.y;
                        return t;
                     }

                     return null;
                  }
               }
            }
         }

         return null;
      }
   }

   static {
      Class<?> repaintManagerClassTmp = null;

      try {
         repaintManagerClassTmp = Class.forName("javax.swing.RepaintManager");
      } catch (ClassNotFoundException var2) {
      } catch (SecurityException var3) {
      }

      repaintManagerClass = repaintManagerClassTmp;
      SWING_GLUELAYER_KEY = new StringBuilder("SwingGlueLayerKey");
   }

   private static class RepaintManagerRegister {
      private static final Method setDelegateRepaintManagerMethod;
      private static final Class<?> swingUtilities3Class;
      private static final Method getWindowsMethod;

      static Object createRepaintManager() {
         return new EmbeddedRepaintManager((RepaintManager)null);
      }

      static void registerRepaintManager(JComponent c, Object repaintManager) {
         if (setDelegateRepaintManagerMethod != null) {
            try {
               setDelegateRepaintManagerMethod.invoke(swingUtilities3Class, c, repaintManager);
            } catch (IllegalArgumentException var8) {
            } catch (IllegalAccessException var9) {
            } catch (InvocationTargetException var10) {
            }

         } else {
            RepaintManager manager = RepaintManager.currentManager((JComponent)null);
            if (manager != repaintManager) {
               ((EmbeddedRepaintManager)repaintManager).setDelegate(manager);
               RepaintManager.setCurrentManager((EmbeddedRepaintManager)repaintManager);
               if (getWindowsMethod != null) {
                  try {
                     Window[] windows = null;
                     windows = (Window[])((Window[])getWindowsMethod.invoke((Object[])null));
                     Window[] arr$ = windows;
                     int len$ = windows.length;

                     for(int i$ = 0; i$ < len$; ++i$) {
                        Window window = arr$[i$];
                        window.repaint();
                     }
                  } catch (IllegalArgumentException var11) {
                  } catch (IllegalAccessException var12) {
                  } catch (InvocationTargetException var13) {
                  }
               }
            }

         }
      }

      static {
         Class<?> swingUtilities3ClassTmp = null;
         Method setDelegateRepaintManagerMethodTmp = null;

         try {
            swingUtilities3ClassTmp = Class.forName("com.sun.java.swing.SwingUtilities3");
            setDelegateRepaintManagerMethodTmp = swingUtilities3ClassTmp.getMethod("setDelegateRepaintManager", JComponent.class, SwingGlueLayer.repaintManagerClass);
         } catch (ClassNotFoundException var5) {
         } catch (SecurityException var6) {
         } catch (NoSuchMethodException var7) {
         }

         if (swingUtilities3ClassTmp == null || setDelegateRepaintManagerMethodTmp == null) {
            swingUtilities3ClassTmp = null;
            setDelegateRepaintManagerMethodTmp = null;
         }

         swingUtilities3Class = swingUtilities3ClassTmp;
         setDelegateRepaintManagerMethod = setDelegateRepaintManagerMethodTmp;
         Method getWindowsMethodTmp = null;

         try {
            getWindowsMethodTmp = Window.class.getMethod("getWindows");
         } catch (Exception var4) {
         }

         getWindowsMethod = getWindowsMethodTmp;
      }
   }
}
