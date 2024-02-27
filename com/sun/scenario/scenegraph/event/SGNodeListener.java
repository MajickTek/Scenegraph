package com.sun.scenario.scenegraph.event;

import com.sun.scenario.scenegraph.SGNode;
import java.util.EventListener;

public interface SGNodeListener extends EventListener {
   void boundsChanged(SGNode var1);

   void transformedBoundsChanged(SGNode var1);
}
