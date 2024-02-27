package com.sun.scenario.scenegraph.event;

import com.sun.scenario.scenegraph.SGNode;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.EventListener;

public interface SGMouseListener extends EventListener {
   void mouseClicked(MouseEvent var1, SGNode var2);

   void mousePressed(MouseEvent var1, SGNode var2);

   void mouseReleased(MouseEvent var1, SGNode var2);

   void mouseEntered(MouseEvent var1, SGNode var2);

   void mouseExited(MouseEvent var1, SGNode var2);

   void mouseDragged(MouseEvent var1, SGNode var2);

   void mouseMoved(MouseEvent var1, SGNode var2);

   void mouseWheelMoved(MouseWheelEvent var1, SGNode var2);
}
