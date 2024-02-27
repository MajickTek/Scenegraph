package com.sun.scenario.scenegraph;

import com.sun.embeddedswing.EmbeddedToolkit;
import com.sun.embeddedswing.SwingGlueLayer;
import com.sun.scenario.animation.FrameJob;
import com.sun.scenario.animation.Timeline;
import java.awt.Container;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class JSGPanelRepainter {
   Set<JSGPanel> dirtyPanels = new HashSet();
   private final FrameJob frameJob = new FrameDisplay();
   private static final Object JSGPANEL_REPAINTER_KEY = new StringBuilder("JSGPanelRepainterKey");

   static JSGPanelRepainter getJSGPanelRepainter() {
      Map<Object, Object> contextMap = SwingGlueLayer.getContextMap();
      JSGPanelRepainter instance = (JSGPanelRepainter)contextMap.get(JSGPANEL_REPAINTER_KEY);
      if (instance == null) {
         instance = new JSGPanelRepainter();
         contextMap.put(JSGPANEL_REPAINTER_KEY, instance);
      }

      return instance;
   }

   private JSGPanelRepainter() {
      Timeline.addFrameJob(this.frameJob);
   }

   void addDirtyPanel(JSGPanel panel) {
      this.dirtyPanels.add(panel);
      this.frameJob.wakeUp();
   }

   private JSGPanel getBottomPanel() {
      if (this.dirtyPanels.size() == 1) {
         return (JSGPanel)this.dirtyPanels.iterator().next();
      } else {
         Set<Container> parents = new HashSet();
         Iterator i$ = this.dirtyPanels.iterator();

         JSGPanel panel;
         while(i$.hasNext()) {
            panel = (JSGPanel)i$.next();

            for(Container container = panel.getParent(); container != null && !parents.contains(container); container = container.getParent()) {
               parents.add(container);
            }
         }

         i$ = this.dirtyPanels.iterator();

         do {
            if (!i$.hasNext()) {
               return null;
            }

            panel = (JSGPanel)i$.next();
         } while(parents.contains(panel));

         return panel;
      }
   }

   void repaintAll() {
      while(this.dirtyPanels.size() > 0) {
         JSGPanel panel = this.getBottomPanel();
         this.dirtyPanels.remove(panel);
         boolean immediately = !EmbeddedToolkit.isEmbedded(panel);
         panel.repaintDirtyRegions(immediately);
      }

   }

   private class FrameDisplay extends FrameJob {
      private FrameDisplay() {
      }

      public void run() {
         JSGPanelRepainter.this.repaintAll();
      }
   }
}
