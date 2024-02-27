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
 * An effect that blends the two inputs together using one of the
 * pre-defined {@code Mode}s.
 * 
 * @author Chris Campbell
 */
public class Blend extends CoreEffect {

    /**
     * A blending mode that defines the manner in which the inputs
     * are composited together.
     * Each {@code Mode} describes a mathematical equation that
     * combines premultiplied inputs to produce some premultiplied result.
     */
    public enum Mode {
        /**
         * The top input is blended over the bottom input.
         * (Equivalent to the Porter-Duff "source over destination" rule.)
         * <p>
         * Thus:
         * <pre>
         *      <em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         *      <em>C<sub>r</sub></em> = <em>C<sub>top</sub></em> + <em>C<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * </pre>
         */
        SRC_OVER,
        
        /**
         * The part of the top input lying inside of the bottom input
         * is kept in the resulting image.
         * (Equivalent to the Porter-Duff "source in destination" rule.)
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em>*<em>A<sub>bot</sub></em>
         * 	<em>C<sub>r</sub></em> = <em>C<sub>top</sub></em>*<em>A<sub>bot</sub></em>
         * </pre>
         */
        SRC_IN,
        
        /**
         * The part of the top input lying outside of the bottom input
         * is kept in the resulting image.
         * (Equivalent to the Porter-Duff "source held out by destination"
         * rule.)
         * <p>
         * Thus:
         * <pre>
         *      <em>A<sub>r</sub></em> = <em>A<sub>top</sub></em>*(1-<em>A<sub>bot</sub></em>)
         *      <em>C<sub>r</sub></em> = <em>C<sub>top</sub></em>*(1-<em>A<sub>bot</sub></em>)
         * </pre>
         */
        SRC_OUT,
        
        /**
         * The part of the top input lying inside of the bottom input
         * is blended with the bottom input.
         * (Equivalent to the Porter-Duff "source atop destination" rule.)
         * <p>
         * Thus:
         * <pre>
         *      <em>A<sub>r</sub></em> = <em>A<sub>top</sub></em>*<em>A<sub>bot</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>) = <em>A<sub>bot</sub></em>
         *      <em>C<sub>r</sub></em> = <em>C<sub>top</sub></em>*<em>A<sub>bot</sub></em> + <em>C<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * </pre>
         */
        SRC_ATOP,

        /**
         * The color and alpha components from the top input are
         * added to those from the bottom input.
         * The result is clamped to 1.0 if it exceeds the logical
         * maximum of 1.0.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = min(1, <em>A<sub>top</sub></em>+<em>A<sub>bot</sub></em>)
         * 	<em>C<sub>r</sub></em> = min(1, <em>C<sub>top</sub></em>+<em>C<sub>bot</sub></em>)
         * </pre>
         * <p>
         * Notes:
         * <ul>
         * <li>This mode is commutative (ordering of inputs
         * does not matter).
         * <li>This mode is sometimes referred to as "linear dodge" in
         * imaging software packages.
         * </ul>
         */
        ADD,

        /**
         * The color components from the first input are multiplied with those
         * from the second input.
         * The alpha components are blended according to
         * the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * 	<em>C<sub>r</sub></em> = <em>C<sub>top</sub></em> * <em>C<sub>bot</sub></em>
         * </pre>
         * <p>
         * Notes:
         * <ul>
         * <li>This mode is commutative (ordering of inputs
         * does not matter).
         * <li>This mode is the mathematical opposite of
         * the {@link #SCREEN} mode.
         * <li>The resulting color is always at least as dark as either
         * of the input colors.
         * <li>Rendering with a completely black top input produces black;
         * rendering with a completely white top input produces a result
         * equivalent to the bottom input.
         * </ul>
         */
        MULTIPLY,

        /**
         * The color components from both of the inputs are
         * inverted, multiplied with each other, and that result
         * is again inverted to produce the resulting color.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * 	<em>C<sub>r</sub></em> = 1 - ((1-<em>C<sub>top</sub></em>) * (1-<em>C<sub>bot</sub></em>))
         * </pre>
         * <p>
         * Notes:
         * <ul>
         * <li>This mode is commutative (ordering of inputs
         * does not matter).
         * <li>This mode is the mathematical opposite of
         * the {@link #MULTIPLY} mode.
         * <li>The resulting color is always at least as light as either
         * of the input colors.
         * <li>Rendering with a completely white top input produces white;
         * rendering with a completely black top input produces a result
         * equivalent to the bottom input.
         * </ul>
         */
        SCREEN,

