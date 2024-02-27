package com.sun.scenario.effect.impl;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ImagePool {
   static final int QUANT = 32;
   private final List<SoftReference<Image>> unlocked = new ArrayList();
   private final List<SoftReference<Image>> locked = new ArrayList();

   private static void clearImage(Image img) {
      Graphics2D g2 = (Graphics2D)img.getGraphics();
      g2.setComposite(AlphaComposite.Clear);
      g2.fillRect(0, 0, img.getWidth((ImageObserver)null), img.getHeight((ImageObserver)null));
      g2.dispose();
   }

   public synchronized Image checkOut(Renderer renderer, int w, int h) {
      if (w <= 0 || h <= 0) {
         h = 1;
         w = 1;
      }

      w = (w + 32 - 1) / 32 * 32;
      h = (h + 32 - 1) / 32 * 32;
      SoftReference<Image> chosen = null;
      int mindiff = Integer.MAX_VALUE;
      Iterator<SoftReference<Image>> entries = this.unlocked.iterator();

      while(true) {
         SoftReference entry;
         Image eimg;
         while(entries.hasNext()) {
            entry = (SoftReference)entries.next();
            eimg = (Image)entry.get();
            if (eimg == null) {
               entries.remove();
            } else {
               int ew = eimg.getWidth((ImageObserver)null);
               int eh = eimg.getHeight((ImageObserver)null);
               if (ew >= w && eh >= h) {
                  int diff = (ew - w) * (eh - h);
                  if (chosen == null || diff < mindiff) {
                     chosen = entry;
                     mindiff = diff;
                  }
               }
            }
         }

         Image img;
         if (chosen != null) {
            this.unlocked.remove(chosen);
            this.locked.add(chosen);
            img = (Image)chosen.get();
            clearImage(img);
            return img;
         }

         entries = this.locked.iterator();

         while(entries.hasNext()) {
            entry = (SoftReference)entries.next();
            eimg = (Image)entry.get();
            if (eimg == null) {
               entries.remove();
            }
         }

         img = null;

         try {
            img = renderer.createCompatibleImage(w, h);
         } catch (OutOfMemoryError var13) {
         }

         if (img == null) {
            this.pruneCache();

            try {
               img = renderer.createCompatibleImage(w, h);
            } catch (OutOfMemoryError var12) {
            }
         }

         if (img != null) {
            this.locked.add(new SoftReference(img));
         }

         return img;
      }
   }

   public synchronized void checkIn(Image img) {
      SoftReference<Image> chosen = null;
      Iterator<SoftReference<Image>> entries = this.locked.iterator();

      while(entries.hasNext()) {
         SoftReference<Image> entry = (SoftReference)entries.next();
         Image eimg = (Image)entry.get();
         if (eimg == null) {
            entries.remove();
         } else if (eimg == img) {
            chosen = entry;
            break;
         }
      }

      if (chosen != null) {
         this.locked.remove(chosen);
         this.unlocked.add(chosen);
      }

   }

   private void pruneCache() {
      int numToRemove = 2 * this.unlocked.size() / 3;
      Iterator<SoftReference<Image>> entries = this.unlocked.iterator();

      while(entries.hasNext() && numToRemove > 0) {
         SoftReference<Image> entry = (SoftReference)entries.next();
         Image eimg = (Image)entry.get();
         if (eimg == null) {
            entries.remove();
         } else {
            --numToRemove;
            eimg.flush();
            entries.remove();
         }
      }

      System.gc();
      System.runFinalization();
      System.gc();
      System.runFinalization();
   }
}
