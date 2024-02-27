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

package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.EffectPeer;
import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

/**
 * The abstract base class for effects that use a Gaussian convolution kernel.
 * 
 * @author Chris Campbell
 */
public abstract class AbstractGaussian extends CoreEffect {

    private float radius;
    private final String prefix;

    /**
     * Package-private constructor.
     * 
     * @param prefix the prefix used for locating an {@code EffectPeer}
     * @param radius the radius of the Gaussian kernel
     * @param input the single input {@code Effect}
     * 
     * @throws IllegalArgumentException if {@code radius} is outside the
     * allowable range, or if {@code input} is null
     */
    AbstractGaussian(String prefix, float radius, Effect input) {
        super(input);
        this.prefix = prefix;
        setRadius(radius);
    }
    
    /**
     * Returns the radius of the Gaussian kernel.
     * 
     * @return the radius of the Gaussian kernel
     */
    public float getRadius() {
        return radius;
    }
    
    /**
     * Sets the radius of the Gaussian kernel.
     * <pre>
     *       Min:  1.0
     *       Max: 63.0
     *   Default: 10.0
     *  Identity:  n/a
     * </pre>
     * 
     * @param radius the radius of the Gaussian kernel
     * @throws IllegalArgumentException if {@code radius} is outside the
     * allowable range
     */
    public void setRadius(float radius) {
        if (radius < 1f || radius > 63f) {
            throw new IllegalArgumentException("Radius must be in the range [1,63]");
        }
        float old = this.radius;
        this.radius = radius;
        firePropertyChange("radius", old, radius);
        updatePeer();
    }

    /**
     * Returns the padding needed on each side of the destination image
     * for the current radius.
     * 
     * @return the padding for the current radius
     */
    private int getPad() {
        return (int)Math.ceil(getRadius());
    }
    
    /**
     * Returns the size of the kernel (in either direction) for the current
     * radius.
     * 
     * @return the size of the kernel
     */
    private int getKernelSize() {
        int r = getPad();
        return (r * 2) + 1;
    }
    
    /**
     * Updates the peer "key" for the current radius.
     */
    private void updatePeer() {
        int ksize = getKernelSize();
        int psize = ksize + (10 - (ksize % 10));
        updatePeerKey(prefix, psize);
    }
    
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D r = getInputs().get(0).getBounds();
        int pad = getPad();
        int pad2 = 2*pad;
        r.setFrame(r.getX()-pad, r.getY()-pad, r.getWidth()+pad2, r.getHeight()+pad2);
        return r;
    }
    
    @Override
    public Image filter(GraphicsConfiguration config) {
        EffectPeer peer = getPeer(config);
        peer.setPass(0);
        ImageData res0 = filterInputs(config, 0);
        
        peer.setPass(1);
        ImageData res1 = filterData(config, res0);
        return res1.getImage();
    }
}