        /**
         * The input color components are either multiplied or screened,
         * depending on the bottom input color.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         *      REMIND: not sure how to express this succinctly yet...
         * </pre>
         * <p>
         * Notes:
         * <ul>
         * <li>This mode is a combination of {@link #SCREEN} and
         * {@link #MULTIPLY}, depending on the bottom input color.
         * <li>This mode is the mathematical opposite of
         * the {@link #HARD_LIGHT} mode.
         * <li>In this mode, the top input colors "overlay" the bottom input
         * while preserving highlights and shadows of the latter.
         * </ul>
         */
        OVERLAY,

        /**
         * REMIND: cross check this formula with OpenVG spec...
         *
         * The darker of the color components from the two inputs are
         * selected to produce the resulting color.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * 	<em>C<sub>r</sub></em> = min(<em>C<sub>top</sub></em>, <em>C<sub>bot</sub></em>)
         * </pre>
         * <p>
         * Notes:
         * <ul>
         * <li>This mode is commutative (ordering of inputs
         * does not matter).
         * <li>This mode is the mathematical opposite of
         * the {@link #LIGHTEN} mode.
         * </ul>
         */
        DARKEN,

        /**
         * REMIND: cross check this formula with OpenVG spec...
         *
         * The lighter of the color components from the two inputs are
         * selected to produce the resulting color.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * 	<em>C<sub>r</sub></em> = max(<em>C<sub>top</sub></em>, <em>C<sub>bot</sub></em>)
         * </pre>
         * <p>
         * Notes:
         * <ul>
         * <li>This mode is commutative (ordering of inputs
         * does not matter).
         * <li>This mode is the mathematical opposite of
         * the {@link #DARKEN} mode.
         * </ul>
         */
        LIGHTEN,

        /**
         * The bottom input color components are divided by the inverse
         * of the top input color components to produce the resulting color.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * 	<em>C<sub>r</sub></em> = <em>C<sub>bot</sub></em> / (1-<em>C<sub>top</sub></em>)
         * </pre>
         */
        COLOR_DODGE,

        /**
         * The inverse of the bottom input color components are divided by
         * the top input color components, all of which is then inverted
         * to produce the resulting color.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * 	<em>C<sub>r</sub></em> = 1-((1-<em>C<sub>bot</sub></em>) / <em>C<sub>top</sub></em>)
         * </pre>
         */
        COLOR_BURN,

        /**
         * The input color components are either multiplied or screened,
         * depending on the top input color.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         *      REMIND: not sure how to express this succinctly yet...
         * </pre>
         * <p>
         * Notes:
         * <ul>
         * <li>This mode is a combination of {@link #SCREEN} and
         * {@link #MULTIPLY}, depending on the top input color.
         * <li>This mode is the mathematical opposite of
         * the {@link #OVERLAY} mode.
         * </ul>
         */
        HARD_LIGHT,

        /**
         * REMIND: this is a complicated formula, TBD...
         */
        SOFT_LIGHT,

        /**
         * The darker of the color components from the two inputs are
         * subtracted from the lighter ones to produce the resulting color.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * 	<em>C<sub>r</sub></em> = abs(<em>C<sub>top</sub></em>-<em>C<sub>bot</sub></em>)
         * </pre>
         * <p>
         * Notes:
         * <ul>
         * <li>This mode is commutative (ordering of inputs
         * does not matter).
         * <li>This mode can be used to invert parts of the bottom input
         * image, or to quickly compare two images (equal pixels will result
         * in black).
         * <li>Rendering with a completely white top input inverts the
         * bottom input; rendering with a completely black top input produces
         * a result equivalent to the bottom input.
         * </ul>
         */
        DIFFERENCE,

