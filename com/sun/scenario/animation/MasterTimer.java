package com.sun.scenario.animation;

import com.sun.embeddedswing.SwingGlueLayer;
import com.sun.scenario.DelayedRunnable;
import com.sun.scenario.Settings;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

final class MasterTimer extends AbstractMasterTimer {
   private static PropertyChangeListener pcl = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent event) {
         if (event.getPropertyName().equals("com.sun.scenario.animation.nogaps")) {
            AbstractMasterTimer.nogaps = Settings.getBoolean("com.sun.scenario.animation.nogaps");
         } else if (event.getPropertyName().equals("com.sun.scenario.animation.fullspeed")) {
            AbstractMasterTimer.fullspeed = Settings.getBoolean("com.sun.scenario.animation.fullspeed");
         } else if (event.getPropertyName().equals("com.sun.scenario.animation.adaptivepulse")) {
            AbstractMasterTimer.useAdaptivePulse = Settings.getBoolean("com.sun.scenario.animation.adaptivepulse");
         } else if (event.getPropertyName().equals("com.sun.scenario.animation.hires")) {
            AbstractMasterTimer.hires = Settings.getBoolean("com.sun.scenario.animation.hires");
            if (AbstractMasterTimer.hires) {
               MasterTimer.startHiResSleep();
            } else {
               MasterTimer.stopHiResSleep();
            }
         }

      }
   };
   private static LongSleepingThread hiResWorkaround;
   private static final Object MASTER_TIMER_KEY;

   private static void startHiResSleep() {
      hiResWorkaround = new LongSleepingThread();
      hiResWorkaround.start();
   }

   private static void stopHiResSleep() {
      if (hiResWorkaround != null) {
         hiResWorkaround.interrupt();
      }

      hiResWorkaround = null;
   }

   private MasterTimer() {
   }

   public static synchronized MasterTimer getInstance() {
      Map<Object, Object> contextMap = SwingGlueLayer.getContextMap();
      MasterTimer instance = (MasterTimer)contextMap.get(MASTER_TIMER_KEY);
      if (instance == null) {
         instance = new MasterTimer();
         contextMap.put(MASTER_TIMER_KEY, instance);
      }

      return instance;
   }

   protected boolean shouldUseNanoTime() {
      boolean useNanoTimeTmp = false;

      try {
         System.nanoTime();
         useNanoTimeTmp = true;
      } catch (NoSuchMethodError var3) {
      }

      return useNanoTimeTmp;
   }

   static long milliTime() {
      return getInstance().milliTimeImpl();
   }

   static long nanoTime() {
      return getInstance().nanoTimeImpl();
   }

   static boolean isIdle() {
      return getInstance().isIdleImpl();
   }

   static void addToRunQueue(Clip target, long tbegin) {
      getInstance().addToRunQueueImpl(target, tbegin);
   }

   static void removeFromRunQueue(Clip target) {
      getInstance().removeFromRunQueueImpl(target);
   }

   static void addFrameJob(FrameJob job) {
      getInstance().addFrameJobImpl(job);
   }

   static void removeFrameJob(FrameJob job) {
      getInstance().removeFrameJobImpl(job);
   }

   static void stop(Clip target) {
      getInstance().stopImpl(target);
   }

   static boolean pause(Clip target) {
      return getInstance().pauseImpl(target);
   }

   static boolean resume(Clip target) {
      return getInstance().resumeImpl(target);
   }

   static void notifyJobsReady() {
      getInstance().notifyJobsReadyImpl();
   }

   protected int getPulseDuration(int precision) {
      int retVal = precision / 60;
      if (Settings.get("com.sun.scenario.animation.pulse") != null) {
         int overrideHz = Settings.getInt("com.sun.scenario.animation.pulse", 60);
         if (overrideHz > 0) {
            retVal = precision / overrideHz;
         }
      } else if (!GraphicsEnvironment.isHeadless()) {
         DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
         if (dm != null) {
            int rate = dm.getRefreshRate();
            if (rate != 0 && rate >= 50) {
               retVal = precision / rate;
            }
         }
      }

      return retVal;
   }

   protected void postUpdateAnimationRunnable(DelayedRunnable animationRunnable) {
      SwingGlueLayer.getSwingGlueLayer().setAnimationRunnable(animationRunnable);
   }

   static void timePulse(long now) {
      getInstance().timePulseImpl(now);
   }

   static {
      Settings.addPropertyChangeListener("com.sun.scenario.animation.nogaps", pcl);
      Settings.addPropertyChangeListener("com.sun.scenario.animation.adaptivepulse", pcl);
      Settings.addPropertyChangeListener("com.sun.scenario.animation.fullspeed", pcl);
      Settings.addPropertyChangeListener("com.sun.scenario.animation.hires", pcl);
      hires = Settings.getBoolean("com.sun.scenario.animation.hires", hires);
      if (hires) {
         startHiResSleep();
      }

      MASTER_TIMER_KEY = new StringBuilder("MasterTimerKey");
   }

   private static class LongSleepingThread extends Thread {
      public LongSleepingThread() {
         super("Long sleeping thread");
         this.setDaemon(true);
      }

      public void run() {
         while(true) {
            try {
               Thread.sleep(2147483647L);
            } catch (InterruptedException var2) {
               if (!AbstractMasterTimer.hires) {
                  return;
               }
            }
         }
      }
   }
}
