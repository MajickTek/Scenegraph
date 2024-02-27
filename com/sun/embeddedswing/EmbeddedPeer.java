package com.sun.embeddedswing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

public abstract class EmbeddedPeer {
   private static final BasicStroke defaultStroke = new BasicStroke(1.0F);
   private static final RenderingHints defaultHints = new RenderingHints((Map)null);
   private static final boolean havePrinterGraphics;
   private final Component component;
   private JComponent parent;
   private final Shell shell;
   private final JPanel container;
   private Dimension size;

   protected EmbeddedPeer(JComponent parent, Component embedded) {
      this.component = embedded;
      this.parent = parent;
      this.shell = new Shell();
      this.shell.add(this.component, "Center");
      this.container = new ShellContainer();
      this.container.add(this.shell);
      if (parent != null) {
         parent.add(this.container);
      }

   }

   private static void resetDoubleBuffering(Component root, Map<JComponent, Boolean> oldStateMap, boolean isReset) {
      if (root instanceof JComponent) {
         JComponent jComponent = (JComponent)root;
         if (isReset) {
            Boolean oldState = (Boolean)oldStateMap.get(jComponent);
            if (oldState != null) {
               jComponent.setDoubleBuffered(oldState);
            }
         } else {
            oldStateMap.put(jComponent, jComponent.isDoubleBuffered());
            jComponent.setDoubleBuffered(false);
         }
      }

      if (root instanceof Container) {
         Container container = (Container)root;

         for(int i = container.getComponentCount() - 1; i >= 0; --i) {
            resetDoubleBuffering(container.getComponent(i), oldStateMap, isReset);
         }
      }

   }

   public void paint(Graphics2D g) {
      if (this.component != null) {
         Graphics2D g2d = (Graphics2D)g.create();
         g2d.setStroke(defaultStroke);
         g2d.setRenderingHints(defaultHints);
         AffineTransform transform = g2d.getTransform();
         if (transform.getType() == 1) {
            double tx = (double)Math.round(transform.getTranslateX());
            double ty = (double)Math.round(transform.getTranslateY());
            transform.setToTranslation(tx, ty);
            g2d.setTransform(transform);
         }

         if (havePrinterGraphics && (transform.getScaleX() != 1.0 || transform.getScaleY() != 1.0 || transform.getShearX() != 0.0 || transform.getShearY() != 0.0)) {
            g2d = EmbeddedPeer.EmbeddedGraphicsFactory.createGraphics(g2d);
         }

         Map<JComponent, Boolean> oldState = new HashMap();
         resetDoubleBuffering(this.shell, oldState, false);
         this.shell.paint(g2d);
         resetDoubleBuffering(this.shell, oldState, true);
         g2d.dispose();
      }

   }

   public final JComponent getParentComponent() {
      return this.parent;
   }

   public final void setParentComponent(JComponent newParent) {
      if (this.parent != newParent) {
         if (this.parent != null) {
            this.parent.remove(this.getContainer());
         }

         this.parent = newParent;
         if (this.parent != null) {
            this.parent.add(this.getContainer());
            this.getEmbeddedToolkit().registerEmbeddedToolkit(this.parent);
         }

      }
   }

   public final Component getEmbeddedComponent() {
      return this.component;
   }

   public final void validate() {
      this.getShell().validate();
   }

   public final void dispose() {
      this.setParentComponent((JComponent)null);
   }

   public abstract void repaint(int var1, int var2, int var3, int var4);

   protected abstract void sizeChanged(Dimension var1, Dimension var2);

   protected abstract EmbeddedToolkit<?> getEmbeddedToolkit();

   public JPanel getShellPanel() {
      return this.getShell();
   }

   Shell getShell() {
      return this.shell;
   }

   JPanel getContainer() {
      return (JPanel)((JPanel)this.getShell().getParent());
   }

   public final Dimension getSize() {
      return this.size;
   }

   public void setSize(Dimension size) {
      this.size = size;
      this.shell.validate();
   }

   static {
      boolean tmpBoolean = false;

      try {
         Class.forName("java.awt.print.PrinterGraphics");
         tmpBoolean = true;
      } catch (ClassNotFoundException var2) {
      }

      havePrinterGraphics = tmpBoolean;
   }

   class Shell extends JPanel {
      private static final long serialVersionUID = 1L;
      private boolean firingPCE = false;
      private boolean contains = false;
      private boolean validateRunnableDone = true;
      private Runnable validateRunnable = new Runnable() {
         public void run() {
            Shell.this.validate();
            Shell.this.validateRunnableDone = true;
         }
      };

      Shell() {
         super(new BorderLayout());
         this.setOpaque(false);
         this.setVisible(true);
         this.setBorder((Border)null);
         this.enableEvents(131120L);
         SwingGlueLayer.getSwingGlueLayer().registerRepaintManager(this);
      }

      public boolean contains(int x, int y) {
         return this.contains;
      }

      void setContains(boolean contains) {
         this.contains = contains;
      }

      public void repaint(long tm, int x, int y, int width, int height) {
         EmbeddedPeer.this.repaint(x, y, width, height);
      }

      public boolean isShowing() {
         return true;
      }

      public boolean isVisible() {
         return true;
      }

      public void invalidate() {
         super.invalidate();
         if (!this.firingPCE && EmbeddedPeer.this.component != null) {
            this.firingPCE = true;
            EmbeddedPeer.this.component.firePropertyChange("$fx_preferredSize", 0L, 1L);
            this.firingPCE = false;
         }

         if (this.validateRunnableDone) {
            this.validateRunnableDone = false;
            SwingUtilities.invokeLater(this.validateRunnable);
         }

      }

      public void validate() {
         Dimension oldSize = this.getSize();
         Dimension newSize;
         if (EmbeddedPeer.this.component != null) {
            if (EmbeddedPeer.this.size != null) {
               newSize = EmbeddedPeer.this.size;
            } else {
               newSize = EmbeddedPeer.this.component.getPreferredSize();
            }
         } else {
            newSize = new Dimension(0, 0);
         }

         if (!newSize.equals(oldSize)) {
            this.setSize(newSize);
            EmbeddedPeer.this.sizeChanged(oldSize, newSize);
         }

         super.validate();
      }

      EmbeddedPeer getEmbeddedPeer() {
         return EmbeddedPeer.this;
      }
   }

   class ShellContainer extends JPanel {
      private static final long serialVersionUID = 1L;

      ShellContainer() {
         super((LayoutManager)null);
         this.setBounds(0, 0, 0, 0);
      }

      public boolean contains(int x, int y) {
         return this.getComponentCount() > 0 ? this.getComponent(0).contains(x, y) : super.contains(x, y);
      }
   }

   private static class EmbeddedGraphicsFactory {
      static Graphics2D createGraphics(Graphics g) {
         return new EmbeddedGraphics((Graphics2D)g);
      }
   }
}
