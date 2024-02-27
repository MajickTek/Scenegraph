package com.sun.scenario.scenegraph.fx;

import com.sun.scenario.scenegraph.SGComponent;
import java.awt.Component;
import java.awt.Dimension;

public class FXComponent extends FXNode {
   private SGComponent componentNode = (SGComponent)this.getLeaf();

   public FXComponent() {
      super(new SGComponent());
   }

   public final Component getComponent() {
      return this.componentNode.getComponent();
   }

   public void setComponent(Component component) {
      this.componentNode.setComponent(component);
   }

   public Dimension getSize() {
      return this.componentNode.getSize();
   }

   public void setSize(Dimension size) {
      this.componentNode.setSize(size);
   }

   public void setSize(int width, int height) {
      this.componentNode.setSize(width, height);
   }
}
