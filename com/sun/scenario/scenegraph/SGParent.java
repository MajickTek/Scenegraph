package com.sun.scenario.scenegraph;

import java.util.Iterator;
import java.util.List;

public abstract class SGParent extends SGNode {
   public abstract List<SGNode> getChildren();

   public abstract void remove(SGNode var1);

   public SGNode lookup(String id) {
      if (id.equals(this.getID())) {
         return this;
      } else {
         List<SGNode> children = this.getChildren();
         Iterator i$ = children.iterator();

         SGNode retVal;
         do {
            if (!i$.hasNext()) {
               return null;
            }

            SGNode child = (SGNode)i$.next();
            retVal = child.lookup(id);
         } while(retVal == null);

         return retVal;
      }
   }
}
