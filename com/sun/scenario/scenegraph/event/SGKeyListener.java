package com.sun.scenario.scenegraph.event;

import com.sun.scenario.scenegraph.SGNode;
import java.awt.event.KeyEvent;
import java.util.EventListener;

public interface SGKeyListener extends EventListener {
   void keyTyped(KeyEvent var1, SGNode var2);

   void keyPressed(KeyEvent var1, SGNode var2);

   void keyReleased(KeyEvent var1, SGNode var2);
}
