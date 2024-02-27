package com.sun.embeddedswing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.print.PrinterGraphics;
import java.awt.print.PrinterJob;
import java.text.AttributedCharacterIterator;
import java.util.Map;

class EmbeddedGraphics extends Graphics2D implements PrinterGraphics {
   static final boolean useCustomStroke = !"Mac OS X".equals(System.getProperty("os.name"));
   private final Graphics2D delegate;

   EmbeddedGraphics(Graphics2D graphics) {
      this.delegate = graphics;
      Stroke stroke = this.delegate.getStroke();
      if (useCustomStroke && stroke instanceof BasicStroke) {
         this.delegate.setStroke(new SGComponentStroke((BasicStroke)stroke));
      }

   }

   public void addRenderingHints(Map<?, ?> hints) {
      this.delegate.addRenderingHints(hints);
   }

   public void clearRect(int x, int y, int width, int height) {
      this.delegate.clearRect(x, y, width, height);
   }

   public void clip(Shape s) {
      this.delegate.clip(s);
   }

   public void clipRect(int x, int y, int width, int height) {
      this.delegate.clipRect(x, y, width, height);
   }

   public void copyArea(int x, int y, int width, int height, int dx, int dy) {
      this.delegate.copyArea(x, y, width, height, dx, dy);
   }

   public Graphics create() {
      return this.delegate.create();
   }

   public Graphics create(int x, int y, int width, int height) {
      return this.delegate.create(x, y, width, height);
   }

   public void dispose() {
      this.delegate.dispose();
   }

   public void draw(Shape s) {
      this.delegate.draw(s);
   }

   public void draw3DRect(int x, int y, int width, int height, boolean raised) {
      this.delegate.draw3DRect(x, y, width, height, raised);
   }

