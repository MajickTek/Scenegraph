package com.sun.scenario.animation;

import java.util.ArrayList;
import java.util.List;

public class KeyFrames<T> extends TimingTargetAdapter {
   private final Property<T> property;
   private final List<KeyFrame<T>> keyFrames;

   public static <T> KeyFrames<T> create(Property<T> property, T... keyValues) {
      return new KeyFrames(property, keyValues);
   }

   public static <T> KeyFrames<T> create(Object target, String property, T... keyValues) {
      BeanProperty<T> bp = new BeanProperty(target, property);
      return new KeyFrames(bp, keyValues);
   }

   public static <T> KeyFrames<T> create(Property<T> property, KeyFrame<T>... keyFrames) {
      return new KeyFrames(property, keyFrames);
   }

   public static <T> KeyFrames<T> create(Object target, String property, KeyFrame<T>... keyFrames) {
      BeanProperty<T> bp = new BeanProperty(target, property);
      return new KeyFrames(bp, keyFrames);
   }

   private KeyFrames(Property<T> property) {
      this.property = property;
      this.keyFrames = new ArrayList();
   }

   private KeyFrames(Property<T> property, T... keyValues) {
      this(property);
      if (keyValues.length == 1) {
         T val = keyValues[0];
         this.keyFrames.add((Object)null);
         this.keyFrames.add(KeyFrame.create(1.0F, val));
      } else {
         for(int i = 0; i < keyValues.length; ++i) {
            float time = (float)i / (float)(keyValues.length - 1);
            this.keyFrames.add(KeyFrame.create(time, keyValues[i]));
         }
      }

   }

   private KeyFrames(Property<T> property, KeyFrame<T>... kfs) {
      this(property);
      if (kfs.length == 1) {
         this.keyFrames.add((Object)null);
         this.keyFrames.add(kfs[0]);
      } else if (kfs[0].getTime() > 0.0F) {
         this.keyFrames.add((Object)null);
      }

      KeyFrame<T>[] arr$ = kfs;
      int len$ = kfs.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         KeyFrame<T> kf = arr$[i$];
         this.keyFrames.add(kf);
      }

      KeyFrame<T> last = kfs[kfs.length - 1];
      if (last.getTime() < 1.0F) {
         this.keyFrames.add(KeyFrame.create(1.0F, last.getValue()));
      }

   }

   public Property<T> getProperty() {
      return this.property;
   }

   public T getValue(float t) {
      if (t < 0.0F) {
         t = 0.0F;
      } else if (t > 1.0F) {
         t = 1.0F;
      }

      KeyFrame<T> kf1 = (KeyFrame)this.keyFrames.get(0);
      if (kf1 == null) {
         kf1 = KeyFrame.create(0.0F, this.getProperty().getValue());
         this.keyFrames.set(0, kf1);
      }

      KeyFrame<T> kf2 = null;
      float segT = 0.0F;
      float prevT = 0.0F;

      for(int i = 1; i < this.keyFrames.size(); ++i) {
         kf2 = (KeyFrame)this.keyFrames.get(i);
         segT = kf2.getTime();
         if (t <= segT) {
            segT = (t - prevT) / (segT - prevT);
            break;
         }

         prevT = segT;
         kf1 = kf2;
      }

      segT = kf2.getInterpolator().interpolate(segT);
      return kf2.getEvaluator().evaluate(kf1.getValue(), kf2.getValue(), segT);
   }

   public void timingEvent(float fraction, long totalElapsed) {
      this.property.setValue(this.getValue(fraction));
   }

   public void begin() {
      if (this.keyFrames.get(0) == null) {
         KeyFrame<T> kf = KeyFrame.create(0.0F, this.getProperty().getValue());
         this.keyFrames.set(0, kf);
      }

   }
}
