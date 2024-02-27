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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InvocationEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.sun.scenario.scenegraph.SGComponent.SGShell;
import com.sun.scenario.scenegraph.SwingGlueLayer.DelayedRunnable;

/**
 * This is required for embedded components to get mouse events as if they are
 * part of regular swing hierarchy.
 * 
 * In addition to that this event queue is used to perform animation at a
 * regular time intervals.
 * 
 * @author Igor Kushnirskiy
 */
class SGEventQueue extends EventQueue {
    private static final Logger logger = 
        Logger.getLogger(SGEventQueue.class.getName());
    private static final AffineTransform IDENTITY_TRANSFORM = 
        new AffineTransform();
    private AffineTransform lastTransform;
    List<SGComponent> lastsgComponents;
    private final ActionListener actionListener =
        new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doPulse();
            }
    };
    private static AffineTransform getDeepestSGComponentTransformInverse(
            Point2D point, Component parent, List<SGComponent> sgComponents) {
        int[] offset = {0, 0};
        JSGPanel jsgPanel = SGSwingUtilities.getFirstComponentOfClassAt(
                JSGPanel.class, parent, 
                (int) point.getX(), (int) point.getY(), offset);
        int offsetX = offset[0];
        int offsetY = offset[1];
        if (jsgPanel != null) {
            SGNode node = jsgPanel.getSceneGroup();
            if (node != null) {
                Point2D jsgPanelPoint = new Point2D.Float(
                        (float) (point.getX() - offsetX), 
                        (float) (point.getY() - offsetY));
                List<SGNode> list = node.pick(jsgPanelPoint);
                for (SGNode leaf : list) {
                    Component embeddedComponent = null;
                    if (leaf instanceof SGComponent 
                            && (embeddedComponent = ((SGComponent) leaf)
                                    .getComponent()) != null) {
                        AffineTransform leafTransformInverse = null;
                        try {
                            leafTransformInverse = 
                                leaf.getCumulativeTransform().createInverse();
                        } catch (NoninvertibleTransformException exc) {
                            /* this should not happen
                             * using identity just in case  
                             */
                            leafTransformInverse = IDENTITY_TRANSFORM;
                            String fmt = 
                                "couldn't dispatch %s for %s, bad transform %s";
                            logger.warning(exc, fmt, jsgPanelPoint, leaf, 
                                    leaf.getCumulativeTransform());
                        }
                        Point2D leafPoint = new Point2D.Float();
                        leafTransformInverse.transform(jsgPanelPoint, leafPoint);
                        sgComponents.add((SGComponent) leaf);
                        AffineTransform embeddedTransformInverse = 
                            getDeepestSGComponentTransformInverse(leafPoint, 
                                    embeddedComponent, sgComponents);
                        
                        AffineTransform resultTransform = new AffineTransform();
                        resultTransform.concatenate(embeddedTransformInverse);
                        resultTransform.concatenate(leafTransformInverse);
                        resultTransform.translate(-offsetX, -offsetY);
                        return resultTransform;
                    }
                }
            }
        }
        return IDENTITY_TRANSFORM;
    }
    
    //copied from LightWeightDispatcher.isMouseGrab
    /* This method effectively returns whether or not a mouse button was down
     * just BEFORE the event happened.  A better method name might be
     * wasAMouseButtonDownBeforeThisEvent().
     */
    private static boolean isMouseGrab(MouseEvent e) {
        int modifiers = e.getModifiersEx();
        
        if(e.getID() == MouseEvent.MOUSE_PRESSED 
                || e.getID() == MouseEvent.MOUSE_RELEASED) {
            switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                modifiers ^= InputEvent.BUTTON1_DOWN_MASK;
                break;
            case MouseEvent.BUTTON2:
                modifiers ^= InputEvent.BUTTON2_DOWN_MASK;
                break;
            case MouseEvent.BUTTON3:
                modifiers ^= InputEvent.BUTTON3_DOWN_MASK;
                break;
            }
        }
        /* modifiers now as just before event */ 
        return ((modifiers & (InputEvent.BUTTON1_DOWN_MASK
                              | InputEvent.BUTTON2_DOWN_MASK
                              | InputEvent.BUTTON3_DOWN_MASK)) != 0);
    }
    @Override
    protected void dispatchEvent(AWTEvent event) {
        if (event instanceof MouseEvent) {
            final int STATE_GRAB = 1;
            final int STATE_RETARGET = 2;
            MouseEvent mouseEvent = (MouseEvent) event;
            Component source = (Component) event.getSource();
            AffineTransform transform = null;
            List<SGComponent> sgComponents = new LinkedList<SGComponent>();
            Point2D mousePoint = 
                new Point2D.Float(mouseEvent.getX(), mouseEvent.getY());
            int state = 0;
            if (!isMouseGrab(mouseEvent) 
                    && mouseEvent.getID() != MouseEvent.MOUSE_CLICKED) {
                state = STATE_GRAB;
            } else if (lastTransform != null){
                //events need to be forwarded to the same component
                switch (mouseEvent.getID()) {
                case MouseEvent.MOUSE_PRESSED:
                case MouseEvent.MOUSE_RELEASED:
                case MouseEvent.MOUSE_MOVED:
                case MouseEvent.MOUSE_DRAGGED:
                    state = STATE_RETARGET;
                    break;
                }
            }
            if (state == STATE_RETARGET) {
                transform = lastTransform;
                sgComponents = lastsgComponents;
            } else {
                transform = getDeepestSGComponentTransformInverse(
                        mousePoint, source, sgComponents);
                if (state == STATE_GRAB) {
                    lastTransform = transform;
                    lastsgComponents = sgComponents;
                }
            }
            if (transform != IDENTITY_TRANSFORM && ! sgComponents.isEmpty()) {
                Point2D tmpPoint2D = transform.transform(mousePoint, null);
                Point translatedPoint = 
                    new Point((int) tmpPoint2D.getX(), (int) tmpPoint2D.getY());
                JComponent bottomContainer = 
                    sgComponents.get(sgComponents.size() - 1).getContainer();
                JComponent topContainer = 
                    sgComponents.get(0).getContainer();
                JSGPanel topPanel = 
                    (JSGPanel) SwingUtilities.getAncestorOfClass(
                            JSGPanel.class, topContainer);
                //bottom container's top left corner in the source coordinates
                Point bottomContainerTopLeftPoint = 
                    SwingUtilities.convertPoint(
                            bottomContainer, new Point(0,0), source);
                translatedPoint.translate(
                        bottomContainerTopLeftPoint.x, 
                        bottomContainerTopLeftPoint.y);
                Point translatedPointInTopPanel = SwingUtilities.convertPoint(
                        source, translatedPoint, topPanel);
                Rectangle visibleRectangle = topPanel.getVisibleRect();
                if (! visibleRectangle.contains(translatedPointInTopPanel)) {
                    /*
                     * We need to ensure translated mouse coordinates 
                     * are in visible rectangle of the topmost JSGPanel
                     */
                    int deltaX = 
                        visibleRectangle.x - translatedPointInTopPanel.x; 
                    int deltaY =  
                        visibleRectangle.y - translatedPointInTopPanel.y;
                    topContainer.setLocation(
                            topContainer.getX() + deltaX,
                            topContainer.getY() + deltaY);
                    translatedPoint.translate(deltaX, deltaY);
                }
                
                mouseEvent.translatePoint(
                        (int) (translatedPoint.getX() - mousePoint.getX()),
                        (int) (translatedPoint.getY() - mousePoint.getY()));
                
                for (SGComponent sgComponent : sgComponents) {
                    SGShell shell = 
                        (SGShell) sgComponent.getComponent().getParent();
                    shell.setContains(true);
                }
               
                super.dispatchEvent(mouseEvent);
                
                for (SGComponent sgComponent : sgComponents) {
                    Component component = sgComponent.getComponent();
                    if (component != null) {
                        //component might be removed as a result of the event
                        SGShell shell = (SGShell) component.getParent();
                        shell.setContains(false);
                    }
                }
                return;
            }
        }
        super.dispatchEvent(event);
    }
    
    //time based animation
    private final InvocationEvent pulseEvent = new InvocationEvent(this, 
            new Runnable() {
                public void run() {
                    doPulse();
                } 
    });
    
    private AWTEvent postponed = null;
    private boolean waitForNonPulseEvent = true;
    private static final long RESPONSIVE_THRESHOLD = 1000 / 30;
    
    private final Object lock = new Object();
    //flag to disable alarmTimer start during the pulse
    //can be accessed with the lock held
    private boolean disableTimer = false;
    
    //can be accessed with the lock held
    private Timer alarmTimer = null;
    
    //can be accessed with the lock held
    private DelayedRunnable animationRunnable = null;
    
    private static long getWhen(AWTEvent e) {
        long when = Long.MIN_VALUE;
        if (e instanceof InputEvent) {
            InputEvent ie = (InputEvent)e;
            when = ie.getWhen(); 
        } else if (e instanceof InputMethodEvent) {
            InputMethodEvent ime = (InputMethodEvent)e;
            when = ime.getWhen(); 
        } else if (e instanceof ActionEvent) {
            ActionEvent ae = (ActionEvent)e;
            when = ae.getWhen(); 
        } else if (e instanceof InvocationEvent) {
            InvocationEvent ie = (InvocationEvent)e;
            when = ie.getWhen(); 
        }
        return when;
    }
    
    @Override
    public AWTEvent getNextEvent() throws InterruptedException {
        AWTEvent nextEvent = null;
        if (postponed != null) {
            nextEvent = postponed;
            postponed = null;
        } else {
            AWTEvent superNextEvent = null;
            do {
                DelayedRunnable delayedRunnable = null;
                synchronized (lock) {
                    delayedRunnable = animationRunnable;
                }
                if (delayedRunnable != null
                        && ! waitForNonPulseEvent 
                        && delayedRunnable.getDelay() <= 0) {
                    nextEvent = pulseEvent;
                    postponed = superNextEvent;
                    waitForNonPulseEvent = true;
                } else if (superNextEvent != null){
                    nextEvent = superNextEvent;
                } else {
                    /*
                     * super.getNextEvent is called at least once between
                     * pulses. We need this to get toolkit's events pushed to
                     * the EventQueue.
                     */
                    superNextEvent = super.getNextEvent();
                    long now = System.currentTimeMillis();
                    /*
                     * Do not return pulseEvent until we catch up with posted 
                     * events. 
                     */
                    waitForNonPulseEvent = 
                        (now - getWhen(superNextEvent) > RESPONSIVE_THRESHOLD);
                }
            } while (nextEvent == null);
        }
        
        return nextEvent;
    }
    
    private void doPulse() {
        synchronized (lock) {
            disableTimer = true;
            stopPulseAlarm();
            if (animationRunnable != null) {
                animationRunnable.run();
            }
            disableTimer = false;
            updatePulseAlarm();
        }
    }
    
    //this method may be called off EDT
    void setAnimationRunnable(DelayedRunnable animationRunnable) {
        synchronized (lock) {
            this.animationRunnable = animationRunnable;
            updatePulseAlarm();
        }
    }
    
    //this method may be called off EDT
    private void updatePulseAlarm() {
        synchronized (lock) {
            if (disableTimer) {
                return;
            }
            if (animationRunnable != null) {
                if (alarmTimer == null
                        || ! alarmTimer.isRunning()) {
                    int timerDelay = (int) animationRunnable.getDelay();
                    alarmTimer = new Timer(timerDelay, actionListener);
                    alarmTimer.setRepeats(false);
                    alarmTimer.start();
                }
            } else {
                stopPulseAlarm();
            }
        }
    }
    
    //this method may be called off EDT
    private void stopPulseAlarm() {
        synchronized (lock) {
            if (alarmTimer != null) {
                alarmTimer.stop();
                alarmTimer = null;
            }
        }
    }
}
