/*
 * Copyright (c) https://sourceforge.net/projects/javavp8decoder/
 *
 * 2016/9/28
 */

package org.vp8Decoder;

public class IDCT {
    private static final int cospi8sqrt2minus1 = 20091;
    private static final int sinpi8sqrt2 = 35468;

    public IDCT() {
    }

    public static int[][] iwalsh4x4(int[] input) {
        int[] output = new int[16];
        int[][] diff = new int[4][4];
        int offset = 0;

        int i;
        int a1;
        int b1;
        int c1;
        int d1;
        for(i = 0; i < 4; ++i) {
            a1 = input[offset + 0] + input[offset + 12];
            b1 = input[offset + 4] + input[offset + 8];
            c1 = input[offset + 4] - input[offset + 8];
            d1 = input[offset + 0] - input[offset + 12];
            output[offset + 0] = a1 + b1;
            output[offset + 4] = c1 + d1;
            output[offset + 8] = a1 - b1;
            output[offset + 12] = d1 - c1;
            ++offset;
        }

        offset = 0;

        for(i = 0; i < 4; ++i) {
            a1 = output[offset + 0] + output[offset + 3];
            b1 = output[offset + 1] + output[offset + 2];
            c1 = output[offset + 1] - output[offset + 2];
            d1 = output[offset + 0] - output[offset + 3];
            int a2 = a1 + b1;
            int b2 = c1 + d1;
            int c2 = a1 - b1;
            int d2 = d1 - c1;
            output[offset + 0] = a2 + 3 >> 3;
            output[offset + 1] = b2 + 3 >> 3;
            output[offset + 2] = c2 + 3 >> 3;
            output[offset + 3] = d2 + 3 >> 3;
            diff[0][i] = a2 + 3 >> 3;
            diff[1][i] = b2 + 3 >> 3;
            diff[2][i] = c2 + 3 >> 3;
            diff[3][i] = d2 + 3 >> 3;
            offset += 4;
        }

        return diff;
    }

    public static int[][] idct4x4llm_c(int[] input) {
        int offset = 0;
        int[] output = new int[16];

        int i;
        int a1;
        int b1;
        int c1;
        int d1;
        int temp1;
        int temp2;
        for(i = 0; i < 4; ++i) {
            a1 = input[offset + 0] + input[offset + 8];
            b1 = input[offset + 0] - input[offset + 8];
            temp1 = input[offset + 4] * '誌' >> 16;
            temp2 = input[offset + 12] + (input[offset + 12] * 20091 >> 16);
            c1 = temp1 - temp2;
            temp1 = input[offset + 4] + (input[offset + 4] * 20091 >> 16);
            temp2 = input[offset + 12] * '誌' >> 16;
            d1 = temp1 + temp2;
            output[offset + 0] = a1 + d1;
            output[offset + 12] = a1 - d1;
            output[offset + 4] = b1 + c1;
            output[offset + 8] = b1 - c1;
            ++offset;
        }

        int diffo = 0;
        int[][] diff = new int[4][4];
        offset = 0;

        for(i = 0; i < 4; ++i) {
            a1 = output[offset * 4 + 0] + output[offset * 4 + 2];
            b1 = output[offset * 4 + 0] - output[offset * 4 + 2];
            temp1 = output[offset * 4 + 1] * '誌' >> 16;
            temp2 = output[offset * 4 + 3] + (output[offset * 4 + 3] * 20091 >> 16);
            c1 = temp1 - temp2;
            temp1 = output[offset * 4 + 1] + (output[offset * 4 + 1] * 20091 >> 16);
            temp2 = output[offset * 4 + 3] * '誌' >> 16;
            d1 = temp1 + temp2;
            output[offset * 4 + 0] = a1 + d1 + 4 >> 3;
            output[offset * 4 + 3] = a1 - d1 + 4 >> 3;
            output[offset * 4 + 1] = b1 + c1 + 4 >> 3;
            output[offset * 4 + 2] = b1 - c1 + 4 >> 3;
            diff[0][diffo] = a1 + d1 + 4 >> 3;
            diff[3][diffo] = a1 - d1 + 4 >> 3;
            diff[1][diffo] = b1 + c1 + 4 >> 3;
            diff[2][diffo] = b1 - c1 + 4 >> 3;
            ++offset;
            ++diffo;
        }

        return diff;
    }
}
