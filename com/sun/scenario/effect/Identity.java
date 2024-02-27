package com.sun.scenario.effect;

import com.sun.scenario.effect.impl.ImageData;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Map;

public class Identity extends Effect {
   private BufferedImage src;
   private Point2D.Float loc = new Point2D.Float();
   private final Map<GraphicsConfiguration, ImageData> datacache = new HashMap();

   public Identity(BufferedImage src) {
      this.src = src;
   }

   public final BufferedImage getSource() {
      return this.src;
   }

   public void setSource(BufferedImage src) {
      BufferedImage old = this.src;
      this.src = src;
      this.clearCache();
      this.firePropertyChange("source", old, src);
   }

   public final Point2D getLocation() {
      return this.loc;
   }

   public void setLocation(Point2D pt) {
      if (pt == null) {
         throw new IllegalArgumentException("Location must be non-null");
      } else {
         Point2D old = this.loc;
         this.loc.setLocation(pt);
         this.firePropertyChange("location", old, pt);
      }
   }

   public Rectangle2D getBounds(AffineTransform transform, Effect defaultInput) {
      if (this.src == null) {
         return new Rectangle();
      } else {
         Rectangle2D r = new Rectangle2D.Float(this.loc.x, this.loc.y, (float)this.src.getWidth(), (float)this.src.getHeight());
         if (transform != null && !transform.isIdentity()) {
            r = this.transformBounds(transform, (Rectangle2D)r);
         }

         return (Rectangle2D)r;
      }
   }

   public ImageData filter(GraphicsConfiguration config, AffineTransform transform, Effect defaultInput) {
      if (this.src == null) {
         return null;
      } else {
         ImageData id = (ImageData)this.datacache.get(config);
         if (id == null) {
            int w = this.src.getWidth();
            int h = this.src.getHeight();
            Image img = Effect.createCompatibleImage(config, w, h);
            Graphics2D g2 = (Graphics2D)img.getGraphics();
            g2.setComposite(AlphaComposite.Src);
            g2.drawImage(this.src, 0, 0, (ImageObserver)null);
            g2.dispose();
            id = new ImageData(config, img, new Rectangle(w, h));
            this.datacache.put(config, id);
         }

         id.addref();
         transform = Offset.getOffsetTransform(transform, (double)this.loc.x, (double)this.loc.y);
         id = this.ensureTransform(config, id, transform);
         return id;
      }
   }

   public Effect.AccelType getAccelType(GraphicsConfiguration config) {
      return Effect.AccelType.INTRINSIC;
   }

   private void clearCache() {
      this.datacache.clear();
   }
}
