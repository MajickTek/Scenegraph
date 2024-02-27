package com.sun.scenario.scenegraph;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

public class SGShape extends SGAbstractGeometry {
   private Shape shape;
   private static final int AT_IDENT = 0;
   private static final int AT_TRANS = 1;
   private static final int AT_GENERAL = 2;
   private AffineTransform cachedAT;
   private Rectangle2D cachedBounds;
   private static final int[] coordsPerSeg = new int[]{2, 2, 4, 6, 0};

   public final Shape getShape() {
      return this.shape;
   }

   public void setShape(Shape shape) {
      this.shape = shape;
      this.invalidateStrokeShape();
      this.invalidateCachedBounds();
   }

   public void setMode(SGAbstractShape.Mode mode) {
      super.setMode(mode);
      this.invalidateCachedBounds();
   }

   public void setDrawStroke(Stroke drawStroke) {
      super.setDrawStroke(drawStroke);
      this.invalidateCachedBounds();
   }

   private static int classify(AffineTransform at) {
      if (at == null) {
         return 0;
      } else {
         switch (at.getType()) {
            case 0:
               return 0;
            case 1:
               return 1;
            default:
               return 2;
         }
      }
   }

   static Rectangle2D getBounds(AffineTransform at, SGAbstractGeometry node, Line2D l) {
      if (node.getMode() != SGAbstractShape.Mode.FILL && node.getMode() != SGAbstractShape.Mode.EMPTY) {
         Stroke drawStroke = node.getDrawStroke();
         if (!(drawStroke instanceof BasicStroke)) {
            return getShapeBounds(at, node, l);
         } else {
            BasicStroke bs = (BasicStroke)drawStroke;
            float x1 = (float)l.getX1();
            float y1 = (float)l.getY1();
            float x2 = (float)l.getX2();
            float y2 = (float)l.getY2();
            float wpad = bs.getLineWidth() / 2.0F;
            int atclass = classify(at);
            float xpad;
            float ypad;
            float t;
            float ecx;
            if (atclass <= 1) {
               wpad += 0.5F;
               if (atclass == 1) {
                  t = (float)at.getTranslateX();
                  ecx = (float)at.getTranslateY();
                  x1 += t;
                  y1 += ecx;
                  x2 += t;
                  y2 += ecx;
               }

               if (y1 == y2 && x1 != x2) {
                  ypad = wpad;
                  xpad = bs.getEndCap() == 0 ? 0.0F : wpad;
               } else if (x1 == x2 && y1 != y2) {
                  xpad = wpad;
                  ypad = bs.getEndCap() == 0 ? 0.0F : wpad;
               } else {
                  if (bs.getEndCap() == 2) {
                     wpad = (float)((double)wpad * Math.sqrt(2.0));
                  }

                  ypad = wpad;
                  xpad = wpad;
               }

               if (x1 > x2) {
                  t = x1;
                  x1 = x2;
                  x2 = t;
               }

               if (y1 > y2) {
                  t = y1;
                  y1 = y2;
                  y2 = t;
               }

               x1 -= xpad;
               y1 -= ypad;
               x2 += xpad;
               y2 += ypad;
               return new Rectangle2D.Float(x1, y1, x2 - x1, y2 - y1);
            } else {
               xpad = x2 - x1;
               ypad = y2 - y1;
               t = (float)Math.sqrt((double)(xpad * xpad + ypad * ypad));
               if (t == 0.0F) {
                  xpad = wpad;
                  ypad = 0.0F;
               } else {
                  xpad = wpad * xpad / t;
                  ypad = wpad * ypad / t;
               }

               float ecy;
               if (bs.getEndCap() != 0) {
                  ecx = xpad;
                  ecy = ypad;
               } else {
                  ecy = 0.0F;
                  ecx = 0.0F;
               }

               float[] corners = new float[]{x1 - ypad - ecx, y1 + xpad - ecy, x1 + ypad - ecx, y1 - xpad - ecy, x2 + ypad + ecx, y2 - xpad + ecy, x2 - ypad + ecx, y2 + xpad + ecy};
               at.transform(corners, 0, corners, 0, 4);
               x1 = Math.min(Math.min(corners[0], corners[2]), Math.min(corners[4], corners[6]));
               y1 = Math.min(Math.min(corners[1], corners[3]), Math.min(corners[5], corners[7]));
               x2 = Math.max(Math.max(corners[0], corners[2]), Math.max(corners[4], corners[6]));
               y2 = Math.max(Math.max(corners[1], corners[3]), Math.max(corners[5], corners[7]));
               x1 -= 0.5F;
               y1 -= 0.5F;
               x2 += 0.5F;
               y2 += 0.5F;
               return new Rectangle2D.Float(x1, y1, x2 - x1, y2 - y1);
            }
         }
      } else {
         return new Rectangle2D.Float(0.0F, 0.0F, -1.0F, -1.0F);
      }
   }

