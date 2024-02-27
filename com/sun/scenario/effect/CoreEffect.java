package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.EffectPeer;
import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.geom.AffineTransform;

abstract class CoreEffect extends FilterEffect {
   private String peerKey;
   private int peerCount = -1;
   private GraphicsConfiguration cachedConfig;
   private EffectPeer cachedPeer;

   CoreEffect() {
   }

   CoreEffect(Effect input) {
      super(input);
   }

   CoreEffect(Effect input1, Effect input2) {
      super(input1, input2);
   }

   final void updatePeerKey(String key) {
      this.updatePeerKey(key, -1);
   }

   final void updatePeerKey(String key, int unrollCount) {
      this.peerKey = key;
      this.peerCount = unrollCount;
      this.cachedPeer = null;
   }

   final EffectPeer getPeer(GraphicsConfiguration config) {
      if (config != this.cachedConfig || this.cachedPeer == null) {
         this.cachedPeer = EffectPeer.getInstance(config, this.peerKey, this.peerCount);
         this.cachedConfig = config;
      }

      return this.cachedPeer;
   }

   public ImageData filterImageDatas(GraphicsConfiguration config, AffineTransform transform, ImageData... inputs) {
      return this.getPeer(config).filter(this, transform, inputs);
   }

   public Effect.AccelType getAccelType(GraphicsConfiguration config) {
      EffectPeer peer = this.getPeer(config);
      return peer.getAccelType();
   }
}
