package com.sun.scenario.animation;

public class Util {
   public static final boolean hasActiveAnimation() {
      return !MasterTimer.isIdle();
   }
}