   static Rectangle2D getBounds(AffineTransform at, SGAbstractGeometry node, RoundRectangle2D rr) {
      if (node.getMode() == SGAbstractShape.Mode.EMPTY) {
         return new Rectangle2D.Float(0.0F, 0.0F, -1.0F, -1.0F);
      } else {
         int atclass = classify(at);
         float upad;
         float dpad;
         if (node.getMode() == SGAbstractShape.Mode.FILL) {
            dpad = 0.0F;
            upad = 0.0F;
         } else {
            Stroke drawStroke = node.getDrawStroke();
            if (!(drawStroke instanceof BasicStroke)) {
               return getShapeBounds(at, node, rr);
            }

            BasicStroke bs = (BasicStroke)drawStroke;
            upad = bs.getLineWidth() / 2.0F;
            if (bs.getLineJoin() == 0) {
               double rw = rr.getWidth();
               double rh = rr.getHeight();
               double aw = Math.min(rw, Math.abs(rr.getArcWidth()));
               double ah = Math.min(rh, Math.abs(rr.getArcHeight()));
               if (!(aw > 0.0) || !(ah > 0.0) || !(aw * 10.0 < ah) && !(ah * 10.0 < aw)) {
                  upad = (float)((double)upad * Math.sqrt(2.0));
               } else {
                  upad *= bs.getMiterLimit();
               }
            }

            dpad = 0.5F;
         }

         return getBounds(at, atclass, upad, dpad, rr);
      }
   }

   static Rectangle2D getBounds(AffineTransform at, SGAbstractGeometry node, Arc2D a) {
      return getShapeBounds(at, node, a);
   }

   static Rectangle2D getBounds(AffineTransform at, SGAbstractGeometry node, Ellipse2D e) {
      if (node.getMode() == SGAbstractShape.Mode.EMPTY) {
         return new Rectangle2D.Float(0.0F, 0.0F, -1.0F, -1.0F);
      } else {
         float upad;
         float dpad;
         if (node.getMode() == SGAbstractShape.Mode.FILL) {
            dpad = 0.0F;
            upad = 0.0F;
         } else {
            Stroke drawStroke = node.getDrawStroke();
            if (!(drawStroke instanceof BasicStroke)) {
               return getShapeBounds(at, node, e);
            }

            BasicStroke bs = (BasicStroke)drawStroke;
            upad = bs.getLineWidth() / 2.0F;
            if (bs.getLineJoin() == 0 && (e.getWidth() * 10.0 < e.getHeight() || e.getHeight() * 10.0 < e.getWidth())) {
               upad *= bs.getMiterLimit();
            }

            dpad = 0.5F;
         }

         return getBounds(at, classify(at), upad, dpad, e);
      }
   }

   static Rectangle2D getBounds(AffineTransform at, SGAbstractGeometry node, Rectangle2D r) {
      if (node.getMode() == SGAbstractShape.Mode.EMPTY) {
         return new Rectangle2D.Float(0.0F, 0.0F, -1.0F, -1.0F);
      } else {
         int atclass = classify(at);
         float upad;
         float dpad;
         if (node.getMode() == SGAbstractShape.Mode.FILL) {
            dpad = 0.0F;
            upad = 0.0F;
         } else {
            Stroke drawStroke = node.getDrawStroke();
            if (!(drawStroke instanceof BasicStroke)) {
               return getShapeBounds(at, node, r);
            }

            BasicStroke bs = (BasicStroke)drawStroke;
            upad = bs.getLineWidth() / 2.0F;
            if (bs.getLineJoin() == 0 && atclass > 1) {
               upad = (float)((double)upad * Math.sqrt(2.0));
            }

            dpad = 0.5F;
         }

         return getBounds(at, atclass, upad, dpad, r);
      }
   }

