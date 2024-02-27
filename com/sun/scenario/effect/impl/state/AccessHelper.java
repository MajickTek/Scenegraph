package com.sun.scenario.effect.impl.state;

import com.sun.scenario.effect.Effect;

public class AccessHelper {
   private static StateAccessor theStateAccessor;

   public static void setStateAccessor(StateAccessor accessor) {
      if (theStateAccessor != null) {
         throw new InternalError("EffectAccessor already initialized");
      } else {
         theStateAccessor = accessor;
      }
   }

   public static Object getState(Effect effect) {
      return effect == null ? null : theStateAccessor.getState(effect);
   }

   public interface StateAccessor {
      Object getState(Effect var1);
   }
}
