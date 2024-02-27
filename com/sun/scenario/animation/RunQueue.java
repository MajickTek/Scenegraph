package com.sun.scenario.animation;

final class RunQueue extends AnimationList<Clip, RunQueue> implements Iterable<RunQueue> {
   private Animation.Status status;
   private long pauseTime;

   public RunQueue() {
   }

   private RunQueue(Clip clip, long tstart) {
      super(clip, tstart);
      this.status = Animation.Status.SCHEDULED;
   }

   protected RunQueue makeEntry(Clip clip, long tstart) {
      return new RunQueue(clip, tstart);
   }

   final void adjustStartTime(long tdelta) {
      this.t += tdelta;
   }

   final Animation.Status getStatus() {
      return this.status;
   }

   final synchronized void pause(long tpause) {
      if (this.status == Animation.Status.SCHEDULED) {
         this.status = Animation.Status.SCHEDULEPAUSED;
      } else {
         if (this.status != Animation.Status.RUNNING) {
            return;
         }

         this.status = Animation.Status.PAUSED;
      }

      this.pauseTime = tpause;
   }

   final synchronized void resume(long tresume) {
      Animation.Status newStatus;
      if (this.status == Animation.Status.SCHEDULEPAUSED) {
         newStatus = Animation.Status.SCHEDULED;
      } else {
         if (this.status != Animation.Status.PAUSED) {
            return;
         }

         newStatus = Animation.Status.RUNNING;
      }

      this.adjustStartTime(tresume - this.pauseTime);
      this.status = newStatus;
   }

   final synchronized void stop(long tstop) {
      if (this.status == Animation.Status.PAUSED) {
         this.adjustStartTime(tstop - this.pauseTime);
      }

      this.status = Animation.Status.STOPPED;
   }

   final synchronized Animation.Status began() {
      if (this.status == Animation.Status.SCHEDULED) {
         this.status = Animation.Status.RUNNING;
      } else if (this.status == Animation.Status.SCHEDULEPAUSED) {
         this.status = Animation.Status.PAUSED;
      }

      return this.status;
   }
}
