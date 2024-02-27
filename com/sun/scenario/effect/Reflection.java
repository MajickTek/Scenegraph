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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * An effect that renders a reflected version of the input below the
 * actual input content.
 * 
 * @author Chris Campbell
 */
public class Reflection extends Effect {
    
    private float topOffset;
    private float topOpacity;
    private float bottomOpacity;
    private float fraction;
    
    /**
     * Constructs a new {@code Reflection} effect with default values,
     * using the {@link Source source content} as the input.
     * This is a shorthand equivalent to:
     * <pre>
     *     new Reflection(new Source(true))
     * </pre>
     */
    public Reflection() {
        this(new Source(true));
    }
    
    /**
     * Constructs a new {@code Reflection} effect with default values.
     * 
     * @param input the single input {@code Effect}
     * @throws IllegalArgumentException if {@code input} is null
     */
    public Reflection(Effect input) {
        super(input);
        this.topOffset = 0f;
        this.topOpacity = 0.5f;
        this.bottomOpacity = 0f;
        this.fraction = 0.75f;
    }

    /**
     * Returns the top offset adjustment, which is the distance between the
     * bottom of the input and the top of the reflection.
     * 
     * @return the top offset adjustment
     */
    public float getTopOffset() {
        return topOffset;
    }

    /**
     * Sets the top offset adjustment, which is the distance between the
     * bottom of the input and the top of the reflection.
     * <pre>
     *       Min: 0.0
     *       Max: n/a
     *   Default: 0.0
     *  Identity: 0.0
     * </pre>
     * 
     * @param topOffset the top offset adjustment
     * @throws IllegalArgumentException if {@code topOffset} is outside the
     * allowable range
     */
    public void setTopOffset(float topOffset) {
        if (topOffset < 0f) {
            throw new IllegalArgumentException("Top offset must be positive");
        }
        float old = this.topOffset;
        this.topOffset = topOffset;
        firePropertyChange("topOffset", old, topOffset);
    }

    /**
     * Returns the top opacity value, which is the opacity of the reflection
     * at its top extreme.
     *
     * @return the top opacity value
     */
    public float getTopOpacity() {
        return topOpacity;
    }

    /**
     * Sets the top opacity value, which is the opacity of the reflection
     * at its top extreme.
     * <pre>
     *       Min: 0.0
     *       Max: 1.0
     *   Default: 0.5
     *  Identity: 1.0
     * </pre>
     * 
     * @param topOpacity the top opacity value
     * @throws IllegalArgumentException if {@code topOpacity} is outside the
     * allowable range
     */
    public void setTopOpacity(float topOpacity) {
        if (topOpacity < 0f || topOpacity > 1f) {
            throw new IllegalArgumentException("Top opacity must be in the range [0,1]");
        }
        float old = this.topOpacity;
        this.topOpacity = topOpacity;
        firePropertyChange("topOpacity", old, topOpacity);
    }

    /**
     * Returns the bottom opacity value, which is the opacity of the reflection
     * at its bottom extreme.
     *
     * @return the bottom opacity value
     */
    public float getBottomOpacity() {
        return bottomOpacity;
    }

    /**
     * Sets the bottom opacity value, which is the opacity of the reflection
     * at its bottom extreme.
     * <pre>
     *       Min: 0.0
     *       Max: 1.0
     *   Default: 0.0
     *  Identity: 1.0
     * </pre>
     * 
     * @param bottomOpacity the bottom opacity value
     * @throws IllegalArgumentException if {@code bottomOpacity} is outside the
     * allowable range
     */
    public void setBottomOpacity(float bottomOpacity) {
        if (bottomOpacity < 0f || bottomOpacity > 1f) {
            throw new IllegalArgumentException("Bottom opacity must be in the range [0,1]");
        }
        float old = this.bottomOpacity;
        this.bottomOpacity = bottomOpacity;
        firePropertyChange("bottomOpacity", old, bottomOpacity);
    }

    /**
     * Returns the fraction of the input that is visible in the reflection.
     * 
     * @return the fraction value
     */
    public float getFraction() {
        return fraction;
    }

    /**
     * Sets the fraction of the input that is visible in the reflection.
     * For example, a value of 0.5 means that only the bottom half of the
     * input will be visible in the reflection.
     * <pre>
     *       Min: 0.0
     *       Max: 1.0
     *   Default: 0.75
     *  Identity: 1.0
     * </pre>
     * 
     * @param fraction the fraction of the input that is visible
     * in the reflection
     * @throws IllegalArgumentException if {@code fraction} is outside the
     * allowable range
     */
    public void setFraction(float fraction) {
        if (fraction < 0f || fraction > 1f) {
            throw new IllegalArgumentException("Fraction must be in the range [0,1]");
        }
        float old = this.fraction;
        this.fraction = fraction;
        firePropertyChange("fraction", old, fraction);
    }

    @Override
    public Rectangle2D getBounds() {
        Rectangle2D r = getInputs().get(0).getBounds();
        double h = r.getHeight() + topOffset + (fraction * r.getHeight());
        r.setFrame(r.getX(), r.getY(), r.getWidth(), h);
        return r;
    }

    @Override
    public Image filter(GraphicsConfiguration config) {
        Rectangle2D fullBounds = getBounds();
        Rectangle2D inputBounds = getInputs().get(0).getBounds();
        Rectangle tmp = fullBounds.getBounds();
        int w = tmp.width;
        int h = tmp.height;
        int imgh = (int)inputBounds.getHeight();
        int rh = (int)(imgh * fraction + 0.5f);
        int ry1 = (int)(imgh + topOffset + 0.5f);
        int ry2 = ry1 + rh;
        Image dst = getCompatibleImage(config, w, h);
        Image src = getInputs().get(0).filter(config);
        
        Graphics2D gdst = (Graphics2D)dst.getGraphics();
        gdst.drawImage(src, 0, 0, null);
        // TODO: we could implement this more efficiently in hardware
        // (use different colors at the top/bottom vertices to get the
        // same gradient effect)
        Color c1 = new Color(1f, 1f, 1f, topOpacity);
        Color c2 = new Color(1f, 1f, 1f, bottomOpacity);
        gdst.setComposite(AlphaComposite.Src);
        gdst.setPaint(new GradientPaint(0, ry1, c1, 0, ry2, c2, true));
        gdst.fillRect(0, ry1, w, rh);
        gdst.setComposite(AlphaComposite.SrcIn);
        gdst.drawImage(src, 0, ry2, w, ry1, 0, imgh-rh, w, imgh, null);
        releaseCompatibleImage(config, src);
        gdst.dispose();
        
        return dst;
    }
    
    @Override
    public AccelType getAccelType(GraphicsConfiguration config) {
        return getInputs().get(0).getAccelType(config);
    }
}
