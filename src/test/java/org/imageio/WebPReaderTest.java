package org.imageio;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WebPReaderTest {

    @Mock
    private WebPImageReaderSpi webPImageReaderSpi;

    private WebPReader fixture;

    @Before
    public void setUp() {
        fixture = new WebPReader(webPImageReaderSpi);
    }

    @Test
    public void shouldReadLossyImageHeader() throws IOException {
        String inputImageFileName = "src/test/resources/lenna.lossy.webp";
        InputStream stream = createImageInputStream(inputImageFileName);
        ImageInputStream webPImageInputStream = ImageIO.createImageInputStream(stream);
        fixture.setInput(webPImageInputStream, false, false);
        fixture.readHeader();
        assertEquals(512, fixture.getWidth(0));
        assertEquals(512, fixture.getHeight(0));
    }

    @Test
    public void shouldReadLosslessImageHeader() throws IOException {
        String inputImageFileName = "src/test/resources/lossless.webp";
        InputStream stream = createImageInputStream(inputImageFileName);
        ImageInputStream webPImageInputStream = ImageIO.createImageInputStream(stream);
        fixture.setInput(webPImageInputStream, false, false);
        fixture.readHeader();
        assertEquals(1200, fixture.getWidth(0));
        assertEquals(800, fixture.getHeight(0));
    }

    @Test
    public void shouldGetWidth() throws IOException {
        String inputImageFileName = "src/test/resources/cntower.webp";
        InputStream stream = createImageInputStream(inputImageFileName);
        ImageInputStream webPImageInputStream = ImageIO.createImageInputStream(stream);
        fixture.setInput(webPImageInputStream, false, false);
        int width = fixture.getWidth(0);
        assertEquals(width, 600);
    }

    @Test
    public void shouldGetHeight() throws IOException {
        String inputImageFileName = "src/test/resources/sample.webp";
        InputStream stream = createImageInputStream(inputImageFileName);
        ImageInputStream webPImageInputStream = ImageIO.createImageInputStream(stream);
        fixture.setInput(webPImageInputStream, false, false);
        int width = fixture.getHeight(0);
        assertEquals(width, 368);
    }

    private InputStream createImageInputStream(String imageFileName) throws IOException {
        when(webPImageReaderSpi.getInputTypes()).thenReturn(new Class[]{javax.imageio.stream.ImageInputStream.class});
        File sourceImage = new File(imageFileName);
        return new BufferedInputStream(new FileInputStream(sourceImage));
    }
}