/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.scenario.effect;

import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Flar
 */
public class PerspectiveTransform extends CoreEffect {
    private float tx[][] = new float[3][3];
    private float itx[][] = new float[3][3];
    private float xmin, ymin, xmax, ymax;
    private float txmin, tymin, txmax, tymax;

    public PerspectiveTransform() {
        this(new Source(true));
    }
    
    public PerspectiveTransform(Effect input) {
        super(input);
        updatePeerKey("PerspectiveTransform");
        setTransform(1, 0, 0,
                     0, 1, 0,
                     0, 0, 1);
    }

    /**
     * Sets the perspective transform to the indicated matrix values.
     * The transform will map source coordinates to the destination using
     * the following matrix formula:
     * <pre>
     *     [ dx ]   [  m00  m01  m02  ] [ sx ]   [ m00*sx + m01*sy + m02 ]
     *     [ dy ] = [  m10  m11  m12  ] [ sy ] = [ m10*sx + m11*sy + m12 ]
     *     [ dw ]   [  m20  m21  m22  ] [  1 ]   [ m20*sx + m21*sy + m22 ]
     * 
     * Final destination coordinate is (dx/dw, dy/dh)
     * </pre>
     *
     * @param m00 The m00 entry of the perpective transform matrix.
     * @param m01 The m01 entry of the perpective transform matrix.
     * @param m02 The m02 entry of the perpective transform matrix.
     * @param m10 The m10 entry of the perpective transform matrix.
     * @param m11 The m11 entry of the perpective transform matrix.
     * @param m12 The m12 entry of the perpective transform matrix.
     * @param m20 The m20 entry of the perpective transform matrix.
     * @param m21 The m21 entry of the perpective transform matrix.
     * @param m22 The m22 entry of the perpective transform matrix.
     */
    public void setTransform(float m00, float m01, float m02,
                             float m10, float m11, float m12,
                             float m20, float m21, float m22)
    {
        float old[][] = tx;
        tx = new float[][] {
            { m00, m01, m02 },
            { m10, m11, m12 },
            { m20, m21, m22 },
        };
        update(old);
    }

    /**
     * Sets the transform to map the unit square to the indicated
     * quadrilateral coordinates.
     * The resulting perspective transform will perform the following
     * coordinate mappings:
     * <pre>
     *     T(0, 0) = (ulx, uly)
     *     T(0, 1) = (urx, ury)
     *     T(1, 1) = (lrx, lry)
     *     T(1, 0) = (llx, lly)
     * </pre>
     * Note that the upper left corner of the unit square {@code (0, 0)}
     * is mapped to the coordinates specified by {@code (ulx, uly)} and
     * so on around the unit square in a clockwise direction.
     *
     * @param ulx The X coordinate to which {@code (0, 0)} is mapped.
     * @param uly The Y coordinate to which {@code (0, 0)} is mapped.
     * @param urx The X coordinate to which {@code (1, 0)} is mapped.
     * @param ury The Y coordinate to which {@code (1, 0)} is mapped.
     * @param lrx The X coordinate to which {@code (1, 1)} is mapped.
     * @param lry The Y coordinate to which {@code (1, 1)} is mapped.
     * @param llx The X coordinate to which {@code (0, 1)} is mapped.
     * @param lly The Y coordinate to which {@code (0, 1)} is mapped.
     */
    public void setUnitQuadMapping(float ulx, float uly,
                                   float urx, float ury,
                                   float lrx, float lry,
                                   float llx, float lly)
    {
        float dx3 = ulx - urx + lrx - llx;
        float dy3 = uly - ury + lry - lly;
        float old[][] = tx;
        tx = new float[3][3];

        tx[2][2] = 1.0F;

        if ((dx3 == 0.0F) && (dy3 == 0.0F)) { // TODO: use tolerance
            tx[0][0] = urx - ulx;
            tx[0][1] = lrx - urx;
            tx[0][2] = ulx;
            tx[1][0] = ury - uly;
            tx[1][1] = lry - ury;
            tx[1][2] = uly;
            tx[2][0] = 0.0F;
            tx[2][1] = 0.0F;
        } else {
            float dx1 = urx - lrx;
            float dy1 = ury - lry;
            float dx2 = llx - lrx;
            float dy2 = lly - lry;

            float invdet = 1.0F/(dx1*dy2 - dx2*dy1);
            tx[2][0] = (dx3*dy2 - dx2*dy3)*invdet;
            tx[2][1] = (dx1*dy3 - dx3*dy1)*invdet;
            tx[0][0] = urx - ulx + tx[2][0]*urx;
            tx[0][1] = llx - ulx + tx[2][1]*llx;
            tx[0][2] = ulx;
            tx[1][0] = ury - uly + tx[2][0]*ury;
            tx[1][1] = lly - uly + tx[2][1]*lly;
            tx[1][2] = uly;
        }
        update(old);
    }

