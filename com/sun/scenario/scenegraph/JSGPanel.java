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

import com.sun.scenario.effect.Effect;
import static java.awt.event.MouseEvent.MOUSE_ENTERED;
import static java.awt.event.MouseEvent.MOUSE_EXITED;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JComponent;

import com.sun.scenario.scenegraph.event.SGMouseAdapter;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.util.HashSet;

/**
 * JSGPanel is a JComponent that renders a scene graph.
 *
 * @author Chet Haase
 * @author Hans Muller
 */
public class JSGPanel extends JComponent {

    // debugging utility: displays red rectangles around node bounds
    private static final boolean debugBounds = false;
    // debugging utility: fills dirty areas with red XOR rectangles
    private static final boolean hiliteDirty = false;
    // toggles the use of incremental repainting optimizations
    private static final boolean incrementalRepaintOpt = true;
    private static final boolean enableFPS = true;
    private static final boolean defaultShowFPS = false;
    private FPSData fpsData;

    private SGNode scene = null;
    private boolean sceneIsNew = false;
    private SGGroup sceneGroup = null;
    private Dimension validatedPreferredSize = null;
    
    public JSGPanel() {
        setOpaque(true);
        MouseInputDispatcher dispatchMouseEvents = new MouseInputDispatcher();
        addMouseListener(dispatchMouseEvents);
        addMouseMotionListener(dispatchMouseEvents);
        addMouseWheelListener(dispatchMouseEvents);
        try {
            setFocusTraversalPolicyProvider(true);
        } catch (NoSuchMethodError e) {
            setFocusCycleRoot(true);
        }
        setFocusTraversalPolicy(FocusHandler.getFocusTraversalPolicy());
    }
    
    SGGroup getSceneGroup() {
        return sceneGroup;
    }
    
    public final SGNode getScene() {
        return fpsData == null ? scene : fpsData.origScene;
    }

    void removeScene() {
        scene = null;
        sceneGroup = null;
        fpsData = null;
        markDirty();
    }

    public void setScene(SGNode scene) {
        JSGPanel oldpanel = scene.getPanel();
        if (oldpanel != null && oldpanel.getScene() == scene) {
            oldpanel.removeScene();
        }
        SGParent oldParent = scene.getParent();
        if (oldParent != null) {
            oldParent.remove(scene);
        }
        if (enableFPS) {
            fpsData = new FPSData(scene);
            scene = fpsData.scene;
        }
        this.scene = scene;
        this.sceneIsNew = true;
        sceneGroup = new SGGroup();
        sceneGroup.add(scene);
        sceneGroup.setParent(this);
        sceneGroup.markDirty(true);
        FocusHandler.addNotify(scene);
        markDirty();
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Insets insets = getInsets();
        int dx = insets.left + insets.right;
        int dy = insets.top + insets.bottom;
        SGNode scene = getScene(); // so that we ignore FPSData, if present
        if (scene == null) {
            return new Dimension(dx + 640, dy + 480);
        } 
        else {
            Rectangle r = scene.getBounds().getBounds();
            return new Dimension(dx + r.width, dy + r.height);
        }
    }
    
    
    /*
     * Input handling below...
     */

