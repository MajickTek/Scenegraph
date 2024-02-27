package com.sun.scenario.animation.util;

public class MouseTriggerEvent extends TriggerEvent {
   public static final MouseTriggerEvent ENTER = new MouseTriggerEvent("Entered");
   public static final MouseTriggerEvent EXIT = new MouseTriggerEvent("Exit");
   public static final MouseTriggerEvent PRESS = new MouseTriggerEvent("Press");
   public static final MouseTriggerEvent RELEASE = new MouseTriggerEvent("Release");
   public static final MouseTriggerEvent CLICK = new MouseTriggerEvent("Click");

   private MouseTriggerEvent(String name) {
      super(name);
   }

   public TriggerEvent getOppositeEvent() {
      if (this == ENTER) {
         return EXIT;
      } else if (this == EXIT) {
         return ENTER;
      } else if (this == PRESS) {
         return RELEASE;
      } else {
         return this == RELEASE ? PRESS : this;
      }
   }
}
