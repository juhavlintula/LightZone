/*
 * $RCSfile: MlibDivideIntoConstOpImage.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:55:54 $
 * $State: Exp $
 */
package com.lightcrafts.media.jai.mlib;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import com.lightcrafts.mediax.jai.ImageLayout;
import com.lightcrafts.mediax.jai.PointOpImage;
import java.util.Map;
import com.sun.medialib.mlib.*;
// import com.lightcrafts.media.jai.test.OpImageTester;

/**
 * An OpImage class that divides an image into a constant
 *
 */
final class MlibDivideIntoConstOpImage extends PointOpImage {
    private double[] constants;

    /**
     * Constructs an MlibDivideIntoConstOpImage. The image dimensions
     * are copied from the source image.  The tile grid layout,
     * SampleModel, and ColorModel may optionally be specified by an
     * ImageLayout object.
     *
     * @param source    a RenderedImage.
     * @param layout    an ImageLayout optionally containing the tile
     *                  grid layout, SampleModel, and ColorModel, or null.
     */
    public MlibDivideIntoConstOpImage(RenderedImage source,
                                      Map config,
                                      ImageLayout layout,
                                      double[] constants) {
        super(source, layout, config, true);
        this.constants = MlibUtils.initConstants(constants,
                                            getSampleModel().getNumBands());
        // Set flag to permit in-place operation.
        permitInPlaceOperation();
    }

    /**
     * Divide the pixel values of a rectangle into a given constant.
     * The sources are cobbled.
     *
     * @param sources   an array of sources, guarantee to provide all
     *                  necessary source data for computing the rectangle.
     * @param dest      a tile that contains the rectangle to be computed.
     * @param destRect  the rectangle within this OpImage to be processed.
     */
    protected void computeRect(Raster[] sources,
                               WritableRaster dest,
                               Rectangle destRect) {
        Raster source = sources[0];
        Rectangle srcRect = mapDestRect(destRect, 0);

        int formatTag = MediaLibAccessor.findCompatibleTag(sources,dest);

        MediaLibAccessor srcAccessor = 
            new MediaLibAccessor(source,srcRect,formatTag);
        MediaLibAccessor dstAccessor = 
            new MediaLibAccessor(dest,destRect,formatTag);

        mediaLibImage[] srcML = srcAccessor.getMediaLibImages();
        mediaLibImage[] dstML = dstAccessor.getMediaLibImages();

        switch (dstAccessor.getDataType()) {
        case DataBuffer.TYPE_BYTE:
        case DataBuffer.TYPE_USHORT:
        case DataBuffer.TYPE_SHORT:
        case DataBuffer.TYPE_INT:
            for (int i = 0; i < dstML.length; i++) {
                double[] mlconstants = dstAccessor.getDoubleParameters(i, constants);
                Image.ConstDiv(dstML[i], srcML[i], mlconstants);
            }
            break;

        case DataBuffer.TYPE_FLOAT:
        case DataBuffer.TYPE_DOUBLE:
            for (int i = 0; i < dstML.length; i++) {
                double[] mlconstants = dstAccessor.getDoubleParameters(i, constants);
                Image.ConstDiv_Fp(dstML[i], srcML[i], mlconstants);
            }
            break;

        default:
            String className = this.getClass().getName();
            throw new RuntimeException(className + JaiI18N.getString("Generic2"));
        }

        if (dstAccessor.isDataCopy()) {
            dstAccessor.clampDataArrays();
            dstAccessor.copyDataToRaster();
        }
    }

//     public static OpImage createTestImage(OpImageTester oit) {
//         double[] consts = { 255, 255, 255 };
//         return new MlibDivideIntoConstOpImage(oit.getSource(), null,
//                                               new ImageLayout(oit.getSource()),
//                                               consts);
//     }

//     // Calls a method on OpImage that uses introspection, to make this
//     // class, discover it's createTestImage() call, call it and then
//     // benchmark the performance of the created OpImage chain.
//     public static void main (String args[]) {
//         String classname = "com.lightcrafts.media.jai.mlib.MlibDivideIntoConstOpImage";
//         OpImageTester.performDiagnostics(classname,args);
//     }
}
