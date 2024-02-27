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

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * An effect that merges two inputs together into one result.  This produces
 * the same result as using the {@code Blend} effect with
 * {@code Blend.Mode.SRC_OVER} and {@code opacity=1.0}, except possibly
 * more efficient.
 * 
 * @author Chris Campbell
 */
public class Merge extends Effect {
    
    /**
     * Constructs a new {@code Merge} effect for the given inputs.
     * 
     * @param bottomInput the bottom input
     * @param topInput the top input
     * @throws IllegalArgumentException if either {@code input1} or
     * {@code input2} is null
     */
    public Merge(Effect bottomInput, Effect topInput) {
        super(bottomInput, topInput);
    }
    
    @Override
    public Rectangle2D getBounds() {
        // return the union of the input bounds
        Rectangle2D r = null;
        for (Effect input : getInputs()) {
            Rectangle2D effectBounds = input.getTransformedBounds();
            if (r == null) {
                r = new Rectangle2D.Float();
                r.setRect(effectBounds);
            } else {
                r.add(effectBounds);
            }
        }
        return r;
    }

    @Override
    public boolean isInDeviceSpace() {
        // TODO: for now, always up-convert; later, we could choose to return
        // false here if all inputs are untransformed
        return true;
    }

    @Override
    public Image filter(GraphicsConfiguration config) {
        SourceContent content = getSourceContent();
        Rectangle2D fullBounds = getBounds();
        Rectangle tmp = fullBounds.getBounds();
        Image dst = getCompatibleImage(config, tmp.width, tmp.height);

        Graphics2D gdst = (Graphics2D)dst.getGraphics();
        for (Effect input : getInputs()) {
            Rectangle2D origBounds = input.getBounds();
            
            AffineTransform oldXform = gdst.getTransform();
            if (!input.isInDeviceSpace()) {
                // transform input effect bounds into device space
                AffineTransform xform = content.getTransform();

                // now setup graphics transform so that the (original,
                // untransformed) effect image is transformed into device space
                AffineTransform xform2 = new AffineTransform();
                xform2.translate(-fullBounds.getX(), -fullBounds.getY());
                xform2.concatenate(xform);
                xform2.translate(origBounds.getX(), origBounds.getY());
                gdst.setTransform(xform2);                
                gdst.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            } else {
                gdst.translate(origBounds.getX() - fullBounds.getX(),
                               origBounds.getY() - fullBounds.getY());
            }
            Image inputImg = input.filter(config);
            gdst.drawImage(inputImg, 0, 0, null);
            gdst.setTransform(oldXform);
            releaseCompatibleImage(config, inputImg);
        }
        gdst.dispose();
        
        return dst;
    }
    
    @Override
    public AccelType getAccelType(GraphicsConfiguration config) {
        // TODO: perhaps we should delegate to one of the inputs...
        return AccelType.NONE;
    }
}
