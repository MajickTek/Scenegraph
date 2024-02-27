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

import com.sun.scenario.effect.light.Light;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * An effect that applies diffuse and specular lighting to an arbitrary
 * input using a positionable light source.
 * 
 * @author Chris Campbell
 */
public class PhongLighting extends CoreEffect {

    private float surfaceScale;
    private float diffuseConstant;
    private float specularConstant;
    private float specularExponent;
    private final Light light;
    
    /**
     * Constructs a new {@code PhongLighting} effect for the given
     * {@code Light}, with default values for all other properties,
     * using the {@link Source source content} as the input.
     * This is a convenience constructor that automatically generates a
     * bump map using the source content as input.
     * 
     * @param light the light source
     * @throws IllegalArgumentException if {@code light} is null
     */
    public PhongLighting(Light light) {
        this(light, new Shadow(10f), new Source(true));
    }

    /**
     * Constructs a new {@code PhongLighting} effect for the given
     * {@code Light}, with default values for all other properties.
     * 
     * @param light the light source
     * @param bumpInput the input containing the bump map
     * @param origInput the input containing the source data
     * @throws IllegalArgumentException if {@code light} is null, or
     * if either {@code bumpInput} or {@code origInput} is null
     */
    public PhongLighting(Light light, Effect bumpInput, Effect origInput) {
        super(bumpInput, origInput);
        
        this.surfaceScale = 1f;
        this.diffuseConstant = 1f;
        this.specularConstant = 1f;
        this.specularExponent = 1f;
        
        if (light == null) {
            throw new IllegalArgumentException("Light must be non-null");
        }
        this.light = light;
        this.light.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange("light", null, PhongLighting.this.light);
            }
        });
        
        updatePeerKey("PhongLighting_" + light.getType().name());
    }

    /**
     * Returns the light source.
     * 
     * @return the light source
     */
    public Light getLight() {
        return light;
    }
    
    /**
     * Returns the diffuse constant.
     * 
     * @return the diffuse constant value
     */
    public float getDiffuseConstant() {
        return diffuseConstant;
    }

    /**
     * Sets the diffuse constant.
     * <pre>
     *       Min: 0.0
     *       Max: 2.0
     *   Default: 1.0
     *  Identity: n/a
     * </pre>
     * 
     * @param diffuseConstant the diffuse constant value
     * @throws IllegalArgumentException if {@code diffuseConstant} is outside
     * the allowable range
     */
    public void setDiffuseConstant(float diffuseConstant) {
        if (diffuseConstant < 0f || diffuseConstant > 2f) {
            throw new IllegalArgumentException("Diffuse constant must be in the range [0,2]");
        }
        float old = this.diffuseConstant;
        this.diffuseConstant = diffuseConstant;
        firePropertyChange("diffuseConstant", old, diffuseConstant);
    }

    /**
     * Returns the specular constant.
     * 
     * @return the specular constant value
     */
    public float getSpecularConstant() {
        return specularConstant;
    }

    /**
     * Sets the specular constant.
     * <pre>
     *       Min: 0.0
     *       Max: 2.0
     *   Default: 1.0
     *  Identity: n/a
     * </pre>
     * 
     * @param specularConstant the specular constant value
     * @throws IllegalArgumentException if {@code specularConstant} is outside
     * the allowable range
     */
    public void setSpecularConstant(float specularConstant) {
        if (specularConstant < 0f || specularConstant > 2f) {
            throw new IllegalArgumentException("Specular constant must be in the range [0,2]");
        }
        float old = this.specularConstant;
        this.specularConstant = specularConstant;
        firePropertyChange("specularConstant", old, specularConstant);
    }

    /**
     * Returns the specular exponent.
     * 
     * @return the specular exponent value
     */
    public float getSpecularExponent() {
        return specularExponent;
    }

    /**
     * Sets the specular exponent.
     * <pre>
     *       Min:  0.0
     *       Max: 40.0
     *   Default:  1.0
     *  Identity:  n/a
     * </pre>
     * 
     * @param specularExponent the specular exponent value
     * @throws IllegalArgumentException if {@code specularExponent} is outside
     * the allowable range
     */
    public void setSpecularExponent(float specularExponent) {
        if (specularExponent < 0f || specularExponent > 40f) {
            throw new IllegalArgumentException("Specular exponent must be in the range [0,40]");
        }
        float old = this.specularExponent;
        this.specularExponent = specularExponent;
        firePropertyChange("specularExponent", old, specularExponent);
    }

    /**
     * Returns the surface scale.
     * 
     * @return the surface scale value
     */
    public float getSurfaceScale() {
        return surfaceScale;
    }

    /**
     * Sets the surface scale.
     * <pre>
     *       Min:  0.0
     *       Max: 10.0
     *   Default:  1.0
     *  Identity:  n/a
     * </pre>
     * 
     * @param surfaceScale the surface scale value
     * @throws IllegalArgumentException if {@code surfaceScale} is outside
     * the allowable range
     */
    public void setSurfaceScale(float surfaceScale) {
        if (surfaceScale < 0f || surfaceScale > 10f) {
            throw new IllegalArgumentException("Surface scale must be in the range [0,10]");
        }
        float old = this.surfaceScale;
        this.surfaceScale = surfaceScale;
        firePropertyChange("surfaceScale", old, surfaceScale);
    }
   
    @Override
    public Rectangle2D getBounds() {
        // resulting bounds are the size of the original input bounds
        // (from input 1); the bump input (from input 0) needs to be
        // positioned accordingly
        return getInputs().get(1).getBounds();
    }
    
    @Override
    public Image filter(GraphicsConfiguration config) {
        return filterInputs(config, true, 0, 1).getImage();
    }
}
