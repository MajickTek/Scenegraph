package com.sun.scenario.animation;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Composer<T> {
   private static final Map<Class<?>, Composer> impls = new HashMap();
   private final int numVals;

   public static void register(Class<?> type, Class<? extends Composer> implClass) {
      Composer impl;
      try {
         Constructor<? extends Composer> ctor = implClass.getConstructor();
         impl = (Composer)ctor.newInstance();
      } catch (Exception var4) {
         throw new IllegalArgumentException("Problem constructing appropriate Composer for type " + type + ":", var4);
      }

      impls.put(type, impl);
   }

   public static <T> Composer<T> getInstance(Class<?> type) {
      Class<? extends Composer> compClass = null;
      Iterator i$ = impls.keySet().iterator();

      Class klass;
      do {
         if (!i$.hasNext()) {
            throw new IllegalArgumentException("No Composer can be found for type " + type + "; consider using" + " different types for your values or supplying a custom" + " Composer");
         }

         klass = (Class)i$.next();
      } while(!klass.isAssignableFrom(type));

      return (Composer)impls.get(klass);
   }

   protected Composer(int numVals) {
      this.numVals = numVals;
   }

   public int getNumVals() {
      return this.numVals;
   }

   public abstract double[] decompose(T var1, double[] var2);

   public abstract T compose(double[] var1);

   static {
      register(Byte.class, ComposerByte.class);
      register(Short.class, ComposerShort.class);
      register(Integer.class, ComposerInteger.class);
      register(Long.class, ComposerLong.class);
      register(Float.class, ComposerFloat.class);
      register(Double.class, ComposerDouble.class);
      register(Boolean.class, ComposerBoolean.class);
      register(Color.class, ComposerColor.class);
      register(Point2D.class, ComposerPoint2D.class);
      register(Line2D.class, ComposerLine2D.class);
      register(Dimension2D.class, ComposerDimension2D.class);
      register(Rectangle2D.class, ComposerRectangle2D.class);
      register(RoundRectangle2D.class, ComposerRoundRectangle2D.class);
      register(Ellipse2D.class, ComposerEllipse2D.class);
      register(Arc2D.class, ComposerArc2D.class);
      register(QuadCurve2D.class, ComposerQuadCurve2D.class);
      register(CubicCurve2D.class, ComposerCubicCurve2D.class);
   }
}
