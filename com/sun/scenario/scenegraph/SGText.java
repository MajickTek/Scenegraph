package com.sun.scenario.scenegraph;

import com.sun.scenario.utils.Utils;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.font.TransformAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SGText extends SGAbstractShape {
   private static Font defaultFont = new Font("SansSerif", 0, 12);
   private static Object antialiasingHintDT;
   private static Object gammaHintDT;
   private static RenderingHints.Key gammaHintKey;
   private static DesktopAAHintsTracker hintsTracker;
   private static final Method FONT_HAS_LAYOUT_ATTRIBUTES;
   private static final RenderingHints.Key KEY_TEXT_LCD_CONTRAST;
   private static final Object VALUE_TEXT_ANTIALIAS_GASP;
   private static final Constructor FRC_CONSTRUCTOR;
   private static final Method FRC_GET_ANTI_ALIASING_HINT;
   private static final Method FRC_GET_FRACTIONAL_METRICS_HINT;
   private static final Method SG2D_GET_TRANSPARENCY;
   private static final Field SD_SURFACEDATA;
   private Font font;
   private String text;
   private boolean overline;
   private boolean underline;
   private boolean strikethrough;
   private float wrappingWidth;
   private boolean useDTAA;
   private Object antialiasingHintApp;
   private final Point2D.Float location;
   private VAlign verticalAlignment;
   private HAlign horizontalAlighment;
   private AffineTransform lastTx;
   private boolean lastAA;
   private Rectangle2D rawBounds;
   private boolean complexText;
   private boolean useLayout;
   private Shape cachedOutline;
   private FontRenderContext cachedFRC;
   private boolean rotatedFont;
   private TextLayout[] cachedLayouts;
   private Point2D.Float[] cachedPositions;
   private int[] eols;
   private Selection selection;
   private static final AffineTransform IDENTITY_TRANSFORM;

   public SGText() {
      this.font = defaultFont;
      this.overline = false;
      this.underline = false;
      this.strikethrough = false;
      this.wrappingWidth = Float.MAX_VALUE;
      this.useDTAA = true;
      this.antialiasingHintApp = antialiasingHintDT;
      this.location = new Point2D.Float();
      this.verticalAlignment = SGText.VAlign.BASELINE;
      this.horizontalAlighment = SGText.HAlign.LEFT;
      this.lastTx = new AffineTransform();
   }

   public final Shape getShape() {
      return this.getOutline(this.getFontRenderContext());
   }

   public final String getText() {
      return this.text;
   }

   private void flushCachedValues() {
      this.cachedLayouts = null;
      this.cachedPositions = null;
      this.rawBounds = null;
      this.cachedOutline = null;
      this.cachedFRC = null;
      if (this.selection != null) {
         this.selection.shape = null;
      }

   }

   private void updateUseLayout() {
      this.useLayout = !this.isTextEmpty() && (this.complexText || this.eols != null || hasLayoutAttributes(this.font) || this.overline || this.strikethrough || this.underline || this.wrappingWidth != Float.MAX_VALUE || this.selection != null);
   }

   public void setText(String text) {
      this.text = text;
      this.complexText = this.isComplexText(text);
      this.eols = this.getEols(text);
      this.flushCachedValues();
      this.updateUseLayout();
      this.repaint(true);
   }

   public void setUnderline(boolean underline) {
      if (underline != this.underline) {
         this.underline = underline;
         this.flushCachedValues();
         this.repaint(true);
      }

   }

   public boolean isUnderline() {
      return this.underline;
   }

   public void setOverline(boolean overline) {
      if (overline != this.overline) {
         this.overline = overline;
         this.flushCachedValues();
         this.repaint(true);
      }

   }

   public boolean isOverline() {
      return this.overline;
   }

   public void setStrikethrough(boolean strikethrough) {
      if (strikethrough != this.strikethrough) {
         this.strikethrough = strikethrough;
         this.flushCachedValues();
         this.repaint(true);
      }

   }

   public void setWrappingWidth(float wrappingWidth) {
      wrappingWidth = Math.max(0.0F, wrappingWidth);
      if (wrappingWidth == 0.0F) {
         wrappingWidth = Float.MAX_VALUE;
      }

      if (wrappingWidth != this.wrappingWidth) {
         this.wrappingWidth = wrappingWidth;
         this.flushCachedValues();
         this.repaint(true);
      }

   }

   public float getWrappingWidth() {
      return this.wrappingWidth == Float.MAX_VALUE ? 0.0F : this.wrappingWidth;
   }

   public TextHitInfo hitTestChar(Point2D point) {
      if (this.isTextEmpty()) {
         return null;
      } else {
         TextHitInfo hit = null;
         if (!this.useLayout) {
            this.setLogicalSelection(0, 0);
         }

         this.getBounds();
         AffineTransform translate = AffineTransform.getTranslateInstance((double)(-this.location.x), (double)(-this.location.y - this.getYAdjustment(this.cachedFRC)) - this.cachedPositions[0].getY());
         Point2D dstPoint = new Point2D.Float();
         translate.transform(point, dstPoint);
         hit = this.cachedLayouts[0].hitTestChar((float)dstPoint.getX(), (float)dstPoint.getY());
         return hit;
      }
   }

   public Shape getCaretShape(TextHitInfo hit) {
      if (!this.isTextEmpty() && this.mode != SGAbstractShape.Mode.EMPTY) {
         if (hit.getCharIndex() > this.text.length()) {
            hit = hit.isLeadingEdge() ? TextHitInfo.leading(this.text.length()) : TextHitInfo.trailing(this.text.length());
         }

         Shape rv = null;
         if (!this.useLayout) {
            this.setLogicalSelection(0, 0);
         }

         this.getBounds();
         AffineTransform translate = AffineTransform.getTranslateInstance((double)this.location.x, (double)(this.location.y + this.getYAdjustment(this.cachedFRC)) + this.cachedPositions[0].getY());
         rv = translate.createTransformedShape(this.cachedLayouts[0].getCaretShape(hit));
         return rv;
      } else {
         return null;
      }
   }

   public void setSelectionPaint(Paint selectionDrawPaint, Paint selectionFillPaint) {
      if (this.selection == null) {
         this.selection = new Selection();
      }

      this.selection.drawPaint = selectionDrawPaint;
      this.selection.fillPaint = selectionFillPaint;
      this.repaint(false);
   }

   public void setLogicalSelection(int start, int end) {
      if (this.selection == null || this.selection.start != start || this.selection.end != end) {
         if (this.selection == null) {
            this.selection = new Selection();
            if (!this.useLayout) {
               this.flushCachedValues();
               this.updateUseLayout();
            }
         }

         this.selection.start = Math.max(Math.min(start, end), 0);
         this.selection.end = Math.min(Math.max(start, end), this.text.length());
         this.selection.shape = null;
         this.repaint(false);
      }

   }

   public Shape getSelectionShape() {
      if (!this.isTextEmpty() && this.mode != SGAbstractShape.Mode.EMPTY) {
         Shape rv = null;
         if (this.selection != null) {
            if (this.selection.shape == null && this.selection.end > this.selection.start) {
               this.getBounds();
               AffineTransform translate = AffineTransform.getTranslateInstance((double)this.location.x, (double)(this.location.y + this.getYAdjustment(this.cachedFRC)) + this.cachedPositions[0].getY());
               this.selection.shape = translate.createTransformedShape(this.cachedLayouts[0].getLogicalHighlightShape(this.selection.start, this.selection.end));
            }

            rv = this.selection.shape;
         }

         return rv;
      } else {
         return null;
      }
   }

   private static boolean hasLayoutAttributes(Font font) {
      if (FONT_HAS_LAYOUT_ATTRIBUTES == null) {
         return false;
      } else {
         try {
            Object ret = FONT_HAS_LAYOUT_ATTRIBUTES.invoke(font, (Object[])null);
            if (ret instanceof Boolean) {
               return (Boolean)ret;
            }
         } catch (Exception var2) {
         }

         return false;
      }
   }

   private int[] getEols(String text) {
      int[] ret = null;
      List<Integer> eolList = null;

      int i;
      for(i = text.indexOf(10); i >= 0; i = text.indexOf(10, i + 1)) {
         if (i < text.length() - 1 && text.charAt(i + 1) != '\n') {
            if (eolList == null) {
               eolList = new ArrayList();
            }

            eolList.add(i);
         }
      }

      if (eolList != null) {
         ret = new int[eolList.size() + 1];

         for(i = 0; i < eolList.size(); ++i) {
            ret[i] = (Integer)eolList.get(i);
         }

         ret[ret.length - 1] = text.length();
      }

      return ret;
   }

   private boolean isComplexText(String text) {
      if (this.isTextEmpty()) {
         return false;
      } else {
         char[] ch = text.toCharArray();
         int count = ch.length;

         for(int i = 0; i < count; ++i) {
            int code = ch[i];
            if (code >= 1424) {
               if (code <= 1535) {
                  return true;
               }

               if (code >= 1536 && code <= 1791) {
                  return true;
               }

               if (code >= 2304 && code <= 3455) {
                  return true;
               }

               if (code >= 3584 && code <= 3711) {
                  return true;
               }

               if (code >= 6016 && code <= 6143) {
                  return true;
               }

               if (code >= 8204 && code <= 8205) {
                  return true;
               }

               if (code >= 8234 && code <= 8238) {
                  return true;
               }

               if (code >= 8298 && code <= 8303) {
                  return true;
               }

               if (code >= '\ud800' && code <= '\udbff') {
                  ++i;
               }
            }
         }

         return false;
      }
   }

   public final Font getFont() {
      return this.font;
   }

   public void setFont(Font font) {
      if (font == null) {
         throw new IllegalArgumentException("null font");
      } else {
         this.font = font;
         this.flushCachedValues();
         this.rotatedFont = false;
         this.updateUseLayout();
         TransformAttribute transformAttr = (TransformAttribute)font.getAttributes().get(TextAttribute.TRANSFORM);
         if (transformAttr != null && !transformAttr.isIdentity()) {
            AffineTransform at = transformAttr.getTransform();
            this.rotatedFont = (at.getType() & 24) != 0;
         }

         this.repaint(true);
      }
   }

   public final Point2D getLocation(Point2D rv) {
      if (rv == null) {
         rv = new Point2D.Float();
      }

      ((Point2D)rv).setLocation(this.location);
      return (Point2D)rv;
   }

   public final Point2D getLocation() {
      return this.getLocation((Point2D)null);
   }

   public void setLocation(Point2D location) {
      if (location == null) {
         throw new IllegalArgumentException("null location");
      } else {
         this.location.setLocation(location);
         this.repaint(true);
      }
   }

   public final VAlign getVerticalAlignment() {
      return this.verticalAlignment;
   }

   public void setVerticalAlignment(VAlign verticalAlignment) {
      if (verticalAlignment == null) {
         throw new IllegalArgumentException("null alignment");
      } else {
         this.verticalAlignment = verticalAlignment;
         this.flushCachedValues();
         this.repaint(true);
      }
   }

   public void setHorizontalAlignment(HAlign horizontalAlignment) {
      if (horizontalAlignment == null) {
         throw new IllegalArgumentException("null alignment");
      } else {
         if (horizontalAlignment != this.horizontalAlighment) {
            this.horizontalAlighment = horizontalAlignment;
            this.flushCachedValues();
            this.repaint(true);
         }

      }
   }

   public final void setAntialiased(boolean aa) {
      this.useDTAA = aa;
      if (aa) {
         this.setAntialiasingHintInt(antialiasingHintDT);
      } else {
         this.setAntialiasingHintInt(RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
      }

   }

   public final boolean isAntialiased() {
      Object hint = this.getAntialiasingHintInt();
      return hint != RenderingHints.VALUE_ANTIALIAS_OFF && hint != RenderingHints.VALUE_ANTIALIAS_DEFAULT;
   }

   /** @deprecated */
   @Deprecated
   public final Object getAntialiasingHint() {
      return this.getAntialiasingHintInt();
   }

   private Object getAntialiasingHintInt() {
      return this.useDTAA ? antialiasingHintDT : this.antialiasingHintApp;
   }

   /** @deprecated */
   @Deprecated
   public void setAntialiasingHint(Object hint) {
      this.setAntialiasingHintInt(hint);
   }

   private void setAntialiasingHintInt(Object hint) {
      if (!RenderingHints.KEY_TEXT_ANTIALIASING.isCompatibleValue(hint)) {
         throw new IllegalArgumentException("invalid hint");
      } else {
         this.useDTAA = false;
         this.antialiasingHintApp = hint;
         this.flushCachedValues();
         this.repaint(false);
      }
   }

   private Rectangle2D getOverlineRect(Point2D position, TextLayout tl, float overlineThickness, Rectangle2D overlineRect) {
      if (overlineRect == null) {
         overlineRect = new Rectangle2D.Float();
      }

      Rectangle2D bounds = tl.getBounds();
      ((Rectangle2D)overlineRect).setFrame(position.getX() - 1.0, position.getY() + bounds.getY() - (double)overlineThickness, bounds.getWidth() + 3.0, (double)overlineThickness);
      return (Rectangle2D)overlineRect;
   }

   public void paint(Graphics2D g) {
      Shape selectedShape = this.getSelectionShape();
      if (selectedShape != null) {
         Shape clipOrig = g.getClip();
         Area area = null;
         if (clipOrig != null) {
            area = new Area(clipOrig);
         } else {
            Rectangle2D bounds = this.getBounds();
            bounds.setFrame(bounds.getX() - 100.0, bounds.getY() - 100.0, bounds.getWidth() + 200.0, bounds.getHeight() + 200.0);
            area = new Area(bounds);
         }

         area.subtract(new Area(selectedShape));
         g.setClip(area);
         this.paintImpl(g, this.getDrawPaint(), this.getFillPaint());
         g.setClip(clipOrig);
         g.clip(selectedShape);
         this.paintImpl(g, this.selection.drawPaint, this.selection.fillPaint);
         g.setClip(clipOrig);
      } else {
         this.paintImpl(g, this.getDrawPaint(), this.getFillPaint());
      }

   }

   private Object getAntialiasingHintInt(AffineTransform transform, Graphics2D g) {
      Object aaHint = this.getAntialiasingHintInt();
      if (this.isAntialiased() && aaHint != RenderingHints.VALUE_TEXT_ANTIALIAS_ON) {
         if (this.rotatedFont) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
         } else {
            int txType = transform != null ? transform.getType() : 0;
            if ((txType & 24) != 0 || isDestTranslucent(g)) {
               aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            }
         }
      }

      return aaHint;
   }

   private void paintImpl(Graphics2D g, Paint _drawPaint, Paint _fillPaint) {
      if (this.mode != SGAbstractShape.Mode.EMPTY && !this.isDegradedTransform(g.getTransform())) {
         if (!this.isTextEmpty()) {
            g.setFont(this.font);
            if (this.mode == SGAbstractShape.Mode.FILL) {
               if (_fillPaint != null) {
                  Object aaHint = this.getAntialiasingHintInt(g.getTransform(), g);
                  g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                  g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, aaHint);
                  if (gammaHintKey != null && gammaHintDT != null && this.useDTAA) {
                     g.setRenderingHint(gammaHintKey, gammaHintDT);
                  }

                  float y = this.location.y;
                  if (this.verticalAlignment != SGText.VAlign.BASELINE) {
                     y += this.getYAdjustment(g.getFontRenderContext());
                  }

                  g.setPaint(_fillPaint);
                  if (DO_PAINT) {
                     g.translate((double)this.location.x, (double)y);
                     if (!this.useLayout) {
                        g.drawString(this.text, 0, 0);
                     } else {
                        FontRenderContext frc = g.getFontRenderContext();
                        this.updateTextLayouts(frc);
                        Rectangle2D overlineRect = null;
                        if (aaHint == RenderingHints.VALUE_TEXT_ANTIALIAS_ON && this.isAntialiased() && (this.overline || this.underline || this.strikethrough)) {
                           g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        }

                        float overlineThickness = 0.0F;
                        if (this.overline) {
                           overlineThickness = this.getOverlineThickness(frc);
                           overlineRect = new Rectangle2D.Float();
                        }

                        for(int i = 0; i < this.cachedLayouts.length; ++i) {
                           TextLayout tl = this.cachedLayouts[i];
                           Point2D position = this.cachedPositions[i];
                           tl.draw(g, (float)position.getX(), (float)position.getY());
                           if (overlineRect != null) {
                              g.fill(this.getOverlineRect(position, tl, overlineThickness, overlineRect));
                           }
                        }
                     }

                     g.translate((double)(-this.location.x), (double)(-y));
                  }
               }
            } else {
               if (this.isAntialiased()) {
                  g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               }

               Shape outline = this.getOutline(g.getFontRenderContext());
               if (this.mode == SGAbstractShape.Mode.STROKE_FILL && _fillPaint != null) {
                  g.setPaint(_fillPaint);
                  if (DO_PAINT) {
                     g.fill(outline);
                  }
               }

               if (_drawPaint != null) {
                  g.setPaint(_drawPaint);
                  g.setStroke(this.drawStroke);
                  if (DO_PAINT) {
                     try {
                        g.draw(outline);
                     } catch (Throwable var12) {
                     }
                  }
               }
            }
         }

      }
   }

   public final Rectangle2D getBounds(AffineTransform transform) {
      if (this.mode == SGAbstractShape.Mode.EMPTY) {
         return new Rectangle2D.Float(0.0F, 0.0F, -1.0F, -1.0F);
      } else if (this.isTextEmpty()) {
         return new Rectangle2D.Float(this.location.x, this.location.y, 0.0F, 0.0F);
      } else {
         Rectangle2D rbounds = this.getRawBounds();
         Rectangle2D bounds = new Rectangle2D.Float();
         ((Rectangle2D)bounds).setRect(rbounds.getX() + (double)this.location.x, rbounds.getY() + (double)this.location.y, rbounds.getWidth(), rbounds.getHeight());
         if (transform != null && !transform.isIdentity()) {
            bounds = transform.createTransformedShape((Shape)bounds).getBounds2D();
         }

         ((Rectangle2D)bounds).setRect(((Rectangle2D)bounds).getX() - 2.0, ((Rectangle2D)bounds).getY() - 2.0, ((Rectangle2D)bounds).getWidth() + 4.0, ((Rectangle2D)bounds).getHeight() + 4.0);
         return (Rectangle2D)bounds;
      }
   }

   final Rectangle2D getLogicalBounds(AffineTransform transform) {
      if (this.isTextEmpty()) {
         return new Rectangle2D.Float(this.location.x, this.location.y, 0.0F, 0.0F);
      } else {
         FontRenderContext frc = this.getFontRenderContext();
         Object bounds;
         if (this.useLayout) {
            this.updateTextLayouts(frc);
            float maxXOffset = Float.MIN_VALUE;
            float minXOffset = Float.MAX_VALUE;

            float firstRowAscent;
            for(int i = 0; i < this.cachedLayouts.length; ++i) {
               float x1 = (float)this.cachedPositions[i].getX();
               firstRowAscent = x1 + this.cachedLayouts[i].getAdvance();
               minXOffset = Math.min(minXOffset, Math.min(x1, firstRowAscent));
               maxXOffset = Math.max(maxXOffset, Math.max(x1, firstRowAscent));
            }

            TextLayout firstRow = this.cachedLayouts[0];
            TextLayout lastRow = this.cachedLayouts[this.cachedLayouts.length - 1];
            firstRowAscent = this.getAscent(firstRow, frc);
            float lastRowAscent = lastRow == firstRow ? firstRowAscent : lastRow.getAscent();
            bounds = new Rectangle2D.Float(minXOffset, -firstRowAscent, maxXOffset - minXOffset, (float)this.cachedPositions[this.cachedPositions.length - 1].getY() + lastRowAscent + lastRow.getDescent() + lastRow.getLeading());
         } else {
            bounds = this.getGlyphVector(frc).getLogicalBounds();
         }

         ((Rectangle2D)bounds).setRect(((Rectangle2D)bounds).getX() + (double)this.location.x, ((Rectangle2D)bounds).getY() + (double)this.location.y, ((Rectangle2D)bounds).getWidth(), ((Rectangle2D)bounds).getHeight());
         if (transform != null && !transform.isIdentity()) {
            bounds = transform.createTransformedShape((Shape)bounds).getBounds2D();
         }

         return (Rectangle2D)bounds;
      }
   }

   private final Rectangle2D getRawBounds() {
      assert this.mode != SGAbstractShape.Mode.EMPTY;

      boolean aa = this.isAntialiased();
      AffineTransform transform = this.getCumulativeTransform();
      if (this.isDegradedTransform(transform)) {
         transform = IDENTITY_TRANSFORM;
      }

      if (this.rawBounds != null && this.lastAA == aa && isTransformCompatible(transform, this.lastTx)) {
         return this.rawBounds;
      } else {
         this.lastTx = transform;
         this.lastAA = aa;
         FontRenderContext frc = this.getFontRenderContext();
         if (this.mode == SGAbstractShape.Mode.FILL) {
            if (this.strikethrough && !this.underline) {
               this.rawBounds = this.getOutline(frc, 0.0F, 0.0F).getBounds2D();
            } else if (this.useLayout) {
               this.updateTextLayouts(frc);
               this.rawBounds = null;
               float overlineThickness = this.getOverlineThickness(frc);
               Rectangle2D overlineRect = this.overline ? new Rectangle2D.Float() : null;

               for(int i = 0; i < this.cachedLayouts.length; ++i) {
                  TextLayout tl = this.cachedLayouts[i];
                  Rectangle2D bounds = tl.getBounds();
                  Point2D position = this.cachedPositions[i];
                  bounds.setFrame(bounds.getX() + position.getX(), bounds.getY() + position.getY(), bounds.getWidth(), bounds.getHeight());
                  if (this.overline) {
                     Rectangle2D.union(bounds, this.getOverlineRect(position, tl, overlineThickness, overlineRect), bounds);
                  }

                  if (this.rawBounds == null) {
                     this.rawBounds = bounds;
                  } else {
                     Rectangle2D.union(this.rawBounds, bounds, this.rawBounds);
                  }
               }
            } else {
               this.rawBounds = this.getGlyphVector(frc).getVisualBounds();
            }
         } else {
            Shape s = this.getOutline(frc, 0.0F, 0.0F);
            s = this.drawStroke.createStrokedShape(s);
            this.rawBounds = s.getBounds2D();
         }

         this.rawBounds.setRect(this.rawBounds.getX(), this.rawBounds.getY() + (double)this.getYAdjustment(frc), this.rawBounds.getWidth(), this.rawBounds.getHeight());
         return this.rawBounds;
      }
   }

   private GlyphVector getGlyphVector(FontRenderContext frc) {
      return this.font.createGlyphVector(frc, this.text);
   }

   private float getOverlineThickness(FontRenderContext frc) {
      return this.overline ? this.font.getLineMetrics("", frc).getUnderlineThickness() : 0.0F;
   }

   private float getAscent(TextLayout tl, FontRenderContext frc) {
      return this.overline ? this.getAscent(tl, this.getOverlineThickness(frc)) : tl.getAscent();
   }

   private float getAscent(TextLayout tl, float overlineThickness) {
      float ascent = tl.getAscent();
      if (overlineThickness != 0.0F) {
         ascent = Math.max(ascent, -((float)tl.getBounds().getY() - overlineThickness));
      }

      return ascent;
   }

   private void updateTextLayouts(FontRenderContext frc) {
      if (this.isTextEmpty()) {
         throw new IllegalStateException("no text specified");
      } else {
         if (this.cachedLayouts == null || this.cachedFRC == null || !isFRCCompatible(this.cachedFRC, frc)) {
            this.cachedFRC = frc;
            Map<AttributedCharacterIterator.Attribute, Object> map = new HashMap();
            map.put(TextAttribute.FONT, this.getFont());
            if (this.underline) {
               map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            }

            if (this.strikethrough) {
               map.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            }

            if (this.eols == null && this.wrappingWidth == Float.MAX_VALUE) {
               this.cachedLayouts = new TextLayout[1];
               this.cachedLayouts[0] = new TextLayout(this.text, map, frc);
               this.cachedPositions = new Point2D.Float[]{new Point2D.Float(0.0F, 0.0F)};
            } else {
               int[] tmpEols = this.eols;
               if (tmpEols == null) {
                  tmpEols = new int[]{this.text.length()};
               }

               AttributedString as = new AttributedString(this.text, map);
               LineBreakMeasurer measurer = new LineBreakMeasurer(as.getIterator(), frc);
               int index = 0;
               List<TextLayout> rows = new ArrayList();
               Set<Integer> indexesToJustify = this.horizontalAlighment == SGText.HAlign.JUSTIFY ? new HashSet() : null;
               float maxAdvance = 0.0F;

               while(measurer.getPosition() < this.text.length()) {
                  TextLayout layout = measurer.nextLayout(this.wrappingWidth, tmpEols[index], false);
                  rows.add(layout);
                  float advance = layout.getAdvance();
                  maxAdvance = Math.max(maxAdvance, advance);
                  if (measurer.getPosition() == tmpEols[index]) {
                     ++index;
                  } else if (indexesToJustify != null) {
                     indexesToJustify.add(rows.size() - 1);
                  }
               }

               this.cachedLayouts = new TextLayout[rows.size()];
               rows.toArray(this.cachedLayouts);
               this.cachedPositions = new Point2D.Float[this.cachedLayouts.length];
               float yOffset = 0.0F;
               boolean isFirstRow = true;
               float overlineThickness = this.getOverlineThickness(frc);

               for(int i = 0; i < this.cachedPositions.length; ++i) {
                  TextLayout tl = this.cachedLayouts[i];
                  this.cachedLayouts[i] = tl;
                  if (!isFirstRow) {
                     yOffset += this.getAscent(tl, overlineThickness);
                  } else {
                     isFirstRow = false;
                  }

                  float xOffset = 0.0F;
                  switch (this.horizontalAlighment) {
                     case LEFT:
                        xOffset = 0.0F;
                        break;
                     case RIGHT:
                        xOffset = maxAdvance - tl.getAdvance();
                        break;
                     case CENTER:
                        xOffset = (maxAdvance - tl.getAdvance()) / 2.0F;
                        break;
                     case JUSTIFY:
                        xOffset = 0.0F;
                        if (indexesToJustify.contains(i)) {
                           this.cachedLayouts[i] = this.cachedLayouts[i].getJustifiedLayout(maxAdvance);
                        }
                        break;
                     default:
                        assert false;
                  }

                  this.cachedPositions[i] = new Point2D.Float(xOffset, yOffset);
                  yOffset += tl.getDescent() + tl.getLeading();
               }
            }
         }

      }
   }

   private FontRenderContext getFontRenderContext() {
      AffineTransform transform = this.getCumulativeTransform();
      if (this.isDegradedTransform(transform)) {
         transform = IDENTITY_TRANSFORM;
      }

      FontRenderContext frc = null;
      if (Utils.isAtLeastJava6) {
         try {
            frc = (FontRenderContext)FRC_CONSTRUCTOR.newInstance(transform, this.getAntialiasingHintInt(transform, (Graphics2D)null), RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
         } catch (Exception var4) {
         }
      }

      if (frc == null) {
         frc = new FontRenderContext(transform, this.isAntialiased(), false);
      }

      return frc;
   }

   private float getYAdjustment(FontRenderContext frc) {
      if (this.isTextEmpty()) {
         throw new IllegalStateException("no text specified");
      } else if (this.verticalAlignment != SGText.VAlign.BASELINE) {
         LineMetrics lm = this.font.getLineMetrics(this.text, frc);
         float yAdjustment;
         if (this.verticalAlignment == SGText.VAlign.TOP) {
            if (this.overline) {
               this.updateTextLayouts(frc);
               yAdjustment = this.getAscent(this.cachedLayouts[0], frc);
            } else {
               yAdjustment = lm.getAscent();
            }

            return yAdjustment - lm.getDescent();
         } else {
            yAdjustment = -lm.getDescent();
            if (this.useLayout) {
               this.updateTextLayouts(frc);
               if (this.cachedPositions.length > 1) {
                  yAdjustment = (float)((double)yAdjustment - this.cachedPositions[this.cachedPositions.length - 1].getY());
               }
            }

            return yAdjustment;
         }
      } else {
         return 0.0F;
      }
   }

   private Shape getOutline(FontRenderContext frc) {
      return this.getOutline(frc, this.location.x, this.location.y + this.getYAdjustment(frc));
   }

   private Shape getOutline(FontRenderContext frc, float x, float y) {
      if (this.cachedOutline == null || !isFRCCompatible(this.cachedFRC, frc)) {
         if (!this.useLayout) {
            GlyphVector gv = this.getGlyphVector(frc);
            this.cachedOutline = gv.getOutline();
         } else {
            GeneralPath gp = null;
            this.cachedOutline = null;
            AffineTransform at = null;
            this.updateTextLayouts(frc);
            Rectangle2D overlineRect = null;
            float overlineThickness = 0.0F;
            if (this.overline) {
               overlineThickness = this.getOverlineThickness(frc);
               overlineRect = new Rectangle2D.Float();
            }

            for(int i = 0; i < this.cachedLayouts.length; ++i) {
               TextLayout tl = this.cachedLayouts[i];
               Point2D.Float position = this.cachedPositions[i];
               if (at == null && (position.getX() != 0.0 || position.getY() != 0.0)) {
                  at = new AffineTransform();
               }

               if (at != null) {
                  at.setToTranslation(position.getX(), position.getY());
               }

               Shape outline = tl.getOutline(at);
               if (this.cachedOutline == null) {
                  this.cachedOutline = outline;
               } else {
                  if (gp == null) {
                     gp = new GeneralPath();
                     gp.append(this.cachedOutline, false);
                     this.cachedOutline = gp;
                  }

                  gp.append(outline, false);
               }

               if (this.overline) {
                  if (gp == null) {
                     gp = new GeneralPath();
                     gp.append(this.cachedOutline, false);
                     this.cachedOutline = gp;
                  }

                  gp.append(this.getOverlineRect(position, tl, overlineThickness, overlineRect), false);
               }
            }
         }

         this.cachedFRC = frc;
      }

      if (x == 0.0F && y == 0.0F) {
         return this.cachedOutline;
      } else {
         AffineTransform tx = AffineTransform.getTranslateInstance((double)x, (double)y);
         return tx.createTransformedShape(this.cachedOutline);
      }
   }

   public boolean contains(Point2D point) {
      if (!this.isTextEmpty() && this.mode != SGAbstractShape.Mode.EMPTY) {
         if (!super.contains(point)) {
            return false;
         } else {
            Shape s = this.getShape();
            if (this.mode == SGAbstractShape.Mode.FILL) {
               return s.contains(point);
            } else if (this.mode == SGAbstractShape.Mode.STROKE) {
               s = this.drawStroke.createStrokedShape(s);
               return s.contains(point);
            } else if (s.contains(point)) {
               return true;
            } else {
               s = this.drawStroke.createStrokedShape(s);
               return s.contains(point);
            }
         }
      } else {
         return false;
      }
   }

   private boolean isTextEmpty() {
      return this.text == null || this.text.length() == 0;
   }

   private static boolean isTransformCompatible(AffineTransform tr1, AffineTransform tr2) {
      if (tr1 == null) {
         tr1 = IDENTITY_TRANSFORM;
      }

      if (tr2 == null) {
         tr2 = IDENTITY_TRANSFORM;
      }

      return tr1.getScaleX() == tr2.getScaleX() && tr1.getScaleY() == tr2.getScaleY() && tr1.getShearX() == tr2.getShearX() && tr1.getShearY() == tr2.getShearY();
   }

   private static Object getAntiAliasingHint(FontRenderContext frc) {
      Object rv = frc.isAntiAliased() ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF;
      if (FRC_GET_ANTI_ALIASING_HINT != null) {
         try {
            rv = FRC_GET_ANTI_ALIASING_HINT.invoke(frc, (Object[])null);
         } catch (Exception var3) {
         }
      }

      return rv;
   }

   private static Object getFractionalMetricsHint(FontRenderContext frc) {
      Object rv = frc.usesFractionalMetrics() ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
      if (FRC_GET_FRACTIONAL_METRICS_HINT != null) {
         try {
            rv = FRC_GET_FRACTIONAL_METRICS_HINT.invoke(frc, (Object[])null);
         } catch (Exception var3) {
         }
      }

      return rv;
   }

   private static boolean isFRCCompatible(FontRenderContext frc1, FontRenderContext frc2) {
      if (frc1 == frc2) {
         return true;
      } else if (frc1 != null && frc2 == null || frc1 == null && frc2 != null) {
         return false;
      } else {
         return getAntiAliasingHint(frc1) == getAntiAliasingHint(frc2) && getFractionalMetricsHint(frc1) == getFractionalMetricsHint(frc2) ? isTransformCompatible(frc1.getTransform(), frc2.getTransform()) : false;
      }
   }

   private static boolean isDestTranslucent(Graphics2D g) {
      if (g != null && SG2D_GET_TRANSPARENCY != null && SD_SURFACEDATA != null) {
         try {
            Object sd = SD_SURFACEDATA.get(g);
            int transparency = (Integer)SG2D_GET_TRANSPARENCY.invoke(sd, (Object[])null);
            return transparency != 1;
         } catch (Throwable var3) {
         }
      }

      return false;
   }

   private boolean isDegradedTransform(AffineTransform transform) {
      if (transform != null) {
         double det = transform.getDeterminant();
         if (det == 0.0) {
            return true;
         } else {
            double side = (double)this.font.getSize();
            if ((this.mode == SGAbstractShape.Mode.STROKE || this.mode == SGAbstractShape.Mode.STROKE_FILL) && this.getDrawStroke() instanceof BasicStroke) {
               side += (double)((BasicStroke)this.getDrawStroke()).getLineWidth();
            }

            double lowerBound = 15.0 * side * Math.abs(det);
            return lowerBound <= Math.abs(transform.getShearX()) + Math.abs(transform.getScaleY()) || lowerBound <= Math.abs(transform.getScaleX()) + Math.abs(transform.getShearY());
         }
      } else {
         return false;
      }
   }

   static {
      antialiasingHintDT = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
      gammaHintDT = null;
      gammaHintKey = null;
      hintsTracker = null;
      Field theField = null;
      Method theMethod = null;
      float version = Utils.getJavaVersionAsFloat();
      if ((double)version == 160.1 || (double)version == 160.11) {
         try {
            Class sg2d = (Class)AccessController.doPrivileged(new PrivilegedExceptionAction<Class>() {
               public Class run() throws Exception {
                  return Class.forName("sun.java2d.SunGraphics2D");
               }
            });
            Class sdc = (Class)AccessController.doPrivileged(new PrivilegedExceptionAction<Class>() {
               public Class run() throws Exception {
                  return Class.forName("sun.java2d.SurfaceData");
               }
            });
            theMethod = sdc.getMethod("getTransparency", (Class[])null);
            final Field f = sg2d.getField("surfaceData");
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
               public Object run() {
                  f.setAccessible(true);
                  return null;
               }
            });
            theField = f;
         } catch (Exception var16) {
         }
      }

      SD_SURFACEDATA = theField;
      SG2D_GET_TRANSPARENCY = theMethod;
      Class<?> theClass = null;
      theMethod = null;

      try {
         theClass = Font.class;
         theMethod = theClass.getMethod("hasLayoutAttributes", (Class[])null);
      } catch (Exception var15) {
      }

      FONT_HAS_LAYOUT_ATTRIBUTES = theMethod;
      theClass = null;
      Constructor theConstructor = null;

      try {
         theClass = FontRenderContext.class;
         theConstructor = theClass.getConstructor(AffineTransform.class, Object.class, Object.class);
      } catch (Exception var14) {
      }

      FRC_CONSTRUCTOR = theConstructor;
      theMethod = null;

      try {
         theMethod = FontRenderContext.class.getMethod("getAntiAliasingHint", (Class[])null);
      } catch (Exception var13) {
      }

      FRC_GET_ANTI_ALIASING_HINT = theMethod;
      theMethod = null;

      try {
         theMethod = FontRenderContext.class.getMethod("getFractionalMetricsHint", (Class[])null);
      } catch (Exception var12) {
      }

      FRC_GET_FRACTIONAL_METRICS_HINT = theMethod;
      theClass = null;
      theField = null;
      Object theObject = null;
      RenderingHints.Key theKey = null;

      try {
         theClass = RenderingHints.class;
         theField = theClass.getField("KEY_TEXT_LCD_CONTRAST");
         theObject = theField.get((Object)null);
         if (theObject instanceof RenderingHints.Key) {
            theKey = (RenderingHints.Key)theObject;
         }
      } catch (Exception var11) {
      }

      KEY_TEXT_LCD_CONTRAST = theKey;
      theClass = null;
      theField = null;
      theObject = null;

      try {
         theClass = RenderingHints.class;
         theField = theClass.getField("VALUE_TEXT_ANTIALIAS_GASP");
         theObject = theField.get((Object)null);
      } catch (Exception var10) {
      }

      VALUE_TEXT_ANTIALIAS_GASP = theObject;
      IDENTITY_TRANSFORM = new AffineTransform();
   }

   private static class Selection {
      int start;
      int end;
      Paint drawPaint;
      Paint fillPaint;
      Shape shape;

      private Selection() {
      }
   }

   private static class DesktopAAHintsTracker implements PropertyChangeListener {
      private static final String propStr = "awt.font.desktophints";

      DesktopAAHintsTracker() {
         this.setHints();
         Toolkit tk = Toolkit.getDefaultToolkit();
         tk.addPropertyChangeListener("awt.font.desktophints", this);
         SGText.gammaHintKey = SGText.KEY_TEXT_LCD_CONTRAST;
      }

      private void setHints() {
         Toolkit tk = Toolkit.getDefaultToolkit();
         Map map = (Map)((Map)tk.getDesktopProperty("awt.font.desktophints"));
         Object aaHint = null;
         Object gammaHint = null;
         if (map != null) {
            aaHint = map.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            gammaHint = map.get(SGText.KEY_TEXT_LCD_CONTRAST);
         }

         if (aaHint == null) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;

            try {
               String os = System.getProperty("os.version", "unknown");
               if (os.startsWith("Windows")) {
                  aaHint = SGText.VALUE_TEXT_ANTIALIAS_GASP;
               }
            } catch (Throwable var6) {
            }
         }

         SGText.antialiasingHintDT = aaHint;
         SGText.gammaHintDT = gammaHint;
      }

      public void propertyChange(PropertyChangeEvent evt) {
         this.setHints();
      }
   }

   public static enum HAlign {
      LEFT,
      CENTER,
      RIGHT,
      JUSTIFY;
   }

   public static enum VAlign {
      BASELINE,
      TOP,
      BOTTOM;
   }
}
