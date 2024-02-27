package com.sun.scenario.animation.util;

import com.sun.scenario.animation.Clip;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

public class ActionTrigger extends Trigger implements ActionListener {
   public static ActionTrigger addTrigger(Object object, Clip clip) {
      ActionTrigger trigger = new ActionTrigger(clip);

      try {
         Method addListenerMethod = object.getClass().getMethod("addActionListener", ActionListener.class);
         addListenerMethod.invoke(object, trigger);
         return trigger;
      } catch (Exception var4) {
         throw new IllegalArgumentException("Problem adding listener to object: " + var4);
      }
   }

   public ActionTrigger(Clip clip) {
      super(clip);
   }

   public void actionPerformed(ActionEvent ae) {
      this.fire();
   }
}