    /**
     * Sets the transform to map the specified square to the indicated
     * quadrilateral coordinates.
     * The resulting perspective transform will perform the following
     * coordinate mappings:
     * <pre>
     * Assuming:
     *     sqx0 = sqx;
     *     sqy0 = sqy;
     *     sqx1 = sqx+sqw;
     *     sqy1 = sqy+sqh;
     *     T(sqx0, sqy0) = (ulx, uly)
     *     T(sqx0, sqy1) = (urx, ury)
     *     T(sqx1, sqy1) = (lrx, lry)
     *     T(sqx1, sqyy) = (llx, lly)
     * </pre>
     * Note that the upper left corner of the square {@code (sqx, sqy)}
     * is mapped to the coordinates specified by {@code (ulx, uly)} and
     * so on around the specified square in a clockwise direction.
     *
     * @param sqx The X coordinate of the upper left corner of the square.
     * @param sqy The Y coordinate of the upper left corner of the square.
     * @param sqw The width of the upper left corner of the square.
     * @param sqh The height of the upper left corner of the square.
     * @param ulx The X coordinate to which {@code (sqx0, sqy0)} is mapped.
     * @param uly The Y coordinate to which {@code (sqx0, sqy0)} is mapped.
     * @param urx The X coordinate to which {@code (sqx1, sqy0)} is mapped.
     * @param ury The Y coordinate to which {@code (sqx1, sqy0)} is mapped.
     * @param lrx The X coordinate to which {@code (sqx1, sqy1)} is mapped.
     * @param lry The Y coordinate to which {@code (sqx1, sqy1)} is mapped.
     * @param llx The X coordinate to which {@code (sqx0, sqy1)} is mapped.
     * @param lly The Y coordinate to which {@code (sqx0, sqy1)} is mapped.
     */
    public void setSquareToQuadMapping(float sqx, float sqy,
                                       float sqw, float sqh,
                                       float ulx, float uly,
                                       float urx, float ury,
                                       float lrx, float lry,
                                       float llx, float lly)
    {
        setUnitQuadMapping((ulx-sqx) / sqw, (uly-sqy) / sqh,
                           (urx-sqx) / sqw, (ury-sqy) / sqh,
                           (lrx-sqx) / sqw, (lry-sqy) / sqh,
                           (llx-sqx) / sqw, (lly-sqy) / sqh);
    }

    public float[][] getTX() {
        return tx;
    }

    public float[][] getITX() {
        return itx;
    }

    public float getMinX() {
        return txmin;
    }
    
    public float getMinY() {
        return tymin;
    }
    
    public float getMaxX() {
        return txmax;
    }
    
    public float getMaxY() {
        return tymax;
    }

