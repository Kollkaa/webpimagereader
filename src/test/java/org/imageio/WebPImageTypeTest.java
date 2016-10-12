package org.imageio;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class WebPImageTypeTest {

    private static final byte[] TYPE_RIFF = new byte[]{ 'R', 'I', 'F', 'F' };
    private static final byte[] TYPE_WEBP = new byte[]{ 'W', 'E', 'B', 'P' };
    private static final byte[] TYPE_VP8_ = new byte[]{ 'V', 'P', '8', ' ' };
    private static final byte[] TYPE_VP8X = new byte[]{ 'V', 'P', '8', 'X' };

    @Test
    public void shouldHaveAllWebPTypesDefined() {
        assertTrue(Arrays.equals(TYPE_RIFF, WebPImageType.RIFF.getValue()));
        assertTrue(Arrays.equals(TYPE_WEBP, WebPImageType.WEBP.getValue()));
        assertTrue(Arrays.equals(TYPE_VP8_, WebPImageType.VP8_.getValue()));
        assertTrue(Arrays.equals(TYPE_VP8X, WebPImageType.VP8X.getValue()));
    }
}