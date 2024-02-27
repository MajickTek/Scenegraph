package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.EffectPeer;
import com.sun.scenario.effect.impl.ImageData;
import com.sun.scenario.effect.impl.state.AccessHelper;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Effect {
   public static final Effect DefaultInput = null;
   private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
   private final PropertyChangeListener inputListener;
   private final List<Effect> inputs;
   private final List<Effect> unmodifiableInputs;
   private final int maxInputs;

   protected Effect() {
      this.inputs = Collections.emptyList();
      this.unmodifiableInputs = this.inputs;
      this.maxInputs = 0;
      this.inputListener = null;
   }

   protected Effect(Effect input) {
      this.inputs = new ArrayList(1);
      this.unmodifiableInputs = Collections.unmodifiableList(this.inputs);
      this.maxInputs = 1;
      this.inputListener = new InputChangeListener();
      this.setInput(0, input);
   }

   protected Effect(Effect input1, Effect input2) {
      this.inputs = new ArrayList(2);
      this.unmodifiableInputs = Collections.unmodifiableList(this.inputs);
      this.maxInputs = 2;
      this.inputListener = new InputChangeListener();
      this.setInput(0, input1);
      this.setInput(1, input2);
   }

   Object getState() {
      return null;
   }

   public int getNumInputs() {
      return this.inputs.size();
   }

   public final List<Effect> getInputs() {
      return this.unmodifiableInputs;
   }

   protected void setInput(int index, Effect input) {
      if (index >= 0 && index < this.maxInputs) {
         if (index < this.inputs.size()) {
            Effect oldInput = (Effect)this.inputs.get(index);
            if (oldInput != null) {
               oldInput.removePropertyChangeListener(this.inputListener);
            }

            this.inputs.set(index, input);
         } else {
            this.inputs.add(input);
         }

         if (input != null) {
            input.addPropertyChangeListener(this.inputListener);
         }

         this.firePropertyChange("inputs", (Object)null, this.inputs);
      } else {
         throw new IllegalArgumentException("Index must be within allowable range");
      }
   }

   public void render(Graphics2D g, float x, float y, Effect defaultInput) {
      AffineTransform transform = g.getTransform();
      transform.translate((double)x, (double)y);
      if (transform.isIdentity()) {
         transform = null;
      }

      Graphics2D gtmp = (Graphics2D)g.create();
      gtmp.setTransform(new AffineTransform());
      GraphicsConfiguration gc = gtmp.getDeviceConfiguration();
      ImageData res = this.filter(gc, transform, defaultInput);
      Rectangle r = res.getBounds();
      gtmp.drawImage(res.getImage(), r.x, r.y, (ImageObserver)null);
      res.unref();
      gtmp.dispose();
   }

   public Rectangle2D combineBounds(Rectangle2D... inputBounds) {
      Rectangle2D ret = null;
      if (inputBounds.length == 1) {
         ret = inputBounds[0];
      } else {
         Rectangle2D[] arr$ = inputBounds;
         int len$ = inputBounds.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Rectangle2D r = arr$[i$];
            if (r != null && !r.isEmpty()) {
               if (ret == null) {
                  ret = (Rectangle2D)r.clone();
               } else {
                  ((Rectangle2D)ret).add(r);
               }
            }
         }
      }

      if (ret == null) {
         ret = new Rectangle2D.Float();
      }

      return (Rectangle2D)ret;
   }

   public Rectangle getResultBounds(AffineTransform transform, ImageData... inputDatas) {
      int numinputs = inputDatas.length;
      Rectangle2D[] inputBounds = new Rectangle2D[numinputs];

      for(int i = 0; i < numinputs; ++i) {
         inputBounds[i] = inputDatas[i].getBounds();
      }

      return this.combineBounds(inputBounds).getBounds();
   }

   public abstract ImageData filter(GraphicsConfiguration var1, AffineTransform var2, Effect var3);

   protected Rectangle2D transformBounds(AffineTransform tx, Rectangle2D r) {
      if (tx != null && !tx.isIdentity()) {
         if (tx.getType() == 1) {
            Rectangle2D.Float ret = new Rectangle2D.Float();
            ret.setRect((float)(r.getX() + tx.getTranslateX()), (float)(r.getY() + tx.getTranslateY()), (float)r.getWidth(), (float)r.getHeight());
            return ret;
         } else {
            return tx.createTransformedShape(r).getBounds();
         }
      } else {
         return r;
      }
   }

   protected ImageData ensureTransform(GraphicsConfiguration config, ImageData original, AffineTransform transform) {
      if (transform != null && !transform.isIdentity()) {
         Rectangle origBounds = original.getBounds();
         if (transform.getType() == 1) {
            double tx = transform.getTranslateX();
            double ty = transform.getTranslateY();
            int itx = (int)tx;
            int ity = (int)ty;
            if ((double)itx == tx && (double)ity == ty) {
               Rectangle r = new Rectangle(origBounds);
               r.translate(itx, ity);
               ImageData ret = new ImageData(original, r);
               original.unref();
               return ret;
            }
         }

         Rectangle xformBounds = this.transformBounds(transform, origBounds).getBounds();
         Image img = getCompatibleImage(config, xformBounds.width, xformBounds.height);
         Graphics2D g2 = (Graphics2D)img.getGraphics();
         g2.translate(-xformBounds.x, -xformBounds.y);
         g2.transform(transform);
         g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g2.drawImage(original.getImage(), origBounds.x, origBounds.y, (ImageObserver)null);
         g2.dispose();
         original.unref();
         return new ImageData(config, img, xformBounds);
      } else {
         return original;
      }
   }

   public final Rectangle2D getBounds() {
      return this.getBounds((AffineTransform)null, (Effect)null);
   }

   Effect getDefaultedInput(int inputIndex, Effect defaultInput) {
      return getDefaultedInput((Effect)this.inputs.get(inputIndex), defaultInput);
   }

   static Effect getDefaultedInput(Effect listedInput, Effect defaultInput) {
      return listedInput == null ? defaultInput : listedInput;
   }

   public abstract Rectangle2D getBounds(AffineTransform var1, Effect var2);

   public Point2D transform(Point2D p, Effect defaultInput) {
      return p;
   }

   public Point2D untransform(Point2D p, Effect defaultInput) {
      return p;
   }

   public static Image createCompatibleImage(GraphicsConfiguration gc, int w, int h) {
      return EffectPeer.getRenderer(gc).createCompatibleImage(w, h);
   }

   public static Image getCompatibleImage(GraphicsConfiguration gc, int w, int h) {
      return EffectPeer.getRenderer(gc).getCompatibleImage(w, h);
   }

   public static void releaseCompatibleImage(GraphicsConfiguration gc, Image image) {
      EffectPeer.getRenderer(gc).releaseCompatibleImage(image);
   }

   public abstract AccelType getAccelType(GraphicsConfiguration var1);

   public void addPropertyChangeListener(PropertyChangeListener listener) {
      this.pcs.addPropertyChangeListener(listener);
   }

   public void removePropertyChangeListener(PropertyChangeListener listener) {
      this.pcs.removePropertyChangeListener(listener);
   }

   protected void firePropertyChange(String prop, Object oldValue, Object newValue) {
      this.pcs.firePropertyChange(prop, oldValue, newValue);
   }

   static {
      AccessHelper.setStateAccessor(new AccessHelper.StateAccessor() {
         public Object getState(Effect effect) {
            return effect.getState();
         }
      });
   }

   public static enum AccelType {
      INTRINSIC("Intrinsic"),
      NONE("CPU/Java"),
      SIMD("CPU/SIMD"),
      FIXED("CPU/Fixed"),
      OPENGL("OpenGL"),
      DIRECT3D("Direct3D");

      private String text;

      private AccelType(String text) {
         this.text = text;
      }

      public String toString() {
         return this.text;
      }
   }

   private class InputChangeListener implements PropertyChangeListener {
      private InputChangeListener() {
      }

      public void propertyChange(PropertyChangeEvent e) {
         Effect.this.firePropertyChange("inputs", (Object)null, Effect.this.inputs);
      }
   }
}
