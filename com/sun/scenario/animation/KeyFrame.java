package com.sun.scenario.animation;

public class KeyFrame<T> {
   private float time;
   private T value;
   private Interpolator interpolator;
   private Evaluator<T> evaluator;

   public static <T> KeyFrame<T> create(float time, T value) {
      return new KeyFrame(time, value, (Interpolator)null, (Evaluator)null);
   }

   public static <T> KeyFrame<T> create(float time, T value, Interpolator interpolator) {
      return new KeyFrame(time, value, interpolator, (Evaluator)null);
   }

   public static <T> KeyFrame<T> create(float time, T value, Evaluator<T> evaluator) {
      return new KeyFrame(time, value, (Interpolator)null, evaluator);
   }

   public static <T> KeyFrame<T> create(float time, T value, Interpolator interpolator, Evaluator<T> evaluator) {
      return new KeyFrame(time, value, interpolator, evaluator);
   }

   public static <T> KeyFrame<T> create(float time, T ctrlPt1, T ctrlPt2, T value) {
      MotionPath<T> mp = MotionPath.create(value.getClass());
      mp.cubicTo(ctrlPt1, ctrlPt2, value);
      return create(time, value, mp.createEvaluator());
   }

   private KeyFrame(float time, T value, Interpolator interpolator, Evaluator<T> evaluator) {
      this.time = time;
      this.value = value;
      if (interpolator != null) {
         this.interpolator = interpolator;
      } else {
         this.interpolator = Interpolators.getLinearInstance();
      }

      if (evaluator != null) {
         this.evaluator = evaluator;
      } else {
         this.evaluator = Evaluators.getLinearInstance(value.getClass());
      }

   }

   public T getValue() {
      return this.value;
   }

   public float getTime() {
      return this.time;
   }

   public Interpolator getInterpolator() {
      return this.interpolator;
   }

   public Evaluator<T> getEvaluator() {
      return this.evaluator;
   }
}
