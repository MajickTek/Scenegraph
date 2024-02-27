package com.sun.scenario.animation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class BeanProperty<T> implements Property<T> {
   private Object object;
   private String propertyName;
   private Method propertySetter;
   private Method propertyGetter;

   public BeanProperty(Object object, String propertyName) {
      this.object = object;
      this.propertyName = propertyName;

      try {
         this.setupMethodInfo();
      } catch (NoSuchMethodException var4) {
         throw new IllegalArgumentException("Bad property name (" + propertyName + "): could not find " + "an appropriate setter or getter method for that property");
      }
   }

   private void setupMethodInfo() throws NoSuchMethodException {
      try {
         String firstChar = this.propertyName.substring(0, 1);
         String remainder = this.propertyName.substring(1);
         String propertySetterName = "set" + firstChar.toUpperCase() + remainder;
         PropertyDescriptor prop = new PropertyDescriptor(this.propertyName, this.object.getClass(), (String)null, propertySetterName);
         this.propertySetter = prop.getWriteMethod();

         try {
            String propertyGetterName = "get" + firstChar.toUpperCase() + remainder;
            prop = new PropertyDescriptor(this.propertyName, this.object.getClass(), propertyGetterName, (String)null);
            this.propertyGetter = prop.getReadMethod();
         } catch (Exception var7) {
            String propertyGetterName = "is" + firstChar.toUpperCase() + remainder;
            prop = new PropertyDescriptor(this.propertyName, this.object.getClass(), propertyGetterName, (String)null);
            this.propertyGetter = prop.getReadMethod();
         }

      } catch (Exception var8) {
         throw new NoSuchMethodException("Cannot find property methods: " + var8);
      }
   }

   public void setValue(T value) {
      try {
         this.propertySetter.invoke(this.object, value);
      } catch (Exception var3) {
         System.out.println("Problem invoking method " + this.propertySetter + " in object " + this.object + " in setValue" + var3);
      }

   }

   public T getValue() {
      try {
         return this.propertyGetter.invoke(this.object);
      } catch (Exception var2) {
         System.out.println("Problem invoking method " + this.propertySetter + " in object " + this.object + " in setValue" + var2);
         return null;
      }
   }
}
