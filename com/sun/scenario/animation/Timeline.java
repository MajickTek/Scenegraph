package com.sun.scenario.animation;

import java.util.Iterator;

public final class Timeline extends Animation {
   private Schedule targets = new Schedule();
   private boolean mightBeRunning;

   public static boolean isIdle() {
      return MasterTimer.isIdle();
   }

   public static void addFrameJob(FrameJob job) {
      MasterTimer.addFrameJob(job);
   }

   public static void removeFrameJob(FrameJob job) {
      MasterTimer.removeFrameJob(job);
   }

   public void schedule(Animation anim) {
      this.schedule(anim, 0L);
   }

   public synchronized void schedule(Animation anim, long offset) {
      this.throwExceptionIfRunning();
      this.targets.insert(anim, offset);
   }

   public Iterable<Animation> animations() {
      return this.targets.animations(this);
   }

   public Iterable<? extends ScheduledAnimation> entries() {
      return this.targets.entries(this);
   }

   public synchronized long unschedule(Animation anim) {
      this.throwExceptionIfRunning();
      Schedule s = (Schedule)this.targets.remove(anim);
      return s == null ? -1L : s.getTime();
   }

   public synchronized void unscheduleAll() {
      this.targets.clear();
   }

   void begin() {
   }

   void end() {
   }

   Animation.Status timePulse(long elapsedTime) {
      return Animation.Status.CANCELED;
   }

   void scheduleTo(long trel, long tend, RunQueue runq) {
      this.throwExceptionIfRunning();
      this.targets.transferUpTo(trel, tend, runq);
   }

   synchronized void startAt(long when) {
      this.throwExceptionIfRunning();
      this.mightBeRunning = true;
      this.targets.scheduleRelativeTo(when);
   }

   public synchronized boolean isRunning() {
      if (this.mightBeRunning) {
         Iterator i$ = this.targets.iterator();

         while(i$.hasNext()) {
            Schedule a = (Schedule)i$.next();
            if (a.getAnimation().isRunning()) {
               return true;
            }
         }

         this.mightBeRunning = false;
      }

      return false;
   }

   public synchronized void pause() {
      Iterator i$ = this.targets.iterator();

      while(i$.hasNext()) {
         Schedule a = (Schedule)i$.next();
         a.getAnimation().pause();
      }

   }

   public synchronized void resume() {
      Iterator i$ = this.targets.iterator();

      while(i$.hasNext()) {
         Schedule a = (Schedule)i$.next();
         a.getAnimation().resume();
      }

   }

   public synchronized void cancel() {
      Iterator i$ = this.targets.iterator();

      while(i$.hasNext()) {
         Schedule a = (Schedule)i$.next();
         a.getAnimation().cancel();
      }

   }

   public synchronized void stop() {
      Iterator i$ = this.targets.iterator();

      while(i$.hasNext()) {
         Schedule a = (Schedule)i$.next();
         a.getAnimation().stop();
      }

   }
}
