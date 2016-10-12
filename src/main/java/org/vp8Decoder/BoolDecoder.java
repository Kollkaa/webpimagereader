/*
 * Copyright (c) https://sourceforge.net/projects/javavp8decoder/
 *
 * 2016/9/28
 */

package org.vp8Decoder;

public class BoolDecoder {
    private int offset;
    private int range;
    private int value;
    int bit_count;
    int[] data;

    public static void main(String[] args) {
        int[] data = new int[]{112, 0, 0};
        BoolDecoder d = new BoolDecoder(data, 0);
        d.read_bool(128);
        d.read_bool(128);
        d.read_bool(128);
        d.read_bool(128);
        d.read_bool(128);
        d.read_bool(128);
        d.read_bool(128);
        d.read_bool(128);
    }

    BoolDecoder(int[] frame, int offset) {
        this.data = frame;
        this.offset = offset;
        this.init_bool_decoder();
    }

    public String toString() {
        return "bc: " + this.value;
    }

    private void init_bool_decoder() {
        this.value = 0;
        this.value = this.data[this.offset] << 8;
        ++this.offset;
        this.range = 255;
        this.bit_count = 0;
    }

    public int read_bool(int probability) {
        byte bit = 0;
        int range = this.range;
        int value = this.value;
        int split = 1 + ((range - 1) * probability >> 8);
        int bigsplit = split << 8;
        range = split;
        if(value >= bigsplit) {
            range = this.range - split;
            value -= bigsplit;
            bit = 1;
        }

        int count = this.bit_count;
        int shift = Globals.vp8dx_bitreader_norm[range];
        range <<= shift;
        value <<= shift;
        count -= shift;
        if(count <= 0) {
            value |= this.data[this.offset] << -count;
            ++this.offset;
            count += 8;
        }

        this.bit_count = count;
        this.value = value;
        this.range = range;
        return bit;
    }

    public int read_literal(int num_bits) {
        int v;
        for(v = 0; num_bits-- > 0; v = (v << 1) + this.read_bool(128)) {
            ;
        }

        return v;
    }

    public int read_bit() {
        return this.read_bool(128);
    }

    int treed_read(int[] t, int[] p) {
        int i = 0;

        while((i = t[i + this.read_bool(p[i >> 1])]) > 0) {
            ;
        }

        return -i;
    }

    int treed_read_skip(int[] t, int[] p, int skip_branches) {
        int i = skip_branches * 2;

        while((i = t[i + this.read_bool(p[i >> 1])]) > 0) {
            ;
        }

        return -i;
    }
}
