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
 * A high-level effect that makes the input image appear to glow,
 * based on a configurable threshold.
 * 
 * @author Chris Campbell
 */
public class Glow extends Effect {

    private final GaussianBlur blur;
    private final Blend blend;
    
    /**
     * Constructs a new {@code Glow} effect with the default level (0.3),
     * using the {@link Source source content} as the input.
     * This is a shorthand equivalent to:
     * <pre>
     *     new Glow(new Source(true))
     * </pre>
     */
    public Glow() {
        this(new Source(true));
    }

    /**
     * Constructs a new {@code Glow} effect with the default level (0.3).
     *
     * @param input the single input {@code Effect}
     * @throws IllegalArgumentException if {@code input} is null
     */
    public Glow(Effect input) {
        super(input);
        
        //
        //    (input)
        //       |
        //  GaussianBlur  (input)
        //             |   | 
        //             Blend
        //               |
        //
        this.blur = new GaussianBlur(10f, input);
        this.blend = new Blend(Blend.Mode.ADD, input, blur);
        this.blend.setOpacity(0.3f);
    }

    /**
     * Returns the level value, which controls the intensity of the glow effect.
     * 
     * @return the level value
     */
    public float getLevel() {
        return blend.getOpacity();
    }
    
    /**
     * Sets the level value, which controls the intensity of the glow effect.
     * <pre>
     *       Min: 0.0
     *       Max: 1.0
     *   Default: 0.3
     *  Identity: 0.0
     * </pre>
     * 
     * @param level the level value
     * @throws IllegalArgumentException if {@code level} is outside
     * the allowable range
     */
    public void setLevel(float level) {
        float old = blend.getOpacity();
        blend.setOpacity(level);
        firePropertyChange("level", old, level);
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
