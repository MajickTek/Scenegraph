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

import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

/**
 * An effect that filters out (i.e., replaces with a transparent value) all
 * pixels with brightness lower than the configurable threshold value.
 * 
 * @author Chris Campbell
 */
public class Brightpass extends CoreEffect {

    private float threshold;
    
    /**
     * Constructs a new {@code Brightpass} effect with the default
     * threshold value (0.3), using the {@link Source source content}
     * as the input.
     * This is a shorthand equivalent to:
     * <pre>
     *     new Brightpass(new Source(true))
     * </pre>
     */
    public Brightpass() {
        this(new Source(true));
    }

    /**
     * Constructs a new {@code Brightpass} effect with the default
     * threshold value (0.3).
     * 
     * @param input the single input {@code Effect}
     * @throws IllegalArgumentException if {@code input} is null
     */
    public Brightpass(Effect input) {
        super(input);
        setThreshold(0.3f);
        updatePeerKey("Brightpass");
    }
    
    /**
     * Returns the threshold, which controls which pixels are included in
     * the resulting image.  Pixel values with brightness greater than
     * this threshold value will be included.
     * 
     * @return the threshold value
     */
    public float getThreshold() {
        return threshold;
    }
    
    /**
     * Sets the threshold, which controls which pixels are included in
     * the resulting image.  Pixel values with brightness greater than
     * this threshold value will be included.
     * <pre>
     *       Min: 0.0
     *       Max: 1.0
     *   Default: 0.3
     *  Identity: n/a
     * </pre>
     * 
     * @param threshold the threshold value
     * @throws IllegalArgumentException if {@code threshold} is outside
     * the allowable range
     */
    public void setThreshold(float threshold) {
        if (threshold < 0f || threshold > 1f) {
            throw new IllegalArgumentException("Threshold must be in the range [0,1]");
        }
        float old = this.threshold;
        this.threshold = threshold;
        firePropertyChange("threshold", old, threshold);
    }
  
    @Override
    public Rectangle2D getBounds() {
        return getInputs().get(0).getBounds();
    }
    
    @Override
    public Image filter(GraphicsConfiguration config) {
        return filterInputs(config, 0).getImage();
    }
}
