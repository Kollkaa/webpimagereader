package org.imageio;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum WebPImageType {
    RIFF(Constants.TYPE_RIFF),
    WEBP(Constants.TYPE_WEBP),
    VP8_(Constants.TYPE_VP8_),
    VP8X(Constants.TYPE_VP8X);

    @Getter
    private final byte[] value;

    private static class Constants {
        private static final byte[] TYPE_RIFF = new byte[]{ 'R', 'I', 'F', 'F' };
        private static final byte[] TYPE_WEBP = new byte[]{ 'W', 'E', 'B', 'P' };
        private static final byte[] TYPE_VP8_ = new byte[]{ 'V', 'P', '8', ' ' };
        private static final byte[] TYPE_VP8X = new byte[]{ 'V', 'P', '8', 'X' };
    }
}


