/*
 * Copyright (c) https://sourceforge.net/projects/javavp8decoder/
 *
 * 2016/9/28
 */

package org.vp8Decoder;

import java.io.IOException;

public class SubBlock {
    public enum PLANE {
        U, V, Y1, Y2;

        PLANE() {
        }
    }

    public static final int UV = 2;
    public static final int Y = 3;
    public static final int Y_AFTER_Y2 = 0;
    public static final int Y2 = 1;

    public static int planeToType(PLANE plane, Boolean withY2) {
        switch (plane) {
        case Y2:
            return 1;
        case Y1:
            if (withY2)
                return 0;
            else
                return 3;
        case U:
            return 2;
        case V:
            return 2;
        }
        return -1;

    }

    private SubBlock above;

    private int[][] dest;
    private int[][] diff;
    private boolean hasNoZeroToken;
    private SubBlock left;
    private MacroBlock macroBlock;
    private int mode;
    private PLANE plane;
    private int predict[][];
    private int tokens[];

    public SubBlock(MacroBlock macroBlock, SubBlock above, SubBlock left,
            PLANE plane) {
        this.macroBlock = macroBlock;
        this.plane = plane;
        this.above = above;
        this.left = left;
        mode = 0;
        tokens = new int[16];
        for (int z = 0; z < 16; z++)
            tokens[z] = 0;
    }

    private int DCTextra(BoolDecoder bc2, int p[]) throws IOException {
        int v = 0;
        int offset = 0;
        do {
            v += v + bc2.read_bool(p[offset]);
            offset++;
        } while (p[offset] > 0);
        return v;
    }

    public void decodeSubBlock(BoolDecoder bc2, int[][][][] coef_probs, int ilc, int type, boolean withY2) throws IOException {
        SubBlock sb = this;
        byte startAt = 0;
        if (withY2) {
            startAt = 1;
        }

        int lc = ilc;
        int c = 0;
        int v = 1;

        for (boolean skip = false; v != 11 && c + startAt < 16; ++c) {
            if (!skip) {
                v = bc2.treed_read(Globals.coef_tree, coef_probs[type][Globals.coef_bands[c + startAt]][lc]);
            } else {
                v = bc2.treed_read_skip(Globals.coef_tree, coef_probs[type][Globals.coef_bands[c + startAt]][lc], 1);
            }

            int dv = this.decodeToken(bc2, v);
            lc = 0;
            skip = false;
            if (dv != 1 && dv != -1) {
                if (dv <= 1 && dv >= -1) {
                    if (dv == 0) {
                        skip = true;
                    }
                } else {
                    lc = 2;
                }
            } else {
                lc = 1;
            }

            int[] tokens = sb.getTokens();
            if (v != 11) {
                tokens[Globals.default_zig_zag1d[c + startAt]] = dv;
            }
        }

    }

    private int decodeToken(BoolDecoder bc2, int v) throws IOException {
        int r = v;
        if (v == 5) {
            r = 5 + this.DCTextra(bc2, Globals.Pcat1);
        }

        if (v == 6) {
            r = 7 + this.DCTextra(bc2, Globals.Pcat2);
        }

        if (v == 7) {
            r = 11 + this.DCTextra(bc2, Globals.Pcat3);
        }

        if (v == 8) {
            r = 19 + this.DCTextra(bc2, Globals.Pcat4);
        }

        if (v == 9) {
            r = 35 + this.DCTextra(bc2, Globals.Pcat5);
        }

        if (v == 10) {
            r = 67 + this.DCTextra(bc2, Globals.Pcat6);
        }

        if (v != 0 && v != 11 && bc2.read_bit() > 0) {
            r = -r;
        }

        return r;
    }

    public void dequantSubBlock(VP8Frame frame, Integer Dc) {
        SubBlock sb = this;
        int[] adjustedValues = new int[16];

        for (int diff = 0; diff < 16; ++diff) {
            int QValue = Globals.ac_qlookup[frame.getQIndex()];
            if (diff == 0) {
                QValue = Globals.dc_qlookup[frame.getQIndex()];
            }

            int inputValue = sb.getTokens()[diff];
            adjustedValues[diff] = inputValue * QValue;
        }

        if (Dc != null) {
            adjustedValues[0] = Dc.intValue();
        }

        int[][] var8 = IDCT.idct4x4llm_c(adjustedValues);
        sb.setDiff(var8);
    }

