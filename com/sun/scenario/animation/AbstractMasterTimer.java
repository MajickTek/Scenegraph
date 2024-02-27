package com.sun.scenario.animation;

import com.sun.scenario.DelayedRunnable;
import com.sun.scenario.Settings;
import java.util.ArrayList;
import java.util.Iterator;

abstract class AbstractMasterTimer {
   protected final boolean useNanoTime = this.shouldUseNanoTime();
   protected static final String NOGAPS_PROP = "com.sun.scenario.animation.nogaps";
   protected static boolean nogaps = false;
   protected static final String FULLSPEED_PROP = "com.sun.scenario.animation.fullspeed";
   protected static boolean fullspeed = false;
   protected static final String HIRES_PROP = "com.sun.scenario.animation.hires";
   protected static boolean hires = true;
   protected static final String ADAPTIVE_PULSE_PROP = "com.sun.scenario.animation.adaptivepulse";
   protected static boolean useAdaptivePulse = false;
   protected static final String PULSE_PROP = "com.sun.scenario.animation.pulse";
   private static final FrameJob[] emptyJobs;
   private final MainLoop theMaster = new MainLoop();
   private final RunQueue waitList = new RunQueue();
   private final RunQueue pauseWaitList = new RunQueue();
   private final ArrayList<FrameJob> frameJobList = new ArrayList();
   private FrameJob[] frameJobs;
   private final RunQueue runList;

   protected AbstractMasterTimer() {
      this.frameJobs = emptyJobs;
      this.runList = new RunQueue();
   }

   protected long milliTimeImpl() {
      return this.useNanoTime ? System.nanoTime() / 1000L / 1000L : System.currentTimeMillis();
   }

   protected long nanoTimeImpl() {
      return this.useNanoTime ? System.nanoTime() : System.currentTimeMillis() * 1000000L;
   }

   protected abstract boolean shouldUseNanoTime();

   protected boolean isIdleImpl() {
      synchronized(this.waitList) {
         return this.runList.isEmpty() && this.waitList.isEmpty() && this.pauseWaitList.isEmpty();
      }
   }

   protected void addToRunQueueImpl(Clip target, long tbegin) {
      synchronized(this.waitList) {
         this.waitList.remove(target);
         this.pauseWaitList.remove(target);
         this.waitList.insert(target, tbegin);
         this.theMaster.setClipsReady(true);
      }
   }

   protected void removeFromRunQueueImpl(Clip target) {
      synchronized(this.waitList) {
         this.waitList.remove(target);
         this.pauseWaitList.remove(target);
      }
   }

   protected synchronized void addFrameJobImpl(FrameJob job) {
      this.frameJobList.add(job);
      this.frameJobs = (FrameJob[])this.frameJobList.toArray(emptyJobs);
   }

   protected synchronized void removeFrameJobImpl(FrameJob job) {
      if (this.frameJobList.remove(job)) {
         this.frameJobs = (FrameJob[])this.frameJobList.toArray(emptyJobs);
      }

   }

   protected void stopImpl(Clip target) {
      synchronized(this.waitList) {
         RunQueue rq = (RunQueue)this.pauseWaitList.remove(target);
         if (rq != null) {
            rq.stop(this.milliTimeImpl());
            this.waitList.insert(rq);
            this.theMaster.setClipsReady(true);
         } else {
            rq = (RunQueue)this.runList.find(target);
            if (rq != null) {
               rq.stop(this.milliTimeImpl());
            }
         }

      }
   }

   protected boolean pauseImpl(Clip target) {
      synchronized(this.waitList) {
         RunQueue rq = (RunQueue)this.waitList.remove(target);
         if (rq != null) {
            this.pauseWaitList.prepend(rq);
         } else {
            rq = (RunQueue)this.runList.find(target);
         }

         if (rq != null) {
            Animation.Status before = rq.getStatus();
            rq.pause(this.milliTimeImpl());
            return rq.getStatus() != before;
         } else {
            return false;
         }
      }
   }

