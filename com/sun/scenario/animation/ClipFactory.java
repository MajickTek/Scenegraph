package com.sun.scenario.animation;

abstract class ClipFactory extends Animation {
   public static <T> Clip create(long duration, Object target, String property, T... keyValues) {
      BeanProperty<T> bp = new BeanProperty(target, property);
      return create(duration, bp, keyValues);
   }

   public static <T> Clip create(long duration, Property<T> property, T... keyValues) {
      KeyFrames<T> kf = KeyFrames.create(property, keyValues);
      return new Clip(duration, 1.0F, kf);
   }

   public static <T> Clip create(long duration, float repeatCount, Property<T> property, T... keyValues) {
      KeyFrames<T> kf = KeyFrames.create(property, keyValues);
      return new Clip(duration, repeatCount, kf);
   }

   public static <T> Clip create(long duration, float repeatCount, Object target, String property, T... keyValues) {
      BeanProperty<T> bp = new BeanProperty(target, property);
      return create(duration, repeatCount, bp, keyValues);
   }
}
