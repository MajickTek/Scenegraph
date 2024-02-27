package com.sun.scenario.animation;

public abstract class Animation {
   final void throwExceptionIfRunning() {
      if (this.isRunning()) {
         throw new IllegalStateException("Cannot perform this operation while Clip is running");
      }
   }

   abstract void begin();

   abstract void end();

   abstract Status timePulse(long var1);

   public void start() {
      this.startAt(MasterTimer.milliTime());
   }

   public void animateTo(long t) {
      RunQueue runq = new RunQueue();
      this.scheduleTo(0L, t, runq);

      while(runq.next != null) {
         RunQueue cur = (RunQueue)runq.next;
         runq.next = cur.next;
         ((Clip)cur.v).runTo(cur.t, t, runq);
      }

   }

   abstract void scheduleTo(long var1, long var3, RunQueue var5);

   abstract void startAt(long var1);

   public abstract boolean isRunning();

   public abstract void pause();

   public abstract void resume();

   public abstract void cancel();

   public abstract void stop();

   static enum Status {
      SCHEDULED,
      SCHEDULEPAUSED,
      PAUSED,
      RUNNING,
      STOPPED,
      CANCELED;
   }
}
