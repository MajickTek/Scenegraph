package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import com.sun.scenario.effect.impl.state.ZoomRadialBlurState;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

public class ZoomRadialBlur extends CoreEffect {
   // $FF: renamed from: r int
   private int field_2;
   private float centerX;
   private float centerY;
   private final ZoomRadialBlurState state;

   public ZoomRadialBlur() {
      this(1);
   }

   public ZoomRadialBlur(int radius) {
      this(radius, DefaultInput);
   }

   public ZoomRadialBlur(int radius, Effect input) {
      super(input);
      this.state = new ZoomRadialBlurState(this);
      this.setRadius(radius);
   }

   Object getState() {
      return this.state;
   }

   public final Effect getInput() {
      return (Effect)this.getInputs().get(0);
   }

   public void setInput(Effect input) {
      this.setInput(0, input);
   }

   public int getRadius() {
      return this.field_2;
   }

   public void setRadius(int radius) {
      if (radius >= 1 && radius <= 64) {
         int old = this.field_2;
         this.field_2 = radius;
         this.state.invalidateDeltas();
         this.firePropertyChange("radius", old, radius);
         this.updatePeer();
      } else {
         throw new IllegalArgumentException("Radius must be in the range [1,64]");
      }
   }

   private void updatePeer() {
      int psize = 4 + this.field_2 - this.field_2 % 4;
      this.updatePeerKey("ZoomRadialBlur", psize);
   }

   public float getCenterX() {
      return this.centerX;
   }

   public void setCenterX(float centerX) {
      float old = this.centerX;
      this.centerX = centerX;
      this.firePropertyChange("centerX", old, centerX);
   }

   public float getCenterY() {
      return this.centerY;
   }

   public void setCenterY(float centerY) {
      float old = this.centerY;
      this.centerY = centerY;
      this.firePropertyChange("centerY", old, centerY);
   }

   public ImageData filterImageDatas(GraphicsConfiguration config, AffineTransform transform, ImageData... inputs) {
      Rectangle bnd = inputs[0].getBounds();
      this.state.updateDeltas((float)(1.0 / bnd.getWidth()), (float)(1.0 / bnd.getHeight()));
      return super.filterImageDatas(config, transform, inputs);
   }

   public boolean operatesInUserSpace() {
      return true;
   }
}
