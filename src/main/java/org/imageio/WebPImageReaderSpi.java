package org.imageio;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebPImageReaderSpi extends ImageReaderSpi {
    static final String[] names = new String[]{"webp"};
    static final String[] suffixes = new String[]{"webp"};
    static final String[] MIMETypes = new String[]{"image/webp"};
    static final String[] writerSpiNames = new String[]{"importer.imageio.WebPImageReader"};

    public WebPImageReaderSpi() {
        super("vp8decoder", "0.1", names, suffixes, MIMETypes, "importer.imageio.WebPImageReader",
                STANDARD_INPUT_TYPE, writerSpiNames, false, null, null, null, null, false, "importer.imageio" +
                        ".WebPMetadata_0.1", "importer.imageio.WebPMetadata_0.1", null, null);
    }

    public String getDescription(Locale locale) {
        return "WebP Image Reader";
    }

    public boolean canDecodeInput(Object input) {
        if(!(input instanceof ImageInputStream)) {
            return false;
        } else {
            ImageInputStream stream = (ImageInputStream)input;
            byte[] b = new byte[4];

            try {
                stream.mark();
                stream.readFully(b);
                stream.reset();
            } catch (IOException e) {
                return false;
            }


            return ( Arrays.equals( b, WebPImageType.RIFF.getValue() ) );
        }
    }

    public ImageReader createReaderInstance(Object extension) {
        return new WebPReader(this);
    }
}
