package com.sun.scenario.effect.light;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class Light {
   private final PropertyChangeSupport pcs;
   private final Type type;
   private Color color;

   Light(Type type) {
      this(type, Color.WHITE);
   }

   Light(Type type, Color color) {
      this.pcs = new PropertyChangeSupport(this);
      if (type == null) {
         throw new InternalError("Light type must be non-null");
      } else {
         this.type = type;
         this.setColor(color);
      }
   }

   public Type getType() {
      return this.type;
   }

   public Color getColor() {
      return this.color;
   }

   public void setColor(Color color) {
      if (color == null) {
         throw new IllegalArgumentException("Color must be non-null");
      } else {
         Color old = this.color;
         this.color = color;
         this.firePropertyChange("color", old, color);
      }
   }

   public abstract float[] getNormalizedLightPosition();

   public void addPropertyChangeListener(PropertyChangeListener listener) {
      this.pcs.addPropertyChangeListener(listener);
   }

   public void removePropertyChangeListener(PropertyChangeListener listener) {
      this.pcs.removePropertyChangeListener(listener);
   }

   void firePropertyChange(String prop, Object oldValue, Object newValue) {
      this.pcs.firePropertyChange(prop, oldValue, newValue);
   }

   public static enum Type {
      DISTANT,
      POINT,
      SPOT;
   }
}
