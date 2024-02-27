package com.sun.scenario.animation;

import java.util.ArrayList;
import java.util.List;

public class MotionPath<V> extends Path {
   private final Composer<V> composer;
   private final List<Segment> segments;
   private final double[] cachedRet;
   // $FF: renamed from: v0 double[]
   private final double[] field_21;
   // $FF: renamed from: v1 double[]
   private final double[] field_22;

   private MotionPath(Composer<V> composer) {
      super(composer.getNumVals());
      this.composer = composer;
      this.segments = new ArrayList();
      this.segments.add((Object)null);
      this.cachedRet = new double[composer.getNumVals()];
      this.field_21 = new double[composer.getNumVals() * 3];
      this.field_22 = new double[composer.getNumVals()];
   }

   public static <V> MotionPath<V> create(Class<?> type) {
      return new MotionPath(Composer.getInstance(type));
   }

   private void checkExpanded() {
      if (!this.segments.isEmpty()) {
         Segment<V> seg = (Segment)this.segments.get(0);
         this.composer.decompose(seg.field_36, this.field_21);
         this.moveTo(this.field_21);

         for(int i = 1; i < this.segments.size(); ++i) {
            seg = (Segment)this.segments.get(i);
            if (seg.type == MotionPath.Segment.Type.LINEAR) {
               this.composer.decompose(seg.field_36, this.field_21);
               this.linearTo(this.field_21);
            } else {
               CubicSegment<V> cseg = (CubicSegment)seg;
               int n = this.composer.getNumVals();
               this.composer.decompose(cseg.ctrlPt1, this.field_22);
               System.arraycopy(this.field_22, 0, this.field_21, 0, n);
               this.composer.decompose(cseg.ctrlPt2, this.field_22);
               System.arraycopy(this.field_22, 0, this.field_21, n, n);
               this.composer.decompose(cseg.pt, this.field_22);
               System.arraycopy(this.field_22, 0, this.field_21, n + n, n);
               this.cubicTo(this.field_21);
            }
         }

         this.segments.clear();
      }
   }

   public void linearTo(V pt) {
      this.segments.add(new LinearSegment(pt));
   }

   public void cubicTo(V ctrlPt1, V ctrlPt2, V pt) {
      this.segments.add(new CubicSegment(ctrlPt1, ctrlPt2, pt));
   }

   public V getValue(float t) {
      this.checkExpanded();
      this.getValue(t, this.cachedRet);
      return this.composer.compose(this.cachedRet);
   }

   public V getRotationVector(float t) {
      this.checkExpanded();
      this.getRotationVector(t, this.cachedRet);
      return this.composer.compose(this.cachedRet);
   }

   public Evaluator<V> createEvaluator() {
      return new PathEvaluator();
   }

   public Evaluator<V> createRotationEvaluator() {
      return new RotationEvaluator();
   }

   private void setExtremeValues(V start, V end) {
      Segment first = new LinearSegment(start);
      this.segments.set(0, first);
      Segment last = (Segment)this.segments.get(this.segments.size() - 1);
      last.field_36 = end;
   }

   private class RotationEvaluator implements Evaluator<V> {
      private RotationEvaluator() {
      }

      public V evaluate(V v0, V v1, float fraction) {
         if (!MotionPath.this.segments.isEmpty()) {
            MotionPath.this.setExtremeValues(v0, v1);
         }

         return MotionPath.this.getRotationVector(fraction);
      }
   }

   private class PathEvaluator implements Evaluator<V> {
      private PathEvaluator() {
      }

      public V evaluate(V v0, V v1, float fraction) {
         if (!MotionPath.this.segments.isEmpty()) {
            MotionPath.this.setExtremeValues(v0, v1);
         }

         return MotionPath.this.getValue(fraction);
      }
   }

   private static class CubicSegment<V> extends Segment<V> {
      V ctrlPt1;
      V ctrlPt2;

      CubicSegment(V ctrlPt1, V ctrlPt2, V pt) {
         super(MotionPath.Segment.Type.CUBIC, pt);
         this.ctrlPt1 = ctrlPt1;
         this.ctrlPt2 = ctrlPt2;
      }
   }

   private static class LinearSegment<V> extends Segment<V> {
      LinearSegment(V pt) {
         super(MotionPath.Segment.Type.LINEAR, pt);
      }
   }

   private static class Segment<V> {
      final Type type;
      // $FF: renamed from: pt java.lang.Object
      V field_36;

      Segment(Type type, V pt) {
         this.type = type;
         this.field_36 = pt;
      }

      static enum Type {
         LINEAR,
         CUBIC;
      }
   }
}
