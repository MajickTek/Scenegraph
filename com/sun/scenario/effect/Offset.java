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
 * A trivial effect that offsets the input image by the given x/y
 * adjustment values.
 * 
 * @author Chris Campbell
 */
public class Offset extends Effect {
    
    private int xoff;
    private int yoff;
    
    /**
     * Constructs a new {@code Offset} effect with the given x/y
     * adjustment values.
     * 
     * @param xoff the offset in the x direction, in pixels
     * @param yoff the offset in the y direction, in pixels
     * @param input the single input {@code Effect}
     * @throws IllegalArgumentException if {@code input} is null
     */
    public Offset(int xoff, int yoff, Effect input) {
        super(input);
        this.xoff = xoff;
        this.yoff = yoff;
    }
    
    /**
     * Returns the offset in the x direction, in pixels.
     * 
     * @return the offset in the x direction, in pixels.
     */
    public int getX() {
        return xoff;
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
    public void setX(int xoff) {
        int old = this.xoff;
        this.xoff = xoff;
        firePropertyChange("x", old, xoff);
    }
    
    /**
     * Returns the offset in the y direction, in pixels.
     * 
     * @return the offset in the y direction, in pixels.
     */
    public int getY() {
        return yoff;
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
    public void setY(int yoff) {
        float old = this.yoff;
        this.yoff = yoff;
        firePropertyChange("y", old, yoff);
    }

    @Override
    public Rectangle2D getBounds() {
        Rectangle2D dst = getInputs().get(0).getBounds();
        dst.setRect(dst.getX()+xoff, dst.getY()+yoff, dst.getWidth(), dst.getHeight());
        return dst;
    }
    
    @Override
    public Image filter(GraphicsConfiguration config) {
        return getInputs().get(0).filter(config);
    }
    
    @Override
    public AccelType getAccelType(GraphicsConfiguration config) {
        return getInputs().get(0).getAccelType(config);
    }
}
