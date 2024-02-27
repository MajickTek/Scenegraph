package com.sun.scenario.scenegraph;

import java.awt.geom.Point2D;

class Point3D extends Point2D {
   // $FF: renamed from: x double
   double field_38;
   // $FF: renamed from: y double
   double field_39;
   // $FF: renamed from: z double
   double field_40;
   // $FF: renamed from: w double
   double field_41;

   public Point3D() {
      this.field_41 = 1.0;
   }

   public Point3D(Point2D p) {
      this.setLocation(p);
   }

   public Point3D(Point3D p) {
      this.setLocation(p);
   }

   public Point3D(double x, double y, double z) {
      this.setLocation(x, y, z, 1.0);
   }

   public Point3D(double x, double y, double z, double w) {
      this.setLocation(x, y, z, w);
   }

   public void setLocation(Point2D p) {
      this.setLocation(p.getX(), p.getY(), 0.0, 1.0);
   }

   public void setLocation(Point3D p) {
      this.setLocation(p.field_38, p.field_39, p.field_40, p.field_41);
   }

   public void setLocation(double x, double y) {
      this.field_38 = x;
      this.field_39 = y;
      this.field_40 = 0.0;
      this.field_41 = 1.0;
   }

   public void setLocation(double x, double y, double z) {
      this.field_38 = x;
      this.field_39 = y;
      this.field_40 = z;
      this.field_41 = 1.0;
   }

   public void setLocation(double x, double y, double z, double w) {
      this.field_38 = x;
      this.field_39 = y;
      this.field_40 = z;
      this.field_41 = w;
   }

   public double getX() {
      return this.field_38 / this.field_41;
   }

   public double getY() {
      return this.field_39 / this.field_41;
   }

   public double getZ() {
      return this.field_40 / this.field_41;
   }

   public void normalize() {
      if (this.field_41 != 1.0) {
         this.field_38 /= this.field_41;
         this.field_39 /= this.field_41;
         this.field_40 /= this.field_41;
         this.field_41 = 1.0;
      }

   }

   public Point3D minus(Point3D sub) {
      return new Point3D(this.getX() - sub.getX(), this.getY() - sub.getY(), this.getZ() - sub.getZ());
   }

   public Point3D cross(Point3D other) {
      double tx = this.getX();
      double ty = this.getY();
      double tz = this.getZ();
      double ox = other.getX();
      double oy = other.getY();
      double oz = other.getZ();
      return new Point3D(oy * tz - oz * ty, oz * tx - ox * tz, ox * ty - oy * tx);
   }

   public String toString() {
      return "Point3D(" + this.field_38 + ", " + this.field_39 + ", " + this.field_40 + ", " + this.field_41 + ")";
   }
}
