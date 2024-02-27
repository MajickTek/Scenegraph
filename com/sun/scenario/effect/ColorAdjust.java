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
 * An effect that allows for per-pixel adjustments of hue, saturation,
 * brightness, and contrast.
 * 
 * @author Chris Campbell
 */
public class ColorAdjust extends CoreEffect {

    private float hue;
    private float saturation;
    private float brightness;
    private float contrast;

    /**
     * Constructs a new {@code ColorAdjust} effect with the default hue (0.0),
     * saturation (0.0), brightness (0.0), and contrast (1.0),
     * using the {@link Source source content} as the input.
     * This is a shorthand equivalent to:
     * <pre>
     *     new ColorAdjust(new Source(true))
     * </pre>
     */
    public ColorAdjust() {
        this(new Source(true));
    }

    /**
     * Constructs a new {@code ColorAdjust} effect with the default hue (0.0),
     * saturation (0.0), brightness (0.0), and contrast (1.0).
     * 
     * @param input the single input {@code Effect}
     * @throws IllegalArgumentException if {@code input} is null
     */
    public ColorAdjust(Effect input) {
        super(input);
        this.hue = 0f;
        this.saturation = 0f;
        this.brightness = 0f;
        this.contrast = 1f;
        updatePeerKey("ColorAdjust");
    }
    
    /**
     * Returns the hue adjustment.
     * 
     * @return the hue adjustment
     */
    public float getHue() {
        return hue;
    }

    /**
     * Sets the hue adjustment.
     * <pre>
     *       Min: -1.0
     *       Max: +1.0
     *   Default:  0.0
     *  Identity:  0.0
     * </pre>
     * 
     * @param hue the hue adjustment
     * @throws IllegalArgumentException if {@code hue} is outside the
     * allowable range
     */
    public void setHue(float hue) {
        if (hue < -1f || hue > 1f) {
            throw new IllegalArgumentException("Hue must be in the range [-1, 1]");
        }
        float old = this.hue;
        this.hue = hue;
        firePropertyChange("hue", old, hue);
    }

    /**
     * Returns the saturation adjustment.
     * 
     * @return the saturation adjustment
     */
    public float getSaturation() {
        return saturation;
    }

    /**
     * Sets the saturation adjustment.
     * <pre>
     *       Min: -1.0
     *       Max: +1.0
     *   Default:  0.0
     *  Identity:  0.0
     * </pre>
     * 
     * @param saturation the saturation adjustment
     * @throws IllegalArgumentException if {@code saturation} is outside the
     * allowable range
     */
    public void setSaturation(float saturation) {
        if (saturation < -1f || saturation > 1f) {
            throw new IllegalArgumentException("Saturation must be in the range [-1, 1]");
        }
        float old = this.saturation;
        this.saturation = saturation;
        firePropertyChange("saturation", old, saturation);
    }
    
    /**
     * Returns the brightness adjustment.
     * 
     * @return the brightness adjustment
     */
    public float getBrightness() {
        return brightness;
    }

    /**
     * Sets the brightness adjustment.
     * <pre>
     *       Min: -1.0
     *       Max: +1.0
     *   Default:  0.0
     *  Identity:  0.0
     * </pre>
     * 
     * @param brightness the brightness adjustment
     * @throws IllegalArgumentException if {@code brightness} is outside the
     * allowable range
     */
    public void setBrightness(float brightness) {
        if (brightness < -1f || brightness > 1f) {
            throw new IllegalArgumentException("Brightness must be in the range [-1, 1]");
        }
        float old = this.brightness;
        this.brightness = brightness;
        firePropertyChange("brightness", old, brightness);
    }

    /**
     * Returns the contrast adjustment.
     * 
     * @return the contrast adjustment
     */
    public float getContrast() {
        return contrast;
    }

    /**
     * Sets the contrast adjustment.
     * <pre>
     *       Min: 0.25
     *       Max: 4.00
     *   Default: 1.00
     *  Identity: 1.00
     * </pre>
     * 
     * @param contrast the contrast adjustment
     * @throws IllegalArgumentException if {@code contrast} is outside the
     * allowable range
     */
    public void setContrast(float contrast) {
        if (contrast < 0.25f || contrast > 4f) {
            throw new IllegalArgumentException("Contrast must be in the range [0.25, 4]");
        }
        float old = this.contrast;
        this.contrast = contrast;
        firePropertyChange("contrast", old, contrast);
    }

    @Override
    public Rectangle2D getBounds() {
        return getInputs().get(0).getBounds();
    }
    
    @Override
    public Image filter(GraphicsConfiguration config) {
        return filterInputs(config, 0).getImage();
    }
}
