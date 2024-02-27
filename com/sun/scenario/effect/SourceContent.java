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

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * A container for source image content that is generated at runtime and
 * passed to an {@code Effect} instance.  The container can hold the
 * transformed (device space) image and/or the untransformed (original)
 * image, depending on the needs of the target {@code Effect}.
 * 
 * @author Chris Campbell
 */
public class SourceContent {
    
    private final AffineTransform xform;
    private final Image unxformImage;
    private final Rectangle2D unxformBounds;
    private final Image xformImage;
    private final Rectangle2D xformBounds;

    /**
     * Constructs a new {@code SourceContent} instance, using the given
     * image for both the untransformed and transformed images/bounds.
     * The transform is assumed to be an identity matrix.
     * 
     * @param image the source image
     */
    public SourceContent(Image image) {
        this(image,
             new Rectangle(0, 0, image.getWidth(null), image.getHeight(null)));
    }
    
    /**
     * Constructs a new {@code SourceContent} instance, using the given
     * image/bounds for both the untransformed and transformed images/bounds.
     * The transform is assumed to be an identity matrix.
     * 
     * @param image the source image
     * @param bounds the source bounding box
     */
    public SourceContent(Image image, Rectangle2D bounds) {
        this(new AffineTransform(), image, bounds, image, bounds);
    }
    
    /**
     * Constructs a new {@code SourceContent} instance, using the given
     * untransformed and transformed image/bounds, as well as the transform
     * that converts from user space to device space.
     * 
     * @param xform the user space to device space transform
     * @param unxformImage the untransformed source image
     * @param unxformBounds the untransformed source bounding box
     * @param xformImage the transformed source image
     * @param xformBounds the transformed source bounding box
     */
    public SourceContent(AffineTransform xform,
                         Image unxformImage, Rectangle2D unxformBounds,
                         Image xformImage, Rectangle2D xformBounds)
    {
        this.xform         = xform;
        this.unxformImage  = unxformImage;
        this.unxformBounds = unxformBounds;
        this.xformImage    = xformImage;
        this.xformBounds   = xformBounds;
    }

    /**
     * Returns the transform that converts from user space to device space.
     * 
     * @return the device space to user space transform
     */
    public AffineTransform getTransform() {
        return xform;
    }
    
    /**
     * Returns the untransformed (original) image.
     * 
     * @return the untransformed image
     */
    public Image getUntransformedImage() {
        return unxformImage;
    }

    /**
     * Returns the untransformed (original) bounding box.
     * 
     * @return the untransformed bounding box
     */
    public Rectangle2D getUntransformedBounds() {
        return unxformBounds;
    }

    /**
     * Returns the transformed (device space) image.
     * 
     * @return the transformed image
     */
    public Image getTransformedImage() {
        return xformImage;
    }

    /**
     * Returns the transformed (device space) bounding box
     * 
     * @return the transformed bounding box
     */
    public Rectangle2D getTransformedBounds() {
        return xformBounds;
    }
}
