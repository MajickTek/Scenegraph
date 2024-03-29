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
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.util.Collections;
import java.util.List;

/**
 * This class is the common base class for a type of node that interposes
 * a single operation or attribute on the tree and then transfers control
 * directly to its descendants.
 * An {@link SGGroup} node could be used here, but most cases only want
 * to interpose in a single location in the tree and becoming an
 * {@code SGGroup} just for that one purpose would be both much more
 * heavyweight and would imply a more extensive modification of the tree
 * (allowing for multiple branches) just to interpose a single element.
 *
 * @author Flar
 */
public class SGFilter extends SGParent {

    /**
     * Flag indicating that the filter implementation does not need
     * access to the source as a raster image.
     */
    public static final int NONE          = (0 << 0);
    /**
     * Flag indicating that the filter implementation needs access to
     * the source as a raster image (in the original, local coordinate
     * space of the child node).
     */
    public static final int UNTRANSFORMED = (1 << 0);
    /**
     * Flag indicating that the filter implementation needs access to
     * the source as a raster image (in the transformed coordinate space
     * of the child node).
     */
    public static final int TRANSFORMED   = (1 << 1);
    /**
     * Flag indicating that the filter implementation needs access to
     * the source as a raster image in both untransformed and transformed
     * formats.
     * This is equivalent to {@code (UNTRANSFORMED | TRANSFORMED)}.
     */
    public static final int BOTH          = UNTRANSFORMED | TRANSFORMED;
    /**
     * Flag indicating that the filter implementation has already cached
     * the rendering of the source and can render it via renderFromCache().
     */
    public static final int CACHED        = (1 << 2);
    
    private SGNode child;
    private List<SGNode> singletonList;
    
    /** Creates a new instance of SGFilter */
    public SGFilter() {
    }
    
    public final List<SGNode> getChildren() {
        if (child == null) {
            return Collections.emptyList();
        } else {
            if (singletonList == null) {
                singletonList = Collections.singletonList(child);
            }
            return singletonList;
        }
    }
    
    public final SGNode getChild() {
        return child;
    }
    
    public void setChild(SGNode child) {
        if (child == null) {
            throw new IllegalArgumentException("null child");
        }
        if (child == this.child) {
            return;
        }
        SGParent oldParent = child.getParent();
        if (oldParent != null) {
            oldParent.remove(child);
        }
        this.singletonList = null;
        this.child = child;
        child.setParent(this);

        // mark the current bounds dirty (and force repaint of former
        // bounds as well)
        markDirty(true);
    }

    @Override
    public void remove(SGNode node) {
        if (node == child) {
            remove();
        }
    }
    
    public void remove() {
        FocusHandler.removeNotify(child);
        this.child.setParent(null);
        this.child = null;
        this.singletonList = null;

        // mark the current bounds dirty (and force repaint of former
        // bounds as well)
        markDirty(true);
        updateCursor();
    }

    public void renderFromCache(Graphics2D g) {
    }

    public boolean canSkipRendering() {
        return false;
    }

    public boolean canSkipChildren() {
        return false;
    }
    
    /**
     * Returns true if the bounds of this filter node are (potentially)
     * larger than the bounds of its child, false otherwise.
     * The default implementation of this method always returns false;
     * subclasses should override accordingly.
     * 
     * @return whether the bounds of this node expand outside the child bounds
     */
    public boolean canExpandBounds() {
        return false;
    }
    
    public int needsSourceContent() {
        return NONE;
    }
    
    public void setupRenderGraphics(Graphics2D g) {
    }
    
    public void renderFinalImage(Graphics2D g, SGSourceContent srcContent) {
    }
    
    @Override
    public Rectangle2D getBounds(AffineTransform transform) {
        if (child == null) {
            // just an empty rectangle
            return new Rectangle2D.Float();
        } else {
            return child.getBounds(transform);
        }
    }
    
    /**
     * Calculates the accumulated bounds object representing the
     * global bounds relative to the root of the tree.
     * Since most filter nodes have the same accumulated bounds as
     * their child, this implementation will simply report the
     * accumulated bounds of its child.
     * Subclasses may override this behavior if they do not conform
     * to the above assumption.
     */
    @Override
    Rectangle2D calculateAccumBounds() {
        if (child == null) {
            return new Rectangle2D.Float();
        } else {
            return child.getTransformedBoundsRelativeToRoot();
        }
    }

    @Override
    boolean hasOverlappingContents() {
        return child.hasOverlappingContents();
    }
}
