package com.sun.scenario.animation.util;

import com.sun.scenario.animation.Clip;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JComponent;

public class FocusTrigger extends Trigger implements FocusListener {
   public static FocusTrigger addTrigger(JComponent component, Clip clip, FocusTriggerEvent event) {
      FocusTrigger trigger = new FocusTrigger(clip, event);
      component.addFocusListener(trigger);
      return trigger;
   }

   public FocusTrigger(Clip clip, FocusTriggerEvent event) {
      super(clip, event);
   }

   public void focusGained(FocusEvent e) {
      this.fire(FocusTriggerEvent.field_20);
   }

   public void focusLost(FocusEvent e) {
      this.fire(FocusTriggerEvent.OUT);
   }
}