   protected boolean resumeImpl(Clip target) {
      synchronized(this.waitList) {
         RunQueue rq = (RunQueue)this.pauseWaitList.remove(target);
         Animation.Status before;
         if (rq != null) {
            before = rq.getStatus();
            rq.resume(this.milliTimeImpl());
            this.waitList.insert(rq);
            this.theMaster.setClipsReady(true);
            return rq.getStatus() != before;
         } else {
            rq = (RunQueue)this.runList.find(target);
            if (rq != null) {
               before = rq.getStatus();
               rq.resume(this.milliTimeImpl());
               return rq.getStatus() != before;
            } else {
               return false;
            }
         }
      }
   }

   protected void notifyJobsReadyImpl() {
      this.theMaster.setJobsReady(true);
   }

   protected abstract void postUpdateAnimationRunnable(DelayedRunnable var1);

   protected abstract int getPulseDuration(int var1);

   protected void timePulseImpl(long now) {
      if (this.runList.isEmpty()) {
         synchronized(this.waitList) {
            if (this.waitList.isEmpty()) {
               this.theMaster.setClipsReady(false);
            } else if (nogaps) {
               long nextTrigger = ((RunQueue)this.waitList.next).getTime();
               if (now < nextTrigger) {
                  long delta = now - nextTrigger;
                  Iterator i$ = this.waitList.iterator();

                  while(i$.hasNext()) {
                     RunQueue target = (RunQueue)i$.next();
                     target.adjustStartTime(delta);
                  }
               }
            }
         }
      }

      RunQueue prev = this.runList;
      RunQueue cur = (RunQueue)prev.next;

      while(true) {
         while(cur == null) {
            if (this.waitList.next != null) {
               this.appendStartingClips(prev, now);
               cur = (RunQueue)prev.next;
            }

            if (cur == null) {
               this.theMaster.setJobsReady(false);
               FrameJob[] jobSnapshot = this.frameJobs;
               FrameJob[] arr$ = jobSnapshot;
               int len$ = jobSnapshot.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  FrameJob job = arr$[i$];

                  try {
                     job.run();
                  } catch (Error var15) {
                     if ("com.sun.javafx.runtime.FXExit".equals(var15.getClass().getName())) {
                        FrameJob[] arr$ = jobSnapshot;
                        int len$ = jobSnapshot.length;

                        for(int i$ = 0; i$ < len$; ++i$) {
                           FrameJob rmjob = arr$[i$];
                           this.removeFrameJobImpl(rmjob);
                        }

                        return;
                     }

                     System.err.println("Unexpected exception caught in MasterTimer.timePulse():");
                     var15.printStackTrace();
                     this.removeFrameJobImpl(job);
                  } catch (Throwable var16) {
                     System.err.println("Unexpected exception caught in MasterTimer.timePulse():");
                     var16.printStackTrace();
                     this.removeFrameJobImpl(job);
                  }
               }

               return;
            }
         }

         if (process(cur, now)) {
            prev.next = cur.next;
         } else {
            prev = cur;
         }

         cur = (RunQueue)prev.next;
      }
   }

   private void appendStartingClips(RunQueue runTail, long now) {
      synchronized(this.waitList) {
         RunQueue prev = null;

         RunQueue cur;
         for(cur = (RunQueue)this.waitList.next; cur != null && cur.getTime() <= now; cur = (RunQueue)cur.next) {
            prev = cur;
         }

         if (prev != null) {
            prev.next = null;
            runTail.next = this.waitList.next;
            this.waitList.next = cur;
         }

      }
   }

   private static boolean process(RunQueue cur, long now) {
      Clip c = (Clip)cur.getAnimation();
      Animation.Status status = cur.getStatus();

      try {
         while(true) {
            switch (status) {
               case RUNNING:
                  status = c.timePulse(now - cur.getTime());
                  if (status == Animation.Status.RUNNING) {
                     return false;
                  }
                  break;
               case SCHEDULED:
                  c.begin();
                  c.scheduleBeginAnimations(cur.getTime());
                  status = cur.began();
                  break;
               case PAUSED:
               case SCHEDULEPAUSED:
                  return false;
               case STOPPED:
                  c.end();
                  c.scheduleEndAnimations(now);
               case CANCELED:
                  return true;
            }
         }
      } catch (Error var6) {
         if (!"com.sun.javafx.runtime.FXExit".equals(var6.getClass().getName())) {
            System.err.println("Unexpected exception caught in MasterTimer.process():");
            var6.printStackTrace();
         }

         return true;
      } catch (Throwable var7) {
         System.err.println("Unexpected exception caught in MasterTimer.process():");
         var7.printStackTrace();
         return true;
      }
   }

   static {
      nogaps = Settings.getBoolean("com.sun.scenario.animation.nogaps");
      fullspeed = Settings.getBoolean("com.sun.scenario.animation.fullspeed");
      useAdaptivePulse = Settings.getBoolean("com.sun.scenario.animation.adaptivepulse", useAdaptivePulse);
      int pulse = Settings.getInt("com.sun.scenario.animation.pulse", -1);
      if (pulse != -1) {
         System.err.println("Setting PULSE_DURATION to " + pulse + " hz");
      }

      emptyJobs = new FrameJob[0];
   }

   private final class MainLoop implements DelayedRunnable {
      private boolean clipsReady;
      private boolean jobsReady;
      private final int PULSE_DURATION;
      private final int PULSE_DURATION_NS;
      private long nextPulseTime;
      private long lastPulseDuration;

      private MainLoop() {
         this.PULSE_DURATION = AbstractMasterTimer.this.getPulseDuration(1000);
         this.PULSE_DURATION_NS = AbstractMasterTimer.this.getPulseDuration(1000000000);
         this.nextPulseTime = -2147483648L;
         this.lastPulseDuration = -2147483648L;
      }

      public void run() {
         long pulseStarted = AbstractMasterTimer.this.nanoTimeImpl();
         AbstractMasterTimer.this.timePulseImpl(AbstractMasterTimer.this.milliTimeImpl());
         this.updateNextPulseTime(pulseStarted);
      }

      public long getDelay() {
         if (this.nextPulseTime == -2147483648L) {
            this.updateNextPulseTime(AbstractMasterTimer.this.nanoTimeImpl());
         }

         long timeUntilPulse = this.nextPulseTime - AbstractMasterTimer.this.milliTimeImpl();
         return Math.max(0L, timeUntilPulse);
      }

      private void updateNextPulseTime(long pulseStarted) {
         this.nextPulseTime = AbstractMasterTimer.this.milliTimeImpl();
         if (!AbstractMasterTimer.fullspeed) {
            if (AbstractMasterTimer.useAdaptivePulse) {
               this.nextPulseTime += (long)this.PULSE_DURATION;
               long pulseDuration = AbstractMasterTimer.this.nanoTimeImpl() - pulseStarted;
               if (pulseDuration - this.lastPulseDuration > 500000L) {
                  pulseDuration /= 2L;
               }

               if (pulseDuration < 2000000L) {
                  pulseDuration = 2000000L;
               }

               if (pulseDuration >= (long)this.PULSE_DURATION_NS) {
                  pulseDuration = (long)(3 * this.PULSE_DURATION_NS / 4);
               }

               this.lastPulseDuration = pulseDuration;
               this.nextPulseTime -= pulseDuration / 1000000L;
            } else {
               this.nextPulseTime = (this.nextPulseTime + (long)this.PULSE_DURATION) / (long)this.PULSE_DURATION * (long)this.PULSE_DURATION;
            }
         }

      }

      synchronized void setClipsReady(boolean clipsReady) {
         if (this.clipsReady != clipsReady) {
            this.clipsReady = clipsReady;
            this.updateAnimationRunnable();
         }

      }

      synchronized void setJobsReady(boolean jobsReady) {
         if (this.jobsReady != jobsReady) {
            this.jobsReady = jobsReady;
            this.updateAnimationRunnable();
         }

      }

      private synchronized void updateAnimationRunnable() {
         DelayedRunnable animationRunnable = null;
         if (this.jobsReady || this.clipsReady) {
            animationRunnable = this;
         }

         AbstractMasterTimer.this.postUpdateAnimationRunnable(animationRunnable);
      }
   }
}
