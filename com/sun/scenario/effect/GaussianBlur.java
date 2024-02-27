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

/**
 * A blur effect using a Gaussian convolution kernel, with a configurable
 * radius.
 * 
 * @author Chris Campbell
 */
public class GaussianBlur extends AbstractGaussian {

    /**
     * Constructs a new {@code GaussianBlur} effect with the default radius
     * (10.0), using the {@link Source source content} as the input.
     * This is a shorthand equivalent to:
     * <pre>
     *     new GaussianBlur(10f, new Source(true))
     * </pre>
     */
    public GaussianBlur() {
        this(10f, new Source(true));
    }

    /**
     * Constructs a new {@code GaussianBlur} effect with the given radius,
     * using the {@link Source source content} as the input.
     * This is a shorthand equivalent to:
     * <pre>
     *     new GaussianBlur(radius, new Source(true))
     * </pre>
     * 
     * @param radius the radius of the Gaussian kernel
     * @throws IllegalArgumentException if {@code radius} is outside the
     * allowable range
     */
    public GaussianBlur(float radius) {
        this(radius, new Source(true));
    }
    
    /**
     * Constructs a new {@code GaussianBlur} effect with the given radius.
     * 
     * @param radius the radius of the Gaussian kernel
     * @param input the single input {@code Effect}
     * @throws IllegalArgumentException if {@code radius} is outside the
     * allowable range, or if {@code input} is null
     */
    public GaussianBlur(float radius, Effect input) {
        super("GaussianBlur", radius, input);
    }
}
