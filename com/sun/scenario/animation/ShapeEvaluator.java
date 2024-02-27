package com.sun.scenario.animation;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.IllegalPathStateException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

public class ShapeEvaluator implements Evaluator<Shape> {
   private Shape savedv0;
   private Shape savedv1;
   private Geometry geom0;
   private Geometry geom1;

   public Shape evaluate(Shape v0, Shape v1, float fraction) {
      if (this.savedv0 != v0 || this.savedv1 != v1) {
         if (this.savedv0 == v1 && this.savedv1 == v0) {
            Geometry gtmp = this.geom0;
            this.geom0 = this.geom1;
            this.geom1 = gtmp;
         } else {
            this.recalculate(v0, v1);
         }

         this.savedv0 = v0;
         this.savedv1 = v1;
      }

      return this.getShape(fraction);
   }

   private void recalculate(Shape v0, Shape v1) {
      this.geom0 = new Geometry(v0);
      this.geom1 = new Geometry(v1);
      double[] tvals0 = this.geom0.getTvals();
      double[] tvals1 = this.geom1.getTvals();
      double[] masterTvals = mergeTvals(tvals0, tvals1);
      this.geom0.setTvals(masterTvals);
      this.geom1.setTvals(masterTvals);
   }

   private Shape getShape(float fraction) {
      return new MorphedShape(this.geom0, this.geom1, (double)fraction);
   }

   private static double[] mergeTvals(double[] tvals0, double[] tvals1) {
      int count = sortTvals(tvals0, tvals1, (double[])null);
      double[] newtvals = new double[count];
      sortTvals(tvals0, tvals1, newtvals);
      return newtvals;
   }

   private static int sortTvals(double[] tvals0, double[] tvals1, double[] newtvals) {
      int i0 = 0;
      int i1 = 0;

      int numtvals;
      for(numtvals = 0; i0 < tvals0.length && i1 < tvals1.length; ++numtvals) {
         double t0 = tvals0[i0];
         double t1 = tvals1[i1];
         if (t0 <= t1) {
            if (newtvals != null) {
               newtvals[numtvals] = t0;
            }

            ++i0;
         }

         if (t1 <= t0) {
            if (newtvals != null) {
               newtvals[numtvals] = t1;
            }

            ++i1;
         }
      }

      return numtvals;
   }

   private static double interp(double v0, double v1, double t) {
      return v0 + (v1 - v0) * t;
   }

   private static class Iterator implements PathIterator {
      // $FF: renamed from: at java.awt.geom.AffineTransform
      AffineTransform field_29;
      // $FF: renamed from: g0 com.sun.scenario.animation.ShapeEvaluator$Geometry
      Geometry field_30;
      // $FF: renamed from: g1 com.sun.scenario.animation.ShapeEvaluator$Geometry
      Geometry field_31;
      // $FF: renamed from: t double
      double field_32;
      int cindex;

      public Iterator(AffineTransform at, Geometry g0, Geometry g1, double t) {
         this.field_29 = at;
         this.field_30 = g0;
         this.field_31 = g1;
         this.field_32 = t;
      }

      public int getWindingRule() {
         return this.field_32 < 0.5 ? this.field_30.getWindingRule() : this.field_31.getWindingRule();
      }

      public boolean isDone() {
         return this.cindex > this.field_30.getNumCoords();
      }

      public void next() {
         if (this.cindex == 0) {
            this.cindex = 2;
         } else {
            this.cindex += 6;
         }

      }

      public int currentSegment(float[] coords) {
         return this.currentSegment(coords, (double[])null);
      }

      public int currentSegment(double[] coords) {
         return this.currentSegment((float[])null, coords);
      }

      private int currentSegment(float[] fcoords, double[] dcoords) {
         byte type;
         byte n;
         if (this.cindex == 0) {
            type = 0;
            n = 2;
         } else if (this.cindex >= this.field_30.getNumCoords()) {
            type = 4;
            n = 0;
         } else {
            type = 3;
            n = 6;
         }

         if (n > 0) {
            for(int i = 0; i < n; ++i) {
               double v = ShapeEvaluator.interp(this.field_30.getCoord(this.cindex + i), this.field_31.getCoord(this.cindex + i), this.field_32);
               if (fcoords != null) {
                  fcoords[i] = (float)v;
               } else {
                  dcoords[i] = v;
               }
            }

            if (this.field_29 != null) {
               if (fcoords != null) {
                  this.field_29.transform(fcoords, 0, fcoords, 0, n / 2);
               } else {
                  this.field_29.transform(dcoords, 0, dcoords, 0, n / 2);
               }
            }
         }

         return type;
      }
   }

