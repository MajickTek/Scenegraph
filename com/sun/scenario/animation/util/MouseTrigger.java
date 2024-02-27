package com.sun.scenario.animation.util;

import com.sun.scenario.animation.Clip;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;

public class MouseTrigger extends Trigger implements MouseListener {
   public static MouseTrigger addTrigger(JComponent component, Clip clip, MouseTriggerEvent event) {
      MouseTrigger trigger = new MouseTrigger(clip, event);
      component.addMouseListener(trigger);
      return trigger;
   }

   public MouseTrigger(Clip clip, MouseTriggerEvent event) {
      super(clip, event);
   }

   public void mouseEntered(MouseEvent e) {
      this.fire(MouseTriggerEvent.ENTER);
   }

   public void mouseExited(MouseEvent e) {
      this.fire(MouseTriggerEvent.EXIT);
   }

   public void mousePressed(MouseEvent e) {
      this.fire(MouseTriggerEvent.PRESS);
   }

   public void mouseReleased(MouseEvent e) {
      this.fire(MouseTriggerEvent.RELEASE);
   }

   public void mouseClicked(MouseEvent e) {
      this.fire(MouseTriggerEvent.CLICK);
   }
}
