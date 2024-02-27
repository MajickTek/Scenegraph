package com.sun.scenario.scenegraph;

import com.sun.scenario.effect.PerspectiveTransform;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class SGPerspective extends SGTransform {
   private static AffineTransform ident = new AffineTransform();
   private Point3D camera = new Point3D();
   private Tx3D ptx = new Tx3D();
   private Tx3D proj;
   private boolean backfaceculling;
   private NodeEffectInput cachedChildInput = new NodeEffectInput();

   public boolean isBackFaceCulling() {
      return this.backfaceculling;
   }

   public void setBackFaceCulling(boolean backfaceculling) {
      if (this.backfaceculling != backfaceculling) {
         this.backfaceculling = backfaceculling;
         this.visualChanged();
      }

   }

   public double[][] getMatrix() {
      return this.ptx.getMatrix();
   }

   public void setMatrix(double[][] matrix) {
      this.ptx.setMatrix(matrix);
      this.invalidateProjection();
   }

   public void concatenate(double[][] matrix) {
      this.ptx.concatenate(matrix);
      this.invalidateProjection();
   }

   public void preConcatenate(double[][] matrix) {
      this.ptx.preConcatenate(matrix);
      this.invalidateProjection();
   }

   public void setView(double x, double y, double distance) {
      this.camera.setLocation(x, y, distance);
      this.invalidateProjection();
   }

   public void setView(Point2D p, double distance) {
      this.setView(p.getX(), p.getY(), distance);
   }

   public Point2D getViewLocation() {
      return new Point2D.Double(this.camera.field_38, this.camera.field_39);
   }

   public void setViewLocation(Point2D p) {
      double newx = p.getX();
      double newy = p.getY();
      if (this.camera.field_38 != newx || this.camera.field_39 != newy) {
         this.camera.field_38 = newx;
         this.camera.field_39 = newy;
         this.invalidateProjection();
      }

   }

   public double getViewDistance() {
      return this.camera.field_40;
   }

   public void setViewDistance(double distance) {
      if (this.camera.field_40 != distance) {
         this.camera.field_40 = distance;
         this.invalidateProjection();
      }

   }

   public void translate(double tx, double ty, double tz) {
      this.ptx.translate(tx, ty, tz);
      this.invalidateProjection();
   }

   public void scale(double sx, double sy, double sz) {
      this.ptx.scale(sx, sy, sz);
      this.invalidateProjection();
   }

   public void rotateAroundAxis(Axis axis, double radians) {
      switch (axis) {
         case field_33:
            this.ptx.rotateAroundX(radians);
            break;
         case field_34:
            this.ptx.rotateAroundY(radians);
            break;
         case field_35:
            this.ptx.rotateAroundZ(radians);
            break;
         default:
            throw new InternalError("unrecognized axis");
      }

      this.invalidateProjection();
   }

   public void rotateAroundAxis(double x, double y, double z, Axis axis, double radians) {
      this.ptx.translate(x, y, z);
      this.rotateAroundAxis(axis, radians);
      this.ptx.translate(-x, -y, -z);
      this.invalidateProjection();
   }

   public Point2D inverseTransform(Point2D src, Point2D dst) {
      Point3D p3d = new Point3D(src.getX(), src.getY(), 0.0);
      this.ptx.inverseTransform(p3d);
      return DesktopSGTransformFactory.setPoint(dst, p3d.getX(), p3d.getY());
   }

   public Point2D transform(Point2D src, Point2D dst) {
      Point3D p3d = new Point3D(src.getX(), src.getY(), 0.0);
      this.ptx.transform(p3d);
      return DesktopSGTransformFactory.setPoint(dst, p3d.getX(), p3d.getY());
   }

   public void reset() {
      this.ptx.setToIdentity();
      this.invalidateProjection();
   }

   public void getTransform(AffineTransform at) {
      at.setToIdentity();
   }

   public void concatenateInto(AffineTransform at) {
   }

   public void concatenateInto(Graphics2D g2d) {
   }

   public boolean canSkipRendering() {
      return this.ptx.isIdentity();
   }

   private void invalidateProjection() {
      this.proj = null;
      this.boundsChanged();
      this.dispatchAllPendingEvents();
   }

   void markDirty(int state) {
      super.markDirty(state);
      if ((state & 49) != 0) {
         this.cachedChildInput.flush();
      }

   }

   public void setChild(SGNode child) {
      this.cachedChildInput.setNode(child);
      super.setChild(child);
   }

   private Tx3D getProjection() {
      if (this.proj == null) {
         this.proj = new Tx3D();
         this.proj.translate(this.camera.field_38, this.camera.field_39, 0.0);
         this.proj.concat(1.0, 0.0, 0.0, -this.camera.field_38, 0.0, 1.0, 0.0, -this.camera.field_39, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 / this.camera.field_40, 0.0);
         this.proj.translate(0.0, 0.0, this.camera.field_40);
         this.proj.concatenate(this.ptx);
      }

      return this.proj;
   }

   Rectangle2D calculateAccumBounds() {
      SGNode child = this.getChild();
      if (child == null) {
         return new Rectangle2D.Float();
      } else {
         AffineTransform at = this.getCumulativeTransform();
         Rectangle2D r = child.getTransformedBounds();
         return this.projectBounds(r, at, (Rectangle2D)null);
      }
   }

   public Rectangle2D getBounds(AffineTransform at) {
      SGNode child = this.getChild();
      if (child == null) {
         return new Rectangle2D.Float();
      } else {
         Rectangle2D r = child.getBounds(at);
         return this.projectBounds(r, at, r);
      }
   }

   private Rectangle2D projectBounds(Rectangle2D rin, AffineTransform at, Rectangle2D rout) {
      if (this.ptx.isIdentity()) {
         return rin;
      } else if (rin.isEmpty()) {
         return rin;
      } else {
         double x0 = rin.getX();
         double y0 = rin.getY();
         double x1 = x0 + rin.getWidth();
         double y1 = y0 + rin.getHeight();
         Point2D p = this.transformBoundsCorner(at, x0, y0);
         if (rout == null) {
            rout = new Rectangle2D.Double();
         }

         ((Rectangle2D)rout).setRect(p.getX(), p.getY(), 0.0, 0.0);
         ((Rectangle2D)rout).add(this.transformBoundsCorner(at, x0, y1));
         ((Rectangle2D)rout).add(this.transformBoundsCorner(at, x1, y0));
         ((Rectangle2D)rout).add(this.transformBoundsCorner(at, x1, y1));
         return (Rectangle2D)rout;
      }
   }

   boolean isFacingBack() {
      Point3D origin = new Point3D(0.0, 0.0, 0.0);
      Point3D xvec = new Point3D(1.0, 0.0, 0.0);
      Point3D yvec = new Point3D(0.0, 1.0, 0.0);
      this.getProjection().transform(origin);
      this.getProjection().transform(xvec);
      this.getProjection().transform(yvec);
      xvec = xvec.minus(origin);
      yvec = yvec.minus(origin);
      Point3D cross = xvec.cross(yvec);
      return cross.getZ() > 0.0;
   }

   private Point2D transformBoundsCorner(AffineTransform at, double x, double y) {
      Point3D p3d = new Point3D(x, y, 0.0);

      try {
         at.inverseTransform(p3d, p3d);
         this.getProjection().transform(p3d);
         at.transform(p3d, p3d);
      } catch (NoninvertibleTransformException var8) {
         p3d.setLocation(0.0, 0.0);
      }

      return p3d;
   }

   void render(Graphics2D g, Rectangle dirtyRegion, boolean clearDirty) {
      SGNode child = this.getChild();
      if (this.isVisible() && child != null) {
         if (this.ptx.isIdentity()) {
            child.render(g, dirtyRegion, clearDirty);
            if (clearDirty) {
               this.clearDirty();
            }

         } else if (this.backfaceculling && this.isFacingBack()) {
            if (clearDirty) {
               this.clearDirty();
            }

         } else if (!dirtyRegion.intersects(this.getTransformedBounds())) {
            if (clearDirty) {
               this.clearDirty();
            }

         } else {
            AffineTransform at = g.getTransform();
            Rectangle dbounds = child.getBounds(at).getBounds();
            double x0 = dbounds.getX();
            double y0 = dbounds.getY();
            double x1 = x0 + dbounds.getWidth();
            double y1 = y0 + dbounds.getHeight();
            Point2D ul = this.transformBoundsCorner(at, x0, y0);
            Point2D ur = this.transformBoundsCorner(at, x1, y0);
            Point2D lr = this.transformBoundsCorner(at, x1, y1);
            Point2D ll = this.transformBoundsCorner(at, x0, y1);
            PerspectiveTransform pt = new PerspectiveTransform();
            int xoff = (int)Math.floor(Math.min(Math.min(ul.getX(), ur.getX()), Math.min(ll.getX(), lr.getX())));
            int yoff = (int)Math.floor(Math.min(Math.min(ul.getY(), ur.getY()), Math.min(ll.getY(), lr.getY())));
            pt.setQuadMapping((float)ul.getX() - (float)xoff, (float)ul.getY() - (float)yoff, (float)ur.getX() - (float)xoff, (float)ur.getY() - (float)yoff, (float)lr.getX() - (float)xoff, (float)lr.getY() - (float)yoff, (float)ll.getX() - (float)xoff, (float)ll.getY() - (float)yoff);
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setTransform(ident);
            pt.render(g2, (float)xoff, (float)yoff, this.cachedChildInput);
            g2.dispose();
            if (clearDirty) {
               this.clearDirty();
            }

         }
      } else {
         if (clearDirty) {
            this.clearDirty();
         }

      }
   }

   public static enum Axis {
      // $FF: renamed from: X com.sun.scenario.scenegraph.SGPerspective$Axis
      field_33,
      // $FF: renamed from: Y com.sun.scenario.scenegraph.SGPerspective$Axis
      field_34,
      // $FF: renamed from: Z com.sun.scenario.scenegraph.SGPerspective$Axis
      field_35;
   }
}