   private static class MorphedShape implements Shape {
      Geometry geom0;
      Geometry geom1;
      // $FF: renamed from: t double
      double field_37;
      private Shape compatShape;

      MorphedShape(Geometry geom0, Geometry geom1, double t) {
         this.geom0 = geom0;
         this.geom1 = geom1;
         this.field_37 = t;
      }

      public Rectangle getBounds() {
         return this.getBounds2D().getBounds();
      }

      public Rectangle2D getBounds2D() {
         int n = this.geom0.getNumCoords();
         double xmax;
         double xmin = xmax = ShapeEvaluator.interp(this.geom0.getCoord(0), this.geom1.getCoord(0), this.field_37);
         double ymax;
         double ymin = ymax = ShapeEvaluator.interp(this.geom0.getCoord(1), this.geom1.getCoord(1), this.field_37);

         for(int i = 2; i < n; i += 2) {
            double x = ShapeEvaluator.interp(this.geom0.getCoord(i), this.geom1.getCoord(i), this.field_37);
            double y = ShapeEvaluator.interp(this.geom0.getCoord(i + 1), this.geom1.getCoord(i + 1), this.field_37);
            if (xmin > x) {
               xmin = x;
            }

            if (ymin > y) {
               ymin = y;
            }

            if (xmax < x) {
               xmax = x;
            }

            if (ymax < y) {
               ymax = y;
            }
         }

         return new Rectangle2D.Double(xmin, ymin, xmax - xmin, ymax - ymin);
      }

      public boolean contains(double x, double y) {
         if (this.compatShape == null) {
            this.compatShape = new GeneralPath(this);
         }

         return this.compatShape.contains(x, y);
      }

      public boolean contains(Point2D p) {
         return this.contains(p.getX(), p.getY());
      }

      public boolean intersects(double x, double y, double w, double h) {
         if (this.compatShape == null) {
            this.compatShape = new GeneralPath(this);
         }

         return this.compatShape.intersects(x, y, w, h);
      }

      public boolean intersects(Rectangle2D r) {
         return this.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
      }

      public boolean contains(double x, double y, double w, double h) {
         if (this.compatShape == null) {
            this.compatShape = new GeneralPath(this);
         }

         return this.compatShape.contains(x, y, w, h);
      }

      public boolean contains(Rectangle2D r) {
         return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
      }

      public PathIterator getPathIterator(AffineTransform at) {
         return new Iterator(at, this.geom0, this.geom1, this.field_37);
      }

      public PathIterator getPathIterator(AffineTransform at, double flatness) {
         return new FlatteningPathIterator(this.getPathIterator(at), flatness);
      }
   }

   private static class Geometry {
      static final double THIRD = 0.3333333333333333;
      static final double MIN_LEN = 0.001;
      double[] bezierCoords = new double[20];
      int numCoords;
      int windingrule;
      double[] myTvals;

