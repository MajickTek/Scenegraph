package com.sun.scenario.scenegraph.fx;

import com.sun.scenario.effect.Effect;
import com.sun.scenario.scenegraph.SGClip;
import com.sun.scenario.scenegraph.SGComposite;
import com.sun.scenario.scenegraph.SGEffect;
import com.sun.scenario.scenegraph.SGFilter;
import com.sun.scenario.scenegraph.SGNode;
import com.sun.scenario.scenegraph.SGRenderCache;
import com.sun.scenario.scenegraph.SGTransform;
import com.sun.scenario.scenegraph.SGWrapper;
import com.sun.scenario.scenegraph.event.SGNodeListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class FXNode extends SGWrapper {
   private static AffineTransform scratch = new AffineTransform();
   private static final ValPair zeroPair = new ValPair.Default(0.0, 0.0);
   private static final ValPair unitPair = new ValPair.Default(1.0, 1.0);
   private ValPair translation;
   private ValPair pivot;
   private double rotation;
   private ValPair scale;
   private AffineTransform affine;
   private SGTransform.Affine effectAffineNode;
   private SGTransform.Affine affineNode;
   private SGClip clipNode;
   private SGComposite compositeNode;
   private SGRenderCache cacheNode;
   private SGEffect effectNode;
   private SGNode leafNode;
   private FXNodeListener fxlistener;
   private boolean notifyDefaultLayoutBoundsChanges;
   private NodeListener parentListener;
   private NodeListener defaultListener;
   private NodeListener localListener;

   public FXNode(SGNode leaf) {
      this.translation = zeroPair;
      this.pivot = zeroPair;
      this.scale = unitPair;
      this.notifyDefaultLayoutBoundsChanges = true;
      this.parentListener = new NodeListener() {
         public void boundsChanged(SGNode src) {
            if (FXNode.this.fxlistener != null) {
               FXNode.this.fxlistener.boundsInParentChanged();
            }

         }
      };
      this.defaultListener = new NodeListener() {
         public void boundsChanged(SGNode src) {
            if (FXNode.this.notifyDefaultLayoutBoundsChanges && FXNode.this.fxlistener != null) {
               FXNode.this.fxlistener.defaultLayoutBoundsChanged();
            }

            if (this.node != null) {
               Rectangle2D bounds = this.node.getBounds();
               FXNode.this.pivot = FXNode.this.pivot.setX(bounds.getX() + bounds.getWidth() / 2.0);
               FXNode.this.pivot = FXNode.this.pivot.setY(bounds.getY() + bounds.getHeight() / 2.0);
               FXNode.this.updateEffectTransform();
            } else {
               FXNode.this.pivot = FXNode.this.pivot.setX(0.0).setY(0.0);
               FXNode.this.updateEffectTransform();
            }

         }
      };
      this.localListener = new NodeListener() {
         public void boundsChanged(SGNode src) {
            if (FXNode.this.fxlistener != null) {
               FXNode.this.fxlistener.boundsInLocalChanged();
            }

         }

         public void transformedBoundsChanged(SGNode src) {
            if (FXNode.this.fxlistener != null) {
               FXNode.this.fxlistener.boundsInSceneChanged();
            }

         }
      };
      if (leaf == null) {
         throw new NullPointerException("leaf cannot be null");
      } else {
         this.leafNode = leaf;
         this.addNodeListener(this.parentListener);
         this.updateTree();
      }
   }

   public void addFXNodeListener(FXNodeListener listener) {
      this.fxlistener = listener;
   }

   public void setNotifyOnDefaultLayoutBoundsChanges(boolean b) {
      this.notifyDefaultLayoutBoundsChanges = b;
      if (b) {
         this.updateTree();
      }

   }

   private SGNode addFilter(SGFilter filter, SGNode child) {
      if (filter != null) {
         filter.setChild((SGNode)child);
         child = filter;
      }

      return (SGNode)child;
   }

   private void updateTree() {
      SGNode localNode = null;
      if (this.clipNode != null) {
         localNode = this.clipNode;
      } else if (this.compositeNode != null) {
         localNode = this.compositeNode;
      } else if (this.cacheNode != null) {
         localNode = this.cacheNode;
      } else if (this.effectNode != null) {
         localNode = this.effectNode;
      } else {
         localNode = this.leafNode;
      }

      if (localNode != this.localListener.node && this.localListener.node != null) {
         this.localListener.node.removeNodeListener(this.localListener);
         this.localListener.node = null;
      }

      SGNode defaultLayoutNode = this.affineNode == null ? localNode : this.affineNode;
      if (defaultLayoutNode != this.defaultListener.node && this.defaultListener.node != null) {
         this.defaultListener.node.removeNodeListener(this.defaultListener);
         this.defaultListener.node = null;
      }

      SGNode root = this.leafNode;
      root = this.addFilter(this.effectNode, root);
      root = this.addFilter(this.cacheNode, root);
      root = this.addFilter(this.compositeNode, root);
      root = this.addFilter(this.clipNode, root);
      root = this.addFilter(this.affineNode, root);
      root = this.addFilter(this.effectAffineNode, root);
      this.setRoot(root);
      if (this.localListener.node == null) {
         this.localListener.node = (SGNode)localNode;
         ((SGNode)localNode).addNodeListener(this.localListener);
      }

      if (this.defaultListener.node == null) {
         this.defaultListener.node = (SGNode)defaultLayoutNode;
         ((SGNode)defaultLayoutNode).addNodeListener(this.defaultListener);
      }

      if (this.fxlistener != null) {
         this.fxlistener.boundsInLocalChanged();
         this.fxlistener.boundsInParentChanged();
         this.fxlistener.boundsInSceneChanged();
         if (this.notifyDefaultLayoutBoundsChanges) {
            this.fxlistener.defaultLayoutBoundsChanged();
         }
      }

   }

   public SGNode getLeaf() {
      return this.leafNode;
   }

   public void remove(SGNode node) {
   }

   public Rectangle2D getBoundsInScene() {
      return this.localListener.node == null ? this.leafNode.getTransformedBounds() : this.localListener.node.getTransformedBounds();
   }

   public Rectangle2D getBoundsInParent() {
      return this.getBounds();
   }

   public Rectangle2D getDefaultLayoutBounds() {
      return this.defaultListener.node == null ? null : this.defaultListener.node.getBounds();
   }

   public Rectangle2D getBoundsInLocal() {
      return this.getLocalNode().getBounds();
   }

   public double getPivotX() {
      return this.pivot.getX();
   }

   public double getPivotY() {
      return this.pivot.getY();
   }

   public SGNode getLocalNode() {
      return this.localListener.node == null ? this.leafNode : this.localListener.node;
   }

   public float getOpacity() {
      return this.compositeNode == null ? 1.0F : this.compositeNode.getOpacity();
   }

   public void setOpacity(float opacity) {
      if (opacity == 1.0F) {
         this.compositeNode = null;
         this.updateTree();
      } else if (this.compositeNode == null) {
         this.compositeNode = new SGComposite();
         this.updateTree();
         this.compositeNode.setOpacity(opacity);
      } else {
         this.compositeNode.setOpacity(opacity);
      }

   }

   public Effect getEffect() {
      return this.effectNode == null ? null : this.effectNode.getEffect();
   }

   public void setEffect(Effect effect) {
      if (effect == null) {
         this.effectNode = null;
         this.updateTree();
      } else if (this.effectNode == null) {
         this.effectNode = new SGEffect();
         this.updateTree();
         this.effectNode.setEffect(effect);
      } else {
         this.effectNode.setEffect(effect);
      }

   }

   public void setCachedAsBitmap(boolean cached) {
      if (cached != (this.cacheNode != null)) {
         this.cacheNode = cached ? new SGRenderCache() : null;
         this.updateTree();
      }
   }

   public void setClipNode(FXNode fxClipNode) {
      if (this.clipNode == null && fxClipNode != null) {
         this.clipNode = new SGClip();
         this.updateTree();
         this.clipNode.setClipNode(fxClipNode);
      } else if (this.clipNode != null && fxClipNode == null) {
         this.clipNode.setClipNode((SGNode)null);
         this.clipNode = null;
         this.updateTree();
      } else if (this.clipNode != null) {
         this.clipNode.setClipNode(fxClipNode);
      }

   }

   public void setTranslateX(double tx) {
      this.translation = this.translation.setX(tx);
      this.updateEffectTransform();
   }

   public double getTranslateX() {
      return this.translation.getX();
   }

   public void setTranslateY(double ty) {
      this.translation = this.translation.setY(ty);
      this.updateEffectTransform();
   }

   public double getTranslateY() {
      return this.translation.getY();
   }

   public void setRotation(double rot) {
      this.rotation = rot;
      this.updateEffectTransform();
   }

   public double getRotation() {
      return this.rotation;
   }

   public void setScaleX(double sx) {
      this.scale = this.scale.setX(sx);
      this.updateEffectTransform();
   }

   public double getScaleX() {
      return this.scale.getX();
   }

   public void setScaleY(double sy) {
      this.scale = this.scale.setY(sy);
      this.updateEffectTransform();
   }

   public double getScaleY() {
      return this.scale.getY();
   }

   public void setTransform(AffineTransform transform) {
      if (transform != null && !transform.isIdentity()) {
         if (this.affine == null) {
            this.affine = new AffineTransform();
         }

         this.affine.setTransform(transform);
         if (this.affine.isIdentity()) {
            this.affine = null;
         }
      } else {
         if (this.affine == null) {
            return;
         }

         this.affine = null;
      }

      this.updateAffine();
   }

   public AffineTransform getLeafTransform() {
      AffineTransform transform = new AffineTransform();
      if (this.effectAffineNode != null) {
         transform.concatenate(this.effectAffineNode.createAffineTransform());
      }

      if (this.affineNode != null) {
         transform.concatenate(this.affineNode.createAffineTransform());
      }

      return transform;
   }

   private void updateAffine() {
      boolean needAffine = this.affine != null && !this.affine.isIdentity();
      if (this.affineNode != null || needAffine) {
         if (this.affineNode != null && !needAffine) {
            this.affineNode = null;
            this.updateTree();
         } else if (this.affineNode == null && needAffine) {
            this.affineNode = SGTransform.createAffine(this.affine, (SGNode)null);
            this.updateTree();
         } else if (this.affineNode != null && needAffine) {
            this.affineNode.setAffine(this.affine);
         }

      }
   }

   private void updateEffectTransform() {
      boolean update = false;
      synchronized(scratch) {
         scratch.setToIdentity();
         if (!this.translation.isDefault()) {
            scratch.translate(this.translation.getX(), this.translation.getY());
         }

         if (!this.pivot.isDefault()) {
            scratch.translate(this.pivot.getX(), this.pivot.getY());
         }

         if (this.rotation != 0.0) {
            scratch.rotate(this.rotation);
         }

         if (!this.scale.isDefault()) {
            scratch.scale(this.scale.getX(), this.scale.getY());
         }

         if (!this.pivot.isDefault()) {
            scratch.translate(-this.pivot.getX(), -this.pivot.getY());
         }

         if (this.effectAffineNode != null && scratch.isIdentity()) {
            this.effectAffineNode = null;
            update = true;
         } else if (this.effectAffineNode != null && !scratch.isIdentity()) {
            this.effectAffineNode.setAffine(scratch);
         } else {
            if (this.effectAffineNode != null || scratch.isIdentity()) {
               return;
            }

            this.effectAffineNode = SGTransform.createAffine(scratch, (SGNode)null);
            update = true;
         }
      }

      if (update) {
         this.updateTree();
      }

   }

   private class NodeListener implements SGNodeListener {
      protected SGNode node;

      private NodeListener() {
      }

      public void boundsChanged(SGNode src) {
      }

      public void transformedBoundsChanged(SGNode src) {
      }
   }

   private static class ValPair {
      // $FF: renamed from: x double
      private double field_27;
      // $FF: renamed from: y double
      private double field_28;
      private ValPair defaultval;

      protected ValPair(double x, double y) {
         this.field_27 = x;
         this.field_28 = y;
         this.defaultval = this;
      }

      protected ValPair(ValPair vp) {
         this.field_27 = vp.field_27;
         this.field_28 = vp.field_28;
         this.defaultval = vp;
      }

      public boolean isDefault() {
         return this.field_27 == this.defaultval.field_27 && this.field_28 == this.defaultval.field_28;
      }

      public final double getX() {
         return this.field_27;
      }

      public ValPair setX(double x) {
         this.field_27 = x;
         return this;
      }

      public final double getY() {
         return this.field_28;
      }

      public ValPair setY(double y) {
         this.field_28 = y;
         return this;
      }

      public String toString() {
         return "ValPair [x=" + this.getX() + ", y=" + this.getY() + "]";
      }

      private static class Default extends ValPair {
         public Default(double x, double y) {
            super(x, y);
         }

         public boolean isDefault() {
            return true;
         }

         private ValPair newInstance() {
            return new ValPair(this);
         }

         public ValPair setX(double x) {
            return (ValPair)(this.getX() == x ? this : this.newInstance().setX(x));
         }

         public ValPair setY(double y) {
            return (ValPair)(this.getY() == y ? this : this.newInstance().setY(y));
         }
      }
   }
}
