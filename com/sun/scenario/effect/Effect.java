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

import com.sun.scenario.effect.impl.EffectPeer;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The base class for all filter effects.
 * 
 * @author Chris Campbell
 */
public abstract class Effect {

    /**
     * Flag indicating that the effect implementation does not need
     * access to the source as a raster image.
     */
    public static final int NONE          = (0 << 0);
    /**
     * Flag indicating that the effect implementation needs access to
     * the source as a raster image (in the original, local coordinate
     * space of the source content).
     */
    public static final int UNTRANSFORMED = (1 << 0);
    /**
     * Flag indicating that the effect implementation needs access to
     * the source as a raster image (in the transformed coordinate space
     * of the source content).
     */
    public static final int TRANSFORMED   = (1 << 1);
    /**
     * Flag indicating that the effect implementation needs access to
     * the source as a raster image in both untransformed and transformed
     * formats.
     * This is equivalent to {@code (UNTRANSFORMED | TRANSFORMED)}.
     */
    public static final int BOTH          = UNTRANSFORMED | TRANSFORMED;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final List<Effect> inputs;
    private SourceContent content;
    
    /**
     * Constructs an {@code Effect} with no inputs.
     */
    protected Effect() {
        this.inputs = Collections.emptyList();
    }
    
    /**
     * Constructs an {@code Effect} with exactly one input.
     * 
     * @param input the input {@code Effect}
     * @throws IllegalArgumentException if {@code input} is null
     */
    protected Effect(Effect input) {
        if (input == null) {
            throw new IllegalArgumentException("Input must be non-null");
        }
        this.inputs = Collections.singletonList(input);
        
        input.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange("input0", null, inputs.get(0));
            }
        });
    }
    
    /**
     * Constructs an {@code Effect} with exactly two inputs.
     *
     * @param input1 the first input {@code Effect}
     * @param input2 the second input {@code Effect}
     * @throws IllegalArgumentException if either {@code input1} or
     * {@code input2} is null
     */
    protected Effect(Effect input1, Effect input2) {
        if (input1 == null || input2 == null) {
            throw new IllegalArgumentException("Inputs must be non-null");
        }
        ArrayList<Effect> tmp = new ArrayList<Effect>();
        tmp.add(input1);
        tmp.add(input2);
        this.inputs = Collections.unmodifiableList(tmp);
        
        // listen for property changes in any of the inputs (the JavaBeans
        // spec is vague about how to handle situations like this, but as
        // long as we fire *some* event, it will be good enough to ensure
        // that e.g. a scenegraph node will be repainted after making a
        // change to one of the input properties)
        input1.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange("input0", null, inputs.get(0));
            }
        });
        input2.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange("input1", null, inputs.get(1));
            }
        });
    }
    
    /**
     * Returns the (immutable) list of input {@code Effect}s, or an empty
     * list if no inputs were specified at construction time.
     * 
     * @return the list of input {@code Effect}s
     */
    public final List<Effect> getInputs() {
        return inputs;
    }
    
    /**
     * Returns the {@code SourceContent} that is currently in use for
     * this {@code Effect}.
     * 
     * @return the current {@code SourceContent}, or null
     */
    public final SourceContent getSourceContent() {
        return content;
    }
    
    /**
     * Sets the {@code SourceContent} for this {@code Effect} and any
     * inputs.  This operation works recursively to set the source content
     * for an entire {@code Effect} tree.
     * 
     * @param content the {@code SourceContent} to be made current
     */
    public void setSourceContent(SourceContent content) {
        this.content = content;
        for (Effect input : inputs) {
            input.setSourceContent(content);
        }
    }
    
    /**
     * Applies this filter effect to the series of images represented by
     * the input {@code Effect}s and/or {@code SourceContent}, and then
     * returns the resulting {@code Image}.
     * 
     * @param config the {@code GraphicsConfiguration} that will be used
     * for creating images and for performing the filter operation
     * @return the result of this filter operation
     */
    public abstract Image filter(GraphicsConfiguration config);
    
    /**
     * Convenience method that calls {@code filter()} and then transforms
     * the resulting image into device space if the {@code transformed}
     * parameter is true.
     * 
     * @param config the {@code GraphicsConfiguration} that will be used
     * for creating images and for performing the filter operation
     * @param transformed if true, converts the filtered result into device
     * space; otherwise, returns the filtered result without transformation
     * @return the transformed result of this filter operation
     */
    public final Image filter(GraphicsConfiguration config,
                              boolean transformed)
    {
        Image img = filter(config);
        if (transformed && !isInDeviceSpace()) {
            AffineTransform xform = content.getTransform();
            int type = xform.getType();
            if (type != AffineTransform.TYPE_IDENTITY &&
                type != AffineTransform.TYPE_TRANSLATION)
            {
                // setup graphics transform so that the (original,
                // untransformed) effect image is transformed into device space
                Image orig = img;
                Rectangle xformBounds = content.getTransformedBounds().getBounds();
                Rectangle2D origBounds = getBounds();
                img = getCompatibleImage(config, xformBounds.width, xformBounds.height);
                Graphics2D g2 = (Graphics2D)img.getGraphics();
                AffineTransform xform2 = new AffineTransform();
                xform2.translate(-xformBounds.getX(), -xformBounds.getY());
                xform2.concatenate(xform);
                xform2.translate(origBounds.getX(), origBounds.getY());
                g2.setTransform(xform2);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(orig, 0, 0, null);
                g2.dispose();
            }
        }
        return img;
    }
    
    /**
     * Applies this filter effect to the series of inputs and then renders
     * the result to the provided {@code Graphics2D}.  This method is
     * similar to calling:
     * <pre>
     *     g.drawImage(filter(transformed), x, y, null);
     * </pre>
     * except that it is likely to be more efficient (and correct).
     * 
     * @param g the {@code Graphics2D} to which the {@code Effect} will be
     * rendered
     * @param x the x location of the filtered result
     * @param y the y location of the filtered result
     * @param transformed if true, converts the filtered result into device
     * space; otherwise, returns the filtered result without transformation
     */
    public final void render(Graphics2D g, int x, int y, boolean transformed) {
        // TODO: validate VolatileImages...
        GraphicsConfiguration gc = g.getDeviceConfiguration();
        Image res = filter(gc, transformed);
        g.drawImage(res, x, y, null);
        releaseCompatibleImage(gc, res);
    }

    /**
     * Returns the bounding box that will be affected by this filter
     * operation, given the list of input {@code Effect}s and/or the
     * current {@code SourceContent}.  Note that the returned bounds can
     * be smaller or larger than one or more of the inputs.
     * 
     * @return the bounding box of this filter
     */
    public abstract Rectangle2D getBounds();

    /**
     * Returns the bounding box of this filter operation, transformed
     * according to the {@code AffineTransform} set in the current
     * {@code SourceContent}.
     * 
     * @return the transformed bounding box of this filter
     */
    public final Rectangle2D getTransformedBounds() {
        Rectangle2D r = getBounds();
        AffineTransform xform = content.getTransform();
        if (!isInDeviceSpace() && xform != null && !xform.isIdentity()) {
            r = xform.createTransformedShape(r).getBounds();
        }
        return r;
    }
    
    /**
     * Returns a new {@code Image} that is most compatible with the
     * given {@code GraphicsConfiguration}.  This method will select the image
     * type that is most appropriate for use with the current rendering
     * pipeline, graphics hardware, and screen pixel layout.
     * 
     * @param gc the target screen device
     * @param w the width of the image
     * @param h the height of the image
     * @return a new {@code Image} with the given dimensions
     * @throws IllegalArgumentException if {@code gc} is null, or if
     * either {@code w} or {@code h} is non-positive
     */
    public static Image createCompatibleImage(GraphicsConfiguration gc, int w, int h) {
        return EffectPeer.getRenderer(gc).createCompatibleImage(w, h);
    }

    /**
     * Returns an {@code Image} that is most compatible with the
     * given {@code GraphicsConfiguration}.  This method will select the image
     * type that is most appropriate for use with the current rendering
     * pipeline, graphics hardware, and screen pixel layout.
     * <p>
     * Note that the framework attempts to pool images for recycling purposes
     * whenever possible.  Therefore, when finished using an image returned
     * by this method, it is highly recommended that you
     * {@link #releaseCompatibleImage release} the image back to the
     * shared pool for others to use.
     * 
     * @param gc the target screen device
     * @param w the width of the image
     * @param h the height of the image
     * @return an {@code Image} with the given dimensions
     * @throws IllegalArgumentException if {@code gc} is null, or if
     * either {@code w} or {@code h} is non-positive
     * @see #releaseCompatibleImage
     */
    public static Image getCompatibleImage(GraphicsConfiguration gc, int w, int h) {
        return EffectPeer.getRenderer(gc).getCompatibleImage(w, h);
    }
    
    /**
     * Releases an {@code Image} created by the
     * {@link #getCompatibleImage getCompatibleImage()} method
     * back into the shared pool.
     * 
     * @param gc the target screen device
     * @param image the {@code Image} to be released
     * @see #getCompatibleImage
     */
    public static void releaseCompatibleImage(GraphicsConfiguration gc, Image image) {
        EffectPeer.getRenderer(gc).releaseCompatibleImage(image);
    }
    
    /**
     * Returns true if this {@code Effect} produces an {@code Image} in
     * device space, or false if the {@code Image} will need to be converted
     * into device space in a later stage.
     * <p>
     * {@code Effect}s that inherently work in pixel space, such as a
     * convolution, will typically operate only on untransformed sources.
     * The result of such {@code Effect}s can be transformed into device
     * space in a later stage (e.g. prior to display), ideally using a
     * high-quality scaling algorithm.
     * 
     * @return true if this {@code Effect} is in device space; false otherwise
     */
    public boolean isInDeviceSpace() {
        for (Effect input : getInputs()) {
            if (input.isInDeviceSpace()) {
                // as long as we have at least one input in device space,
                // we will end up converting the rest to match
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns an integer indicating whether the effect implementation needs
     * access to the {@code TRANSFORMED} source, {@code UNTRANSFORMED} source,
     * {@code BOTH}, or {@code NONE}.
     * 
     * @return one of {@code TRANSFORMED}, {@code UNTRANSFORMED},
     * {@code BOTH}, or {@code NONE}
     */
    public int needsSourceContent() {
        int val = NONE;
        for (Effect input : getInputs()) {
            val |= input.needsSourceContent();
        }
        return val;
    }
    
    /**
     * A set of values that represent the possible levels of acceleration
     * for an {@code Effect} implementation.
     * 
     * @see Effect#getAccelType
     */
    public enum AccelType {
        /**
         * Indicates that this {@code Effect} is implemented in software
         * (i.e., running on the CPU).
         */
        NONE("CPU"),
        /**
         * Indicates that this {@code Effect} is being accelerated in
         * graphics hardware via OpenGL.
         */
        OPENGL("OpenGL"),
        /**
         * Indicates that this {@code Effect} is being accelerated in
         * graphics hardware via Direct3D.
         */
        DIRECT3D("Direct3D");
        
        private String text;
        
        private AccelType(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
    
    /**
     * Returns one of the {@link AccelType AccelType} values, indicating
     * whether this {@code Effect} is accelerated in hardware for the
     * given {@code GraphicsConfiguration}.
     * 
     * @param config the {@code GraphicsConfiguration} that will be used
     * for performing the filter operation
     * @return one of the {@code AccelType} values
     */
    public abstract AccelType getAccelType(GraphicsConfiguration config);
    
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
    protected void firePropertyChange(String prop,
                                      Object oldValue, Object newValue)
    {
        pcs.firePropertyChange(prop, oldValue, newValue);
    }
}
