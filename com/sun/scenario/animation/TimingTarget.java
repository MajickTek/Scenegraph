package com.sun.scenario.animation;

public interface TimingTarget {
   void timingEvent(float var1, long var2);

   void begin();

   void end();

   void pause();

   void resume();
}
