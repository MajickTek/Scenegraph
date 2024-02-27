package com.sun.scenario.scenegraph.fx;

import com.sun.scenario.scenegraph.SGGroup;
import com.sun.scenario.scenegraph.SGNode;
import java.util.List;

public class FXGroup extends FXNode {
   private SGGroup groupNode = (SGGroup)this.getLeaf();

   public FXGroup() {
      super(new SGGroup());
   }

   public final List<SGNode> getFXChildren() {
      return this.groupNode.getChildren();
   }

   public final void add(int index, SGNode child) {
      this.groupNode.add(index, child);
   }

   public final void add(SGNode child) {
      this.groupNode.add(-1, child);
   }

   public final void remove(SGNode child) {
      this.groupNode.remove(child);
   }

   public final void remove(int index) {
      this.groupNode.remove(index);
   }
}
