package com.sun.scenario.scenegraph;

import com.sun.media.jmc.MediaProvider;
import com.sun.media.jmc.control.VideoRenderControl;
import com.sun.media.jmc.event.VideoRendererEvent;
import com.sun.media.jmc.event.VideoRendererListener;
import com.sun.scenario.utils.Utils;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class SGMediaView extends SGLeaf implements VideoRendererListener {
   private MediaProvider mediaProvider;
   private VideoRenderControl videoRenderControl;
   private final Rectangle2D.Float dimension = new Rectangle2D.Float();
   private Rectangle2D.Float viewport = new Rectangle2D.Float();
   private Rectangle2D userViewport = null;
   private Object interpolationHint;
   private boolean smoothTranslation;
   private final Runnable markBoundsDirtyRunnable;
   private final Runnable markVisualDirtyRunnable;

   public SGMediaView() {
      this.interpolationHint = Utils.isAtLeastJava6 ? RenderingHints.VALUE_INTERPOLATION_BILINEAR : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
      this.smoothTranslation = false;
      this.markBoundsDirtyRunnable = new Runnable() {
         public void run() {
            SGMediaView.this.repaint(true);
         }
      };
      this.markVisualDirtyRunnable = new Runnable() {
         public void run() {
            SGMediaView.this.repaint(false);
         }
      };
   }

   private final VideoRenderControl getVRC() {
      VideoRenderControl vrc = (VideoRenderControl)AccessController.doPrivileged(new PrivilegedAction<VideoRenderControl>() {
         public VideoRenderControl run() {
            return (VideoRenderControl)SGMediaView.this.mediaProvider.getControl(VideoRenderControl.class);
         }
      });
      return vrc;
   }

   public void setMediaProvider(MediaProvider provider) {
      if (this.videoRenderControl != null) {
         this.videoRenderControl.removeVideoRendererListener(this);
         this.videoRenderControl = null;
      }

      this.mediaProvider = provider;
      if (this.mediaProvider != null) {
         this.videoRenderControl = this.getVRC();
         if (this.videoRenderControl != null) {
            this.videoRenderControl.addVideoRendererListener(this);
         }
      }

      if (this.userViewport == null) {
         if (this.mediaProvider != null && this.videoRenderControl != null) {
            this.viewport.width = (float)this.videoRenderControl.getFrameSize().width;
            this.viewport.height = (float)this.videoRenderControl.getFrameSize().width;
         } else {
            this.viewport.width = 0.0F;
            this.viewport.height = 0.0F;
         }
      }

      this.repaint(true);
   }

   public final Point2D getLocation(Point2D rv) {
      if (rv == null) {
         rv = new Point2D.Float();
      }

      ((Point2D)rv).setLocation(this.dimension.getX(), this.dimension.getY());
      return (Point2D)rv;
   }

   public final Point2D getLocation() {
      return this.getLocation((Point2D)null);
   }

   public void setLocation(Point2D location) {
      if (location == null) {
         throw new IllegalArgumentException("null location");
      } else {
         float newx = (float)location.getX();
         float newy = (float)location.getY();
         if (this.dimension.x != newx || this.dimension.y != newy) {
            this.dimension.x = newx;
            this.dimension.y = newy;
            this.repaint(true);
         }

      }
   }

   public void setDimensions(Dimension2D dimension) {
      float newW = 0.0F;
      float newH = 0.0F;
      if (dimension != null) {
         newW = (float)Math.abs(dimension.getWidth());
         newH = (float)Math.abs(dimension.getHeight());
      }

      if (this.dimension.width != newW || this.dimension.height != newH) {
         this.dimension.width = newW;
         this.dimension.height = newH;
         this.repaint(true);
      }

   }

   public final Object getInterpolationHint() {
      return this.interpolationHint;
   }

   public void setInterpolationHint(Object hint) {
      if (!RenderingHints.KEY_INTERPOLATION.isCompatibleValue(hint)) {
         throw new IllegalArgumentException("invalid hint");
      } else {
         if (this.interpolationHint != hint) {
            this.interpolationHint = hint;
            this.repaint(false);
         }

      }
   }

   public final boolean getSmoothTranslation() {
      return this.smoothTranslation;
   }

   public void setSmoothTranslation(boolean smooth) {
      if (this.smoothTranslation != smooth) {
         this.smoothTranslation = smooth;
         this.repaint(false);
      }

   }

   public void setViewport(Rectangle2D userViewport) {
      if (userViewport != null) {
         this.userViewport = userViewport;
         this.viewport.x = (float)userViewport.getX();
         this.viewport.y = (float)userViewport.getY();
         this.viewport.width = (float)userViewport.getWidth();
         this.viewport.height = (float)userViewport.getHeight();
      } else {
         this.userViewport = null;
         this.viewport.x = 0.0F;
         this.viewport.y = 0.0F;
         if (this.videoRenderControl != null) {
            this.viewport.width = (float)this.videoRenderControl.getFrameSize().width;
            this.viewport.height = (float)this.videoRenderControl.getFrameSize().height;
         } else {
            this.viewport.width = 0.0F;
            this.viewport.height = 0.0F;
         }
      }

      this.repaint(true);
   }

   public Rectangle2D getViewport() {
      return this.userViewport;
   }

   public void paint(Graphics2D g) {
      if (this.mediaProvider != null && this.videoRenderControl != null) {
         double x = this.dimension.getX();
         double y = this.dimension.getY();
         int iw = (int)this.viewport.width;
         int ih = (int)this.viewport.height;
         boolean dimensionsSet = this.dimension.getWidth() > 0.0 && this.dimension.getHeight() > 0.0;
         boolean doScale = dimensionsSet && ((double)iw != this.dimension.getWidth() || (double)ih != this.dimension.getHeight());
         g = (Graphics2D)g.create();
         g.translate(x, y);
         if (doScale && iw != 0 && ih != 0) {
            double scaleW = this.dimension.getWidth() / (double)iw;
            double scaleH = this.dimension.getHeight() / (double)ih;
            g.scale(scaleW, scaleH);
         }

         Object hint = this.interpolationHint;
         if (!this.smoothTranslation && hint != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR) {
            AffineTransform xform = g.getTransform();
            if (xform.isIdentity() || xform.getType() == 1) {
               hint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
            }
         }

         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
         Rectangle src = this.viewport.getBounds();
         Rectangle dst = new Rectangle(0, 0, iw, ih);
         if (DO_PAINT && !src.isEmpty() && !dst.isEmpty()) {
            this.videoRenderControl.paintVideo(g, src, dst);
         }
      }

   }

   public void videoFrameUpdated(VideoRendererEvent arg0) {
      boolean boundsUpdated = false;
      if (this.userViewport == null && this.videoRenderControl != null) {
         Dimension d = this.videoRenderControl.getFrameSize();
         if ((float)d.width != this.viewport.width) {
            this.viewport.width = (float)d.width;
            boundsUpdated = true;
         }

         if ((float)d.height != this.viewport.height) {
            this.viewport.height = (float)d.height;
            boundsUpdated = true;
         }
      }

      EventQueue.invokeLater(boundsUpdated ? this.markBoundsDirtyRunnable : this.markVisualDirtyRunnable);
   }

   public final Rectangle2D getBounds(AffineTransform transform) {
      float x = this.dimension.x;
      float y = this.dimension.y;
      boolean dimensionsSet = this.dimension.getWidth() > 0.0 && this.dimension.getHeight() > 0.0;
      float w = dimensionsSet ? this.dimension.width : this.viewport.width;
      float h = dimensionsSet ? this.dimension.height : this.viewport.height;
      if (w != 0.0F && h != 0.0F) {
         if (transform != null && !transform.isIdentity()) {
            float[] coords;
            if (transform.getShearX() == 0.0 && transform.getShearY() == 0.0) {
               if (transform.getScaleX() == 1.0 && transform.getScaleY() == 1.0) {
                  x = (float)((double)x + transform.getTranslateX());
                  y = (float)((double)y + transform.getTranslateY());
               } else {
                  coords = new float[]{x, y, x + w, y + h};
                  transform.transform(coords, 0, coords, 0, 2);
                  x = Math.min(coords[0], coords[2]);
                  y = Math.min(coords[1], coords[3]);
                  w = Math.max(coords[0], coords[2]) - x;
                  h = Math.max(coords[1], coords[3]) - y;
               }
            } else {
               coords = new float[]{x, y, x + w, y, x, y + h, x + w, y + h};
               transform.transform(coords, 0, coords, 0, 4);
               x = w = coords[0];
               y = h = coords[1];

               for(int i = 2; i < coords.length; i += 2) {
                  if (x > coords[i]) {
                     x = coords[i];
                  }

                  if (w < coords[i]) {
                     w = coords[i];
                  }

                  if (y > coords[i + 1]) {
                     y = coords[i + 1];
                  }

                  if (h < coords[i + 1]) {
                     h = coords[i + 1];
                  }
               }

               w -= x;
               h -= y;
            }
         }

         return new Rectangle2D.Float(x, y, w, h);
      } else {
         return new Rectangle2D.Float();
      }
   }

   public boolean hasOverlappingContents() {
      return false;
   }
}