    private void update(float old[][]) {
        firePropertyChange("transform", old, new float[][] {
            { tx[0][0], tx[0][1], tx[0][2] },
            { tx[1][0], tx[1][1], tx[1][2] },
            { tx[2][0], tx[2][1], tx[2][2] },
        });
        // First calculate the matrix inversion
        float det = get3x3Determinant(tx);
        if (Math.abs(det) < 1e-10) {
            itx[0][0] = itx[1][0] = itx[2][0] = 0f;
            itx[0][1] = itx[1][1] = itx[2][1] = 0f;
            itx[0][2] = itx[1][2] = -1f;
            itx[2][2] = 1f;
        } else {
            float invdet = 1.0f / det;
            // Note that we calculate the matrix subdeterminants in
            // row-column order, but we store them into the inverted
            // matrix in column-row order.  This performs the transpose
            // operation needed for matrix inversion as we go.
            itx[0][0] = invdet * (tx[1][1]*tx[2][2] - tx[1][2]*tx[2][1]);
            itx[1][0] = invdet * (tx[1][2]*tx[2][0] - tx[1][0]*tx[2][2]); // flipped sign
            itx[2][0] = invdet * (tx[1][0]*tx[2][1] - tx[1][1]*tx[2][0]);
            itx[0][1] = invdet * (tx[0][2]*tx[2][1] - tx[0][1]*tx[2][2]); // flipped sign
            itx[1][1] = invdet * (tx[0][0]*tx[2][2] - tx[0][2]*tx[2][0]);
            itx[2][1] = invdet * (tx[0][1]*tx[2][0] - tx[0][0]*tx[2][1]); // flipped sign
            itx[0][2] = invdet * (tx[0][1]*tx[1][2] - tx[0][2]*tx[1][1]);
            itx[1][2] = invdet * (tx[0][2]*tx[1][0] - tx[0][0]*tx[1][2]); // flipped sign
            itx[2][2] = invdet * (tx[0][0]*tx[1][1] - tx[0][1]*tx[1][0]);
        }
        
        // Next save the transformed unit square bounds:
        float corners[] = { 0, 0, 1, 0, 1, 1, 0, 1 };
        for (int i = 0; i < corners.length; i += 2) {
            float x = corners[i];
            float y = corners[i+1];
            float px = x * tx[0][0] + y * tx[0][1] + tx[0][2];
            float py = x * tx[1][0] + y * tx[1][1] + tx[1][2];
            float pw = x * tx[2][0] + y * tx[2][1] + tx[2][2];
            corners[i] = px / pw;
            corners[i+1] = py / pw;
        }
        xmin = xmax = corners[0];
        ymin = ymax = corners[1];
        for (int i = 2; i < corners.length; i += 2) {
            xmin = Math.min(xmin, corners[i]);
            xmax = Math.max(xmax, corners[i]);
            ymin = Math.min(ymin, corners[i+1]);
            ymax = Math.max(ymax, corners[i+1]);
        }
    }

    public static float get3x3Determinant(float m[][]) {
        return (  (m[0][0] * ((m[1][1] * m[2][2]) - (m[1][2] * m[2][1]))) -
                  (m[0][1] * ((m[1][0] * m[2][2]) - (m[1][2] * m[2][0]))) +
                  (m[0][2] * ((m[1][0] * m[2][1]) - (m[1][1] * m[2][0]))) );
    }

    public Rectangle2D getBounds() {
        Rectangle2D r = getInputs().get(0).getBounds();
        float bxmin = (float) Math.floor(r.getX() + xmin * r.getWidth());
        float bymin = (float) Math.floor(r.getY() + ymin * r.getHeight());
        float bxmax = (float) Math.ceil(r.getX() + xmax * r.getWidth());
        float bymax = (float) Math.ceil(r.getY() + ymax * r.getHeight());
        // TODO - we calculate xyminmax rounded to the outer
        // "pixel boundaries" to prevent jiggling of the image when
        // they don't map exactly to pixels.
        // But, doing this calculation here is tenuous as it assumes that
        // this method will be called right before the effect is rendered...
        txmin = (float) ((bxmin - r.getX()) / r.getWidth());
        tymin = (float) ((bymin - r.getY()) / r.getHeight());
        txmax = (float) ((bxmax - r.getX()) / r.getWidth());
        tymax = (float) ((bymax - r.getY()) / r.getHeight());
        r.setFrameFromDiagonal(bxmin, bymin, bxmax, bymax);
        return r;
    }

    @Override
    public Image filter(GraphicsConfiguration config) {
        return filterInputs(config, 0).getImage();
    }
}
