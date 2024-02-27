package com.sun.scenario.scenegraph;

import java.awt.event.InputMethodListener;
import java.awt.im.InputMethodRequests;

public interface InputMethodHelper extends InputMethodListener {
   InputMethodRequests getInputMethodRequests();
}
