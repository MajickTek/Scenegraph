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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * The abstract base class for all light implementations.
 * 
 * @author Chris Campbell
 */
public abstract class Light {

    /**
     * The light type.
     */
    public enum Type {
        /** Light is a {@code DistantLight}. */
        DISTANT,
        /** Light is a {@code PointLight}. */
        POINT,
        /** Light is a {@code SpotLight}. */
        SPOT
    }
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Type type;
    private Color color;
    
    /**
     * Package-private constructor.
     * 
     * @param type the type of the light
     * @throws IllegalArgumentException if {@code color} is null
     */
    Light(Type type) {
        this(type, Color.WHITE);
    }

    /**
     * Package-private constructor.
     * 
     * @param type the type of the light
     * @param color the color of the light
     * @throws IllegalArgumentException if {@code color} is null
     */
    Light(Type type, Color color) {
        if (type == null) {
            throw new InternalError("Light type must be non-null");
        }
        this.type = type;
        setColor(color);
    }
    
    /**
     * Returns the light {@code Type}, either {@code DISTANT}, {@code POINT},
     * or {@code SPOT}.
     * 
     * @return the light type
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Returns the light {@code Color}.
     * 
     * @return the light {@code Color}
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Sets the light {@code Color}.
     * <pre>
     *       Min: n/a
     *       Max: n/a
     *   Default: Color.WHITE
     *  Identity: n/a
     * </pre>
     * 
     * @param color the light {@code Color}
     * @throws IllegalArgumentException if {@code color} is null
     */
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color must be non-null");
        }
        Color old = this.color;
        this.color = color;
        firePropertyChange("color", old, color);
    }
    
    /**
     * Returns a float array containing the normalized {@code (x,y,z)}
     * position of this light source.
     * 
     * @return the normalized position of this light source
     */
    public abstract float[] getNormalizedLightPosition();
    
    /**
     * Adds the given {@code PropertyChangeListener} to the list
     * of listeners.
     * 
     * @param listener the {@code PropertyChangeListener} to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes the given {@code PropertyChangeListener} to the list
     * of listeners.
     * 
     * @param listener the {@code PropertyChangeListener} to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    /**
     * Reports a bound property update to any registered listeners.
     * No event is fired if {@code oldValue} and {@code newValue}
     * are equal and non-null.
     * 
     * @param prop the programmatic name of the property that was changed
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     */
    void firePropertyChange(String prop, Object oldValue, Object newValue) {
        pcs.firePropertyChange(prop, oldValue, newValue);
    }
}