    /* 
     * A mouse event whose location isn't contained by an SGLeaf will be 
     * discarded.
     * 
     * The MouseEvent location/X/Y properties are never updated.  Their
     * value is left as JSGPanel-relative.
     * 
     * Mouse events will be dispatched to all of the nodes returned by 
     * SGNode#pick() - in order - that have a matching listener.  Any listener
     * can short circuit this process by calling consume().
     * 
     * All of the events that comprise a press-drag-release gesture will be
     * delivered to the node that contained the press event.  
     * 
     * A press-drag-release gesture begins when no buttons are down and
     * a button is pressed.  It ends when no buttons are down again.
     * The trailing click (if any), which comes after the last release, 
     * is also considered part of the gesture.
     *
     * A node that recieves an enter event, will receive an exit event
     * before a subsequent event is dispatched to another non-overlapping node.
     */
    private class MouseInputDispatcher implements 
    MouseListener, MouseMotionListener, MouseWheelListener {
        private int buttonsPressed = 0;        // bit mask, bits 1,2,3 (0 unused)
        private List<SGNode> pdrNodes = null;  // pdr - press-drag-release
        private List<SGNode> enterNodes = null; 
    
        private void deliver(MouseEvent e, List<SGNode> nodes) {
            if (nodes != null) {
                for (SGNode node : nodes) {
                    node.processMouseEvent(e);
                    if (e.isConsumed()) {
                        break;
                    }
                }
            }
        }

        private MouseEvent createEvent(MouseEvent e, int id) {
            return new MouseEvent(
                JSGPanel.this, id, e.getWhen(), e.getModifiers(), e.getX(), 
                e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
        }


        /* Generate an exit event for all nodes that are no longer
         * under the mouse cursor, and an enter event for all nodes
         * that are under the mouse cursor but were not the last time
         * enter/exit notifications were delivered.
         */
        private void deliverEnterExit(MouseEvent e, List<SGNode> nodes) {
            if (nodes.size() == 0) {
                if ((enterNodes != null) && (enterNodes.size() > 0)) {
                    deliver(createEvent(e, MOUSE_EXITED), enterNodes);
                    enterNodes = null;
                }
            }
            else {
                if (enterNodes == null) {
                    enterNodes = nodes;
                    deliver(createEvent(e, MOUSE_ENTERED), enterNodes);
                }
                else {
                    int size = Math.max(enterNodes.size(), nodes.size());
                    HashSet notifiedNodes = new HashSet(size);
                    for(SGNode n : enterNodes) {
                        if (!notifiedNodes.contains(n) && !nodes.contains(n)) {
                            n.processMouseEvent(createEvent(e, MOUSE_EXITED));
                            notifiedNodes.add(n);
                        }
                    }
                    notifiedNodes.clear();
                    for(SGNode n : nodes) {
                        if (!notifiedNodes.contains(n) && !enterNodes.contains(n)) {
                            n.processMouseEvent(createEvent(e, MOUSE_ENTERED));
                            notifiedNodes.add(n);
                        }
                    }
                    enterNodes = nodes;
                }
            }
        }

        public void mousePressed(MouseEvent e) {
            SGGroup sceneGroup = getSceneGroup();
            if (sceneGroup != null) {
                if (buttonsPressed == 0) {
                    pdrNodes = getSceneGroup().pick(e.getPoint());
                }
                buttonsPressed |= (1 << e.getButton());  // getButton returns 1,2,3 
                deliver(e, pdrNodes);
                deliverEnterExit(e, sceneGroup.pick(e.getPoint()));
            }
        }

        public void mouseDragged(MouseEvent e) {
            SGGroup sceneGroup = getSceneGroup();
            if (sceneGroup != null) {
                if (pdrNodes != null) {
                    deliver(e, pdrNodes);
                } 
                deliverEnterExit(e, sceneGroup.pick(e.getPoint()));
            }
        }

        public void mouseReleased(MouseEvent e) {
            SGGroup sceneGroup = getSceneGroup();
            if (sceneGroup != null) {
                buttonsPressed &= ~(1 << e.getButton());
                deliver(e, pdrNodes);
                deliverEnterExit(e, sceneGroup.pick(e.getPoint()));
            }
        }

        public void mouseClicked(MouseEvent e) {
            deliver(e, pdrNodes);
        }

        public void mouseMoved(MouseEvent e) {
            SGGroup sceneGroup = getSceneGroup();
            mousePoint = e.getPoint();
            if (sceneGroup != null) {
                List<SGNode> nodes =  sceneGroup.pick(mousePoint);
                updateCursor(nodes);
                deliverEnterExit(e, nodes);
                deliver(e, nodes);
            }
        }

        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        
        public void mouseWheelMoved(MouseWheelEvent e) {
            SGGroup sceneGroup = getSceneGroup();
            if (sceneGroup != null) {
                List<SGNode> nodes =  sceneGroup.pick(e.getPoint());
                deliverEnterExit(e, nodes);
                deliver(e, nodes);
            }
        }
    }

    
    /*
     * Cursor management below...
     */
    private Cursor defaultCursor = null;
    private Point mousePoint = null;
    
    @Override
    public void setCursor(Cursor cursor) {
        setCursor(cursor, true);
    }
    
    private void setCursor(Cursor cursor, boolean isDefault) {
        if (isDefault) {
            defaultCursor = cursor;
        }
        super.setCursor(cursor);
    }
    void updateCursor() {
        if (mousePoint != null && sceneGroup != null) {
            List<SGNode> nodes =  sceneGroup.pick(mousePoint);
            updateCursor(nodes);
        }
    }
    
    void updateCursor(List<SGNode> nodes) {
        if (mousePoint == null) {
            return;
        }
        Cursor cursor = null;
        for (SGNode node : nodes) {
            cursor = node.getCursor();
            if (cursor != null) {
                break;
            }
        }
        cursor = (cursor == null) ? defaultCursor : cursor;
        setCursor(cursor, false);
    }
    
    /*
     * Focus management below...
     */
    
    private SGNode focusOwner = null;
    final void setFocusOwner(SGNode newFocusOwner) {
        SGNode oldFocusOwner = focusOwner;
        focusOwner = newFocusOwner; 
        firePropertyChange("focusOwner", oldFocusOwner, focusOwner);
    }
    public final SGNode getFocusOwner() {
        return focusOwner;
    }

    
    /*
     * Rendering code below...
     */
    
    private void render(SGNode node, Rectangle dirtyRegion, Graphics2D g,
                        AffineTransform origXform)
    {
        if (!node.isVisible()) {
            return;
        }
        
        if (incrementalRepaintOpt) {
            // check to see whether we need to render this node (including
            // any children) at all
            Rectangle2D bounds = node.getTransformedBoundsRelativeToRoot();
            if (bounds != null && bounds.intersects(dirtyRegion)) {
                // save the most recently painted bounds in the node, which
                // will be used later when accumulating dirty regions
                node.setLastPaintedBounds(bounds);
            } else {
                // no need to render this node (or any children)
                return;
            }
        }

        if (node instanceof SGLeaf) {
            SGLeaf leaf = (SGLeaf)node;
            Graphics2D gLeaf = (Graphics2D)g.create();
            leaf.paint(gLeaf);
            if (debugBounds) {
                Rectangle leafBounds = node.getBounds(origXform).getBounds();
                g.setTransform(new AffineTransform());
                g.setColor(Color.RED);
                g.drawRect(leafBounds.x, leafBounds.y,
                           leafBounds.width-1,
                           leafBounds.height-1);
                g.setTransform(origXform);
            }
        } else if (node instanceof SGFilter) {
            SGFilter filter = (SGFilter)node;
            SGNode child = filter.getChild();
            if (child != null) {
                Graphics2D gOrig = (Graphics2D)g.create();
                AffineTransform curXform = origXform;
                if (filter instanceof SGTransform) {
                    curXform = ((SGTransform) filter).createAffineTransform();
                    gOrig.transform(curXform);
                    curXform.preConcatenate(origXform);
                }
                if (filter.canSkipRendering()) {
                    if (!filter.canSkipChildren()) {
                        render(child, dirtyRegion, gOrig, curXform);
                    }
                } else {
                    int sourceType = filter.needsSourceContent();
                    if (sourceType == SGFilter.NONE) {
                        filter.setupRenderGraphics(gOrig);
                        render(child, dirtyRegion, gOrig, curXform);
                    } else if (sourceType == SGFilter.CACHED) {
                        filter.renderFromCache(gOrig);
                    } else {
                        Image xformImage = null;
                        Rectangle xformBounds = null;
                        Image unxformImage = null;
                        Rectangle unxformBounds = child.getBounds().getBounds();
                        if (unxformBounds.isEmpty()) {
                            // nothing to render
                            return;
                        }
                        GraphicsConfiguration gc = getGraphicsConfiguration();
                        if ((sourceType & SGFilter.TRANSFORMED) != 0) {
                            xformBounds = child.getBounds(curXform).getBounds();
                            int nodeX = xformBounds.x;
                            int nodeY = xformBounds.y;
                            int nodeW = xformBounds.width;
                            int nodeH = xformBounds.height;
                            // TODO: image should be constrained to the size of the clip
                            if (filter instanceof SGRenderCache) {
                                // SGRenderCache will hold onto the image
                                // for some time, so create a fresh one
                                // (don't use the pool)
                                xformImage = Effect.createCompatibleImage(gc, nodeW, nodeH);
                            } else {
                                xformImage = Effect.getCompatibleImage(gc, nodeW, nodeH);
                            }
                            
                            Graphics2D gFilter =
                                (Graphics2D)xformImage.getGraphics();
                            AffineTransform filterXform =
                                AffineTransform.getTranslateInstance(-nodeX, -nodeY);
                            filterXform.concatenate(curXform);
                            gFilter.setTransform(filterXform);

                            filter.setupRenderGraphics(gFilter);
                            render(child, dirtyRegion, gFilter, filterXform);

                            gOrig.setTransform(AffineTransform.getTranslateInstance(nodeX, nodeY));
                        }
                        if ((sourceType & SGFilter.UNTRANSFORMED) != 0) {
                            if (xformImage != null && curXform.isIdentity()) {
                                // in this case there will be no difference
                                // between xformImage and unxformImage; we
                                // can reuse xformImage instead of creating
                                // what would essentially be a duplicate image
                                unxformImage = xformImage;
                            } else {
                                int nodeX = unxformBounds.x;
                                int nodeY = unxformBounds.y;
                                int nodeW = unxformBounds.width;
                                int nodeH = unxformBounds.height;
                                unxformImage = Effect.getCompatibleImage(gc, nodeW, nodeH);

                                Graphics2D gFilter =
                                    (Graphics2D)unxformImage.getGraphics();
                                AffineTransform filterXform =
                                    AffineTransform.getTranslateInstance(-nodeX, -nodeY);
                                gFilter.setTransform(filterXform);

                                filter.setupRenderGraphics(gFilter);
                                render(child, dirtyRegion, gFilter, filterXform);

                                // TODO: we have very little support for
                                // untransformed source effects at this time;
                                // this needs to be fixed asap...
                                //gOrig.setTransform(AffineTransform.getTranslateInstance(nodeX, nodeY));
                            }
                        }
                        
                        SGSourceContent sourceContent =
                            new SGSourceContent(curXform,
                                                unxformImage, unxformBounds,
                                                xformImage, xformBounds);

                        filter.renderFinalImage(gOrig, sourceContent);
                        
                        if (unxformImage != null) {
                            Effect.releaseCompatibleImage(gc, unxformImage);
                        }
                        if (xformImage != null && xformImage != unxformImage) {
                            Effect.releaseCompatibleImage(gc, xformImage);
                        }
                    }
                }
            }
        } else if (node instanceof SGParent) {
            for (SGNode child : ((SGParent)node).getChildren()) {
                render(child, dirtyRegion, g, origXform);
            }
        }
    }

    private Rectangle dmgrect;

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle dirtyRegion = g.getClipBounds();
        if (dirtyRegion == null) {
            dirtyRegion = new Rectangle(0, 0, getWidth(), getHeight());
        }

        if (!dirtyRegion.isEmpty()) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (isOpaque()) {
                // fill in the background
                g2.setColor(getBackground());
                g2.fill(dirtyRegion);
            }
            if (getSceneGroup() != null) {
                // render all areas of the scene that intersect the dirtyRegion
                render(getSceneGroup(), dirtyRegion, g2, g2.getTransform());
            }
            g2.dispose();
        }
        if (dmgrect != null) {
            Graphics g2 = g.create();
            g2.setXORMode(getBackground());
            g2.setColor(Color.red);
            g2.fillRect(dmgrect.x, dmgrect.y, dmgrect.width, dmgrect.height);
            g2.dispose();
        }
    }
    
    /**
     * Repaints (at minimum) the dirty regions of this panel.
     *
     * @param immediately if true, use paintImmediately() to paint
     *     the dirty regions (useful for "top-level" JSGPanels that are
     *     driven by the master timeline); otherwise, use repaint() (useful
     *     for "embedded" JSGPanels)
     */
    final void repaintDirtyRegions(boolean immediately) {
        if (getSceneGroup() != null) {
            if (getSceneGroup().isDirty() 
                    || SGNodeEventDispatcher.hasPendingEvents()) {
                // process bounds notification events prior to painting
                SGNodeEventDispatcher.dispatchPendingNodeEvents();

                if (!isPreferredSizeSet()) {
                    // if the preferred size hasn't been explicitly set
                    // and there has been a change in the scene bounds,
                    // we may need to revalidate; here we cache the most
                    // recently checked preferred size and compare it to
                    // the current, to avoid repeated calls to revalidate()
                    Dimension d = getPreferredSize();
                    if (!d.equals(validatedPreferredSize)) {
                        validatedPreferredSize = d;
                        revalidate();
                    }
                }

                Rectangle clip = new Rectangle(0, 0, getWidth(), getHeight());
                Rectangle dirtyRegion;
                if (incrementalRepaintOpt && !sceneIsNew) {
                    // walk the entire scene and build the "master"
                    // dirty region
                    dirtyRegion = accumulateDirtyRegions(clip);
                    if (hiliteDirty) {
                        Rectangle olddmg = dmgrect;
                        if (dirtyRegion != null) {
                            if (olddmg == null) {
                                olddmg = dirtyRegion;
                            } else {
                                olddmg.add(dirtyRegion);
                            }
                        }
                        dmgrect = dirtyRegion;
                        dirtyRegion = olddmg;
                    }
                } else {
                    dirtyRegion = clip;
                    sceneIsNew = false;
                }
                if (dirtyRegion != null) {
                    if (immediately) {
                        paintImmediately(dirtyRegion);
                    } else {
                        repaint(dirtyRegion);
                    }
                    if (fpsData != null) {
                        fpsData.nextFrame();
                    }
                }
                clearDirty();
            }
        }
    }
    
    
    /*
     * Dirty region management below...
     */
    
    /**
     * Notifies this JSGPanel that the scene contained within has been
     * made dirty.  This is mainly useful for JSGPanels that are embedded
     * in an SGComponent so that this JSGPanel's dirty region can be properly
     * reported as part of the painting process of that SGComponent.
     */
    final void markDirty() {
        JSGPanelRepainter.getJSGPanelRepainter().addDirtyPanel(this);
    }
    
    /**
     * Clears the dirty state of this JSGPanel as well as that of the scene
     * contained within.
     */
    void clearDirty() {
        if (getSceneGroup() != null) {
            getSceneGroup().clearDirty();
        }
    }
    
    private Rectangle2D accumulateDirty(SGNode node, Rectangle2D r,
                                        Rectangle2D clip)
    {
        if (!node.isDirty()) {
            return r;
        }

        boolean accumulateNodeBounds = false;
        boolean accumulateChildBounds = false;
        
        if (node instanceof SGLeaf) {
            accumulateNodeBounds = true;
        }
        else if (node instanceof SGParent) {
            int dirtyState = node.getDirtyState();
            if (((dirtyState & SGNode.DIRTY_BOUNDS) != 0) ||
                ((dirtyState & SGNode.DIRTY_VISUAL) != 0))
            {
                /*
                 * The group's overall bounds and/or visual state have changed;
                 * delegate to SGNode.accumulateDirty() just like we would
                 * for any leaf node.
                 *
                 * Since SGNode.accumulateDirty() will accurately capture
                 * the overall "former" bounds state, as well as the overall
                 * "updated" bounds state (including any child bounds changes)
                 * it is unnecessary to accumulate for its individual
                 * descendents as we do in the CHILDREN_BOUNDS and
                 * CHILDREN_VISUAL cases below.
                 */
                accumulateNodeBounds = true;
            } else if ((dirtyState & SGNode.DIRTY_CHILDREN_BOUNDS) != 0) {
                /*
                 * There's been a change in the bounds of one or more the
                 * group's descendents; just accumulate dirty children on an
                 * individual basis.  This is less heavyhanded than
                 * accumulating the entire bounds of the group, as we do above,
                 * since it may just be that a single child node has changed
                 * position).
                 *
                 * Note that for this case we can't do the group/clip
                 * intersection optimization as we do for the CHILDREN_VISUAL
                 * case below.  This is because a child node may have changed
                 * its position from a location outside the current
                 * accumulated transformed bounds of the group, and we may
                 * need to repaint the "former" bounds of that child node
                 * (this is ultimately handled by SGNode.accumulateDirty()).
                 * 
                 * Also note that this optimization cannot be applied to
                 * certain SGFilter implementations that paint outside the
                 * child bounds.  In those cases, we simply accumulate the
                 * entire SGFilter bounds, similar to what we do in the
                 * DIRTY_BOUNDS/VISUAL case above.
                 */
                if (node instanceof SGFilter &&
                    ((SGFilter)node).canExpandBounds())
                {
                    accumulateNodeBounds = true;
                } else {
                    accumulateChildBounds = true;
                }
            } else if ((dirtyState & SGNode.DIRTY_CHILDREN_VISUAL) != 0) {
                /*
                 * There's been a visual change in one or more of the
                 * group's descendents; just accumulate dirty children
                 * on an individual basis.
                 *
                 * Optimization: only look at the group's children
                 * if the group's bounding box intersects the clip region.
                 * Looking at only the current accumulated transformed bounds
                 * of the group is safe here because we know at this point that
                 * no children have changed their bounds, so we don't need to
                 * worry about repainting "former" bounds of any children.
                 */
                Rectangle2D bounds = node.getTransformedBoundsRelativeToRoot();
                if (bounds.intersects(clip)) {
                    accumulateChildBounds = true;
                }
            }
        }

        if (accumulateNodeBounds) {
            r = node.accumulateDirty(r);
        } else if (accumulateChildBounds) {
            for (SGNode child : ((SGParent) node).getChildren()) {
                r = accumulateDirty(child, r, clip);
            }
        }
        
        return r;
    }
    
    private Rectangle accumulateDirtyRegions(Rectangle clip) {
        assert (getSceneGroup() != null);
        
        // walk down the tree and accumulate dirty regions
        Rectangle2D dirtyRegion = accumulateDirty(getSceneGroup(), null, clip);
        if (dirtyRegion == null || dirtyRegion.isEmpty()) {
            return null;
        } else {
            // expand the bounds just slightly, since a fractional
            // bounding box will still require a complete repaint on
            // pixel boundaries
            return dirtyRegion.getBounds();
        }
    }

    
    /*
     * FPS overlay stuff below...
     */
    
    private static class FPSData extends SGMouseAdapter {
        public SGNode scene;
        public SGNode origScene;
        public SGGroup fpsGroup;
        public SGText fpsText;
        
        long prevMillis;
        
        public FPSData(SGNode scene) {
            SGShape bg = new SGShape();
            bg.setShape(new Rectangle(0, 0, 60, 10));
            bg.setMode(SGShape.Mode.FILL);
            bg.setFillPaint(Color.yellow);

            this.fpsText = new SGText();
            fpsText.setMode(SGText.Mode.FILL);
            fpsText.setFillPaint(Color.black);
            fpsText.setFont(new Font("Serif", Font.PLAIN, 10));
            fpsText.setLocation(new Point(5, 8));
            fpsText.setText("? FPS");

            this.fpsGroup = new SGGroup();
            fpsGroup.add(bg);
            fpsGroup.add(fpsText);
            fpsGroup.setVisible(defaultShowFPS);
            fpsGroup.addMouseListener(this);

            SGGroup g = new SGGroup();
            g.add(scene);
            g.add(fpsGroup);
            g.addMouseListener(this);

            this.origScene = scene;
            this.scene = g;
            this.prevMillis = System.currentTimeMillis();
        }

        static final int enableMask =
                (MouseEvent.CTRL_MASK | MouseEvent.ALT_MASK);

        @Override
        public void mouseClicked(MouseEvent e, SGNode node) {
            if (node == fpsGroup) {
                fpsGroup.setVisible(false);
                e.consume();
            } else {
                if ((e.getModifiers() & enableMask) == enableMask) {
                    fpsGroup.setVisible(!fpsGroup.isVisible());
                    e.consume();
                }
            }
        }

        public void nextFrame() {
            if (fpsGroup.isVisible()) {
                long now = System.currentTimeMillis();
                float fps = 1000f/(now-prevMillis);
                fps = Math.round(fps*100)/100f;
                fpsText.setText(fps+" FPS");
                prevMillis = now;
            }
        }
    }

    
    /*
     * TODO: these are just temporary stubs for the FX port...
     */
    
    public javax.swing.Icon toIcon() {
        return null;
    }
    
    public java.awt.image.BufferedImage getIconImage() {
        return null;
    }
}
