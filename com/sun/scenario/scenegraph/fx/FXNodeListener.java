package com.sun.scenario.scenegraph.fx;

public interface FXNodeListener {
   void boundsInLocalChanged();

   void boundsInParentChanged();

   void boundsInSceneChanged();

   void defaultLayoutBoundsChanged();
}
