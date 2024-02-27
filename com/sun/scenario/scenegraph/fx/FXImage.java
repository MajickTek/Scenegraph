package com.sun.scenario.scenegraph.fx;

import com.sun.scenario.scenegraph.SGImage;
import java.awt.Image;
import java.awt.geom.Point2D;

public class FXImage extends FXNode {
   private SGImage imageNode = (SGImage)this.getLeaf();

   public FXImage() {
      super(new SGImage());
   }

   public final Image getImage() {
      return this.imageNode.getImage();
   }

   public void setImage(Image image) {
      this.imageNode.setImage(image);
   }

   public final Point2D getLocation(Point2D rv) {
      return this.imageNode.getLocation(rv);
   }

   public final Point2D getLocation() {
      return this.imageNode.getLocation((Point2D)null);
   }

   public void setLocation(Point2D location) {
      this.imageNode.setLocation(location);
   }

   public final Object getInterpolationHint() {
      return this.imageNode.getInterpolationHint();
   }

   public void setInterpolationHint(Object hint) {
      this.imageNode.setInterpolationHint(hint);
   }
}
