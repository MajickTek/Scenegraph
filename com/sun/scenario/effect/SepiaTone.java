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
 * A filter that produces a sepia tone effect, similar to antique photographs.
 * 
 * @author Chris Campbell
 */
public class SepiaTone extends CoreEffect {

    private float level;
    
    /**
     * Constructs a new {@code SepiaTone} effect with the default
     * level value (1.0), using the {@link Source source content}
     * as the input.
     * This is a shorthand equivalent to:
     * <pre>
     *     new SepiaTone(new Source(true))
     * </pre>
     */
    public SepiaTone() {
        this(new Source(true));
    }

    /**
     * Constructs a new {@code SepiaTone} effect with the default
     * level value (1.0).
     * 
     * @param input the single input {@code Effect}
     * @throws IllegalArgumentException if {@code input} is null
     */
    public SepiaTone(Effect input) {
        super(input);
        setLevel(1f);
        updatePeerKey("SepiaTone");
    }

    /**
     * Returns the level value, which controls the intensity of the
     * sepia effect.
     * 
     * @return the level value
     */
    public float getLevel() {
        return level;
    }
    
    /**
     * Sets the level value, which controls the intensity of the sepia effect.
     * <pre>
     *       Min: 0.0
     *       Max: 1.0
     *   Default: 1.0
     *  Identity: 0.0
     * </pre>
     * 
     * @param level the level value
     * @throws IllegalArgumentException if {@code level} is outside
     * the allowable range
     */
    public void setLevel(float level) {
        if (level < 0f || level > 1f) {
            throw new IllegalArgumentException("Level must be in the range [0,1]");
        }
        float old = this.level;
        this.level = level;
        firePropertyChange("level", old, level);
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
