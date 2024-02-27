package com.sun.scenario.animation;

public abstract class FrameJob {
   public abstract void run();

   public void wakeUp() {
      MasterTimer.notifyJobsReady();
   }
}
