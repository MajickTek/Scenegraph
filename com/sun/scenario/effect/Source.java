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
 * An effect that simply returns either the transformed (device space) image
 * or untransformed (original) image from a {@code SourceContent} instance.
 * This is mainly useful in a scene graph environment where the source
 * content is generated/modified at runtime, outside the user's control.
 * 
 * @author Chris Campbell
 */
public class Source extends Effect {
    
    private boolean transformed;
    
    /**
     * Constructs a new {@code Source} instance.
     * 
     * @param transformed if true, return the transformed (device space)
     * image from the current {@code SourceContent}; otherwise, return the
     * untransformed (original) image
     */
    public Source(boolean transformed) {
        this.transformed = transformed;
    }
    
    @Override
    public boolean isInDeviceSpace() {
        return transformed;
    }
    
    @Override
    public int needsSourceContent() {
        return transformed ? TRANSFORMED : UNTRANSFORMED;
    }

    @Override
    public Rectangle2D getBounds() {
        Rectangle2D dst = new Rectangle2D.Float();
        if (transformed) {
            dst.setRect(getSourceContent().getTransformedBounds());
        } else {
            dst.setRect(getSourceContent().getUntransformedBounds());
        }
        return dst;
    }

    @Override
    public Image filter(GraphicsConfiguration config) {
        // TODO: the peer code currently assumes that the image returned
        // here is in the expected format (e.g. a VolatileImage in the case
        // of hardware peers); is this a valid assumption?
        return transformed ?
            getSourceContent().getTransformedImage() :
            getSourceContent().getUntransformedImage();
    }
    
    @Override
    public AccelType getAccelType(GraphicsConfiguration config) {
        // TODO: perhaps we should look at the image type here...
        return AccelType.NONE;
    }
}
