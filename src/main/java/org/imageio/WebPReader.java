package org.imageio;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import lombok.extern.slf4j.Slf4j;

import org.vp8Decoder.VP8Decoder;
import org.vp8Decoder.VP8Frame;

@Slf4j
public class WebPReader extends ImageReader {
    WebPImage imageRead;

    public WebPReader(ImageReaderSpi imageReader) {
        super(imageReader);
        imageRead = new WebPImage(new VP8Decoder());
    }

    public void setInput(Object input) {
        super.setInput(input);
        this.setStream(input);
    }

    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    private void checkIndex(int imageIndex) {
        if(imageIndex != 0) {
            throw new IndexOutOfBoundsException("bad image index");
        }
    }

    public void readHeader() throws IOException {
        if (imageRead.isHeaderDefined()) {
            return;
        }

        byte[] signature = new byte[4];
        imageRead.setHeaderDefined(true);
        ImageInputStream currentStream = imageRead.getStream();

        if(currentStream.equals(null)) {
            throw new IllegalStateException("No input stream provided");
        }

        try {
            currentStream.readFully(signature);
        } catch (IOException var11) {
            throw new IOException("Error reading RIFF signature", var11);
        }

        // File format information specification:
        //   https://developers.google.com/speed/webp/docs/riff_container
        if ( Arrays.equals( signature, WebPImageType.RIFF.getValue() ) ) {
            int frameSize;

            try {
                currentStream.readUnsignedByte();
                currentStream.readUnsignedByte();
                currentStream.readUnsignedByte();
                currentStream.readUnsignedByte();
            } catch (IOException var10) {
                throw new IOException("Error reading frame size 1", var10);
            }

            try {
                currentStream.readFully(signature);
            } catch (IOException var9) {
                throw new IOException("Error reading WEBP signature", var9);
            }

            if( Arrays.equals( signature, WebPImageType.WEBP.getValue() ) ) {
                try {
                    currentStream.readFully(signature);
                } catch (IOException var8) {
                    throw new IOException("Error reading VP8 signature", var8);
                }

                if( Arrays.equals( signature, WebPImageType.VP8_.getValue() ) || Arrays.equals( signature,
                        WebPImageType.VP8X.getValue() ) ) {
                    try {
                        //read image (vp8) data specified in: https://developers.google.com/speed/webp/docs/riff_container
                        frameSize = currentStream.readUnsignedByte();
                        frameSize += currentStream.readUnsignedByte() << 8;
                        frameSize += currentStream.readUnsignedByte() << 16;
                        frameSize += currentStream.readUnsignedByte() << 24;
                    } catch (IOException var7) {
                        throw new IOException("Error reading frame size 1", var7);
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("VP8 IMAGE DATA SIZE: " + frameSize);
                    }

                    int[] frame = new int[frameSize];

                    for(int x = 0; x < frameSize; ++x) {
                        try {
                            frame[x] = currentStream.readUnsignedByte();
                        } catch (IOException var6) {
                            throw new IOException("Error reading frame", var6);
                        }
                    }

                    imageRead.getDecoder().decodeFrame(frame, false);

                    // Set image width and heights
                    imageRead.setWidth(imageRead.getDecoder().getWidth());
                    imageRead.setHeight(imageRead.getDecoder().getHeight());
                } else {
                    throw new IOException("Bad VP8 signature!");
                }
            } else {
                throw new IOException("Bad WEBP signature!");
            }
        } else {
            throw new IOException("Bad RIFF signature!");
        }
    }

    public int getWidth(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        this.readHeader();
        return imageRead.getWidth();
    }

    public int getHeight(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        this.readHeader();
        return imageRead.getHeight();
    }

    public Iterator getImageTypes(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        this.readHeader();
        ImageTypeSpecifier imageType = null;
        byte datatype = 0;
        ArrayList l = new ArrayList();
        ColorSpace rgb = ColorSpace.getInstance(1000);
        int[] bandOffsets = new int[]{0, 1, 2};
        imageType = ImageTypeSpecifier.createInterleaved(rgb, bandOffsets, datatype, false, false);
        l.add(imageType);
        return l.iterator();
    }

    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        super.processImageStarted(0);

        readHeader(); // get image width and height

        getSourceRegion(param, imageRead.getWidth(), imageRead.getHeight());

        BufferedImage dst = getDestination(param, this.getImageTypes(0), imageRead.getWidth(), imageRead.getHeight());

        checkReadParamBandSettings(param, 3, dst.getSampleModel().getNumBands());

        WritableRaster imRas = dst.getWritableTile(0, 0);

        VP8Frame frame = imageRead.getDecoder().getFrame();
        int[][] YBuffer = frame.getYBuffer();
        int[][] UBuffer = frame.getUBuffer();
        int[][] VBuffer = frame.getVBuffer();

        for(int x = 0; x < imageRead.getDecoder().getWidth(); ++x) {
            for(int y = 0; y < imageRead.getDecoder().getHeight(); ++y) {
                int[] c = new int[3];
                int yy = YBuffer[x][y];
                int u = UBuffer[x / 2][y / 2];
                int v = VBuffer[x / 2][y / 2];
                c[0] = (int)(1.164D * (double)(yy - 16) + 1.596D * (double)(v - 128));
                c[1] = (int)(1.164D * (double)(yy - 16) - 0.813D * (double)(v - 128) - 0.391D * (double)(u - 128));
                c[2] = (int)(1.164D * (double)(yy - 16) + 2.018D * (double)(u - 128));

                int z = 0;
                while (z < 3) {
                    if(c[z] < 0) {
                        c[z] = 0;
                    } else if (c[z] > 255) {
                        c[z] = 255;
                    }
                    z++;
                }

                imRas.setPixel(x, y, c);
            }
        }

        super.processImageComplete();
        return dst;
    }

    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int i) throws IOException {
        return null;
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        this.setStream(input);
    }

    @Override
    public void setInput(Object input, boolean canStream) {
        super.setInput(input, canStream);
        this.setStream(input);
    }

    private void setStream(Object input) {
        if(input == null) {
            imageRead.setStream(null);
        } else if(input instanceof ImageInputStream) {
            imageRead.setStream((ImageInputStream)input);
        } else {
            throw new IllegalArgumentException("bad input stream");
        }
    }
}