      public Geometry(Shape s) {
         PathIterator pi = s.getPathIterator((AffineTransform)null);
         this.windingrule = pi.getWindingRule();
         if (pi.isDone()) {
            this.numCoords = 8;
         }

         double[] coords = new double[6];
         int type = pi.currentSegment(coords);
         pi.next();
         if (type != 0) {
            throw new IllegalPathStateException("missing initial moveto");
         } else {
            double curx;
            double movx;
            this.bezierCoords[0] = curx = movx = coords[0];
            double cury;
            double movy;
            this.bezierCoords[1] = cury = movy = coords[1];
            Vector<Point2D> savedpathendpoints = new Vector();

            double newx;
            double newy;
            for(this.numCoords = 2; !pi.isDone(); pi.next()) {
               switch (pi.currentSegment(coords)) {
                  case 0:
                     if (curx != movx || cury != movy) {
                        this.appendLineTo(curx, cury, movx, movy);
                        curx = movx;
                        cury = movy;
                     }

                     newx = coords[0];
                     newy = coords[1];
                     if (curx != newx || cury != newy) {
                        savedpathendpoints.add(new Point2D.Double(movx, movy));
                        this.appendLineTo(curx, cury, newx, newy);
                        movx = newx;
                        curx = newx;
                        movy = newy;
                        cury = newy;
                     }
                     break;
                  case 1:
                     newx = coords[0];
                     newy = coords[1];
                     this.appendLineTo(curx, cury, newx, newy);
                     curx = newx;
                     cury = newy;
                     break;
                  case 2:
                     double ctrlx = coords[0];
                     double ctrly = coords[1];
                     newx = coords[2];
                     newy = coords[3];
                     this.appendQuadTo(curx, cury, ctrlx, ctrly, newx, newy);
                     curx = newx;
                     cury = newy;
                     break;
                  case 3:
                     this.appendCubicTo(coords[0], coords[1], coords[2], coords[3], curx = coords[4], cury = coords[5]);
                     break;
                  case 4:
                     if (curx != movx || cury != movy) {
                        this.appendLineTo(curx, cury, movx, movy);
                        curx = movx;
                        cury = movy;
                     }
               }
            }

            if (this.numCoords < 8 || curx != movx || cury != movy) {
               this.appendLineTo(curx, cury, movx, movy);
               curx = movx;
               cury = movy;
            }

            int minPt;
            for(minPt = savedpathendpoints.size() - 1; minPt >= 0; --minPt) {
               Point2D p = (Point2D)savedpathendpoints.get(minPt);
               newx = p.getX();
               newy = p.getY();
               if (curx != newx || cury != newy) {
                  this.appendLineTo(curx, cury, newx, newy);
                  curx = newx;
                  cury = newy;
               }
            }

            minPt = 0;
            double minX = this.bezierCoords[0];
            double minY = this.bezierCoords[1];

            for(int ci = 6; ci < this.numCoords; ci += 6) {
               double x = this.bezierCoords[ci];
               double y = this.bezierCoords[ci + 1];
               if (y < minY || y == minY && x < minX) {
                  minPt = ci;
                  minX = x;
                  minY = y;
               }
            }

            if (minPt > 0) {
               double[] newCoords = new double[this.numCoords];
               System.arraycopy(this.bezierCoords, minPt, newCoords, 0, this.numCoords - minPt);
               System.arraycopy(this.bezierCoords, 2, newCoords, this.numCoords - minPt, minPt);
               this.bezierCoords = newCoords;
            }

            double area = 0.0;
            curx = this.bezierCoords[0];
            cury = this.bezierCoords[1];

            int i;
            for(i = 2; i < this.numCoords; i += 2) {
               newx = this.bezierCoords[i];
               newy = this.bezierCoords[i + 1];
               area += curx * newy - newx * cury;
               curx = newx;
               cury = newy;
            }

            if (area < 0.0) {
               i = 2;

               for(int j = this.numCoords - 4; i < j; j -= 2) {
                  curx = this.bezierCoords[i];
                  cury = this.bezierCoords[i + 1];
                  this.bezierCoords[i] = this.bezierCoords[j];
                  this.bezierCoords[i + 1] = this.bezierCoords[j + 1];
                  this.bezierCoords[j] = curx;
                  this.bezierCoords[j + 1] = cury;
                  i += 2;
               }
            }

         }
      }

      private void appendLineTo(double x0, double y0, double x1, double y1) {
         this.appendCubicTo(ShapeEvaluator.interp(x0, x1, 0.3333333333333333), ShapeEvaluator.interp(y0, y1, 0.3333333333333333), ShapeEvaluator.interp(x1, x0, 0.3333333333333333), ShapeEvaluator.interp(y1, y0, 0.3333333333333333), x1, y1);
      }

      private void appendQuadTo(double x0, double y0, double ctrlx, double ctrly, double x1, double y1) {
         this.appendCubicTo(ShapeEvaluator.interp(ctrlx, x0, 0.3333333333333333), ShapeEvaluator.interp(ctrly, y0, 0.3333333333333333), ShapeEvaluator.interp(ctrlx, x1, 0.3333333333333333), ShapeEvaluator.interp(ctrly, y1, 0.3333333333333333), x1, y1);
      }

      private void appendCubicTo(double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x1, double y1) {
         if (this.numCoords + 6 > this.bezierCoords.length) {
            int newsize = (this.numCoords - 2) * 2 + 2;
            double[] newCoords = new double[newsize];
            System.arraycopy(this.bezierCoords, 0, newCoords, 0, this.numCoords);
            this.bezierCoords = newCoords;
         }

         this.bezierCoords[this.numCoords++] = ctrlx1;
         this.bezierCoords[this.numCoords++] = ctrly1;
         this.bezierCoords[this.numCoords++] = ctrlx2;
         this.bezierCoords[this.numCoords++] = ctrly2;
         this.bezierCoords[this.numCoords++] = x1;
         this.bezierCoords[this.numCoords++] = y1;
      }

      public int getWindingRule() {
         return this.windingrule;
      }

      public int getNumCoords() {
         return this.numCoords;
      }

      public double getCoord(int i) {
         return this.bezierCoords[i];
      }

