package com.sun.scenario.scenegraph;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

abstract class Logger {
   private static final Map<String, Logger> loggerMap = new HashMap();
   private static Constructor loggerCtor = null;

   private static Logger createLogger(String name) {
      try {
         if (loggerCtor != null) {
            return (Logger)loggerCtor.newInstance(name);
         } else {
            try {
               loggerCtor = UtilLogger.class.getConstructor(String.class);
               return (Logger)loggerCtor.newInstance(name);
            } catch (Exception var2) {
               loggerCtor = DefaultLogger.class.getConstructor(String.class);
               return (Logger)loggerCtor.newInstance(name);
            }
         }
      } catch (Exception var3) {
         throw new Error(var3);
      }
   }

   public static synchronized Logger getLogger(String name) {
      Logger logger = (Logger)loggerMap.get(name);
      if (logger == null) {
         logger = createLogger(name);
         loggerMap.put(name, logger);
      }

      return logger;
   }

   public abstract boolean isEnabled(Level var1);

   public abstract void setEnabled(Level var1);

   public abstract void message(String var1, Object... var2);

   public abstract void warning(Exception var1, String var2, Object... var3);

   public abstract void error(Exception var1, String var2, Object... var3);

   public final void warning(String format, Object... args) {
      this.warning((Exception)null, format, args);
   }

   public final void error(String format, Object... args) {
      this.error((Exception)null, format, args);
   }

   private static class UtilLogger extends Logger {
      private final java.util.logging.Logger logger;

      public UtilLogger(String name) {
         this.logger = java.util.logging.Logger.getLogger(name);
      }

      private java.util.logging.Level convertLevel(Level l) {
         switch (l) {
            case ERROR:
               return java.util.logging.Level.SEVERE;
            case WARNING:
               return java.util.logging.Level.WARNING;
            default:
               return java.util.logging.Level.ALL;
         }
      }

      public final boolean isEnabled(Level l) {
         return this.logger.isLoggable(this.convertLevel(l));
      }

      public final void setEnabled(Level l) {
         this.logger.setLevel(this.convertLevel(l));
      }

      private void log(java.util.logging.Level l, Exception e, String format, Object[] args) {
         this.logger.log(l, String.format(format, args), e);
      }

      public void message(String format, Object... args) {
         this.log(java.util.logging.Level.INFO, (Exception)null, format, args);
      }

      public void warning(Exception e, String format, Object... args) {
         this.log(java.util.logging.Level.WARNING, (Exception)null, format, args);
      }

      public void error(Exception e, String format, Object... args) {
         this.log(java.util.logging.Level.SEVERE, (Exception)null, format, args);
      }
   }

   private static class DefaultLogger extends Logger {
      private Level level;

      public final boolean isEnabled(Level l) {
         return l.compareTo(this.level) >= 0;
      }

      public final void setEnabled(Level l) {
         this.level = l;
      }

      public DefaultLogger(String name) {
         this.level = Logger.Level.WARNING;
      }

      private void log(Level l, PrintStream p, Exception e, String format, Object[] args) {
         if (l.compareTo(this.level) >= 0) {
            if (e != null) {
               e.printStackTrace(p);
            }

            p.print(format);
            Object[] arr$ = args;
            int len$ = args.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Object a = arr$[i$];
               p.print(" " + a);
            }

            p.println();
         }

      }

      public final void message(String format, Object... args) {
         this.log(Logger.Level.MESSAGE, System.out, (Exception)null, format, args);
      }

      public final void warning(Exception e, String format, Object... args) {
         this.log(Logger.Level.WARNING, System.err, e, format, args);
      }

      public final void error(Exception e, String format, Object... args) {
         this.log(Logger.Level.ERROR, System.err, e, format, args);
      }
   }

   public static enum Level {
      MESSAGE,
      WARNING,
      ERROR;
   }
}