    public SubBlock getAbove() {
        return this.above;
    }

    public int[][] getDest() {
        return this.dest != null ? this.dest : new int[4][4];
    }

    public boolean isDest() {
        return this.dest != null;
    }

    public int[][] getDiff() {
        return this.diff;
    }

    public SubBlock getLeft() {
        return this.left;
    }

    public MacroBlock getMacroBlock() {
        return this.macroBlock;
    }

    public int getMode() {
        return this.mode;
    }

    public PLANE getPlane() {
        return this.plane;
    }

    public int[][] getPredict(int intra_bmode, boolean left) {
        if (this.dest != null) {
            return this.dest;
        } else if (this.predict != null) {
            return this.predict;
        } else {
            short rv = 127;
            if ((intra_bmode == 0 || intra_bmode == 2 || intra_bmode == 6 || intra_bmode == 5 || intra_bmode == 8) && left) {
                rv = 129;
            }

            int[][] r = new int[4][4];

            for (int j = 0; j < 4; ++j) {
                for (int i = 0; i < 4; ++i) {
                    r[i][j] = rv;
                }
            }

            return r;
        }
    }

    public int[][] getMacroBlockPredict(int intra_mode) {
        if (this.dest != null) {
            return this.dest;
        } else {
            short rv = 127;
            if (intra_mode == 2) {
                rv = 129;
            }

            int[][] r = new int[4][4];

            for (int j = 0; j < 4; ++j) {
                for (int i = 0; i < 4; ++i) {
                    r[i][j] = rv;
                }
            }

            return r;
        }
    }

    public int[] getTokens() {
        return this.tokens;
    }

    public boolean hasNoZeroToken() {
        for (int x = 0; x < 16; ++x) {
            if (this.tokens[x] != 0) {
                return true;
            }
        }

        return false;
    }

