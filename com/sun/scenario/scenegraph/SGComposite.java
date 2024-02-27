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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

/**
 * @author Chris Campbell
 */
public class SGComposite extends SGFilter {
    
    public enum Mode { SRC_OVER }

    /**
     * Specifies the behavior to be used to optimize the rendering
     * of sub-trees which may contain multiple overlapping elements.
     * If there are multiple overlapping elements in the sub-tree
     * then the sub-tree can be rendered into an intermediate image
     * and then composited to the destination using an imaging operation
     * to minimize bleed-through of the elements where they overlap.
     * Alternatively the sub-tree can be rendered piece-wise if there
     * are no overlapping elements, or if the bleed-through effect is
     * desired, either for performance or for graphical style.
     * <p>
     * The default behavior is {@code SCAN_TREE}.
     */
    public enum OverlapBehavior {
        /**
         * The descendent tree is scanned to determine if there are
         * multiple graphical elements present and the appropriate behavior
         * ({@link #USE_INTERMEDIATE_IMAGE} for the overlapping case or
         * {@link #RENDER_PIECEWISE} for the non-overlapping case) is
         * used as needed on the fly.
         * <p>
         * This behavior produces the least surprising output in the
         * safest manner and so is the default behavior for
         * {@link SGComposite} nodes.
         * In most cases the scan can discover what it needs to know to
         * make the determination from a fairly shallow scan into the
         * descendant nodes.
         */
        SCAN_TREE,

        /**
         * The sub-tree is always rendered using an intermediate image
         * to prevent bleed-through.
         * <p>
         * While the {@link #SCAN_TREE} behavior will use this behavior
         * as needed, this behavior can be selected manually to avoid the
         * render-time cost of scanning the sub-tree in cases where the
         * tree has a relatively static configuration that is known to
         * require this behavior.
         */
        USE_INTERMEDIATE_IMAGE,

        /**
         * The composite mode of the graphics is modified as indicated
         * and the sub-tree is rendered normally, allowing bleed-through
         * if any elements in the subtree overlap.
         * <p>
         * While the {@link #SCAN_TREE} behavior can easily and safely
         * determine whether there might be any visible difference in
         * using this behavior, this behavior can be selected manually,
         * either to avoid the render-time cost associated with scanning
         * the tree or constructing and using an intermediate image, or
         * in cases where the bleed-through effect that may occur with
         * overlapping descendants is a desired visual style.
         */
        RENDER_PIECEWISE
    }

    private float opacity = 1f;
    private Mode mode = Mode.SRC_OVER;
    private OverlapBehavior overlapbehavior = OverlapBehavior.SCAN_TREE;

    public SGComposite() {
    }
    
    public SGComposite(float opacity, SGNode child) {
        setOpacity(opacity);
        setChild(child);
    }
    
    public float getOpacity() {
        return opacity;
    }
    
    public void setOpacity(float opacity) {
        this.opacity = opacity;
        markDirty(false);
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode mode) {
        this.mode = mode;
        markDirty(false);
    }

    /**
     * Gets the current {@code OverlapBehavior} to be
     * used to optimize rendering in the case of overlapping elements
     * in the sub-tree.
     * @return the current {@code OverlapBehavior}
     */
    public OverlapBehavior getOverlapBehavior() {
        return overlapbehavior;
    }

    /**
     * Sets the {@code OverlapBehavior} to be used to optimize rendering
     * in the case of overlapping elements in the sub-tree.
     * @param overlapbehavior the desired {@code OverlapBehavior}
     */
    public void setOverlapBehavior(OverlapBehavior overlapbehavior) {
        this.overlapbehavior = overlapbehavior;
        markDirty(false);
    }

    @Override
    public boolean canSkipRendering() {
        return (mode == Mode.SRC_OVER && opacity == 0f);
    }
    
    @Override
    public boolean canSkipChildren() {
        return canSkipRendering();
    }
    
    @Override
    public int needsSourceContent() {
        SGNode child = getChild();
        boolean needsSource;
        if (child == null) {
            needsSource = false;
        } else {
            if (opacity < 1f) {
                switch (overlapbehavior) {
                    case SCAN_TREE:
                        needsSource = child.hasOverlappingContents();
                        break;
                    case USE_INTERMEDIATE_IMAGE:
                        needsSource = true;
                        break;
                    case RENDER_PIECEWISE:
                        needsSource = false;
                        break;
                    default:
                        needsSource = true;
                        break;
                }
            } else {
                needsSource = false;
            }
        }
        return needsSource ? TRANSFORMED : NONE;
    }
    
    @Override
    public void setupRenderGraphics(Graphics2D g) {
        if (needsSourceContent() == NONE) {
            g.setComposite(makeComposite(g));
        }
    }
    
    @Override
    public void renderFinalImage(Graphics2D g, SGSourceContent srcContent) {
        if (opacity < 1f) {
            g.setComposite(makeComposite(g));
        }
        g.drawImage(srcContent.getTransformedImage(), 0, 0, null);
    }
    
    private Composite makeComposite(Graphics2D g) {
        int rule;
        switch (mode) {
        case SRC_OVER:
            rule = AlphaComposite.SRC_OVER;
            break;
        default:
            throw new InternalError("unknown Mode: "+mode);
        }
        AlphaComposite ac = (AlphaComposite) g.getComposite();
        if (ac.getRule() != rule) {
            throw new InternalError("mixed AlphaComposite modes");
        }
        float alpha = this.opacity * ac.getAlpha();
        return AlphaComposite.getInstance(rule, alpha);
    }

    @Override
    boolean hasOverlappingContents() {
        return (mode != Mode.SRC_OVER) ? true : super.hasOverlappingContents();
    }
}
