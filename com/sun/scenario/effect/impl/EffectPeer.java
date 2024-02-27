package com.sun.scenario.effect.impl;

import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.impl.sw.SWRenderer;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class EffectPeer {
   public static final String rootPkg = "com.sun.scenario.effect";
   private static boolean trySIMD = true;
   private final GraphicsConfiguration config;
   private final Renderer renderer;
   private Effect effect;
   private int pass;
   private final Rectangle[] inputBounds = new Rectangle[2];
   private final Rectangle[] inputNativeBounds = new Rectangle[2];
   private Rectangle destBounds;
   private final Rectangle destNativeBounds = new Rectangle();
   private static Map<GraphicsConfiguration, Map<String, EffectPeer>> peerCache;
   private static Map<GraphicsConfiguration, Renderer> rendererMap;

   protected EffectPeer(GraphicsConfiguration config) {
      if (config == null) {
         throw new IllegalArgumentException("GraphicsConfig must be non-null");
      } else {
         this.config = config;
         this.renderer = getRenderer(config);
      }
   }

   public abstract ImageData filter(Effect var1, AffineTransform var2, ImageData... var3);

   public abstract Effect.AccelType getAccelType();

   protected final GraphicsConfiguration getGraphicsConfig() {
      return this.config;
   }

   protected Renderer getRenderer() {
      return this.renderer;
   }

   protected Effect getEffect() {
      return this.effect;
   }

   protected void setEffect(Effect effect) {
      this.effect = effect;
   }

   public final int getPass() {
      return this.pass;
   }

   public void setPass(int pass) {
      this.pass = pass;
   }

   protected final Rectangle getInputBounds(int inputIndex) {
      return this.inputBounds[inputIndex];
   }

   protected final void setInputBounds(int inputIndex, Rectangle r) {
      this.inputBounds[inputIndex] = r;
   }

   protected final Rectangle getInputNativeBounds(int inputIndex) {
      return this.inputNativeBounds[inputIndex];
   }

   protected final void setInputNativeBounds(int inputIndex, Rectangle r) {
      this.inputNativeBounds[inputIndex] = r;
   }

   protected float[] getSourceRegion(int inputIndex) {
      return getSourceRegion(this.getInputBounds(inputIndex), this.getInputNativeBounds(inputIndex), this.getDestBounds());
   }

   static float[] getSourceRegion(Rectangle srcBounds, Rectangle srcNativeBounds, Rectangle dstBounds) {
      float x1 = (float)(dstBounds.x - srcBounds.x);
      float y1 = (float)(dstBounds.y - srcBounds.y);
      float x2 = x1 + (float)dstBounds.width;
      float y2 = y1 + (float)dstBounds.height;
      float sw = (float)srcNativeBounds.width;
      float sh = (float)srcNativeBounds.height;
      return new float[]{x1 / sw, y1 / sh, x2 / sw, y2 / sh};
   }

   protected final void setDestBounds(Rectangle r) {
      this.destBounds = r;
   }

   protected final Rectangle getDestBounds() {
      return this.destBounds;
   }

   protected final Rectangle getDestNativeBounds() {
      return this.destNativeBounds;
   }

   protected final void setDestNativeBounds(int w, int h) {
      this.destNativeBounds.setSize(w, h);
   }

   protected Object getSamplerData(int i) {
      return null;
   }

   protected boolean isOriginUpperLeft() {
      return this.getAccelType() != Effect.AccelType.OPENGL;
   }

   public static float[] getPremultipliedComponents(Color c) {
      float[] comps = c.getComponents((float[])null);
      comps[0] *= comps[3];
      comps[1] *= comps[3];
      comps[2] *= comps[3];
      return comps;
   }

   public static EffectPeer getInstance(GraphicsConfiguration gc, String name, int unrollCount) {
      Map<String, EffectPeer> cache = (Map)peerCache.get(gc);
      if (cache == null) {
         cache = new HashMap();
         peerCache.put(gc, cache);
      }

      EffectPeer peer = (EffectPeer)((Map)cache).get(name);
      if (peer != null) {
         return peer;
      } else {
         String uname;
         if (unrollCount > 0) {
            uname = name + "_" + unrollCount;
            peer = (EffectPeer)((Map)cache).get(uname);
            if (peer != null) {
               return peer;
            }
         } else {
            uname = name;
         }

         Class klass = null;
         String cacheName = null;
         if (isHWEffectPeerAvailable(gc)) {
            try {
               cacheName = uname;
               klass = Class.forName("com.sun.scenario.effect.impl.hw.HW" + uname + "Peer");
            } catch (ClassNotFoundException var12) {
               System.err.println("Warning: hardware peer not found for: " + uname);
            }
         }

         if (klass == null && trySIMD) {
            try {
               cacheName = name;
               klass = Class.forName("com.sun.scenario.effect.impl.sw.sse.SSE" + name + "Peer");
            } catch (ClassNotFoundException var11) {
            }
         }

         if (klass == null) {
            try {
               cacheName = name;
               klass = Class.forName("com.sun.scenario.effect.impl.sw.SW" + name + "Peer");
            } catch (ClassNotFoundException var10) {
               throw new IllegalArgumentException("Software peer not found for: " + name);
            }
         }

         try {
            Constructor ctor = klass.getConstructor(GraphicsConfiguration.class);
            peer = (EffectPeer)ctor.newInstance(gc);
         } catch (Exception var9) {
            throw new RuntimeException("Error instantiating " + klass.getName(), var9);
         }

         ((Map)cache).put(cacheName, peer);
         return peer;
      }
   }

   private static boolean isRSLFriendly(Class klass) {
      if (klass.getName().equals("sun.java2d.pipe.hw.AccelGraphicsConfig")) {
         return true;
      } else {
         boolean rsl = false;
         Class[] arr$ = klass.getInterfaces();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Class iface = arr$[i$];
            if (isRSLFriendly(iface)) {
               rsl = true;
               break;
            }
         }

         return rsl;
      }
   }

   private static boolean isRSLAvailable(GraphicsConfiguration gc) {
      return isRSLFriendly(gc.getClass());
   }

   private static boolean isHWEffectPeerAvailable(GraphicsConfiguration gc) {
      Renderer renderer = getRenderer(gc);
      String klass = renderer.getClass().getSimpleName();
      return !klass.startsWith("SW") && !klass.startsWith("SSE");
   }

   private static Renderer createHWRenderer(GraphicsConfiguration gc) {
      Class klass;
      try {
         klass = Class.forName("com.sun.scenario.effect.impl.hw.HWEffectPeer");
      } catch (Throwable var6) {
         return null;
      }

      Method m;
      try {
         m = klass.getMethod("createRenderer", GraphicsConfiguration.class);
      } catch (Throwable var5) {
         return null;
      }

      try {
         return (Renderer)m.invoke((Object)null, gc);
      } catch (Throwable var4) {
         return null;
      }
   }

   private static Renderer getSSERenderer() {
      Class klass;
      try {
         klass = Class.forName("com.sun.scenario.effect.impl.sw.sse.SSERenderer");
      } catch (Throwable var5) {
         return null;
      }

      Method m;
      try {
         m = klass.getMethod("getInstance");
      } catch (Throwable var4) {
         return null;
      }

      try {
         return (Renderer)m.invoke((Object)null);
      } catch (Throwable var3) {
         return null;
      }
   }

   public static synchronized Renderer getRenderer(GraphicsConfiguration gc) {
      if (gc == null) {
         throw new IllegalArgumentException("GraphicsConfig must be non-null");
      } else {
         Renderer r = (Renderer)rendererMap.get(gc);
         if (r == null) {
            if (isRSLAvailable(gc)) {
               r = createHWRenderer(gc);
            }

            if (r == null) {
               if (trySIMD) {
                  r = getSSERenderer();
                  if (r == null) {
                     trySIMD = false;
                  }
               }

               if (r == null) {
                  r = SWRenderer.getInstance();
               }
            }

            rendererMap.put(gc, r);
         }

         return (Renderer)r;
      }
   }

   static {
      try {
         if ("false".equals(System.getProperty("com.sun.scenario.effect.simd"))) {
            trySIMD = false;
         }
      } catch (SecurityException var1) {
      }

      peerCache = new HashMap();
      rendererMap = new HashMap();
   }
}
