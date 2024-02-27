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
 * A high-level effect that makes brighter portions of the input image
 * appear to glow, based on a configurable threshold.
 * 
 * @author Chris Campbell
 */
public class Bloom extends Effect {

    private final Brightpass brightpass;
    private final GaussianBlur blur;
    private final Blend blend;
    
    /**
     * Constructs a new {@code Bloom} effect with the default threshold (0.3),
     * using the {@link Source source content} as the input.
     * This is a shorthand equivalent to:
     * <pre>
     *     new Bloom(new Source(true))
     * </pre>
     */
    public Bloom() {
        this(new Source(true));
    }

    /**
     * Constructs a new {@code Bloom} effect with the default threshold (0.3).
     *
     * @param input the single input {@code Effect}
     * @throws IllegalArgumentException if {@code input} is null
     */
    public Bloom(Effect input) {
        super(input);
        
        //
        //    (input)
        //       |
        //   Brightpass
        //       |
        //  GaussianBlur  (input)
        //             |   | 
        //             Blend
        //               |
        //
        this.brightpass = new Brightpass(input);
        this.blur = new GaussianBlur(10f, brightpass);
        this.blend = new Blend(Blend.Mode.ADD, input, blur);
    }

    /**
     * Returns the threshold, which controls the intensity of the glow effect.
     * 
     * @return the threshold value
     */
    public float getThreshold() {
        return brightpass.getThreshold();
    }
    
    /**
     * Sets the threshold, which controls the intensity of the glow effect.
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
        float old = brightpass.getThreshold();
        brightpass.setThreshold(threshold);
        firePropertyChange("threshold", old, threshold);
    }

    @Override
    public void setSourceContent(SourceContent content) {
        super.setSourceContent(content);
        blend.setSourceContent(content);
    }

    @Override
    public int needsSourceContent() {
        return TRANSFORMED;
    }

    @Override
    public Rectangle2D getBounds() {
        // TODO: resulting bounds should only be as large as the input
        // (fringes from blur operation should be discarded/ignored)
        return blend.getBounds();
    }
    
    @Override
    public Image filter(GraphicsConfiguration config) {
        return blend.filter(config);
    }
    
    @Override
    public AccelType getAccelType(GraphicsConfiguration config) {
        return blend.getAccelType(config);
    }
}