   public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
      this.delegate.drawArc(x, y, width, height, startAngle, arcAngle);
   }

   public void drawBytes(byte[] data, int offset, int length, int x, int y) {
      this.delegate.drawBytes(data, offset, length, x, y);
   }

   public void drawChars(char[] data, int offset, int length, int x, int y) {
      this.delegate.drawChars(data, offset, length, x, y);
   }

   public void drawGlyphVector(GlyphVector g, float x, float y) {
      this.delegate.drawGlyphVector(g, x, y);
   }

   public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
      this.delegate.drawImage(img, op, x, y);
   }

   public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
      return this.delegate.drawImage(img, xform, obs);
   }

   public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
      return this.delegate.drawImage(img, x, y, bgcolor, observer);
   }

   public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
      return this.delegate.drawImage(img, x, y, observer);
   }

   public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
      return this.delegate.drawImage(img, x, y, width, height, bgcolor, observer);
   }

   public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
      return this.delegate.drawImage(img, x, y, width, height, observer);
   }

   public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
      return this.delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
   }

   public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
      return this.delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
   }

   public void drawLine(int x1, int y1, int x2, int y2) {
      this.delegate.drawLine(x1, y1, x2, y2);
   }

   public void drawOval(int x, int y, int width, int height) {
      this.delegate.drawOval(x, y, width, height);
   }

   public void drawPolygon(int[] points, int[] points2, int points3) {
      this.delegate.drawPolygon(points, points2, points3);
   }

   public void drawPolygon(Polygon p) {
      this.delegate.drawPolygon(p);
   }

   public void drawPolyline(int[] points, int[] points2, int points3) {
      this.delegate.drawPolyline(points, points2, points3);
   }

   public void drawRect(int x, int y, int width, int height) {
      this.delegate.drawRect(x, y, width, height);
   }

   public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
      this.delegate.drawRenderableImage(img, xform);
   }

   public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
      this.delegate.drawRenderedImage(img, xform);
   }

   public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
      this.delegate.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
   }

   public void drawString(AttributedCharacterIterator iterator, float x, float y) {
      this.delegate.drawString(iterator, x, y);
   }

   public void drawString(AttributedCharacterIterator iterator, int x, int y) {
      this.delegate.drawString(iterator, x, y);
   }

   public void drawString(String str, float x, float y) {
      this.delegate.drawString(str, x, y);
   }

   public void drawString(String str, int x, int y) {
      this.delegate.drawString(str, x, y);
   }

   public void fill(Shape s) {
      this.delegate.fill(s);
   }

   public void fill3DRect(int x, int y, int width, int height, boolean raised) {
      this.delegate.fill3DRect(x, y, width, height, raised);
   }

   public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
      this.delegate.fillArc(x, y, width, height, startAngle, arcAngle);
   }

   public void fillOval(int x, int y, int width, int height) {
      this.delegate.fillOval(x, y, width, height);
   }

   public void fillPolygon(int[] points, int[] points2, int points3) {
      this.delegate.fillPolygon(points, points2, points3);
   }

   public void fillPolygon(Polygon p) {
      this.delegate.fillPolygon(p);
   }

   public void fillRect(int x, int y, int width, int height) {
      this.delegate.fillRect(x, y, width, height);
   }

   public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
      this.delegate.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
   }

   public void finalize() {
      this.delegate.finalize();
   }

   public Color getBackground() {
      return this.delegate.getBackground();
   }

   public Shape getClip() {
      return this.delegate.getClip();
   }

   public Rectangle getClipBounds() {
      return this.delegate.getClipBounds();
   }

   public Rectangle getClipBounds(Rectangle r) {
      return this.delegate.getClipBounds(r);
   }

   public Rectangle getClipRect() {
      return this.delegate.getClipBounds();
   }

   public Color getColor() {
      return this.delegate.getColor();
   }

   public Composite getComposite() {
      return this.delegate.getComposite();
   }

   public GraphicsConfiguration getDeviceConfiguration() {
      return this.delegate.getDeviceConfiguration();
   }

   public Font getFont() {
      return this.delegate.getFont();
   }

   public FontMetrics getFontMetrics() {
      return this.delegate.getFontMetrics();
   }

   public FontMetrics getFontMetrics(Font f) {
      return this.delegate.getFontMetrics(f);
   }

   public FontRenderContext getFontRenderContext() {
      return this.delegate.getFontRenderContext();
   }

   public Paint getPaint() {
      return this.delegate.getPaint();
   }

   public Object getRenderingHint(RenderingHints.Key hintKey) {
      return this.delegate.getRenderingHint(hintKey);
   }

   public RenderingHints getRenderingHints() {
      return this.delegate.getRenderingHints();
   }

   public Stroke getStroke() {
      return this.delegate.getStroke();
   }

   public AffineTransform getTransform() {
      return this.delegate.getTransform();
   }

   public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
      return this.delegate.hit(rect, s, onStroke);
   }

   public boolean hitClip(int x, int y, int width, int height) {
      return this.delegate.hitClip(x, y, width, height);
   }

   public void rotate(double theta, double x, double y) {
      this.delegate.rotate(theta, x, y);
   }

   public void rotate(double theta) {
      this.delegate.rotate(theta);
   }

   public void scale(double sx, double sy) {
      this.delegate.scale(sx, sy);
   }

   public void setBackground(Color color) {
      this.delegate.setBackground(color);
   }

   public void setClip(int x, int y, int width, int height) {
      this.delegate.setClip(x, y, width, height);
   }

   public void setClip(Shape clip) {
      this.delegate.setClip(clip);
   }

   public void setColor(Color c) {
      this.delegate.setColor(c);
   }

   public void setComposite(Composite comp) {
      this.delegate.setComposite(comp);
   }

   public void setFont(Font font) {
      this.delegate.setFont(font);
   }

   public void setPaint(Paint paint) {
      this.delegate.setPaint(paint);
   }

   public void setPaintMode() {
      this.delegate.setPaintMode();
   }

   public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
      this.delegate.setRenderingHint(hintKey, hintValue);
   }

   public void setRenderingHints(Map<?, ?> hints) {
      this.delegate.setRenderingHints(hints);
   }

   public void setStroke(Stroke s) {
      this.delegate.setStroke(s);
   }

   public void setTransform(AffineTransform Tx) {
      this.delegate.setTransform(Tx);
   }

   public void setXORMode(Color c1) {
      this.delegate.setXORMode(c1);
   }

   public void shear(double shx, double shy) {
      this.delegate.shear(shx, shy);
   }

   public String toString() {
      return this.delegate.toString();
   }

   public void transform(AffineTransform Tx) {
      this.delegate.transform(Tx);
   }

   public void translate(double tx, double ty) {
      this.delegate.translate(tx, ty);
   }

   public void translate(int x, int y) {
      this.delegate.translate(x, y);
   }

   public PrinterJob getPrinterJob() {
      return null;
   }

   static class SGComponentStroke implements Stroke {
      private static final AffineTransform transform = AffineTransform.getTranslateInstance(0.5, 0.5);
      private final BasicStroke delegate;

      SGComponentStroke(BasicStroke stroke) {
         this.delegate = stroke;
      }

      public Shape createStrokedShape(Shape s) {
         Shape shape = this.delegate.createStrokedShape(s);
         return transform.createTransformedShape(shape);
      }

      public float[] getDashArray() {
         return this.delegate.getDashArray();
      }

      public float getDashPhase() {
         return this.delegate.getDashPhase();
      }

      public int getEndCap() {
         return this.delegate.getEndCap();
      }

      public int getLineJoin() {
         return this.delegate.getLineJoin();
      }

      public float getLineWidth() {
         return this.delegate.getLineWidth();
      }

      public float getMiterLimit() {
         return this.delegate.getMiterLimit();
      }

      public String toString() {
         return this.delegate.toString();
      }
   }
}
