package com.sun.embeddedswing;

import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InvocationEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

class EmbeddedEventQueue extends EventQueue {
   private List<EmbeddedToolkit.CoordinateHandler> lastCoordinateHandlerList;
   private final ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         EmbeddedEventQueue.this.doPulse();
      }
   };
   private final Runnable pulseRunnable = new Runnable() {
      public void run() {
         EmbeddedEventQueue.this.doPulse();
      }
   };
   private final InvocationEvent pulseEvent;
   private AWTEvent postponed;
   private boolean waitForNonPulseEvent;
   private static final long RESPONSIVE_THRESHOLD = 33L;
   private final Object lock;
   private boolean disableTimer;
   private ScheduledFuture<?> pulseAlarmFuture;
   private FutureTask<Void> pulseRunnableFuture;
   private DelayedRunnable animationRunnable;
   private final ScheduledExecutorService pulseAlarmExecutor;
   private final Runnable pulseAlarmRunnable;

   EmbeddedEventQueue() {
      this.pulseEvent = new InvocationEvent(this, this.pulseRunnable);
      this.postponed = null;
      this.waitForNonPulseEvent = true;
      this.lock = new Object();
      this.disableTimer = false;
      this.pulseAlarmFuture = null;
      this.pulseRunnableFuture = null;
      this.animationRunnable = null;
      this.pulseAlarmExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
         private final ThreadFactory delegate = Executors.defaultThreadFactory();
         private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
            }
         };

         public Thread newThread(Runnable r) {
            Thread thread = this.delegate.newThread(r);
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
            return thread;
         }
      });
      this.pulseAlarmRunnable = new Runnable() {
         public void run() {
            synchronized(EmbeddedEventQueue.this.lock) {
               if (EmbeddedEventQueue.this.pulseRunnableFuture != null) {
                  EventQueue.invokeLater(EmbeddedEventQueue.this.pulseRunnableFuture);
               }

            }
         }
      };
   }

   private static boolean isMouseGrab(MouseEvent e) {
      int modifiers = e.getModifiersEx();
      if (e.getID() == 501 || e.getID() == 502) {
         switch (e.getButton()) {
            case 1:
               modifiers ^= 1024;
               break;
            case 2:
               modifiers ^= 2048;
               break;
            case 3:
               modifiers ^= 4096;
         }
      }

      return (modifiers & 7168) != 0;
   }

   private static void moveToVisibleAndOnScreen(EmbeddedToolkit.CoordinateHandler topCoordinateHandler, Component targetComponent, Point2D embeddedPoint) {
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      GraphicsConfiguration gc = targetComponent.getGraphicsConfiguration();
      Rectangle screenBounds;
      if (gc != null) {
         Insets screenInsets = toolkit.getScreenInsets(gc);
         screenBounds = gc.getBounds();
         screenBounds.width -= screenInsets.left + screenInsets.right;
         screenBounds.height -= screenInsets.top + screenInsets.bottom;
         screenBounds.x += screenInsets.left;
         screenBounds.y += screenInsets.top;
      } else {
         screenBounds = new Rectangle(new Point(), toolkit.getScreenSize());
      }

      JPanel topContainer = topCoordinateHandler.getEmbeddedPeer().getContainer();
      Point topContainerLocationOnScreen = new Point();
      SwingUtilities.convertPointToScreen(topContainerLocationOnScreen, topContainer);
      JComponent topParent = topCoordinateHandler.getEmbeddedPeer().getParentComponent();
      Rectangle topVisibleRectangle = topParent.getVisibleRect();
      Point topParentTargetPoint = SwingUtilities.convertPoint(targetComponent, (int)embeddedPoint.getX(), (int)embeddedPoint.getY(), topParent);
      int deltaX = (int)Math.min((double)(topVisibleRectangle.x + topVisibleRectangle.width - topParentTargetPoint.x), Math.max((double)screenBounds.x - topContainerLocationOnScreen.getX(), (double)(topVisibleRectangle.x - topParentTargetPoint.x)));
      int deltaY = (int)Math.min((double)(topVisibleRectangle.y + topVisibleRectangle.height - topParentTargetPoint.y), Math.max((double)screenBounds.y - topContainerLocationOnScreen.getY(), (double)(topVisibleRectangle.y - topParentTargetPoint.y)));
      topContainer.setLocation(topContainer.getX() + deltaX, topContainer.getY() + deltaY);
   }

   private Object beforeDispatch(AWTEvent event) {
      Object rv = null;
      if (event instanceof MouseEvent) {
         int STATE_GRAB = true;
         int STATE_RETARGET = true;
         MouseEvent mouseEvent = (MouseEvent)event;
         Component originalSource = mouseEvent.getComponent();
         Point originalMousePoint = mouseEvent.getPoint();
         Component rootComponent = SwingUtilities.getRoot(originalSource);
         if (rootComponent == null) {
            return rv;
         }

         Point mousePoint;
         if (rootComponent != originalSource && !(originalSource instanceof Applet)) {
            mousePoint = SwingUtilities.convertPoint(originalSource, originalMousePoint, rootComponent);
            mouseEvent.setSource(rootComponent);
            mouseEvent.translatePoint(mousePoint.x - mouseEvent.getX(), mousePoint.y - mouseEvent.getY());
         }

         mousePoint = mouseEvent.getPoint();
         Component source = mouseEvent.getComponent();
         int state = 0;
         if (!isMouseGrab(mouseEvent) && mouseEvent.getID() != 500) {
            state = 1;
         } else if (this.lastCoordinateHandlerList != null && !this.lastCoordinateHandlerList.isEmpty()) {
            switch (mouseEvent.getID()) {
               case 501:
               case 502:
               case 503:
               case 506:
                  state = 2;
               case 504:
               case 505:
            }
         }

         Point2D embeddedPoint = new Point2D.Double();
         List<Point2D> parentPointList = null;
         Object coordinateHandlerList;
         if (state == 2) {
            coordinateHandlerList = this.lastCoordinateHandlerList;
         } else {
            parentPointList = new ArrayList();
            coordinateHandlerList = new ArrayList();
            Point2D point = new Point2D.Double();
            ((Point2D)point).setLocation(mousePoint);
            Component root = source;

            while(root != null) {
               Point offset = new Point();
               JComponent target = SwingGlueLayer.getFirstComponentWithClientProperty(root, EmbeddedToolkit.EmbeddedToolkitKey, (int)((Point2D)point).getX(), (int)((Point2D)point).getY(), offset);
               root = null;
               if (target != null) {
                  ((Point2D)point).setLocation(((Point2D)point).getX() - offset.getX(), ((Point2D)point).getY() - offset.getY());
                  EmbeddedToolkit.CoordinateHandler coordinateHandler = EmbeddedToolkit.getEmbeddedToolkit(target).createCoordinateHandler(target, (Point2D)point, mouseEvent);
                  if (coordinateHandler != null) {
                     Point2D parentPoint = new Point2D.Double();
                     parentPoint.setLocation((Point2D)point);
                     point = coordinateHandler.transform((Point2D)point, (Point2D)null);
                     ((List)coordinateHandlerList).add(coordinateHandler);
                     embeddedPoint.setLocation((Point2D)point);
                     parentPointList.add(parentPoint);
                     root = coordinateHandler.getEmbeddedPeer().getEmbeddedComponent();
                  }
               }
            }

            if (state == 1) {
               this.lastCoordinateHandlerList = (List)coordinateHandlerList;
            }
         }

         if (!((List)coordinateHandlerList).isEmpty()) {
            Point parentPoint;
            EmbeddedToolkit.CoordinateHandler targetCoordianteHandler;
            EmbeddedPeer embeddedPeer;
            if (parentPointList == null) {
               parentPointList = new ArrayList();
               Component root = source;
               Point2D point = new Point2D.Double();
               ((Point2D)point).setLocation(mousePoint);

               for(Iterator i$ = ((List)coordinateHandlerList).iterator(); i$.hasNext(); root = embeddedPeer.getEmbeddedComponent()) {
                  targetCoordianteHandler = (EmbeddedToolkit.CoordinateHandler)i$.next();
                  embeddedPeer = targetCoordianteHandler.getEmbeddedPeer();
                  JComponent parent = embeddedPeer.getParentComponent();
                  parentPoint = SwingUtilities.convertPoint(parent, 0, 0, root);
                  ((Point2D)point).setLocation(((Point2D)point).getX() - parentPoint.getX(), ((Point2D)point).getY() - parentPoint.getY());
                  Point2D parentPoint = new Point2D.Double(((Point2D)point).getX(), ((Point2D)point).getY());
                  point = targetCoordianteHandler.transform((Point2D)point, (Point2D)null);
                  embeddedPoint.setLocation((Point2D)point);
                  parentPointList.add(parentPoint);
               }
            }

            List<Runnable> runnableList = new ArrayList();
            Point offPoint = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
            boolean haveMouseBlocker = false;

            for(int i = 0; i < ((List)coordinateHandlerList).size(); ++i) {
               embeddedPeer = ((EmbeddedToolkit.CoordinateHandler)((List)coordinateHandlerList).get(i)).getEmbeddedPeer();
               EmbeddedToolkit<?> embeddedToolkit = embeddedPeer.getEmbeddedToolkit();
               parentPoint = null;
               Object parentPoint;
               if (haveMouseBlocker) {
                  parentPoint = offPoint;
               } else {
                  parentPoint = (Point2D)parentPointList.get(i);
               }

               mouseEvent.translatePoint((int)((Point2D)parentPoint).getX() - mouseEvent.getX(), (int)((Point2D)parentPoint).getY() - mouseEvent.getY());
               Runnable runnable = embeddedToolkit.processMouseEvent(mouseEvent, (Point2D)parentPointList.get(i), embeddedPeer);
               if (!haveMouseBlocker && runnable != null) {
                  runnableList.add(runnable);
               } else {
                  haveMouseBlocker = true;
               }
            }

            targetCoordianteHandler = (EmbeddedToolkit.CoordinateHandler)((List)coordinateHandlerList).get(((List)coordinateHandlerList).size() - 1);
            Component targetComponent = targetCoordianteHandler.getEmbeddedPeer().getEmbeddedComponent();
            Point targetPoint;
            if (!haveMouseBlocker) {
               moveToVisibleAndOnScreen((EmbeddedToolkit.CoordinateHandler)((List)coordinateHandlerList).get(0), targetComponent, embeddedPoint);
               targetPoint = SwingUtilities.convertPoint(targetComponent, (int)embeddedPoint.getX(), (int)embeddedPoint.getY(), source);
            } else {
               targetPoint = offPoint;
            }

            mouseEvent.translatePoint(targetPoint.x - mouseEvent.getX(), targetPoint.y - mouseEvent.getY());
            Iterator i$ = ((List)coordinateHandlerList).iterator();

            while(i$.hasNext()) {
               EmbeddedToolkit.CoordinateHandler coordinateHandler = (EmbeddedToolkit.CoordinateHandler)i$.next();
               coordinateHandler.getEmbeddedPeer().getShell().setContains(true);
            }

            rv = new Object[]{coordinateHandlerList, parentPointList, runnableList};
         } else {
            mouseEvent.setSource(originalSource);
            mouseEvent.translatePoint(originalMousePoint.x - mouseEvent.getX(), originalMousePoint.y - mouseEvent.getY());
         }
      }

      return rv;
   }

   private void afterDispatch(AWTEvent event, Object handle) {
      if (event instanceof MouseEvent && handle != null) {
         MouseEvent mouseEvent = (MouseEvent)event;
         Object[] objectArray = (Object[])((Object[])handle);
         List<EmbeddedToolkit.CoordinateHandler> coordinateHandlerList = (List)objectArray[0];
         List<Point2D> parentPointList = (List)objectArray[1];
         List<Runnable> runnableList = (List)objectArray[2];
         Iterator i$ = coordinateHandlerList.iterator();

         while(i$.hasNext()) {
            EmbeddedToolkit.CoordinateHandler coordinateHandler = (EmbeddedToolkit.CoordinateHandler)i$.next();
            coordinateHandler.getEmbeddedPeer().getShell().setContains(false);
         }

         for(int i = runnableList.size() - 1; i >= 0; --i) {
            Runnable runnable = (Runnable)runnableList.get(i);
            if (runnable != null) {
               Point2D parentPoint = (Point2D)parentPointList.get(i);
               mouseEvent.translatePoint((int)parentPoint.getX() - mouseEvent.getX(), (int)parentPoint.getY() - mouseEvent.getY());
               runnable.run();
            }
         }
      }

   }

   protected void dispatchEvent(AWTEvent event) {
      Object handle = this.beforeDispatch(event);
      super.dispatchEvent(event);
      this.afterDispatch(event, handle);
   }

   private static long getWhen(AWTEvent e) {
      long when = Long.MIN_VALUE;
      if (e instanceof InputEvent) {
         InputEvent ie = (InputEvent)e;
         when = ie.getWhen();
      } else if (e instanceof InputMethodEvent) {
         InputMethodEvent ime = (InputMethodEvent)e;
         when = ime.getWhen();
      } else if (e instanceof ActionEvent) {
         ActionEvent ae = (ActionEvent)e;
         when = ae.getWhen();
      } else if (e instanceof InvocationEvent) {
         InvocationEvent ie = (InvocationEvent)e;
         when = ie.getWhen();
      }

      return when;
   }

   private AWTEvent getNextEvent(EventQueue eventQueue) throws InterruptedException {
      AWTEvent nextEvent = null;
      if (this.postponed != null) {
         nextEvent = this.postponed;
         this.postponed = null;
      } else {
         AWTEvent superNextEvent = null;

         do {
            DelayedRunnable delayedRunnable = null;
            synchronized(this.lock) {
               delayedRunnable = this.animationRunnable;
            }

            if (delayedRunnable != null && !this.waitForNonPulseEvent && delayedRunnable.getDelay() <= 0L) {
               nextEvent = this.pulseEvent;
               this.postponed = superNextEvent;
               this.waitForNonPulseEvent = true;
            } else if (superNextEvent != null) {
               nextEvent = superNextEvent;
            } else {
               if (eventQueue == this) {
                  superNextEvent = super.getNextEvent();
               } else {
                  superNextEvent = eventQueue.getNextEvent();
               }

               long now = System.currentTimeMillis();
               this.waitForNonPulseEvent = now - getWhen(superNextEvent) > 33L;
            }
         } while(nextEvent == null);
      }

      return (AWTEvent)nextEvent;
   }

   public AWTEvent getNextEvent() throws InterruptedException {
      return this.getNextEvent(this);
   }

   private void doPulse() {
      synchronized(this.lock) {
         this.disableTimer = true;
         this.stopPulseAlarm();
         if (this.animationRunnable != null) {
            this.animationRunnable.run();
         }

         this.disableTimer = false;
         this.updatePulseAlarm();
      }
   }

   void setAnimationRunnable(DelayedRunnable animationRunnable) {
      synchronized(this.lock) {
         this.animationRunnable = animationRunnable;
         this.updatePulseAlarm();
      }
   }

   private void updatePulseAlarm() {
      synchronized(this.lock) {
         if (!this.disableTimer) {
            if (this.animationRunnable != null) {
               if (this.pulseRunnableFuture == null || this.pulseRunnableFuture.isDone()) {
                  int timerDelay = (int)this.animationRunnable.getDelay();
                  this.pulseRunnableFuture = new FutureTask(this.pulseRunnable, (Object)null);
                  this.pulseAlarmFuture = this.pulseAlarmExecutor.schedule(this.pulseAlarmRunnable, (long)timerDelay, TimeUnit.MILLISECONDS);
               }
            } else {
               this.stopPulseAlarm();
            }

         }
      }
   }

   private void stopPulseAlarm() {
      synchronized(this.lock) {
         if (this.pulseAlarmFuture != null) {
            this.pulseAlarmFuture.cancel(false);
            this.pulseAlarmFuture = null;
         }

         if (this.pulseRunnableFuture != null) {
            this.pulseRunnableFuture.cancel(false);
            this.pulseRunnableFuture = null;
         }

      }
   }

   void register() {
      boolean registered = false;

      try {
         Class.forName("java.util.concurrent.Callable");
         Class<?> swingUtilities3Class = Class.forName("com.sun.java.swing.SwingUtilities3");
         Method setEventQueueDelegate = swingUtilities3Class.getMethod("setEventQueueDelegate", Map.class);
         setEventQueueDelegate.invoke(swingUtilities3Class, EmbeddedEventQueue.EventQueueDelegateFactory.getObjectMap(this));
         registered = true;
      } catch (ClassNotFoundException var4) {
      } catch (SecurityException var5) {
      } catch (NoSuchMethodException var6) {
      } catch (IllegalArgumentException var7) {
      } catch (IllegalAccessException var8) {
      } catch (InvocationTargetException var9) {
      }

      if (!registered) {
         EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
         eq.push(this);
      }

   }

   private static class EventQueueDelegateFactory {
      static Map<String, Map<String, Object>> getObjectMap(final EmbeddedEventQueue delegate) {
         Map<String, Map<String, Object>> objectMap = new HashMap();
         final AWTEvent[] afterDispatchEventArgument = new AWTEvent[1];
         final Object[] afterDispatchHandleArgument = new Object[1];
         Callable<Void> afterDispatchCallable = new Callable<Void>() {
            public Void call() {
               delegate.afterDispatch(afterDispatchEventArgument[0], afterDispatchHandleArgument[0]);
               return null;
            }
         };
         Map<String, Object> methodMap = new HashMap();
         methodMap.put("event", afterDispatchEventArgument);
         methodMap.put("handle", afterDispatchHandleArgument);
         methodMap.put("method", afterDispatchCallable);
         objectMap.put("afterDispatch", methodMap);
         final AWTEvent[] beforeDispatchEventArgument = new AWTEvent[1];
         Callable<Object> beforeDispatchCallable = new Callable<Object>() {
            public Object call() {
               return delegate.beforeDispatch(beforeDispatchEventArgument[0]);
            }
         };
         methodMap = new HashMap();
         methodMap.put("event", beforeDispatchEventArgument);
         methodMap.put("method", beforeDispatchCallable);
         objectMap.put("beforeDispatch", methodMap);
         final EventQueue[] getNextEventEventQueueArgument = new EventQueue[1];
         Callable<AWTEvent> getNextEventCallable = new Callable<AWTEvent>() {
            public AWTEvent call() throws Exception {
               return delegate.getNextEvent(getNextEventEventQueueArgument[0]);
            }
         };
         methodMap = new HashMap();
         methodMap.put("eventQueue", getNextEventEventQueueArgument);
         methodMap.put("method", getNextEventCallable);
         objectMap.put("getNextEvent", methodMap);
         return objectMap;
      }
   }
}