   private static Rectangle2D getBounds(AffineTransform at, int atclass, float upad, float dpad, RectangularShape r) {
      float x1 = (float)r.getWidth();
      float y1 = (float)r.getHeight();
      if (!(x1 < 0.0F) && !(y1 < 0.0F)) {
         float x0 = (float)r.getX();
         float y0 = (float)r.getY();
         float tx;
         float ty;
         if (atclass <= 1) {
            x1 += x0;
            y1 += y0;
            if (atclass == 1) {
               tx = (float)at.getTranslateX();
               ty = (float)at.getTranslateY();
               x0 += tx;
               y0 += ty;
               x1 += tx;
               y1 += ty;
            }

            dpad += upad;
         } else {
            x0 -= upad;
            y0 -= upad;
            x1 += upad * 2.0F;
            y1 += upad * 2.0F;
            tx = (float)at.getScaleX();
            ty = (float)at.getShearX();
            float m10 = (float)at.getShearY();
            float m11 = (float)at.getScaleY();
            float m02 = x0 * tx + y0 * ty + (float)at.getTranslateX();
            float m12 = x0 * m10 + y0 * m11 + (float)at.getTranslateY();
            tx *= x1;
            ty *= y1;
            m10 *= x1;
            m11 *= y1;
            x0 = Math.min(Math.min(0.0F, tx), Math.min(ty, tx + ty)) + m02;
            y0 = Math.min(Math.min(0.0F, m10), Math.min(m11, m10 + m11)) + m12;
            x1 = Math.max(Math.max(0.0F, tx), Math.max(ty, tx + ty)) + m02;
            y1 = Math.max(Math.max(0.0F, m10), Math.max(m11, m10 + m11)) + m12;
         }

         x0 -= dpad;
         y0 -= dpad;
         x1 += dpad;
         y1 += dpad;
         return new Rectangle2D.Float(x0, y0, x1 - x0, y1 - y0);
      } else {
         return new Rectangle2D.Float(0.0F, 0.0F, -1.0F, -1.0F);
      }
   }

   public final Rectangle2D getBounds(AffineTransform at) {
      Shape s = this.getShape();
      if (s instanceof Rectangle2D) {
         return getBounds(at, this, (Rectangle2D)((Rectangle2D)s));
      } else if (s instanceof Ellipse2D) {
         return getBounds(at, this, (Ellipse2D)((Ellipse2D)s));
      } else if (s instanceof RoundRectangle2D) {
         return getBounds(at, this, (RoundRectangle2D)((RoundRectangle2D)s));
      } else if (s instanceof Line2D) {
         return getBounds(at, this, (Line2D)((Line2D)s));
      } else if (s instanceof Arc2D) {
         return getBounds(at, this, (Arc2D)((Arc2D)s));
      } else if (s == null) {
         return new Rectangle2D.Float(0.0F, 0.0F, -1.0F, -1.0F);
      } else {
         double m00;
         double m01;
         double m10;
         double m11;
         double m02;
         double m12;
         if (at == null) {
            m11 = 1.0;
            m00 = 1.0;
            m10 = 0.0;
            m01 = 0.0;
            m12 = 0.0;
            m02 = 0.0;
         } else {
            m00 = at.getScaleX();
            m01 = at.getShearX();
            m10 = at.getShearY();
            m11 = at.getScaleY();
            m02 = at.getTranslateX();
            m12 = at.getTranslateY();
         }

         if (this.cachedAT == null || this.cachedAT.getScaleX() != m00 || this.cachedAT.getScaleY() != m11 || this.cachedAT.getShearX() != m01 || this.cachedAT.getShearY() != m10) {
            this.cachedAT = new AffineTransform(m00, m10, m01, m11, 0.0, 0.0);
            this.cachedBounds = getShapeBounds(this.cachedAT, this, s);
         }

         Rectangle2D ret = new Rectangle2D.Float();
         ret.setFrame(this.cachedBounds.getX() + m02, this.cachedBounds.getY() + m12, this.cachedBounds.getWidth(), this.cachedBounds.getHeight());
         return ret;
      }
   }

   private void invalidateCachedBounds() {
      this.cachedAT = null;
   }

