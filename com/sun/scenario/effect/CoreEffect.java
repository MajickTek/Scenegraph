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
import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;

/**
 * Package-private base class for built-in effects, i.e., those that are
 * backed by an EffectPeer implementation.
 * 
 * @author Chris Campbell
 */
abstract class CoreEffect extends Effect {
    
    private String peerKey;
    private int peerCount = -1;
    private GraphicsConfiguration cachedConfig;
    private EffectPeer cachedPeer;
    
    CoreEffect() {
        super();
    }
    
    CoreEffect(Effect input) {
        super(input);
    }
    
    CoreEffect(Effect input1, Effect input2) {
        super(input1, input2);
    }
    
    final void updatePeerKey(String key) {
        updatePeerKey(key, -1);
    }
    
    final void updatePeerKey(String key, int unrollCount) {
        this.peerKey = key;
        this.peerCount = unrollCount;
        this.cachedPeer = null;
    }
    
    final EffectPeer getPeer(GraphicsConfiguration config) {
        if (config != cachedConfig || cachedPeer == null) {
            cachedPeer = EffectPeer.getInstance(config, peerKey, peerCount);
            cachedConfig = config;
        }
        return cachedPeer;
    }
    
    private ImageData getInputData(GraphicsConfiguration config,
                                   boolean transformed, int inputIndex)
    {
        Effect input = getInputs().get(inputIndex);
        Image img = input.filter(config, transformed);
        Rectangle bounds = transformed ?
            input.getTransformedBounds().getBounds() :
            input.getBounds().getBounds();
        bounds.setLocation(0, 0);
        return new ImageData(img, bounds);
    }
    
    /**
     * Convenience method that is equivalent to calling:
     * <pre>
     *     filterInputs(config, false, inputIndices);
     * </pre>
     */
    ImageData filterInputs(GraphicsConfiguration config, int... inputIndices) {
        return filterInputs(config, false, inputIndices);
    }
    
    /**
     * Convenience method that filters the effect input, sends that result
     * through the current peer, and then attempts to release the original
     * input image data.
     */
    ImageData filterInputs(GraphicsConfiguration config,
                           boolean transformed, int... inputIndices)
    {
        ImageData[] inputData = new ImageData[inputIndices.length];
        for (int i = 0; i < inputIndices.length; i++) {
            inputData[i] = getInputData(config, transformed, inputIndices[i]);
        }
        EffectPeer peer = getPeer(config);
        ImageData res = peer.filter(this, inputData);

        // check the type of input; we don't want to release images
        // used by Source or Identity inputs as the caller is responsible
        // for releasing those
        // TODO: we could be more sophisticated about the lifecycle of
        // effect inputs (for example, we should scan the effect tree
        // and see if the result of one is used as an input in multiple
        // places)...
        for (int i = 0; i < inputIndices.length; i++) {
            Effect input = getInputs().get(inputIndices[i]);
            if (!(input instanceof Source) &&
                !(input instanceof Identity))
            {
                releaseCompatibleImage(config, inputData[i].getImage());
            }
        }
        
        return res;
    }

    /**
     * Convenience method that sends the given input data through the
     * current peer, and then attempts to release the input image data.
     */
    ImageData filterData(GraphicsConfiguration config, ImageData... inputs) {
        EffectPeer peer = getPeer(config);
        ImageData res = peer.filter(this, inputs);
        for (ImageData input : inputs) {
            releaseCompatibleImage(config, input.getImage());
        }
        return res;
    }
    
    @Override
    public AccelType getAccelType(GraphicsConfiguration config) {
        EffectPeer peer = getPeer(config);
        return peer.getAccelType();
    }
}
