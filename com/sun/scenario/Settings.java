package com.sun.scenario;

import com.sun.embeddedswing.SwingGlueLayer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

public class Settings {
   private final Map<String, String> settings = new HashMap(5);
   private final PropertyChangeSupport pcs = new PropertyChangeSupport(Settings.class);
   private static final Object SETTINGS_KEY;

   private static synchronized Settings getInstance() {
      Map<Object, Object> contextMap = SwingGlueLayer.getContextMap();
      Settings instance = (Settings)contextMap.get(SETTINGS_KEY);
      if (instance == null) {
         instance = new Settings();
         contextMap.put(SETTINGS_KEY, instance);
      }

      return instance;
   }

   public static void set(String key, String value) {
      getInstance().setImpl(key, value);
   }

   private void setImpl(String key, String value) {
      this.checkKeyArg(key);
      String oldVal = this.getImpl(key);
      this.settings.put(key, value);
      String newVal = value;
      if (value == null) {
         newVal = this.getImpl(key);
      }

      this.pcs.firePropertyChange(key, oldVal, newVal);
   }

   public static String get(String key) {
      return getInstance().getImpl(key);
   }

   private String getImpl(String key) {
      this.checkKeyArg(key);
      String retVal = (String)this.settings.get(key);
      if (retVal == null) {
         try {
            retVal = System.getProperty(key);
         } catch (SecurityException var4) {
         }
      }

      return retVal;
   }

   public static boolean getBoolean(String key) {
      return getInstance().getBooleanImpl(key);
   }

   private boolean getBooleanImpl(String key) {
      String value = this.getImpl(key);
      return "true".equals(value);
   }

   public static boolean getBoolean(String key, boolean defaultVal) {
      return getInstance().getBooleanImpl(key, defaultVal);
   }

   private boolean getBooleanImpl(String key, boolean defaultVal) {
      String value = this.getImpl(key);
      boolean retVal = defaultVal;
      if (value != null) {
         if ("false".equals(value)) {
            retVal = false;
         } else if ("true".equals(value)) {
            retVal = true;
         }
      }

      return retVal;
   }

   public static int getInt(String key, int defaultVal) {
      return getInstance().getIntImpl(key, defaultVal);
   }

   private int getIntImpl(String key, int defaultVal) {
      String value = this.getImpl(key);
      int retVal = defaultVal;

      try {
         retVal = Integer.parseInt(value);
      } catch (NumberFormatException var6) {
      }

      return retVal;
   }

   public static void addPropertyChangeListener(String key, PropertyChangeListener pcl) {
      getInstance().addPropertyChangeListenerImpl(key, pcl);
   }

   private void addPropertyChangeListenerImpl(String key, PropertyChangeListener pcl) {
      this.checkKeyArg(key);
      this.pcs.addPropertyChangeListener(key, pcl);
   }

   public static void removePropertyChangeListener(PropertyChangeListener pcl) {
      getInstance().removePropertyChangeListenerImpl(pcl);
   }

   private void removePropertyChangeListenerImpl(PropertyChangeListener pcl) {
      this.pcs.removePropertyChangeListener(pcl);
   }

   private void checkKeyArg(String key) {
      if (null == key || "".equals(key)) {
         throw new IllegalArgumentException("null key not allowed");
      }
   }

   private Settings() {
   }

   static {
      try {
         Class var0 = Class.forName("com.sun.scenario.animation.MasterTimer");
      } catch (ClassNotFoundException var1) {
         var1.printStackTrace();
      }

      SETTINGS_KEY = new StringBuilder("SettingsKey");
   }
}