   static Rectangle2D getShapeBounds(AffineTransform at, SGAbstractGeometry node, Shape s) {
      SGAbstractShape.Mode mode = node.getMode();
      if (mode == SGAbstractShape.Mode.EMPTY) {
         return new Rectangle2D.Float(0.0F, 0.0F, -1.0F, -1.0F);
      } else {
         boolean includeShape = mode != SGAbstractShape.Mode.STROKE;
         boolean includeStroke = mode != SGAbstractShape.Mode.FILL;
         float[] bbox = new float[]{Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
         if (includeShape) {
            accumulate(bbox, s, at);
         }

         if (includeStroke) {
            Stroke drawStroke = node.getDrawStroke();
            accumulate(bbox, node.getStrokeShape(), at);
            if (drawStroke instanceof BasicStroke) {
               bbox[0] -= 0.5F;
               bbox[1] -= 0.5F;
               bbox[2] += 0.5F;
               bbox[3] += 0.5F;
            }
         }

         return !(bbox[2] < bbox[0]) && !(bbox[3] < bbox[1]) ? new Rectangle2D.Float(bbox[0], bbox[1], bbox[2] - bbox[0], bbox[3] - bbox[1]) : new Rectangle2D.Float(0.0F, 0.0F, -1.0F, -1.0F);
      }
   }

   private static void accumulate(float[] bbox, Shape s, AffineTransform at) {
      if (at != null && !at.isIdentity()) {
         PathIterator pi = s.getPathIterator(at);
         float[] coords = new float[6];

         while(!pi.isDone()) {
            int numcoords = coordsPerSeg[pi.currentSegment(coords)];

            for(int i = 0; i < numcoords; ++i) {
               float v = coords[i];
               int off = i & 1;
               if (bbox[off + 0] > v) {
                  bbox[off + 0] = v;
               }

               if (bbox[off + 2] < v) {
                  bbox[off + 2] = v;
               }
            }

            pi.next();
         }

      } else {
         Rectangle2D r2d = s.getBounds2D();
         if ((double)bbox[0] > r2d.getMinX()) {
            bbox[0] = (float)r2d.getMinX();
         }

         if ((double)bbox[1] > r2d.getMinY()) {
            bbox[1] = (float)r2d.getMinY();
         }

         if ((double)bbox[2] < r2d.getMaxX()) {
            bbox[2] = (float)r2d.getMaxX();
         }

         if ((double)bbox[3] < r2d.getMaxY()) {
            bbox[3] = (float)r2d.getMaxY();
         }

      }
   }

   public boolean contains(Point2D point) {
      Shape s = this.getShape();
      if (s == null) {
         return false;
      } else {
         double x = point.getX();
         double y = point.getY();
         if (s instanceof Rectangle2D) {
            return rectContains(x, y, this, (RectangularShape)s);
         } else if (s instanceof Ellipse2D) {
            return contains(x, y, this, (Ellipse2D)((Ellipse2D)s));
         } else if (s instanceof RoundRectangle2D) {
            return contains(x, y, this, (RoundRectangle2D)((RoundRectangle2D)s));
         } else {
            return s instanceof Line2D ? contains(x, y, this, (Line2D)((Line2D)s)) : shapeContains(x, y, this, s);
         }
      }
   }

   static boolean rectContains(double x, double y, SGAbstractGeometry node, RectangularShape r) {
      double rw = r.getWidth();
      double rh = r.getHeight();
      if (!(rw < 0.0) && !(rh < 0.0)) {
         SGAbstractShape.Mode mode = node.getMode();
         if (mode == SGAbstractShape.Mode.EMPTY) {
            return false;
         } else {
            Stroke drawstroke = null;
            double lw = 0.0;
            if (mode != SGAbstractShape.Mode.FILL) {
               drawstroke = node.getDrawStroke();
               if (drawstroke instanceof BasicStroke) {
                  BasicStroke bs = (BasicStroke)drawstroke;
                  if (bs.getLineJoin() == 0 && bs.getDashArray() == null) {
                     drawstroke = null;
                     lw = (double)bs.getLineWidth() / 2.0;
                  }
               }
            }

            if (mode != SGAbstractShape.Mode.STROKE || drawstroke == null) {
               double rx = r.getX();
               double ry = r.getY();
               if (x >= rx - lw && y >= ry - lw && x < rx + rw + lw && y < ry + rh + lw) {
                  if (mode == SGAbstractShape.Mode.STROKE && lw < rw / 2.0 && lw < rh / 2.0 && x >= rx + lw && y >= ry + lw && x < rx + rw - lw && y < ry + rh - lw) {
                     return false;
                  }

                  return true;
               }
            }

            return node.getStrokeShape().contains(x, y);
         }
      } else {
         return false;
      }
   }

   static boolean contains(double x, double y, SGAbstractGeometry node, Ellipse2D e) {
      return shapeContains(x, y, node, e);
   }

   static boolean contains(double x, double y, SGAbstractGeometry node, Arc2D a) {
      return shapeContains(x, y, node, a);
   }

   static boolean contains(double x, double y, SGAbstractGeometry node, RoundRectangle2D rr) {
      return rr.getArcWidth() != 0.0 && rr.getArcHeight() != 0.0 ? shapeContains(x, y, node, rr) : rectContains(x, y, node, rr);
   }

   static boolean contains(double x, double y, SGAbstractGeometry node, Line2D l) {
      SGAbstractShape.Mode mode = node.getMode();
      return mode != SGAbstractShape.Mode.FILL && mode != SGAbstractShape.Mode.EMPTY ? node.getStrokeShape().contains(x, y) : false;
   }

   static boolean shapeContains(double x, double y, SGAbstractGeometry node, Shape s) {
      SGAbstractShape.Mode mode = node.getMode();
      if (mode == SGAbstractShape.Mode.EMPTY) {
         return false;
      } else if (mode != SGAbstractShape.Mode.STROKE && s.contains(x, y)) {
         return true;
      } else {
         return mode != SGAbstractShape.Mode.FILL ? node.getStrokeShape().contains(x, y) : false;
      }
   }
}
