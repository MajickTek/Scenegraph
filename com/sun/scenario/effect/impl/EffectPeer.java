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

import com.sun.scenario.effect.Effect;
import com.sun.scenario.effect.Effect.AccelType;
import com.sun.scenario.effect.impl.sw.SWRenderer;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * The abstract base class for all {@code Effect} implementation peers.
 * 
 * @author Chris Campbell
 */
public abstract class EffectPeer {

    public static final String rootPkg = "com.sun.scenario.effect";
    
    private final GraphicsConfiguration config;
    private final Renderer renderer;
    private Effect effect;
    private int pass;
    
    protected EffectPeer(GraphicsConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("GraphicsConfig must be non-null");
        }
        this.config = config;
        this.renderer = getRenderer(config); // cached for easy access
    }
    
    public abstract ImageData filter(Effect effect, ImageData... inputs);
    
    public abstract AccelType getAccelType();
   
    protected final GraphicsConfiguration getGraphicsConfig() {
        return config;
    }
    
    protected Renderer getRenderer() {
        return renderer;
    }
    
    protected Effect getEffect() {
        return effect;
    }
    
    protected void setEffect(Effect effect) {
        this.effect = effect;
    }
    
    public final int getPass() {
        return pass;
    }
    
    public void setPass(int pass) {
        this.pass = pass;
    }
    
    // TODO: this input(Native)Bounds stuff is unpleasant, but we somehow
    // need to provide access to the native surface bounds for various glue
    // methods (e.g. getKvals())

    private final Rectangle[] inputBounds = new Rectangle[2];
    /**
     * Returns the "valid" bounds of the source image for the given input.
     * Since Effect implementations try to recycle temporary Images, it is
     * quite possible that the input bounds returned by this method will
     * differ from the size of the associated input Image.  For example,
     * this method may return (0, 0, 210, 180) even though the associated
     * Image has dimensions of 230x200 pixels.  Pixels in the input Image
     * outside these "valid" bounds are undefined and should be avoided.
     * 
     * @param inputIndex the index of the source input
     * @return the valid bounds of the source Image
     */
    protected final Rectangle getInputBounds(int inputIndex) {
        return inputBounds[inputIndex];
    }
    protected final void setInputBounds(int inputIndex, Rectangle r) {
        inputBounds[inputIndex] = r;
    }

    private final Rectangle[] inputNativeBounds = new Rectangle[2];
    /**
     * Returns the bounds of the native surface for the given input.
     * It is quite possible that the input native bounds returned by this
     * method will differ from the size of the associated input (Java-level)
     * Image.  This is common for the OGL and D3D backends of Java 2D,
     * where on older hardware the dimensions of a VRAM surface (e.g. texture)
     * must be a power of two.  For example, this method may return
     * (0, 0, 256, 256) even though the associated (Volatile)Image has
     * dimensions of 230x200 pixels.
     * <p>
     * This method is useful in cases where it is necessary to access
     * adjacent pixels in a native surface.  For example, the horizontal
     * distance between two texel centers of a native surface can be
     * calculated as (1f/inputNativeBounds.width); for the vertical distance,
     * (1f/inputNativeBounds.height).
     * 
     * @param inputIndex the index of the source input
     * @return the native surface bounds
     */
    protected final Rectangle getInputNativeBounds(int inputIndex) {
        return inputNativeBounds[inputIndex];
    }
    protected final void setInputNativeBounds(int inputIndex, Rectangle r) {
        inputNativeBounds[inputIndex] = r;
    }
    
    /**
     * Returns an array of four floats that represent the bounds of the
     * source region for the given input.  The returned values are in the
     * "unit" coordinate space of the native surface, where (0,0) is at
     * the upper-left corner, and (1,1) is at the lower-right corner.
     * The returned array contains the values in order (x1, y1, x2, y2).
     * <p>
     * The default implementation converts the logical source region
     * (0, 0, 1, 1) into the coordinate space of the native surface.
     * For example, if the input image is 200x220 pixels, and the native
     * surface (e.g. texture) is actually 256x256 pixels, this method will
     * return (0, 0, 200/256, 220/256).
     * <p>
     * Subclasses can override this method to provide more sophisticated
     * positioning behavior.
     * 
     * @param inputIndex the index of the source input
     * @return an array of four float values
     */
    protected float[] getSourceRegion(int inputIndex) {
        Rectangle b = getInputBounds(inputIndex);
        Rectangle nb = getInputNativeBounds(inputIndex);
        float x1 = b.x;
        float y1 = b.y;
        float x2 = x1 + b.width;
        float y2 = y1 + b.height;
        float tx1 = x1 / nb.width;
        float ty1 = y1 / nb.height;
        float tx2 = x2 / nb.width;
        float ty2 = y2 / nb.height;
        return new float[] {tx1, ty1, tx2, ty2};
    }
    
    protected Rectangle getDestBounds() {
        Rectangle r = getEffect().getBounds().getBounds();
        r.setLocation(0, 0);
        return r;
    }
    
    /**
     * Returns true if the native coordinate system has its origin at
     * the upper-left corner of the destination surface; otherwise, returns
     * false, indicating that the origin is at the lower-left corner.
     * <p>
     * This method may be useful in determining the direction of adjacent
     * pixels in an OpenGL surface (since many OpenGL methods take parameters
     * assuming a lower-left origin).
     * 
     * @return true if the coordinate system has an upper-left origin
     */
    protected boolean isOriginUpperLeft() {
        return (getAccelType() != Effect.AccelType.OPENGL);
    }
    
    
    /*
     * Peer registry methods below...
     */

    private static Map<GraphicsConfiguration, Map<String, EffectPeer>> peerCache =
        new HashMap<GraphicsConfiguration, Map<String, EffectPeer>>();

    public static EffectPeer getInstance(GraphicsConfiguration gc,
                                         String name, int unrollCount)
    {
        Map<String, EffectPeer> cache = peerCache.get(gc);
        if (cache == null) {
            cache = new HashMap<String, EffectPeer>();
            peerCache.put(gc, cache);
        }
        
        // first look for a previously cached peer using only the base name
        // (e.g. GaussianBlur); software peers do not (currently) have
        // unrolled loops, so this step should locate those...
        EffectPeer peer = cache.get(name);
        if (peer != null) {
            return peer;
        }
        
        // failing that, if there is a positive unrollCount, we attempt
        // to find a previously cached hardware peer for that unrollCount
        String uname;
        if (unrollCount > 0) {
            uname = name + "_" + unrollCount;
            peer = cache.get(uname);
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
                // first try hardware peer
                cacheName = uname;
                klass = Class.forName(rootPkg + ".impl.hw.HW" + uname + "Peer");
            } catch (ClassNotFoundException e) {
                System.err.println("Warning: hardware peer not found for: " + uname);
            }
        } else {
            //System.err.println("Warning: hardware peers not available, trying software instead...");
        }
        if (klass == null) {
            try {
                // otherwise fall back on software peer
                cacheName = name;
                klass = Class.forName(rootPkg + ".impl.sw.SW" + name + "Peer");
            } catch (ClassNotFoundException e2) {
                throw new IllegalArgumentException("Software peer not found for: " + name);
            }
        }
        try {
            Constructor ctor = klass.getConstructor(GraphicsConfiguration.class);
            peer = (EffectPeer)ctor.newInstance(gc);
        } catch (Exception e) {
            throw new RuntimeException("Error instantiating " + klass.getName(), e);
        }
        cache.put(cacheName, peer);
        return peer;
    }
    
    private static boolean isRSLFriendly(Class klass) {
        // can't use reflection here to check for sun.* class when running
        // in sandbox; however, we are allowed to walk up the tree and
        // check names of interfaces loaded by the system
        if (klass.getName().equals("sun.java2d.pipe.hw.AccelGraphicsConfig")) {
            return true;
        }
        boolean rsl = false;
        for (Class iface : klass.getInterfaces()) {
            if (isRSLFriendly(iface)) {
                rsl = true;
                break;
            }
        }
        return rsl;
    }
    
    private static boolean isRSLAvailable(GraphicsConfiguration gc) {
        return isRSLFriendly(gc.getClass());
    }

    private static boolean isHWEffectPeerAvailable(GraphicsConfiguration gc) {
        Renderer renderer = getRenderer(gc);
        return !(renderer instanceof SWRenderer);
    }
    
    
    /*
     * Renderer methods below...
     */
    
    private static Map<GraphicsConfiguration, Renderer> rendererMap =
        new HashMap<GraphicsConfiguration, Renderer>();
    
    private static Renderer createHWRenderer(GraphicsConfiguration gc) {
        Class klass;
        try {
            klass = Class.forName(rootPkg + ".impl.hw.HWEffectPeer");
        } catch (ClassNotFoundException e) {
            // we shouldn't get here since we've already verified
            // that the HWEffectPeer is available at this point; but
            // just in case, return null
            return null;
        }
        Method m;
        try {
            m = klass.getMethod("createRenderer", GraphicsConfiguration.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
        try {
            return (Renderer)m.invoke(null, gc);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static synchronized Renderer getRenderer(GraphicsConfiguration gc) {
        if (gc == null) {
            throw new IllegalArgumentException("GraphicsConfig must be non-null");
        }
        Renderer r = rendererMap.get(gc);
        if (r == null) {
            // first check to see whether one of the hardware accelerated
            // Java 2D pipelines is in use and exposes the necessary
            // "resource sharing layer" APIs (only in Sun's JDK 6u10 and above)
            if (isRSLAvailable(gc)) {
                // try locating a HWRenderer (need to use reflection in case
                // certain HW backend classes are not available;
                // this step will trigger lazy downloading of impl jars
                // via JNLP, if not already available)
                r = createHWRenderer(gc);
            }
            if (r == null) {
                // otherwise, fall back on the SWRenderer
                r = SWRenderer.getInstance();
            }
            rendererMap.put(gc, r);
        }
        return r;
    }
}
