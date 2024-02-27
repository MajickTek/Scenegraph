package com.sun.scenario.scenegraph;

import com.sun.scenario.utils.Utils;
import java.awt.AlphaComposite;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;

public class SGImage extends SGLeaf {
   private Image image;
   private final Rectangle2D.Float dimension = new Rectangle2D.Float();
   private Rectangle2D.Float viewport = new Rectangle2D.Float();
   private Rectangle2D userViewport = null;
   private Object interpolationHint;
   private boolean smoothTranslation;
   private final ImageObserver observer;
   private boolean imageOpaque;
   private static BufferedImage hitBI;

   public SGImage() {
      this.interpolationHint = Utils.isAtLeastJava6 ? RenderingHints.VALUE_INTERPOLATION_BILINEAR : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
      this.smoothTranslation = false;
      this.observer = new SGImageObserver();
   }

   public final Image getImage() {
      return this.image;
   }

   public void setImage(Image image) {
      boolean boundsChanged;
      if (this.image != null && image != null) {
         boundsChanged = image.getWidth((ImageObserver)null) != this.image.getWidth((ImageObserver)null) || image.getHeight((ImageObserver)null) != this.image.getHeight((ImageObserver)null);
      } else {
         boundsChanged = true;
      }

      this.image = image;
      if (this.userViewport == null) {
         if (this.image != null) {
            this.viewport.width = (float)this.image.getWidth((ImageObserver)null);
            this.viewport.height = (float)this.image.getHeight((ImageObserver)null);
         } else {
            this.viewport.width = 0.0F;
            this.viewport.height = 0.0F;
         }
      }

      if (image instanceof BufferedImage) {
         this.imageOpaque = !((BufferedImage)image).getColorModel().hasAlpha();
      } else if (image instanceof VolatileImage) {
         this.imageOpaque = ((VolatileImage)image).getTransparency() == 1;
      } else {
         this.imageOpaque = false;
      }

      this.repaint(boundsChanged);
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
      if (dimension != null && !(dimension.getWidth() <= 0.0) && !(dimension.getHeight() <= 0.0)) {
         float neww = (float)dimension.getWidth();
         float newh = (float)dimension.getHeight();
         if (this.dimension.width != neww || this.dimension.height != newh) {
            this.dimension.width = neww;
            this.dimension.height = newh;
            this.repaint(true);
         }

      } else {
         throw new IllegalArgumentException("null or empty dimensions");
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

   public void setViewport(Rectangle2D newUserViewport) {
      if (newUserViewport != null) {
         this.userViewport = newUserViewport;
         this.viewport.x = (float)newUserViewport.getX();
         this.viewport.y = (float)newUserViewport.getY();
         this.viewport.width = (float)newUserViewport.getWidth();
         this.viewport.height = (float)newUserViewport.getHeight();
      } else {
         this.userViewport = null;
         this.viewport.x = 0.0F;
         this.viewport.y = 0.0F;
         if (this.image != null) {
            this.viewport.width = (float)this.image.getWidth((ImageObserver)null);
            this.viewport.height = (float)this.image.getHeight((ImageObserver)null);
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
      if (this.image != null) {
         double x = this.dimension.getX();
         double y = this.dimension.getY();
         double iw = (double)this.viewport.width;
         double ih = (double)this.viewport.height;
         boolean dimensionsSet = this.dimension.getWidth() > 0.0 && this.dimension.getHeight() > 0.0;
         boolean doScale = dimensionsSet && (iw != this.dimension.getWidth() || ih != this.dimension.getHeight());
         g = (Graphics2D)g.create();
         g.translate(x, y);
         if (doScale && iw != 0.0 && ih != 0.0) {
            double scaleW = this.dimension.getWidth() / iw;
            double scaleH = this.dimension.getHeight() / ih;
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
         if (DO_PAINT) {
            g.drawImage(this.image, 0, 0, (int)this.viewport.width, (int)this.viewport.height, (int)this.viewport.x, (int)this.viewport.y, (int)(this.viewport.x + this.viewport.width), (int)(this.viewport.y + this.viewport.height), (ImageObserver)null);
         }
      }

   }

   public final Rectangle2D getBounds(AffineTransform transform) {
      if (this.image == null) {
         return new Rectangle2D.Float();
      } else {
         float x = this.dimension.x;
         float y = this.dimension.y;
         boolean dimensionsSet = this.dimension.getWidth() > 0.0 && this.dimension.getHeight() > 0.0;
         float w = dimensionsSet ? this.dimension.width : this.viewport.width;
         float h = dimensionsSet ? this.dimension.height : this.viewport.height;
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
      }
   }

   private boolean imageContains(double dx, double dy) {
      boolean dimensionsSet = this.dimension.width > 0.0F && this.dimension.height > 0.0F;
      float destW = dimensionsSet ? this.dimension.width : this.viewport.width;
      float destH = dimensionsSet ? this.dimension.height : this.viewport.height;
      dx = (double)this.viewport.x + dx * (double)this.viewport.width / (double)destW;
      dy = (double)this.viewport.y + dy * (double)this.viewport.height / (double)destH;
      if (!(dx < 0.0) && !(dy < 0.0) && !(dx >= (double)this.image.getWidth((ImageObserver)null)) && !(dy >= (double)this.image.getHeight((ImageObserver)null))) {
         if (this.imageOpaque) {
            return true;
         } else {
            int x = (int)dx;
            int y = (int)dy;
            BufferedImage bi;
            if (this.image instanceof BufferedImage) {
               bi = (BufferedImage)this.image;
            } else {
               if (hitBI == null) {
                  hitBI = new BufferedImage(1, 1, 2);
               }

               bi = hitBI;
               Graphics2D g = bi.createGraphics();
               g.setComposite(AlphaComposite.Src);
               g.drawImage(this.image, -x, -y, (ImageObserver)null);
               if (this.image instanceof VolatileImage) {
                  VolatileImage vi = (VolatileImage)this.image;
                  if (vi.contentsLost()) {
                     return false;
                  }
               }

               x = 0;
               y = 0;
            }

            return (bi.getRGB(x, y) >> 24 & 255) > 0;
         }
      } else {
         return false;
      }
   }

   public boolean contains(Point2D point) {
      if (this.image != null && point != null) {
         Rectangle2D b = this.getBounds();
         return b.contains(point) ? this.imageContains(point.getX() - b.getX(), point.getY() - b.getY()) : false;
      } else {
         return false;
      }
   }

   public boolean hasOverlappingContents() {
      return false;
   }

   private class SGImageObserver implements ImageObserver {
      private final Runnable markDirtyRunnable;

      private SGImageObserver() {
         this.markDirtyRunnable = new Runnable() {
            public void run() {
               SGImage.this.visualChanged();
            }
         };
      }

      public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
         boolean ret = false;
         if (img == SGImage.this.image && SGImage.this.isVisible()) {
            if ((infoflags & 56) != 0) {
               EventQueue.invokeLater(this.markDirtyRunnable);
            }

            ret = (infoflags & 224) == 0;
         }

         return ret;
      }
   }
}
