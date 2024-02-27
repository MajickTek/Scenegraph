package com.sun.scenario.scenegraph;

import java.awt.geom.AffineTransform;

class Tx3D {
   double[][] matrix;

   public Tx3D() {
      this.setToIdentity();
   }

   public Tx3D(Tx3D other) {
      this.matrix = new double[4][4];
      this.setTransform(other);
   }

   double[][] getMatrix() {
      return cloneMatrix(this.matrix);
   }

   void setMatrix(double[][] m) {
      this.matrix = cloneMatrix(m);
   }

   static double[][] cloneMatrix(double[][] m) {
      return new double[][]{{m[0][0], m[0][1], m[0][2], m[0][3]}, {m[1][0], m[1][1], m[1][2], m[1][3]}, {m[2][0], m[2][1], m[2][2], m[2][3]}, {m[3][0], m[3][1], m[3][2], m[3][3]}};
   }

   void setTransform(Tx3D other) {
      for(int i = 0; i < 4; ++i) {
         System.arraycopy(other.matrix[i], 0, this.matrix[i], 0, 4);
      }

   }

   public void setToIdentity() {
      this.matrix = new double[4][4];
      this.matrix[0][0] = this.matrix[1][1] = this.matrix[2][2] = this.matrix[3][3] = 1.0;
   }

   public boolean isIdentity() {
      return this.matrix[0][0] == 1.0 && this.matrix[0][1] == 0.0 && this.matrix[0][2] == 0.0 && this.matrix[0][3] == 0.0 && this.matrix[1][0] == 0.0 && this.matrix[1][1] == 1.0 && this.matrix[1][2] == 0.0 && this.matrix[1][3] == 0.0 && this.matrix[2][0] == 0.0 && this.matrix[2][1] == 0.0 && this.matrix[2][2] == 1.0 && this.matrix[2][3] == 0.0 && this.matrix[3][0] == 0.0 && this.matrix[3][1] == 0.0 && this.matrix[3][2] == 0.0 && this.matrix[3][3] == 1.0;
   }

   public Tx3D createInverse() {
      Tx3D s3d = new Tx3D();
      double det = this.getDeterminant();

      for(int row = 0; row < 4; ++row) {
         for(int col = 0; col < 4; ++col) {
            s3d.matrix[row][col] = this.cofactor(col, row) / det;
         }
      }

      return s3d;
   }

   public void transform(Point3D p) {
      double x = p.field_38;
      double y = p.field_39;
      double z = p.field_40;
      double w = p.field_41;
      p.field_38 = this.dot(this.matrix[0], x, y, z, w);
      p.field_39 = this.dot(this.matrix[1], x, y, z, w);
      p.field_40 = this.dot(this.matrix[2], x, y, z, w);
      p.field_41 = this.dot(this.matrix[3], x, y, z, w);
   }

   public void inverseTransform(Point3D p) {
      double x = p.field_38;
      double y = p.field_39;
      double z = p.field_40;
      double w = p.field_41;
      p.field_38 = this.cofactor(0, 0) * x + this.cofactor(1, 0) * y + this.cofactor(2, 0) * z + this.cofactor(3, 0) * w;
      p.field_39 = this.cofactor(0, 1) * x + this.cofactor(1, 1) * y + this.cofactor(2, 1) * z + this.cofactor(3, 1) * w;
      p.field_40 = this.cofactor(0, 2) * x + this.cofactor(1, 2) * y + this.cofactor(2, 2) * z + this.cofactor(3, 2) * w;
      p.field_41 = this.cofactor(0, 3) * x + this.cofactor(1, 3) * y + this.cofactor(2, 3) * z + this.cofactor(3, 3) * w;
   }

   public double getDeterminant() {
      return this.matrix[0][0] * this.minor(0, 0) - this.matrix[0][1] * this.minor(0, 1) + this.matrix[0][2] * this.minor(0, 2) - this.matrix[0][3] * this.minor(0, 3);
   }

   double cofactor(int row, int col) {
      return this.minor(row, col) * (1.0 - (double)(row + col) * 2.0);
   }

   double minor(int row, int col) {
      int r1 = (row + 1) % 4;
      int r2 = (row + 2) % 4;
      int r3 = (row + 3) % 4;
      int c1 = (col + 1) % 4;
      int c2 = (col + 2) % 4;
      int c3 = (col + 3) % 4;
      double[][] m = this.matrix;
      return m[r1][c1] * (m[r2][c2] * m[r3][c3] - m[r2][c3] * m[r3][c2]) + m[r1][c2] * (m[r2][c3] * m[r3][c1] - m[r2][c1] * m[r3][c3]) + m[r1][c3] * (m[r2][c1] * m[r3][c2] - m[r2][c2] * m[r3][c1]);
   }

   // $FF: renamed from: Tx (double, double, double) double
   public double method_0(double x, double y, double z) {
      return this.dot(this.matrix[0], x, y, z, 1.0) / this.dot(this.matrix[3], x, y, z, 1.0);
   }

   // $FF: renamed from: Ty (double, double, double) double
   public double method_1(double x, double y, double z) {
      return this.dot(this.matrix[1], x, y, z, 1.0) / this.dot(this.matrix[3], x, y, z, 1.0);
   }

   // $FF: renamed from: Tz (double, double, double) double
   public double method_2(double x, double y, double z) {
      return this.dot(this.matrix[2], x, y, z, 1.0) / this.dot(this.matrix[3], x, y, z, 1.0);
   }

   double dot(double[] row, double x, double y, double z, double w) {
      return x * row[0] + y * row[1] + z * row[2] + w * row[3];
   }

