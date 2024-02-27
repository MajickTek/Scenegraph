package com.sun.scenario.utils;

public class Utils {
   private static float versionAsFloat = -1.0F;
   public static final boolean isAtLeastJava6;
   public static final boolean isJava6u10;
   public static final boolean isAtLeastJava7;

   public static final synchronized float getJavaVersionAsFloat() {
      if (versionAsFloat > 0.0F) {
         return versionAsFloat;
      } else {
         String versionString = System.getProperty("java.version", "1.5.0");
         StringBuffer sb = new StringBuffer();
         int firstDot = versionString.indexOf(".");
         sb.append(versionString.substring(0, firstDot));
         int secondDot = versionString.indexOf(".", firstDot + 1);
         sb.append(versionString.substring(firstDot + 1, secondDot));
         int underscore = versionString.indexOf("_", secondDot + 1);
         int dash;
         if (underscore >= 0) {
            dash = versionString.indexOf("-", underscore + 1);
            if (dash < 0) {
               dash = versionString.length();
            }

            sb.append(versionString.substring(secondDot + 1, underscore)).append(".").append(versionString.substring(underscore + 1, dash));
         } else {
            dash = versionString.indexOf("-", secondDot + 1);
            if (dash < 0) {
               dash = versionString.length();
            }

            sb.append(versionString.substring(secondDot + 1, dash));
         }

         float version = 150.0F;

         try {
            version = Float.parseFloat(sb.toString());
         } catch (NumberFormatException var7) {
         }

         versionAsFloat = version;
         return versionAsFloat;
      }
   }

   static {
      String javaVersionString = System.getProperty("java.version", "1.5.0");
      int dash = javaVersionString.indexOf("-");
      if (dash > 0) {
         javaVersionString = javaVersionString.substring(0, dash);
      }

      if (javaVersionString.compareTo("1.6.0") >= 0) {
         isAtLeastJava6 = true;
         isJava6u10 = javaVersionString.compareTo("1.6.0_10") == 0;
         isAtLeastJava7 = javaVersionString.compareTo("1.7.0") >= 0;
      } else {
         isAtLeastJava7 = false;
         isJava6u10 = false;
         isAtLeastJava6 = false;
      }

   }
}
