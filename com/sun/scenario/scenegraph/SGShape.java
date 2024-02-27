/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.scenario.scenegraph;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import com.sun.scenario.scenegraph.SGAbstractShape.Mode;
import java.awt.Stroke;

/**
 * A scene graph node that renders a Shape.
 * 
 * @author Chet Haase
 * @author Hans Muller
 */
public class SGShape extends SGAbstractShape {
    private Shape shape;
    private Shape cachedStrokeShape;
    private Object antialiasingHint = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
    
    /**
     * Returns a reference to (not a copy of) the {@code Shape} of this node.
     * The default value of this property is null.
     * <p>
     * Typically the {@code shape} property will be set once when the
     * {@code SGShape} is first constructed.  If thereafter the {@code shape}
     * object is modified, it is the user's responsibility to call
     * {@code setShape()} to ensure that the node state is properly updated.
     *
     * @return the {@code Shape} of this node
     */
    public final Shape getShape() { 
        return shape;
    }

    /**
     * Sets the {@code Shape} of this node.
     * <p>
     * Typically the {@code shape} property will be set once when the
     * {@code SGShape} is first constructed.  If thereafter the {@code shape}
     * object is modified, it is the user's responsibility to call
     * {@code setShape()} to ensure that the node state is properly updated.
     *
     * @param shape the {@code Shape} of this node
     */
    public void setShape(Shape shape) {
        this.shape = shape;
        cachedStrokeShape = null;
        repaint(true);
    }

    /**
     * Returns the {@code KEY_ANTIALIASING} rendering hint.
     * The {@code hint} will be
     * one of: {@code RenderingHints.VALUE_ANTIALIAS_ON}, 
     * {@code RenderingHints.VALUE_ANTIALIAS_OFF}, 
     * {@code RenderingHints.VALUE_ANTIALIAS_DEFAULT}.
     * 
     * @return the {@code KEY_ANTIALIASING} hint
     * @see java.awt.RenderingHints
     */
    public Object getAntialiasingHint() {
        return this.antialiasingHint;
    }

    /**
     * Sets the {@code KEY_ANTIALIASING} rendering hint. The {@code hint} must be
     * one of: {@code RenderingHints.VALUE_ANTIALIAS_ON}, 
     * {@code RenderingHints.VALUE_ANTIALIAS_OFF}, 
     * {@code RenderingHints.VALUE_ANTIALIAS_DEFAULT}.  The default is
     * {@code VALUE_ANTIALIAS_DEFAULT}.
     * 
     * @see java.awt.RenderingHints
     * @see java.awt.Graphics2D
     */
    public void setAntialiasingHint(Object hint) {
        if (!RenderingHints.KEY_ANTIALIASING.isCompatibleValue(hint)) {
            // Note that KEY_ANTIALIASING.isCompatibleValue also rejects null
            throw new IllegalArgumentException("invalid hint");
        }
        this.antialiasingHint = hint;
        repaint(false);
    }

    @Override
    public void paint(Graphics2D g) {
        if (shape == null) {
            return;
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasingHint);
        if ((mode == Mode.FILL) || (mode == Mode.STROKE_FILL)) {
            g.setPaint(fillPaint);
            if (DO_PAINT) {
                g.fill(shape);
            }
        }
        if ((mode == Mode.STROKE) || (mode == Mode.STROKE_FILL)) {
            g.setPaint(drawPaint);
            g.setStroke(drawStroke);
            if (DO_PAINT) {
                g.draw(shape);
            }
        }
    }

    @Override
    public void setDrawStroke(Stroke drawStroke) {
        super.setDrawStroke(drawStroke);
        cachedStrokeShape = null;
    }

    @Override 
    public final Rectangle2D getBounds(AffineTransform at) {
        Shape s = getShape();
        if (s == null) {
            // REMIND: Use -1 w/h or just plain 0,0,0,0?
            return new Rectangle2D.Float(0, 0, -1, -1);
        }
        boolean includeShape = (mode != Mode.STROKE);
        boolean includeStroke = (mode != Mode.FILL);
        float bbox[] = {
            Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
            Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
        };
        if (includeShape) {
            accumulate(bbox, s, at);
        }
        if (includeStroke) {
            if (cachedStrokeShape == null) {
                cachedStrokeShape = drawStroke.createStrokedShape(s);
            }
            accumulate(bbox, cachedStrokeShape, at);
        }
        if (bbox[2] < bbox[0] || bbox[3] < bbox[1]) {
            // They are probably +/-INFINITY which would yield NaN if subtracted
            // Let's just return a "safe" empty bbox...
            return new Rectangle2D.Float(0, 0, -1, -1);
        }
        return new Rectangle2D.Float(bbox[0], bbox[1],
                                     bbox[2]-bbox[0],
                                     bbox[3]-bbox[1]);
    }
    
    private static final int coordsPerSeg[] = { 2, 2, 4, 6, 0 };
    private void accumulate(float bbox[], Shape s, AffineTransform at) {
        if (at == null || at.isIdentity()) {
            // The shape itself will often have a more optimal algorithm
            // to calculate the untransformed bounds...
            Rectangle2D r2d = s.getBounds2D();
            if (bbox[0] > r2d.getMinX()) bbox[0] = (float) r2d.getMinX();
            if (bbox[1] > r2d.getMinY()) bbox[1] = (float) r2d.getMinY();
            if (bbox[2] < r2d.getMaxX()) bbox[2] = (float) r2d.getMaxX();
            if (bbox[3] < r2d.getMaxY()) bbox[3] = (float) r2d.getMaxY();
            return;
        }
        PathIterator pi = s.getPathIterator(at);
        float coords[] = new float[6];
        while (!pi.isDone()) {
            int numcoords = coordsPerSeg[pi.currentSegment(coords)];
            for (int i = 0; i < numcoords; i++) {
                float v = coords[i];
                int off = (i & 1); // 0 for X, 1 for Y coords
                if (bbox[off+0] > v) bbox[off+0] = v;
                if (bbox[off+2] < v) bbox[off+2] = v;
            }
            pi.next();
        }
    }

    @Override 
    public boolean contains(Point2D point) { 
        return (shape == null) ? false : shape.contains(point);
    }
}