   private static double[][] concatenate(double[][] a, double[][] b) {
      double[][] result = new double[4][4];

      for(int r = 0; r < 4; ++r) {
         for(int c = 0; c < 4; ++c) {
            for(int i = 0; i < 4; ++i) {
               result[r][c] += a[r][i] * b[i][c];
            }
         }
      }

      return result;
   }

   public void concatenate(double[][] m) {
      this.matrix = concatenate(this.matrix, m);
   }

   public void concatenate(Tx3D s3d) {
      this.matrix = concatenate(this.matrix, s3d.matrix);
   }

   public void concatenate(AffineTransform at) {
      double m00 = at.getScaleX();
      double m01 = at.getShearX();
      double m02 = at.getTranslateX();
      double m10 = at.getShearY();
      double m11 = at.getScaleY();
      double m12 = at.getTranslateY();
      this.concat(m00, m01, 0.0, m02, m10, m11, 0.0, m12, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
   }

   public void preConcatenate(double[][] m) {
      this.matrix = concatenate(m, this.matrix);
   }

   public void preConcatenate(Tx3D s3d) {
      this.matrix = concatenate(s3d.matrix, this.matrix);
   }

   public void preConcatenate(AffineTransform at) {
      Tx3D s = new Tx3D();
      s.concatenate(at);
      this.matrix = concatenate(s.matrix, this.matrix);
   }

   public void concat(double b00, double b01, double b02, double b03, double b10, double b11, double b12, double b13, double b20, double b21, double b22, double b23, double b30, double b31, double b32, double b33) {
      this.matrix[0] = new double[]{this.dot(this.matrix[0], b00, b10, b20, b30), this.dot(this.matrix[0], b01, b11, b21, b31), this.dot(this.matrix[0], b02, b12, b22, b32), this.dot(this.matrix[0], b03, b13, b23, b33)};
      this.matrix[1] = new double[]{this.dot(this.matrix[1], b00, b10, b20, b30), this.dot(this.matrix[1], b01, b11, b21, b31), this.dot(this.matrix[1], b02, b12, b22, b32), this.dot(this.matrix[1], b03, b13, b23, b33)};
      this.matrix[2] = new double[]{this.dot(this.matrix[2], b00, b10, b20, b30), this.dot(this.matrix[2], b01, b11, b21, b31), this.dot(this.matrix[2], b02, b12, b22, b32), this.dot(this.matrix[2], b03, b13, b23, b33)};
      this.matrix[3] = new double[]{this.dot(this.matrix[3], b00, b10, b20, b30), this.dot(this.matrix[3], b01, b11, b21, b31), this.dot(this.matrix[3], b02, b12, b22, b32), this.dot(this.matrix[3], b03, b13, b23, b33)};
   }

   public void rotateAroundZ(double theta) {
      double sin = Math.sin(theta);
      double cos = Math.cos(theta);
      this.concat(cos, -sin, 0.0, 0.0, sin, cos, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
   }

   public void rotateAroundY(double theta) {
      double sin = Math.sin(theta);
      double cos = Math.cos(theta);
      this.concat(cos, 0.0, sin, 0.0, 0.0, 1.0, 0.0, 0.0, -sin, 0.0, cos, 0.0, 0.0, 0.0, 0.0, 1.0);
   }

   public void rotateAroundX(double theta) {
      double sin = Math.sin(theta);
      double cos = Math.cos(theta);
      this.concat(1.0, 0.0, 0.0, 0.0, 0.0, cos, -sin, 0.0, 0.0, sin, cos, 0.0, 0.0, 0.0, 0.0, 1.0);
   }

   public void translate(double tx, double ty, double tz) {
      this.concat(1.0, 0.0, 0.0, tx, 0.0, 1.0, 0.0, ty, 0.0, 0.0, 1.0, tz, 0.0, 0.0, 0.0, 1.0);
   }

   public void scale(double sx, double sy, double sz) {
      this.concat(sx, 0.0, 0.0, 0.0, 0.0, sy, 0.0, 0.0, 0.0, 0.0, sz, 0.0, 0.0, 0.0, 0.0, 1.0);
   }

   public void shearX(double shy, double shz) {
      this.concat(1.0, shy, shz, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
   }

   public void shearY(double shx, double shz) {
      this.concat(1.0, 0.0, 0.0, 0.0, shx, 1.0, shz, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
   }

   public void shearZ(double shx, double shy) {
      this.concat(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, shx, shy, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
   }

   public void rotateXYaroundZ(double x, double y, double theta) {
      this.translate(x, y, 0.0);
      this.rotateAroundZ(theta);
      this.translate(-x, -y, 0.0);
   }

   public void rotateXZaroundY(double x, double z, double theta) {
      this.translate(x, 0.0, z);
      this.rotateAroundY(theta);
      this.translate(-x, 0.0, -z);
   }

   public void rotateYZaroundX(double y, double z, double theta) {
      this.translate(0.0, y, z);
      this.rotateAroundX(theta);
      this.translate(0.0, -y, -z);
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("3D Surface transform matrix\n");
      this.append(this.matrix[0], sb);
      this.append(this.matrix[1], sb);
      this.append(this.matrix[2], sb);
      this.append(this.matrix[3], sb);
      return new String(sb);
   }

   void append(double[] row, StringBuffer sb) {
      sb.append(row[0]);
      sb.append("\t");
      sb.append(row[1]);
      sb.append("\t");
      sb.append(row[2]);
      sb.append("\t");
      sb.append(row[3]);
      sb.append("\n");
   }
}
