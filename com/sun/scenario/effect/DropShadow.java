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
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

/**
 * A high-level effect that renders a shadow of the given content behind
 * the content with the specified color, radius, and offset.
 * 
 * @author Chris Campbell
 */
public class DropShadow extends Effect {

    private final Shadow shadow;
    private final Offset offset;
    private final Merge merge;

    /**
     * Constructs a new {@code DropShadow} effect, with the default
     * blur radius (10.0), x offset (0.0), and y offset (0.0), using the
     * {@link Source source content} as the input.
     */
    public DropShadow() {
        //
        //    (input)
        //       |
        //    Shadow
        //       |
        //    Offset  (input)
        //         |   | 
        //         Merge
        //           |
        //
        Source usource = new Source(false);
        Source xsource = new Source(true);
        this.shadow = new Shadow(10f, Color.BLACK, usource);
        this.offset = new Offset(0, 0, shadow);
        this.merge = new Merge(offset, xsource);
    }
    
    /**
     * Returns the radius of the Gaussian kernel.
     * 
     * @return the radius of the Gaussian kernel
     */
    public float getRadius() {
        return shadow.getRadius();
    }
    
    /**
     * Sets the radius of the shadow blur kernel.
     * <pre>
     *       Min:  1.0
     *       Max: 63.0
     *   Default: 10.0
     *  Identity:  n/a
     * </pre>
     * 
     * @param radius the radius of the shadow blur kernel
     * @throws IllegalArgumentException if {@code radius} is outside the
     * allowable range
     */
    public void setRadius(float radius) {
        float old = shadow.getRadius();
        shadow.setRadius(radius);
        firePropertyChange("radius", old, radius);
    }
    
    /**
     * Returns the shadow {@code Color}.
     * 
     * @return the shadow {@code Color}
     */
    public Color getColor() {
        return shadow.getColor();
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
        Color old = shadow.getColor();
        shadow.setColor(color);
        firePropertyChange("color", old, color);
    }
    
    /**
     * Returns the offset in the x direction, in pixels.
     * 
     * @return the offset in the x direction, in pixels.
     */
    public int getOffsetX() {
        return offset.getX();
    }
    
    /**
     * Sets the offset in the x direction, in pixels.
     * <pre>
     *       Min: Integer.MIN_VALUE
     *       Max: Integer.MAX_VALUE
     *   Default: 0
     *  Identity: 0
     * </pre>
     * 
     * @param xoff the offset in the x direction, in pixels
     */
    public void setOffsetX(int xoff) {
        int old = offset.getX();
        offset.setX(xoff);
        firePropertyChange("offsetX", old, xoff);
    }
    
    /**
     * Returns the offset in the x direction, in pixels.
     * 
     * @return the offset in the x direction, in pixels.
     */
    public int getOffsetY() {
        return offset.getY();
    }
    
    /**
     * Sets the offset in the y direction, in pixels.
     * <pre>
     *       Min: Integer.MIN_VALUE
     *       Max: Integer.MAX_VALUE
     *   Default: 0
     *  Identity: 0
     * </pre>
     * 
     * @param yoff the offset in the y direction, in pixels
     */
    public void setOffsetY(int yoff) {
        int old = offset.getY();
        offset.setY(yoff);
        firePropertyChange("offsetY", old, yoff);
    }

    @Override
    public void setSourceContent(SourceContent content) {
        super.setSourceContent(content);
        merge.setSourceContent(content);
    }

    @Override
    public int needsSourceContent() {
        return BOTH;
    }

    @Override
    public boolean isInDeviceSpace() {
        return true;
    }
    
    @Override
    public Rectangle2D getBounds() {
        return merge.getBounds();
    }
    
    @Override
    public Image filter(GraphicsConfiguration config) {
        return merge.filter(config);
    }
    
    @Override
    public AccelType getAccelType(GraphicsConfiguration config) {
        return shadow.getAccelType(config);
    }
}
