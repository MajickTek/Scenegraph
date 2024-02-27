package com.sun.scenario.animation.util;

public class TriggerEvent {
   private String name;

   protected TriggerEvent(String name) {
      this.name = name;
   }

   public TriggerEvent getOppositeEvent() {
      return this;
   }
}
