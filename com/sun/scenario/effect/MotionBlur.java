package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import java.awt.GraphicsConfiguration;
import java.awt.geom.AffineTransform;

public class MotionBlur extends AbstractGaussian {
   private float angle;

   public MotionBlur() {
      this(10.0F, 0.0F, DefaultInput);
   }

   public MotionBlur(float radius, float angle) {
      this(radius, angle, DefaultInput);
   }

   public MotionBlur(float radius, float angle, Effect input) {
      super("MotionBlur", radius, input);
      this.angle = angle;
   }

   public float getAngle() {
      return this.angle;
   }

   public void setAngle(float angle) {
      float old = this.angle;
      this.angle = angle;
      this.firePropertyChange("angle", old, angle);
   }

   public ImageData filterImageDatas(GraphicsConfiguration config, AffineTransform transform, ImageData... inputs) {
      return this.getPeer(config).filter(this, transform, inputs);
   }
}
