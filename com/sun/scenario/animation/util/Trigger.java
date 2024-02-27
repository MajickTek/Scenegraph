package com.sun.scenario.animation.util;

import com.sun.scenario.animation.Clip;

public abstract class Trigger {
   private boolean disarmed;
   private Clip clip;
   private TriggerEvent triggerEvent;

   protected Trigger(Clip clip) {
      this(clip, (TriggerEvent)null);
   }

   protected Trigger(Clip clip, TriggerEvent triggerEvent) {
      this.disarmed = false;
      this.clip = clip;
      this.triggerEvent = triggerEvent;
   }

   public void disarm() {
      this.disarmed = true;
   }

   protected void fire(TriggerEvent currentEvent) {
      if (!this.disarmed) {
         if (currentEvent == this.triggerEvent) {
            this.fire();
         } else if (this.triggerEvent != null && currentEvent == this.triggerEvent.getOppositeEvent()) {
            this.fire();
         }

      }
   }

   protected void fire() {
      if (!this.disarmed) {
         if (!this.clip.isAutoReverse()) {
            this.clip.stop();
         }

         this.clip.start();
      }
   }
}
