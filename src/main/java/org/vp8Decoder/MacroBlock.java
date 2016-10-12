/*
 * Copyright (c) https://sourceforge.net/projects/javavp8decoder/
 *
 * 2016/9/28
 */

package org.vp8Decoder;

import java.io.IOException;

public class MacroBlock {
    SubBlock[][] ySubBlocks;
    SubBlock[][] uSubBlocks;
    SubBlock[][] vSubBlocks;
    SubBlock y2SubBlock;
    private int x;
    private int y;
    private int mb_skip_coeff;
    private int yMode;
    private int uvMode;

    public int getYMode() {
        return this.yMode;
    }

    public void setYMode(int yMode) {
        this.yMode = yMode;
    }

    public int getMb_skip_coeff() {
        return this.mb_skip_coeff;
    }

    public void setMb_skip_coeff(int mbSkipCoeff) {
        this.mb_skip_coeff = mbSkipCoeff;
    }

    MacroBlock(int x, int y) {
        this.x = x - 1;
        this.y = y - 1;
        this.ySubBlocks = new SubBlock[4][4];
        this.uSubBlocks = new SubBlock[2][2];
        this.vSubBlocks = new SubBlock[2][2];
        SubBlock above = null;
        SubBlock left = null;

        int i;
        int j;
        for(i = 0; i < 4; ++i) {
            for(j = 0; j < 4; ++j) {
                left = null;
                above = null;
                if(j > 0) {
                    left = this.ySubBlocks[j - 1][i];
                }

                if(i > 0) {
                    above = this.ySubBlocks[j][i - 1];
                }

                this.ySubBlocks[j][i] = new SubBlock(this, above, left, SubBlock.PLANE.Y1);
            }
        }

        for(i = 0; i < 2; ++i) {
            for(j = 0; j < 2; ++j) {
                left = null;
                above = null;
                if(j > 0) {
                    left = this.uSubBlocks[j - 1][i];
                }

                if(i > 0) {
                    above = this.uSubBlocks[j][i - 1];
                }

                this.uSubBlocks[j][i] = new SubBlock(this, above, left, SubBlock.PLANE.U);
            }
        }

        for(i = 0; i < 2; ++i) {
            for(j = 0; j < 2; ++j) {
                left = null;
                above = null;
                if(j > 0) {
                    left = this.vSubBlocks[j - 1][i];
                }

                if(i > 0) {
                    above = this.vSubBlocks[j][i - 1];
                }

                this.vSubBlocks[j][i] = new SubBlock(this, above, left, SubBlock.PLANE.V);
            }
        }

        this.y2SubBlock = new SubBlock(this, (SubBlock)null, (SubBlock)null, SubBlock.PLANE.Y2);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public SubBlock getYSubBlock(int i, int j) {
        return this.ySubBlocks[i][j];
    }

    public SubBlock getY2SubBlock() {
        return this.y2SubBlock;
    }

    public SubBlock getUSubBlock(int i, int j) {
        return this.uSubBlocks[i][j];
    }

    public SubBlock getVSubBlock(int i, int j) {
        return this.vSubBlocks[i][j];
    }

    public SubBlock getSubBlock(SubBlock.PLANE plane, int i, int j) {
        switch (plane) {
        case Y1:
            return getYSubBlock(i, j);
        case U:
            return getUSubBlock(i, j);

        case V:
            return getVSubBlock(i, j);
        case Y2:
            return getY2SubBlock();
        }
        return null;
    }

    public String toString() {
        return "x: " + this.x + "y: " + this.y;
    }

    public int getSubblockY(SubBlock sb) {
        int y;
        int x;
        if(sb.getPlane() == SubBlock.PLANE.Y1) {
            for(y = 0; y < 4; ++y) {
                for(x = 0; x < 4; ++x) {
                    if(this.ySubBlocks[x][y] == sb) {
                        return y;
                    }
                }
            }
        } else if(sb.getPlane() == SubBlock.PLANE.U) {
            for(y = 0; y < 2; ++y) {
                for(x = 0; x < 2; ++x) {
                    if(this.uSubBlocks[x][y] == sb) {
                        return y;
                    }
                }
            }
        } else if(sb.getPlane() == SubBlock.PLANE.V) {
            for(y = 0; y < 2; ++y) {
                for(x = 0; x < 2; ++x) {
                    if(this.vSubBlocks[x][y] == sb) {
                        return y;
                    }
                }
            }
        } else if(sb.getPlane() == SubBlock.PLANE.Y2) {
            return 0;
        }

        return -100;
    }

    public int getSubblockX(SubBlock sb) {
        int y;
        int x;
        if(sb.getPlane() == SubBlock.PLANE.Y1) {
            for(y = 0; y < 4; ++y) {
                for(x = 0; x < 4; ++x) {
                    if(this.ySubBlocks[x][y] == sb) {
                        return x;
                    }
                }
            }
        } else if(sb.getPlane() == SubBlock.PLANE.U) {
            for(y = 0; y < 2; ++y) {
                for(x = 0; x < 2; ++x) {
                    if(this.uSubBlocks[x][y] == sb) {
                        return x;
                    }
                }
            }
        } else if(sb.getPlane() == SubBlock.PLANE.V) {
            for(y = 0; y < 2; ++y) {
                for(x = 0; x < 2; ++x) {
                    if(this.vSubBlocks[x][y] == sb) {
                        return x;
                    }
                }
            }
        } else if(sb.getPlane() == SubBlock.PLANE.Y2) {
            return 0;
        }

        return -100;
    }

    public SubBlock getRightSubBlock(int y, SubBlock.PLANE plane) {
        return plane == SubBlock.PLANE.Y1?this.ySubBlocks[3][y]:(plane == SubBlock.PLANE.U?this.uSubBlocks[1][y]:(plane == SubBlock.PLANE.V?this.vSubBlocks[1][y]:(plane == SubBlock.PLANE.Y2?this.y2SubBlock:null)));
    }

    public SubBlock getLeftSubBlock(int y, SubBlock.PLANE plane) {
        return plane == SubBlock.PLANE.Y1?this.ySubBlocks[0][y]:(plane == SubBlock.PLANE.V?this.vSubBlocks[0][y]:(plane == SubBlock.PLANE.Y2?this.y2SubBlock:(plane == SubBlock.PLANE.U?this.uSubBlocks[0][y]:null)));
    }

    public SubBlock getBottomSubBlock(int x, SubBlock.PLANE plane) {
        return plane == SubBlock.PLANE.Y1?this.ySubBlocks[x][3]:(plane == SubBlock.PLANE.U?this.uSubBlocks[x][1]:(plane == SubBlock.PLANE.V?this.vSubBlocks[x][1]:(plane == SubBlock.PLANE.Y2?this.y2SubBlock:null)));
    }

    public void predictUV(VP8Frame frame) {
        MacroBlock aboveMb = frame.getMacroBlock(this.x, this.y - 1);
        MacroBlock leftMb = frame.getMacroBlock(this.x - 1, this.y);
        SubBlock[] aboveUSb;
        SubBlock[] aboveVSb;
        SubBlock[] leftUSb;
        SubBlock[] leftVSb;
        SubBlock ALUSb;
        SubBlock ALVSb;
        int alv;
        int b;
        int a;
        int d;
        switch(this.uvMode) {
        case 0:
            boolean up_available = false;
            boolean left_available = false;
            int Uaverage = 0;
            int Vaverage = 0;
            boolean expected_udc = false;
            boolean expected_vdc = false;
            if(this.x > 0) {
                left_available = true;
            }

            if(this.y > 0) {
                up_available = true;
            }

            int var27;
            int var28;
            int var34;
            if(!up_available && !left_available) {
                var27 = 128;
                var28 = 128;
            } else {
                int ufill;
                SubBlock vfill;
                SubBlock var31;
                if(up_available) {
                    for(ufill = 0; ufill < 2; ++ufill) {
                        vfill = aboveMb.getUSubBlock(ufill, 1);
                        var31 = aboveMb.getVSubBlock(ufill, 1);

                        for(var34 = 0; var34 < 4; ++var34) {
                            Uaverage += vfill.getDest()[var34][3];
                            Vaverage += var31.getDest()[var34][3];
                        }
                    }
                }

                if(left_available) {
                    for(ufill = 0; ufill < 2; ++ufill) {
                        vfill = leftMb.getUSubBlock(1, ufill);
                        var31 = leftMb.getVSubBlock(1, ufill);

                        for(var34 = 0; var34 < 4; ++var34) {
                            Uaverage += vfill.getDest()[3][var34];
                            Vaverage += var31.getDest()[3][var34];
                        }
                    }
                }

                ufill = 2;
                if(up_available) {
                    ++ufill;
                }

                if(left_available) {
                    ++ufill;
                }

                var27 = Uaverage + (1 << ufill - 1) >> ufill;
                var28 = Vaverage + (1 << ufill - 1) >> ufill;
            }

            int[][] var32 = new int[4][4];

            int var33;
            for(int var29 = 0; var29 < 4; ++var29) {
                for(var33 = 0; var33 < 4; ++var33) {
                    var32[var33][var29] = var27;
                }
            }

            int[][] var30 = new int[4][4];

            for(var33 = 0; var33 < 4; ++var33) {
                for(var34 = 0; var34 < 4; ++var34) {
                    var30[var34][var33] = var28;
                }
            }

            for(var33 = 0; var33 < 2; ++var33) {
                for(var34 = 0; var34 < 2; ++var34) {
                    SubBlock var37 = this.uSubBlocks[var34][var33];
                    SubBlock var39 = this.vSubBlocks[var34][var33];
                    var37.setPredict(var32);
                    var39.setPredict(var30);
                }
            }

            return;
        case 1:
            aboveUSb = new SubBlock[2];
            aboveVSb = new SubBlock[2];

            int var35;
            for(var35 = 0; var35 < 2; ++var35) {
                aboveUSb[var35] = aboveMb.getUSubBlock(var35, 1);
                aboveVSb[var35] = aboveMb.getVSubBlock(var35, 1);
            }

            for(var35 = 0; var35 < 2; ++var35) {
                for(int var36 = 0; var36 < 2; ++var36) {
                    SubBlock var41 = this.uSubBlocks[var35][var36];
                    ALUSb = this.vSubBlocks[var35][var36];
                    int[][] var43 = new int[4][4];
                    int[][] var44 = new int[4][4];

                    for(alv = 0; alv < 4; ++alv) {
                        for(b = 0; b < 4; ++b) {
                            var43[alv][b] = aboveUSb[var35].getMacroBlockPredict(1)[alv][3];
                            var44[alv][b] = aboveVSb[var35].getMacroBlockPredict(1)[alv][3];
                        }
                    }

                    var41.setPredict(var43);
                    ALUSb.setPredict(var44);
                }
            }

            return;
        case 2:
            leftUSb = new SubBlock[2];
            leftVSb = new SubBlock[2];

            int var38;
            for(var38 = 0; var38 < 2; ++var38) {
                leftUSb[var38] = leftMb.getUSubBlock(1, var38);
                leftVSb[var38] = leftMb.getVSubBlock(1, var38);
            }

            for(var38 = 0; var38 < 2; ++var38) {
                for(int var40 = 0; var40 < 2; ++var40) {
                    SubBlock var42 = this.uSubBlocks[var40][var38];
                    ALVSb = this.vSubBlocks[var40][var38];
                    int[][] var45 = new int[4][4];
                    int[][] var46 = new int[4][4];

                    for(a = 0; a < 4; ++a) {
                        for(d = 0; d < 4; ++d) {
                            var45[d][a] = leftUSb[var38].getMacroBlockPredict(2)[3][a];
                            var46[d][a] = leftVSb[var38].getMacroBlockPredict(2)[3][a];
                        }
                    }

                    var42.setPredict(var45);
                    ALVSb.setPredict(var46);
                }
            }

            return;
        case 3:
            MacroBlock ALMb = frame.getMacroBlock(this.x - 1, this.y - 1);
            ALUSb = ALMb.getUSubBlock(1, 1);
            int alu = ALUSb.getDest()[3][3];
            ALVSb = ALMb.getVSubBlock(1, 1);
            alv = ALVSb.getDest()[3][3];
            aboveUSb = new SubBlock[2];
            leftUSb = new SubBlock[2];
            aboveVSb = new SubBlock[2];
            leftVSb = new SubBlock[2];

            for(b = 0; b < 2; ++b) {
                aboveUSb[b] = aboveMb.getUSubBlock(b, 1);
                leftUSb[b] = leftMb.getUSubBlock(1, b);
                aboveVSb[b] = aboveMb.getVSubBlock(b, 1);
                leftVSb[b] = leftMb.getVSubBlock(1, b);
            }

            for(b = 0; b < 2; ++b) {
                for(a = 0; a < 4; ++a) {
                    for(d = 0; d < 2; ++d) {
                        for(int c = 0; c < 4; ++c) {
                            int upred = leftUSb[b].getDest()[3][a] + aboveUSb[d].getDest()[c][3] - alu;
                            this.uSubBlocks[d][b].setPixel(c, a, upred);
                            int vpred = leftVSb[b].getDest()[3][a] + aboveVSb[d].getDest()[c][3] - alv;
                            this.vSubBlocks[d][b].setPixel(c, a, vpred);
                        }
                    }
                }
            }

            return;
        default:
            System.out.println("TODO predict_mb_uv: " + this.yMode);
            System.exit(0);
        }
    }

    public void predictY(VP8Frame frame) {
        MacroBlock aboveMb = frame.getMacroBlock(this.x, this.y - 1);
        MacroBlock leftMb = frame.getMacroBlock(this.x - 1, this.y);
        int[][] fill;
        SubBlock[] aboveYSb;
        SubBlock[] leftYSb;
        int leftUSb;
        SubBlock ALSb;
        int al;
        int b;
        int a;
        int var24;
        switch(this.yMode) {
        case 0:
            boolean up_available = false;
            boolean left_available = false;
            int average = 0;
            boolean expected_dc = false;
            if(this.x > 0) {
                left_available = true;
            }

            if(this.y > 0) {
                up_available = true;
            }

            int var20;
            if(!up_available && !left_available) {
                var20 = 128;
            } else {
                int var21;
                SubBlock var22;
                if(up_available) {
                    for(var21 = 0; var21 < 4; ++var21) {
                        var22 = aboveMb.getYSubBlock(var21, 3);

                        for(var24 = 0; var24 < 4; ++var24) {
                            average += var22.getDest()[var24][3];
                        }
                    }
                }

                if(left_available) {
                    for(var21 = 0; var21 < 4; ++var21) {
                        var22 = leftMb.getYSubBlock(3, var21);

                        for(var24 = 0; var24 < 4; ++var24) {
                            average += var22.getDest()[3][var24];
                        }
                    }
                }

                var21 = 3;
                if(up_available) {
                    ++var21;
                }

                if(left_available) {
                    ++var21;
                }

                var20 = average + (1 << var21 - 1) >> var21;
            }

            fill = new int[4][4];

            int var23;
            for(var23 = 0; var23 < 4; ++var23) {
                for(var24 = 0; var24 < 4; ++var24) {
                    fill[var24][var23] = var20;
                }
            }

            for(var23 = 0; var23 < 4; ++var23) {
                for(var24 = 0; var24 < 4; ++var24) {
                    SubBlock var27 = this.ySubBlocks[var24][var23];
                    var27.setPredict(fill);
                }
            }

            return;
        case 1:
            aboveYSb = new SubBlock[4];

            for(var24 = 0; var24 < 4; ++var24) {
                aboveYSb[var24] = aboveMb.getYSubBlock(var24, 3);
            }

            for(var24 = 0; var24 < 4; ++var24) {
                for(leftUSb = 0; leftUSb < 4; ++leftUSb) {
                    SubBlock var28 = this.ySubBlocks[leftUSb][var24];
                    int[][] var29 = new int[4][4];

                    for(al = 0; al < 4; ++al) {
                        for(b = 0; b < 4; ++b) {
                            var29[b][al] = aboveYSb[leftUSb].getPredict(2, false)[b][3];
                        }
                    }

                    var28.setPredict(var29);
                }
            }

            return;
        case 2:
            leftYSb = new SubBlock[4];

            for(leftUSb = 0; leftUSb < 4; ++leftUSb) {
                leftYSb[leftUSb] = leftMb.getYSubBlock(3, leftUSb);
            }

            int var26;
            for(leftUSb = 0; leftUSb < 4; ++leftUSb) {
                for(var26 = 0; var26 < 4; ++var26) {
                    ALSb = this.ySubBlocks[var26][leftUSb];
                    int[][] var30 = new int[4][4];

                    for(b = 0; b < 4; ++b) {
                        for(a = 0; a < 4; ++a) {
                            var30[a][b] = leftYSb[leftUSb].getPredict(0, true)[3][b];
                        }
                    }

                    ALSb.setPredict(var30);
                }
            }

            SubBlock[] var25 = new SubBlock[2];

            for(var26 = 0; var26 < 2; ++var26) {
                var25[var26] = leftMb.getYSubBlock(1, var26);
            }

            return;
        case 3:
            MacroBlock ALMb = frame.getMacroBlock(this.x - 1, this.y - 1);
            ALSb = ALMb.getYSubBlock(3, 3);
            al = ALSb.getDest()[3][3];
            aboveYSb = new SubBlock[4];
            leftYSb = new SubBlock[4];

            for(b = 0; b < 4; ++b) {
                aboveYSb[b] = aboveMb.getYSubBlock(b, 3);
            }

            for(b = 0; b < 4; ++b) {
                leftYSb[b] = leftMb.getYSubBlock(3, b);
            }

            fill = new int[4][4];

            for(b = 0; b < 4; ++b) {
                for(a = 0; a < 4; ++a) {
                    for(int d = 0; d < 4; ++d) {
                        for(int c = 0; c < 4; ++c) {
                            int pred = leftYSb[b].getDest()[3][a] + aboveYSb[d].getDest()[c][3] - al;
                            this.ySubBlocks[d][b].setPixel(c, a, pred);
                        }
                    }
                }
            }

            return;
        default:
            System.out.println("TODO predict_mb_y: " + this.yMode);
            System.exit(0);
        }
    }

    public void recon_mb() {
        int j;
        int i;
        SubBlock sb;
        for(j = 0; j < 4; ++j) {
            for(i = 0; i < 4; ++i) {
                sb = this.ySubBlocks[i][j];
                sb.reconstruct();
            }
        }

        for(j = 0; j < 2; ++j) {
            for(i = 0; i < 2; ++i) {
                sb = this.uSubBlocks[i][j];
                sb.reconstruct();
            }
        }

        for(j = 0; j < 2; ++j) {
            for(i = 0; i < 2; ++i) {
                sb = this.vSubBlocks[i][j];
                sb.reconstruct();
            }
        }

    }

    public void setUvMode(int mode) {
        this.uvMode = mode;
    }

    public int getUvMode() {
        return this.uvMode;
    }

    public void decodeMacroBlock(VP8Frame frame) throws IOException {
        if(this.getMb_skip_coeff() <= 0) {
            if(this.getYMode() != 4) {
                this.decodeMacroBlockTokens(frame, true);
            } else {
                this.decodeMacroBlockTokens(frame, false);
            }
        }

    }

    private void decodeMacroBlockTokens(VP8Frame frame, boolean withY2) throws IOException {
        if(withY2) {
            this.decodePlaneTokens(frame, 1, SubBlock.PLANE.Y2, false);
        }

        this.decodePlaneTokens(frame, 4, SubBlock.PLANE.Y1, withY2);
        this.decodePlaneTokens(frame, 2, SubBlock.PLANE.U, false);
        this.decodePlaneTokens(frame, 2, SubBlock.PLANE.V, false);
    }

    public void dequantMacroBlock(VP8Frame frame) {
        MacroBlock mb = this;
        int j;
        if(this.getYMode() != 4) {
            SubBlock i = this.getY2SubBlock();
            j = Globals.ac_qlookup[frame.getQIndex()] * 155 / 100;
            int sb = Globals.dc_qlookup[frame.getQIndex()] * 2;
            int[] input = new int[16];
            input[0] = i.getTokens()[0] * sb;

            int i1;
            for(i1 = 1; i1 < 16; ++i1) {
                input[i1] = i.getTokens()[i1] * j;
            }

            i.setDiff(IDCT.iwalsh4x4(input));

            int j1;
            SubBlock uvsb;
            for(i1 = 0; i1 < 4; ++i1) {
                for(j1 = 0; j1 < 4; ++j1) {
                    uvsb = mb.getYSubBlock(j1, i1);
                    uvsb.dequantSubBlock(frame, Integer.valueOf(i.getDiff()[j1][i1]));
                }
            }

            mb.predictY(frame);
            mb.predictUV(frame);

            for(i1 = 0; i1 < 2; ++i1) {
                for(j1 = 0; j1 < 2; ++j1) {
                    uvsb = mb.getUSubBlock(j1, i1);
                    uvsb.dequantSubBlock(frame, (Integer)null);
                    uvsb = mb.getVSubBlock(i1, j1);
                    uvsb.dequantSubBlock(frame, (Integer)null);
                }
            }

            mb.recon_mb();
        } else {
            int var10;
            SubBlock var11;
            for(var10 = 0; var10 < 4; ++var10) {
                for(j = 0; j < 4; ++j) {
                    var11 = mb.getYSubBlock(j, var10);
                    var11.dequantSubBlock(frame, (Integer)null);
                    var11.predict(frame);
                    var11.reconstruct();
                }
            }

            mb.predictUV(frame);

            for(var10 = 0; var10 < 2; ++var10) {
                for(j = 0; j < 2; ++j) {
                    var11 = mb.getUSubBlock(j, var10);
                    var11.dequantSubBlock(frame, (Integer)null);
                    var11.reconstruct();
                }
            }

            for(var10 = 0; var10 < 2; ++var10) {
                for(j = 0; j < 2; ++j) {
                    var11 = mb.getVSubBlock(j, var10);
                    var11.dequantSubBlock(frame, (Integer)null);
                    var11.reconstruct();
                }
            }
        }

    }

    private void decodePlaneTokens(VP8Frame frame, int dimentions, SubBlock.PLANE plane, boolean withY2) throws IOException {
        MacroBlock mb = this;

        for(int y = 0; y < dimentions; ++y) {
            for(int x = 0; x < dimentions; ++x) {
                byte L = 0;
                byte A = 0;
                byte lc = 0;
                SubBlock sb = mb.getSubBlock(plane, x, y);
                SubBlock left = frame.getLeftSubBlock(sb, plane);
                SubBlock above = frame.getAboveSubBlock(sb, plane);
                if(left.hasNoZeroToken()) {
                    L = 1;
                }

                int var14 = lc + L;
                if(above.hasNoZeroToken()) {
                    A = 1;
                }

                var14 += A;
                sb.decodeSubBlock(frame.getTokenBoolDecoder(), frame.getCoefProbs(), var14, SubBlock.planeToType(plane, Boolean.valueOf(withY2)), withY2);
            }
        }

    }

    public void drawDebug() {
        for(int j = 0; j < 4; ++j) {
            for(int i = 0; i < 4; ++i) {
                SubBlock sb = this.ySubBlocks[i][0];
                sb.drawDebugH();
                sb = this.ySubBlocks[0][j];
                sb.drawDebugV();
            }
        }

    }
}
