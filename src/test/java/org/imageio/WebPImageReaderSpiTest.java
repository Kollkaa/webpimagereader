package org.imageio;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.junit.Before;
import org.junit.Test;

public class WebPImageReaderSpiTest {

    private WebPImageReaderSpi fixture;

    @Before
    public void setUp() {
        fixture = new WebPImageReaderSpi();
    }

    @Test
    public void shouldAbleToDecodeInput() throws Exception {
        String inputImageFileName = "src/test/resources/cntower.webp";
        InputStream stream = createImageInputStream(inputImageFileName);
        ImageInputStream firstWebpImage = ImageIO.createImageInputStream(stream);
        boolean canDecode = fixture.canDecodeInput(firstWebpImage);
        assertTrue(canDecode);
    }

    private InputStream createImageInputStream(String imageFileName) throws IOException {
        File sourceImage = new File(imageFileName);
        return new BufferedInputStream(new FileInputStream(sourceImage));
    }

}