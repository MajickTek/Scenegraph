package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.EffectPeer;
import com.sun.scenario.effect.impl.ImageData;
import com.sun.scenario.effect.impl.state.BoxBlurState;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

public class BoxBlur extends CoreEffect {
   // $FF: renamed from: r int
   private int field_1;
   private final BoxBlurState state;

   public BoxBlur() {
      this(1);
   }

   public BoxBlur(int radius) {
      this(radius, DefaultInput);
   }

   public BoxBlur(int radius, Effect input) {
      super(input);
      this.state = this.createState();
      this.setRadius(radius);
   }

   Object getState() {
      return this.state;
   }

   BoxBlurState createState() {
      return new BoxBlurState(this);
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
   }

   public int getRadius() {
      return this.field_1;
   }

   public void setRadius(int radius) {
      if (radius >= 1 && radius <= 64) {
         int old = this.field_1;
         this.field_1 = radius;
         this.state.invalidateDeltas();
         this.firePropertyChange("radius", old, radius);
         this.updatePeer();
      } else {
         throw new IllegalArgumentException("Radius must be in the range [1,64]");
      }
   }

   private void updatePeer() {
      int psize = 4 + this.field_1 - this.field_1 % 4;
      this.updatePeerKey("BoxBlur", psize);
   }

   public ImageData filterImageDatas(GraphicsConfiguration config, AffineTransform transform, ImageData... inputs) {
      EffectPeer peer = this.getPeer(config);
      Rectangle bnd = inputs[0].getBounds();
      Image resImage = Effect.getCompatibleImage(config, bnd.width, bnd.height);
      ImageData resData = new ImageData(config, resImage, bnd);
      this.state.updateDeltas(1.0F / (float)bnd.width, 1.0F / (float)bnd.height);

      for(int i = -this.field_1; i <= this.field_1; ++i) {
         this.state.setRow(i);
         ImageData tmpData = peer.filter(this, transform, inputs[0], resData);
         resData.unref();
         resData = tmpData;
      }

      return resData;
   }

   public boolean operatesInUserSpace() {
      return true;
   }
}
