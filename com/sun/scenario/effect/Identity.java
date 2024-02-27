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
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A type of source effect that simply returns the specified {@code Image}.
 * No other processing is performed on the specified {@code Image}.
 * 
 * @author Chris Campbell
 */
public class Identity extends Effect {

    private Image src;
    private Point2D.Float loc = new Point2D.Float();
    
    /**
     * Constructs a new {@code Identity} effect with the given {@code Image}.
     *
     * @param src the source image, or null
     */
    public Identity(Image src) {
        this.src = src;
    }
    
    /**
     * Returns the source image (can be null).
     * 
     * @return the source image
     */
    public final Image getSource() {
        return src;
    }

    /**
     * Sets the source image.
     * 
     * @param src the source image, or null
     */
    public void setSource(Image src) {
        this.src = src;
    }
    
    /**
     * Returns the location of the source image, relative to the untransformed
     * source content bounds.
     * 
     * @return the location of the source image
     */
    public final Point2D getLocation() {
        return loc;
    }
    
    /**
     * Sets the location of the source image, relative to the untransformed
     * source content bounds.
     * 
     * @param pt the new location of the source image
     * @throws IllegalArgumentException if {@code pt} is null
     */
    public void setLocation(Point2D pt) {
        if (pt == null) {
            throw new IllegalArgumentException("Location must be non-null");
        }
        this.loc.setLocation(pt);
    }

    @Override
    public boolean isInDeviceSpace() {
        return false;
    }

    @Override
    public int needsSourceContent() {
        return NONE;
    }

    @Override
    public Rectangle2D getBounds() {
        if (src == null) {
            // just an empty rectangle
            return new Rectangle();
        } else {
            float srcx = loc.x;
            float srcy = loc.y;
            Rectangle2D r = getSourceContent().getUntransformedBounds();
            if (r != null) {
                srcx += r.getX();
                srcy += r.getY();
            }
            return new Rectangle2D.Float(srcx, srcy, src.getWidth(null), src.getHeight(null));
        }
    }

    @Override
    public Image filter(GraphicsConfiguration config) {
        // TODO: the peer code currently assumes that the image returned
        // here is in the expected format (e.g. a VolatileImage in the case
        // of hardware peers); perhaps we should convert the source image
        // here if it's not already compatible with the given config...
        return src;
    }
    
    @Override
    public AccelType getAccelType(GraphicsConfiguration config) {
        // TODO: perhaps we should look at the image type here...
        return AccelType.NONE;
    }
}
