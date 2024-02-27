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

package com.sun.scenario.effect.light;

import java.awt.Color;

/**
 * Represents a light source at a given position in 3D space.
 * 
 * @author Chris Campbell
 */
public class PointLight extends Light {

    private float x;
    private float y;
    private float z;

    /**
     * Constructs a new {@code PointLight} with default position (0,0,0)
     * and color ({@code Color.WHITE}).
     */
    public PointLight() {
        this(0f, 0f, 0f, Color.WHITE);
    }
    
    /**
     * Constructs a new {@code PointLight} with the given position and color.
     *
     * @param x the x coordinate of the light position
     * @param y the y coordinate of the light position
     * @param z the z coordinate of the light position
     * @param color the color of the light
     * @throws IllegalArgumentException if {@code color} is null
     */
    public PointLight(float x, float y, float z, Color color) {
        this(Type.POINT, x, y, z, color);
    }
    
    /**
     * Package-private constructor.
     * 
     * @param type the type of the light (either {@code POINT} or {@code SPOT})
     * @param x the x coordinate of the light position
     * @param y the y coordinate of the light position
     * @param z the z coordinate of the light position
     * @param color the color of the light
     * @throws IllegalArgumentException if {@code color} is null
     */
    PointLight(Type type, float x, float y, float z, Color color) {
        super(type, color);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns the x coordinate of the light position.
     * 
     * @return the x coordinate of the light position
     */
    public float getX() {
        return x;
    }

    /**
     * Sets the x coordinate of the light position.
     * <pre>
     *       Min: n/a
     *       Max: n/a
     *   Default: 0.0
     *  Identity: n/a
     * </pre>
     * 
     * @param x the x coordinate of the light position
     */
    public void setX(float x) {
        float old = this.x;
        this.x = x;
        firePropertyChange("x", old, x);
    }

    /**
     * Returns the y coordinate of the light position.
     * 
     * @return the y coordinate of the light position
     */
    public float getY() {
        return y;
    }

    /**
     * Sets the y coordinate of the light position.
     * <pre>
     *       Min: n/a
     *       Max: n/a
     *   Default: 0.0
     *  Identity: n/a
     * </pre>
     * 
     * @param y the y coordinate of the light position
     */
    public void setY(float y) {
        float old = this.y;
        this.y = y;
        firePropertyChange("y", old, y);
    }

    /**
     * Returns the z coordinate of the light position.
     * 
     * @return the z coordinate of the light position
     */
    public float getZ() {
        return z;
    }

    /**
     * Sets the z coordinate of the light position.
     * <pre>
     *       Min: n/a
     *       Max: n/a
     *   Default: 0.0
     *  Identity: n/a
     * </pre>
     * 
     * @param z the z coordinate of the light position
     */
    public void setZ(float z) {
        float old = this.z;
        this.z = z;
        firePropertyChange("z", old, z);
    }
    
    @Override
    public float[] getNormalizedLightPosition() {
        // normalize
        float len = (float)Math.sqrt(x*x + y*y + z*z);
        if (len == 0f) len = 1f;
        float[] pos = new float[] {x/len, y/len, z/len};
        return pos;
    }
}