    public void predict(VP8Frame frame) {
        SubBlock aboveSb = frame.getAboveSubBlock(this, this.getPlane());
        SubBlock leftSb = frame.getLeftSubBlock(this, this.getPlane());
        int[] above = new int[4];
        int[] left = new int[4];
        above[0] = aboveSb.getPredict(this.getMode(), false)[0][3];
        above[1] = aboveSb.getPredict(this.getMode(), false)[1][3];
        above[2] = aboveSb.getPredict(this.getMode(), false)[2][3];
        above[3] = aboveSb.getPredict(this.getMode(), false)[3][3];
        left[0] = leftSb.getPredict(this.getMode(), true)[3][0];
        left[1] = leftSb.getPredict(this.getMode(), true)[3][1];
        left[2] = leftSb.getPredict(this.getMode(), true)[3][2];
        left[3] = leftSb.getPredict(this.getMode(), true)[3][3];
        SubBlock AL = frame.getLeftSubBlock(aboveSb, this.getPlane());
        int al;
        if (!leftSb.isDest() && !aboveSb.isDest()) {
            al = AL.getPredict(this.getMode(), true)[3][3];
        } else if (!aboveSb.isDest()) {
            al = AL.getPredict(this.getMode(), false)[3][3];
        } else {
            al = AL.getPredict(this.getMode(), true)[3][3];
        }

        int[][] p;
        SubBlock AR = frame.getAboveRightSubBlock(this, this.plane);
        int[] ar = new int[] { AR.getPredict(this.getMode(), false)[0][3], AR.getPredict(this.getMode(), false)[1][3], AR.getPredict(this.getMode(), false)[2][3], AR.getPredict(this.getMode(), false)[3][3] };
        p = new int[4][4];
        int[] pp;
        int r;
        int var18;
        int var19;
        label109:
        switch (this.getMode()) {
        case 0:
            int expected_dc = 0;

            for (var18 = 0; var18 < 4; ++var18) {
                expected_dc += above[var18];
                expected_dc += left[var18];
            }

            expected_dc = expected_dc + 4 >> 3;
            var18 = 0;

            while (true) {
                if (var18 >= 4) {
                    break label109;
                }

                for (var19 = 0; var19 < 4; ++var19) {
                    p[var19][var18] = expected_dc;
                }

                ++var18;
            }
        case 1:
            var18 = 0;

            while (true) {
                if (var18 >= 4) {
                    break label109;
                }

                for (var19 = 0; var19 < 4; ++var19) {
                    r = above[var19] - al + left[var18];
                    if (r < 0) {
                        r = 0;
                    }

                    if (r > 255) {
                        r = 255;
                    }

                    p[var19][var18] = r;
                }

                ++var18;
            }
        case 2:
            int[] ap = new int[] { al + 2 * above[0] + above[1] + 2 >> 2, above[0] + 2 * above[1] + above[2] + 2 >> 2, above[1] + 2 * above[2] + above[3] + 2 >> 2, above[2] + 2 * above[3] + ar[0] + 2 >> 2 };
            var19 = 0;

            while (true) {
                if (var19 >= 4) {
                    break label109;
                }

                for (r = 0; r < 4; ++r) {
                    p[r][var19] = ap[r];
                }

                ++var19;
            }
        case 3:
            int[] lp = new int[] { al + 2 * left[0] + left[1] + 2 >> 2, left[0] + 2 * left[1] + left[2] + 2 >> 2, left[1] + 2 * left[2] + left[3] + 2 >> 2, left[2] + 2 * left[3] + left[3] + 2 >> 2 };
            r = 0;

            while (true) {
                if (r >= 4) {
                    break label109;
                }

                for (int c = 0; c < 4; ++c) {
                    p[c][r] = lp[r];
                }

                ++r;
            }
        case 4:
            p[0][0] = above[0] + above[1] * 2 + above[2] + 2 >> 2;
            p[1][0] = p[0][1] = above[1] + above[2] * 2 + above[3] + 2 >> 2;
            p[2][0] = p[1][1] = p[0][2] = above[2] + above[3] * 2 + ar[0] + 2 >> 2;
            p[3][0] = p[2][1] = p[1][2] = p[0][3] = above[3] + ar[0] * 2 + ar[1] + 2 >> 2;
            p[3][1] = p[2][2] = p[1][3] = ar[0] + ar[1] * 2 + ar[2] + 2 >> 2;
            p[3][2] = p[2][3] = ar[1] + ar[2] * 2 + ar[3] + 2 >> 2;
            p[3][3] = ar[2] + ar[3] * 2 + ar[3] + 2 >> 2;
            break;
        case 5:
            pp = new int[] { left[3], left[2], left[1], left[0], al, above[0], above[1], above[2], above[3] };
            p[0][3] = pp[0] + pp[1] * 2 + pp[2] + 2 >> 2;
            p[1][3] = p[0][2] = pp[1] + pp[2] * 2 + pp[3] + 2 >> 2;
            p[2][3] = p[1][2] = p[0][1] = pp[2] + pp[3] * 2 + pp[4] + 2 >> 2;
            p[3][3] = p[2][2] = p[1][1] = p[0][0] = pp[3] + pp[4] * 2 + pp[5] + 2 >> 2;
            p[3][2] = p[2][1] = p[1][0] = pp[4] + pp[5] * 2 + pp[6] + 2 >> 2;
            p[3][1] = p[2][0] = pp[5] + pp[6] * 2 + pp[7] + 2 >> 2;
            p[3][0] = pp[6] + pp[7] * 2 + pp[8] + 2 >> 2;
            break;
        case 6:
            pp = new int[] { left[3], left[2], left[1], left[0], al, above[0], above[1], above[2], above[3] };
            p[0][3] = pp[1] + pp[2] * 2 + pp[3] + 2 >> 2;
            p[0][2] = pp[2] + pp[3] * 2 + pp[4] + 2 >> 2;
            p[1][3] = p[0][1] = pp[3] + pp[4] * 2 + pp[5] + 2 >> 2;
            p[1][2] = p[0][0] = pp[4] + pp[5] + 1 >> 1;
            p[2][3] = p[1][1] = pp[4] + pp[5] * 2 + pp[6] + 2 >> 2;
            p[2][2] = p[1][0] = pp[5] + pp[6] + 1 >> 1;
            p[3][3] = p[2][1] = pp[5] + pp[6] * 2 + pp[7] + 2 >> 2;
            p[3][2] = p[2][0] = pp[6] + pp[7] + 1 >> 1;
            p[3][1] = pp[6] + pp[7] * 2 + pp[8] + 2 >> 2;
            p[3][0] = pp[7] + pp[8] + 1 >> 1;
            break;
        case 7:
            p[0][0] = above[0] + above[1] + 1 >> 1;
            p[0][1] = above[0] + above[1] * 2 + above[2] + 2 >> 2;
            p[0][2] = p[1][0] = above[1] + above[2] + 1 >> 1;
            p[1][1] = p[0][3] = above[1] + above[2] * 2 + above[3] + 2 >> 2;
            p[1][2] = p[2][0] = above[2] + above[3] + 1 >> 1;
            p[1][3] = p[2][1] = above[2] + above[3] * 2 + ar[0] + 2 >> 2;
            p[3][0] = p[2][2] = above[3] + ar[0] + 1 >> 1;
            p[3][1] = p[2][3] = above[3] + ar[0] * 2 + ar[1] + 2 >> 2;
            p[3][2] = ar[0] + ar[1] * 2 + ar[2] + 2 >> 2;
            p[3][3] = ar[1] + ar[2] * 2 + ar[3] + 2 >> 2;
            break;
        case 8:
            pp = new int[] { left[3], left[2], left[1], left[0], al, above[0], above[1], above[2], above[3] };
            p[0][3] = pp[0] + pp[1] + 1 >> 1;
            p[1][3] = pp[0] + pp[1] * 2 + pp[2] + 2 >> 2;
            p[0][2] = p[2][3] = pp[1] + pp[2] + 1 >> 1;
            p[1][2] = p[3][3] = pp[1] + pp[2] * 2 + pp[3] + 2 >> 2;
            p[2][2] = p[0][1] = pp[2] + pp[3] + 1 >> 1;
            p[3][2] = p[1][1] = pp[2] + pp[3] * 2 + pp[4] + 2 >> 2;
            p[2][1] = p[0][0] = pp[3] + pp[4] + 1 >> 1;
            p[3][1] = p[1][0] = pp[3] + pp[4] * 2 + pp[5] + 2 >> 2;
            p[2][0] = pp[4] + pp[5] * 2 + pp[6] + 2 >> 2;
            p[3][0] = pp[5] + pp[6] * 2 + pp[7] + 2 >> 2;
            break;
        case 9:
            p[0][0] = left[0] + left[1] + 1 >> 1;
            p[1][0] = left[0] + left[1] * 2 + left[2] + 2 >> 2;
            p[2][0] = p[0][1] = left[1] + left[2] + 1 >> 1;
            p[3][0] = p[1][1] = left[1] + left[2] * 2 + left[3] + 2 >> 2;
            p[2][1] = p[0][2] = left[2] + left[3] + 1 >> 1;
            p[3][1] = p[1][2] = left[2] + left[3] * 2 + left[3] + 2 >> 2;
            p[2][2] = p[3][2] = p[0][3] = p[1][3] = p[2][3] = p[3][3] = left[3];
            break;
        default:
            System.out.println("TODO: " + this.getMode());
            System.exit(0);
        }

        this.setPredict(p);
    }

