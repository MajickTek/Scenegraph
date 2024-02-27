package com.sun.scenario.animation;

import java.util.HashMap;
import java.util.Map;

public class Evaluators {
   private static Map<Class<?>, Evaluator> linearCache = new HashMap();

   private Evaluators() {
   }

   public static <T> Evaluator<T> getLinearInstance(Class<?> type) {
      Evaluator evaluator = (Evaluator)linearCache.get(type);
      if (evaluator == null) {
         evaluator = new Linear(Composer.getInstance(type));
         linearCache.put(type, evaluator);
      }

      return (Evaluator)evaluator;
   }

   private static class Linear<T> implements Evaluator<T> {
      private Composer<T> composer;
      private double[] v0arr;
      private double[] v1arr;

      private Linear(Composer<T> composer) {
         this.composer = composer;
         this.v0arr = new double[composer.getNumVals()];
         this.v1arr = new double[composer.getNumVals()];
      }

      public T evaluate(T v0, T v1, float fraction) {
         this.composer.decompose(v0, this.v0arr);
         this.composer.decompose(v1, this.v1arr);

         for(int i = 0; i < this.v0arr.length; ++i) {
            double[] var10000 = this.v0arr;
            var10000[i] += (this.v1arr[i] - this.v0arr[i]) * (double)fraction;
         }

         return this.composer.compose(this.v0arr);
      }
   }
}
