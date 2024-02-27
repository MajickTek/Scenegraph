package com.sun.scenario.animation.util;

public class FocusTriggerEvent extends TriggerEvent {
   // $FF: renamed from: IN com.sun.scenario.animation.util.FocusTriggerEvent
   public static final FocusTriggerEvent field_20 = new FocusTriggerEvent("FocusIn");
   public static final FocusTriggerEvent OUT = new FocusTriggerEvent("FocusOut");

   private FocusTriggerEvent(String name) {
      super(name);
   }

   public TriggerEvent getOppositeEvent() {
      return this == field_20 ? OUT : field_20;
   }
}
