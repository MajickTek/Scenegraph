package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.EffectPeer;
import com.sun.scenario.effect.impl.Renderer;
import java.awt.GraphicsConfiguration;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FloatMap {
   private final int width;
   private final int height;
   private final FloatBuffer buf;
   private boolean cacheValid;
   private Map<GraphicsConfiguration, Entry> cache;

   public FloatMap(int width, int height) {
      if (width > 0 && width <= 4096 && height > 0 && height <= 4096) {
         this.width = width;
         this.height = height;
         this.buf = FloatBuffer.allocate(width * height * 4);
      } else {
         throw new IllegalArgumentException("Width and height must be in the range [1, 4096]");
      }
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public float[] getData() {
      return this.buf.array();
   }

   public FloatBuffer getBuffer() {
      return this.buf;
   }

   public float getSample(int x, int y, int band) {
      return this.buf.get((x + y * this.width) * 4 + band);
   }

   public void setSample(int x, int y, int band, float sample) {
      this.buf.put((x + y * this.width) * 4 + band, sample);
      this.cacheValid = false;
   }

   public void setSamples(int x, int y, float s0) {
      int index = (x + y * this.width) * 4;
      this.buf.put(index + 0, s0);
      this.cacheValid = false;
   }

   public void setSamples(int x, int y, float s0, float s1) {
      int index = (x + y * this.width) * 4;
      this.buf.put(index + 0, s0);
      this.buf.put(index + 1, s1);
      this.cacheValid = false;
   }

   public void setSamples(int x, int y, float s0, float s1, float s2) {
      int index = (x + y * this.width) * 4;
      this.buf.put(index + 0, s0);
      this.buf.put(index + 1, s1);
      this.buf.put(index + 2, s2);
      this.cacheValid = false;
   }

   public void setSamples(int x, int y, float s0, float s1, float s2, float s3) {
      int index = (x + y * this.width) * 4;
      this.buf.put(index + 0, s0);
      this.buf.put(index + 1, s1);
      this.buf.put(index + 2, s2);
      this.buf.put(index + 3, s3);
      this.cacheValid = false;
   }

   public Object getAccelData(GraphicsConfiguration config) {
      Entry entry;
      if (this.cache == null) {
         this.cache = new HashMap();
      } else if (!this.cacheValid) {
         for(Iterator i$ = this.cache.values().iterator(); i$.hasNext(); entry.valid = false) {
            entry = (Entry)i$.next();
         }

         this.cacheValid = true;
      }

      Renderer renderer = EffectPeer.getRenderer(config);
      entry = (Entry)this.cache.get(config);
      if (entry == null) {
         Object texture = renderer.createFloatTexture(this.width, this.height);
         entry = new Entry(texture);
         this.cache.put(config, entry);
      }

      if (!entry.valid) {
         renderer.updateFloatTexture(entry.texture, this);
         entry.valid = true;
      }

      return entry.texture;
   }

   private static class Entry {
      Object texture;
      boolean valid;

      Entry(Object texture) {
         this.texture = texture;
      }
   }
}