      public double[] getTvals() {
         if (this.myTvals != null) {
            return this.myTvals;
         } else {
            double[] tvals = new double[(this.numCoords - 2) / 6 + 1];
            double segx = this.bezierCoords[0];
            double segy = this.bezierCoords[1];
            double tlen = 0.0;
            int ci = 2;

            double newy;
            int ti;
            double prevt;
            double nextt;
            for(ti = 0; ci < this.numCoords; segy = newy) {
               double newx = this.bezierCoords[ci++];
               newy = this.bezierCoords[ci++];
               prevt = segx - newx;
               nextt = segy - newy;
               double len = Math.sqrt(prevt * prevt + nextt * nextt);
               prevt = newx;
               nextt = newy;
               newx = this.bezierCoords[ci++];
               newy = this.bezierCoords[ci++];
               prevt -= newx;
               nextt -= newy;
               len += Math.sqrt(prevt * prevt + nextt * nextt);
               prevt = newx;
               nextt = newy;
               newx = this.bezierCoords[ci++];
               newy = this.bezierCoords[ci++];
               prevt -= newx;
               nextt -= newy;
               len += Math.sqrt(prevt * prevt + nextt * nextt);
               segx -= newx;
               segy -= newy;
               len += Math.sqrt(segx * segx + segy * segy);
               len /= 2.0;
               if (len < 0.001) {
                  len = 0.001;
               }

               tlen += len;
               tvals[ti++] = tlen;
               segx = newx;
            }

            prevt = tvals[0];
            tvals[0] = 0.0;

            for(ti = 1; ti < tvals.length - 1; ++ti) {
               nextt = tvals[ti];
               tvals[ti] = prevt / tlen;
               prevt = nextt;
            }

            tvals[ti] = 1.0;
            return this.myTvals = tvals;
         }
      }

      public void setTvals(double[] newTvals) {
         double[] oldCoords = this.bezierCoords;
         double[] newCoords = new double[2 + (newTvals.length - 1) * 6];
         double[] oldTvals = this.getTvals();
         int oldci = 0;
         double xc0;
         double xc1;
         double x1;
         double x0 = xc0 = xc1 = x1 = oldCoords[oldci++];
         double yc0;
         double yc1;
         double y1;
         double y0 = yc0 = yc1 = y1 = oldCoords[oldci++];
         int newci = 0;
         newCoords[newci++] = x0;
         newCoords[newci++] = y0;
         double t0 = 0.0;
         double t1 = 0.0;
         int oldti = 1;

         double nt;
         for(int newti = 1; newti < newTvals.length; t0 = nt) {
            if (t0 >= t1) {
               x0 = x1;
               y0 = y1;
               xc0 = oldCoords[oldci++];
               yc0 = oldCoords[oldci++];
               xc1 = oldCoords[oldci++];
               yc1 = oldCoords[oldci++];
               x1 = oldCoords[oldci++];
               y1 = oldCoords[oldci++];
               t1 = oldTvals[oldti++];
            }

            nt = newTvals[newti++];
            if (nt < t1) {
               double relt = (nt - t0) / (t1 - t0);
               newCoords[newci++] = x0 = ShapeEvaluator.interp(x0, xc0, relt);
               newCoords[newci++] = y0 = ShapeEvaluator.interp(y0, yc0, relt);
               xc0 = ShapeEvaluator.interp(xc0, xc1, relt);
               yc0 = ShapeEvaluator.interp(yc0, yc1, relt);
               xc1 = ShapeEvaluator.interp(xc1, x1, relt);
               yc1 = ShapeEvaluator.interp(yc1, y1, relt);
               newCoords[newci++] = x0 = ShapeEvaluator.interp(x0, xc0, relt);
               newCoords[newci++] = y0 = ShapeEvaluator.interp(y0, yc0, relt);
               xc0 = ShapeEvaluator.interp(xc0, xc1, relt);
               yc0 = ShapeEvaluator.interp(yc0, yc1, relt);
               newCoords[newci++] = x0 = ShapeEvaluator.interp(x0, xc0, relt);
               newCoords[newci++] = y0 = ShapeEvaluator.interp(y0, yc0, relt);
            } else {
               newCoords[newci++] = xc0;
               newCoords[newci++] = yc0;
               newCoords[newci++] = xc1;
               newCoords[newci++] = yc1;
               newCoords[newci++] = x1;
               newCoords[newci++] = y1;
            }
         }

         this.bezierCoords = newCoords;
         this.numCoords = newCoords.length;
         this.myTvals = newTvals;
      }
   }
}