    public void reconstruct() {
        int[][] p = this.getPredict(1, false);
        int[][] dest = new int[4][4];
        int[][] diff = this.getDiff();

        for (int r = 0; r < 4; ++r) {
            for (int c = 0; c < 4; ++c) {
                int a = diff[r][c] + p[r][c];
                if (a < 0) {
                    a = 0;
                }

                if (a > 255) {
                    a = 255;
                }

                dest[r][c] = a;
            }
        }

        this.setDest(dest);
    }

    public void setDest(int[][] dest) {
        this.dest = dest;
    }

    public void setDiff(int[][] diff) {
        this.diff = diff;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setPixel(int x, int y, int p) {
        if (this.dest == null) {
            this.dest = new int[4][4];
        }

        this.dest[x][y] = p;
    }

    public void setPredict(int[][] predict) {
        this.predict = predict;
    }

    public String toString() {
        String r = "[";

        for (int x = 0; x < 16; ++x) {
            r = r + this.tokens[x] + " ";
        }

        r = r + "]";
        return r;
    }

    public void drawDebugV() {
        if (this.dest != null) {
            this.dest[0][0] = 0;
            this.dest[0][1] = 0;
            this.dest[0][2] = 0;
            this.dest[0][3] = 0;
        }
    }

    public void drawDebugH() {
        if (this.dest != null) {
            this.dest[0][0] = 0;
            this.dest[1][0] = 0;
            this.dest[2][0] = 0;
            this.dest[3][0] = 0;
        }
    }
}