        /**
         * The color components from the two inputs are multiplied and
         * doubled, and then subtracted from the sum of the bottom input
         * color components, to produce the resulting color.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * 	<em>C<sub>r</sub></em> = <em>C<sub>top</sub></em> + <em>C<sub>bot</sub></em> - (2*<em>C<sub>top</sub></em>*<em>C<sub>bot</sub></em>)
         * </pre>
         * <p>
         * Notes:
         * <ul>
         * <li>This mode is commutative (ordering of inputs
         * does not matter).
         * <li>This mode can be used to invert parts of the bottom input.
         * <li>This mode produces results that are similar to those of
         * {@link #DIFFERENCE}, except with lower contrast.
         * <li>Rendering with a completely white top input inverts the
         * bottom input; rendering with a completely black top input produces
         * a result equivalent to the bottom input.
         * </ul>
         */
        EXCLUSION,

        /**
         * The red component of the bottom input is replaced with the
         * red component of the top input; the other color components
         * are unaffected.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * 	<em>R<sub>r</sub></em> = <em>R<sub>top</sub></em>
         * 	<em>G<sub>r</sub></em> = <em>G<sub>bot</sub></em>
         * 	<em>B<sub>r</sub></em> = <em>B<sub>bot</sub></em>
         * </pre>
         */
        RED,

        /**
         * The green component of the bottom input is replaced with the
         * green component of the top input; the other color components
         * are unaffected.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * 	<em>R<sub>r</sub></em> = <em>R<sub>bot</sub></em>
         * 	<em>G<sub>r</sub></em> = <em>G<sub>top</sub></em>
         * 	<em>B<sub>r</sub></em> = <em>B<sub>bot</sub></em>
         * </pre>
         */
        GREEN,

        /**
         * The blue component of the bottom input is replaced with the
         * blue component of the top input; the other color components
         * are unaffected.
         * The alpha components are blended according
         * to the {@link #SRC_OVER} equation.
         * <p>
         * Thus:
         * <pre>
         * 	<em>A<sub>r</sub></em> = <em>A<sub>top</sub></em> + <em>A<sub>bot</sub></em>*(1-<em>A<sub>top</sub></em>)
         * 	<em>R<sub>r</sub></em> = <em>R<sub>bot</sub></em>
         * 	<em>G<sub>r</sub></em> = <em>G<sub>bot</sub></em>
         * 	<em>B<sub>r</sub></em> = <em>B<sub>top</sub></em>
         * </pre>
         */
        BLUE,
    }
    
    private Mode mode;
    private float opacity;
    
    /**
     * Constructs a new {@code Blend} effect with the given mode and the
     * default opacity (1.0).
     * 
     * @param mode the blending mode
     * @param bottomInput the bottom input
     * @param topInput the top input
     * @throws IllegalArgumentException if {@code mode} is null, or if
     * either input is null
     */
    public Blend(Mode mode, Effect bottomInput, Effect topInput) {
        super(bottomInput, topInput);
        setMode(mode);
        setOpacity(1f);
    }
    
    /**
     * Returns the {@code Mode} used to blend the two inputs together.
     * 
     * @return the {@code Mode} used to blend the two inputs together.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the {@code Mode} used to blend the two inputs together.
     * <pre>
     *       Min: n/a
     *       Max: n/a
     *   Default: Mode.SRC_OVER
     *  Identity: n/a
     * </pre>
     * 
     * @param mode the blending mode
     * @throws IllegalArgumentException if {@code mode} is null
     */
    public void setMode(Mode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Mode must be non-null");
        }
        Blend.Mode old = this.mode;
        this.mode = mode;
        updatePeerKey("Blend_" + mode.name());
        firePropertyChange("mode", old, mode);
    }

    /**
     * Returns the opacity value, which is modulated with the top input
     * prior to blending.
     * 
     * @return the opacity value
     */
    public float getOpacity() {
        return opacity;
    }
    
    /**
     * Sets the opacity value, which is modulated with the top input prior
     * to blending.
     * <pre>
     *       Min: 0.0
     *       Max: 1.0
     *   Default: 1.0
     *  Identity: 1.0
     * </pre>
     * 
     * @param opacity the opacity value
     * @throws IllegalArgumentException if {@code opacity} is outside the
     * allowable range
     */
    public void setOpacity(float opacity) {
        if (opacity < 0f || opacity > 1f) {
            throw new IllegalArgumentException("Opacity must be in the range [0,1]");
        }
        float old = this.opacity;
        this.opacity = opacity;
        firePropertyChange("opacity", old, opacity);
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
        return filterInputs(config, true, 0, 1).getImage();
    }
}
