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

import java.awt.Color;

/**
 * A blurred shadow effect using a Gaussian convolution kernel, with a
 * configurable radius and shadow color.  Only the alpha channel of the
 * input is used to create the shadow effect.  The alpha value of each
 * pixel from the result of the blur operation is modulated with the
 * specified shadow color to produce the resulting image.
 *
 * @author Chris Campbell
 */
public class Shadow extends AbstractGaussian {

    private Color color;

    /**
     * Constructs a new {@code GaussianBlur} effect with the default radius
     * (10.0) and the default color ({@code Color.BLACK}), using the
     * {@link Source source content} as the input.
     * This is a shorthand equivalent to:
     * <pre>
     *     new Shadow(10f, Color.BLACK, new Source(true))
     * </pre>
     */
    public Shadow() {
        this(10f);
    }

    /**
     * Constructs a new {@code GaussianBlur} effect with the given radius
     * and the default color ({@code Color.BLACK}), using the
     * {@link Source source content} as the input.
     * This is a shorthand equivalent to:
     * <pre>
     *     new Shadow(radius, Color.BLACK, new Source(true))
     * </pre>
     * 
     * @param radius the radius of the Gaussian kernel
     * @throws IllegalArgumentException if {@code radius} is outside the
     * allowable range
     */
    public Shadow(float radius) {
        this(radius, Color.BLACK);
    }
    
    /**
     * Constructs a new {@code Shadow} effect with the given radius and color,
     * using the {@link Source source content} as the input.
     * This is a shorthand equivalent to:
     * <pre>
     *     new Shadow(radius, color, new Source(true))
     * </pre>
     * 
     * @param radius the radius of the Gaussian kernel
     * @param color the shadow {@code Color}
     * @throws IllegalArgumentException if {@code radius} is outside the
     * allowable range, or if {@code color} is null
     */
    public Shadow(float radius, Color color) {
        this(radius, color, new Source(true));
    }

    /**
     * Constructs a new {@code Shadow} effect with the given radius and color.
     * 
     * @param radius the radius of the Gaussian kernel
     * @param color the shadow {@code Color}
     * @param input the single input {@code Effect}
     * @throws IllegalArgumentException if {@code radius} is outside the
     * allowable range, or if {@code color} is null, or if {@code input} is null
     */
    public Shadow(float radius, Color color, Effect input) {
        super("Shadow", radius, input);
        setColor(color);
    }

    /**
     * Returns the shadow {@code Color}.
     * 
     * @return the shadow {@code Color}
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Sets the shadow {@code Color}.
     * <pre>
     *       Min: n/a
     *       Max: n/a
     *   Default: Color.BLACK
     *  Identity: n/a
     * </pre>
     * 
     * @param color the shadow {@code Color}
     * @throws IllegalArgumentException if {@code color} is null
     */
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color must be non-null");
        }
        Color old = this.color;
        this.color = color;
        firePropertyChange("color", old, color);
    }
}
