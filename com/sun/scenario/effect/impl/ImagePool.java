/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.scenario.effect.impl;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple object pool used to recycle temporary images used by the
 * various {@code EffectPeer} implementations.  Image allocation can be
 * a fairly expensive operation (in terms of footprint and performance),
 * especially for the GPU backends, so image reuse is critical.
 * 
 * @author Chris Campbell
 */
class ImagePool {

    private final List<SoftReference<Image>> unlocked =
        new ArrayList<SoftReference<Image>>();
    private final List<SoftReference<Image>> locked =
        new ArrayList<SoftReference<Image>>();

    /**
     * Package-private constructor.
     */
    ImagePool() {
    }
    
    private static void clearImage(Image img) {
        Graphics2D g2 = (Graphics2D)img.getGraphics();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
        g2.dispose();
    }
    
    public synchronized Image checkOut(Renderer renderer, int w, int h) {
        // first look for an already cached image of sufficient size,
        // choosing the one that is closest in size to the requested dimensions
        SoftReference<Image> chosen = null;
        int mindiff = Integer.MAX_VALUE;
        Iterator<SoftReference<Image>> entries = unlocked.iterator();
        while (entries.hasNext()) {
            SoftReference<Image> entry = entries.next();
            Image eimg = entry.get();
            if (eimg == null) {
                entries.remove();
                continue;
            }
            int ew = eimg.getWidth(null);
            int eh = eimg.getHeight(null);
            if (ew >= w && eh >= h) {
                int diff = (ew-w) * (eh-h);
                if (chosen == null || diff < mindiff) {
                    chosen = entry;
                    mindiff = diff;
                }
            }
        }

        if (chosen != null) {
            unlocked.remove(chosen);
            locked.add(chosen);
            Image img = chosen.get();
            clearImage(img);
            return img;
        }
        
        // get rid of expired entries from locked list
        entries = locked.iterator();
        while (entries.hasNext()) {
            SoftReference<Image> entry = entries.next();
            Image eimg = entry.get();
            if (eimg == null) {
                entries.remove();
            }
        }
        
        // if all else fails, just create a new one...
        Image img = null;
        try {
            img = renderer.createCompatibleImage(w, h);
        } catch (OutOfMemoryError e) {}
        
        if (img == null) {
            // we may be out of vram or heap
            pruneCache();
            try {
                img = renderer.createCompatibleImage(w, h);
            } catch (OutOfMemoryError e) {}
        }
        if (img != null) {
            locked.add(new SoftReference<Image>(img));
        }
        return img;
    }
    
    public synchronized void checkIn(Image img) {
        SoftReference<Image> chosen = null;
        Iterator<SoftReference<Image>> entries = locked.iterator();
        while (entries.hasNext()) {
            SoftReference<Image> entry = entries.next();
            Image eimg = entry.get();
            if (eimg == null) {
                entries.remove();
            } else if (eimg == img) {
                chosen = entry;
                break;
            }
        }
        
        if (chosen != null) {
            locked.remove(chosen);
            unlocked.add(chosen);
        }
    }

    private void pruneCache() {
        // prune 2/3 of the cache
        int numToRemove = 2 * unlocked.size() / 3;
        Iterator<SoftReference<Image>> entries = unlocked.iterator();
        while (entries.hasNext() && numToRemove > 0) {
            SoftReference<Image> entry = entries.next();
            Image eimg = entry.get();
            if (eimg == null) {
                entries.remove();
                continue;
            }
            numToRemove--;
            eimg.flush();
            entries.remove();
        }
        // this is to help to free up space held by those images that we no
        // longer have references to
        System.gc();
        System.runFinalization();
        System.gc();
        System.runFinalization();
    }
}
