package org.imageio;

import javax.imageio.stream.ImageInputStream;

import lombok.Data;

import org.vp8Decoder.VP8Decoder;

@Data
public class WebPImage {
    private VP8Decoder decoder;
    private ImageInputStream stream;
    private int width;
    private int height;
    private boolean headerDefined;

    public WebPImage(VP8Decoder decoder) {
        this.decoder = decoder;
    }
}
