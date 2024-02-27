package com.sun.scenario.scenegraph.event;

import com.sun.scenario.scenegraph.SGNode;
import java.awt.event.FocusEvent;
import java.util.EventListener;

public interface SGFocusListener extends EventListener {
   void focusGained(FocusEvent var1, SGNode var2);

   void focusLost(FocusEvent var1, SGNode var2);
}
