/*
 * Copyright (c) https://sourceforge.net/projects/javavp8decoder/
 *
 * 2016/9/28
 */

package org.vp8Decoder;

import java.io.IOException;

public class VP8Decoder {
    private int[][][][] coefProbs;
    private int frameCount = 0;
    VP8Frame f;

    public VP8Decoder() {
    }

    public void decodeFrame(int[] frameData, boolean debug) throws IOException {
        this.coefProbs = Globals.get_default_coef_probs();
        this.f = new VP8Frame(frameData, this.coefProbs);
        this.f.decodeFrame(debug);
        ++this.frameCount;
    }

    public int getWidth() {
        return this.f.getWidth();
    }

    public int getHeight() {
        return this.f.getHeight();
    }

    public VP8Frame getFrame() {
        return this.f;
    }
}
