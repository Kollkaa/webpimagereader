/*
 * Copyright (c) https://sourceforge.net/projects/javavp8decoder/
 *
 * 2016/9/28
 */

package org.vp8Decoder;

public class LoopFilter {
    public LoopFilter() {
    }

    public static void loopFilter(VP8Frame frame) {
        System.out.println("loop filter");
        System.out.println("filterLevel: " + frame.getFilterLevel());
        System.out.println("filterType: " + frame.getFilterType());
        int sharpnessLevel = frame.getSharpnessLevel();
        int loop_filter_level = frame.getFilterLevel();
        int interior_limit = frame.getFilterLevel();
        if(sharpnessLevel > 0) {
            interior_limit >>= sharpnessLevel > 4?2:1;
            if(interior_limit > 9 - sharpnessLevel) {
                interior_limit = 9 - sharpnessLevel;
            }
        }

        if(interior_limit == 0) {
            interior_limit = 1;
        }

        System.out.println("interior_limit: " + interior_limit);
        byte hev_threshold = 0;
        if(frame.getFrameType() == 0) {
            if(loop_filter_level >= 40) {
                hev_threshold = 2;
            } else if(loop_filter_level >= 15) {
                hev_threshold = 1;
            }
        } else if(loop_filter_level >= 40) {
            hev_threshold = 3;
        } else if(loop_filter_level >= 20) {
            hev_threshold = 2;
        } else if(loop_filter_level >= 15) {
            hev_threshold = 1;
        }

        System.out.println("hev_threshold: " + hev_threshold);
        int mbedge_limit = (loop_filter_level + 2) * 2 + interior_limit;
        int sub_bedge_limit = loop_filter_level * 2 + interior_limit;
        System.out.println("mbedge_limit: " + mbedge_limit);
        System.out.println("sub_bedge_limit: " + sub_bedge_limit);

        for(int y = 0; y < frame.getMacroBlockRows(); ++y) {
            for(int x = 0; x < frame.getMacroBlockCols(); ++x) {
                MacroBlock rmb = frame.getMacroBlock(x, y);
                MacroBlock bmb = frame.getMacroBlock(x, y);
                MacroBlock a;
                int b;
                SubBlock tsb;
                SubBlock bsb;
                int c;
                int[][] bdest;
                int[][] tdest;
                Segment seg;
                if(x > 0) {
                    a = frame.getMacroBlock(x - 1, y);

                    for(b = 0; b < 4; ++b) {
                        tsb = rmb.getSubBlock(SubBlock.PLANE.Y1, 0, b);
                        bsb = a.getSubBlock(SubBlock.PLANE.Y1, 3, b);

                        for(c = 0; c < 4; ++c) {
                            bdest = tsb.getDest();
                            tdest = bsb.getDest();
                            seg = new Segment();
                            seg.P0 = tdest[3][c];
                            seg.P1 = tdest[2][c];
                            seg.P2 = tdest[1][c];
                            seg.P3 = tdest[0][c];
                            seg.Q0 = bdest[0][c];
                            seg.Q1 = bdest[1][c];
                            seg.Q2 = bdest[2][c];
                            seg.Q3 = bdest[3][c];
                            MBfilter(hev_threshold, interior_limit, mbedge_limit, seg);
                            tdest[3][c] = seg.P0;
                            tdest[2][c] = seg.P1;
                            tdest[1][c] = seg.P2;
                            tdest[0][c] = seg.P3;
                            bdest[0][c] = seg.Q0;
                            bdest[1][c] = seg.Q1;
                            bdest[2][c] = seg.Q2;
                            bdest[3][c] = seg.Q3;
                        }
                    }
                }

                int var19;
                for(var19 = 1; var19 < 4; ++var19) {
                    for(b = 0; b < 4; ++b) {
                        tsb = rmb.getSubBlock(SubBlock.PLANE.Y1, var19 - 1, b);
                        bsb = rmb.getSubBlock(SubBlock.PLANE.Y1, var19, b);

                        for(c = 0; c < 4; ++c) {
                            bdest = bsb.getDest();
                            tdest = tsb.getDest();
                            seg = new Segment();
                            seg.P0 = tdest[3][c];
                            seg.P1 = tdest[2][c];
                            seg.P2 = tdest[1][c];
                            seg.P3 = tdest[0][c];
                            seg.Q0 = bdest[0][c];
                            seg.Q1 = bdest[1][c];
                            seg.Q2 = bdest[2][c];
                            seg.Q3 = bdest[3][c];
                            subblock_filter(hev_threshold, interior_limit, sub_bedge_limit, seg);
                            tdest[3][c] = seg.P0;
                            tdest[2][c] = seg.P1;
                            tdest[1][c] = seg.P2;
                            tdest[0][c] = seg.P3;
                            bdest[0][c] = seg.Q0;
                            bdest[1][c] = seg.Q1;
                            bdest[2][c] = seg.Q2;
                            bdest[3][c] = seg.Q3;
                        }
                    }
                }

                if(y > 0) {
                    a = frame.getMacroBlock(x, y - 1);

                    for(b = 0; b < 4; ++b) {
                        tsb = a.getSubBlock(SubBlock.PLANE.Y1, b, 3);
                        bsb = bmb.getSubBlock(SubBlock.PLANE.Y1, b, 0);

                        for(c = 0; c < 4; ++c) {
                            bdest = bsb.getDest();
                            tdest = tsb.getDest();
                            seg = new Segment();
                            seg.P0 = tdest[c][3];
                            seg.P1 = tdest[c][2];
                            seg.P2 = tdest[c][1];
                            seg.P3 = tdest[c][0];
                            seg.Q0 = bdest[c][0];
                            seg.Q1 = bdest[c][1];
                            seg.Q2 = bdest[c][2];
                            seg.Q3 = bdest[c][3];
                            System.out.println("a: " + c);
                            MBfilter(hev_threshold, interior_limit, mbedge_limit, seg);
                            tdest[c][3] = seg.P0;
                            tdest[c][2] = seg.P1;
                            tdest[c][1] = seg.P2;
                            tdest[c][0] = seg.P3;
                            bdest[c][0] = seg.Q0;
                            bdest[c][1] = seg.Q1;
                            bdest[c][2] = seg.Q2;
                            bdest[c][3] = seg.Q3;
                        }
                    }
                }

                for(var19 = 1; var19 < 4; ++var19) {
                    for(b = 0; b < 4; ++b) {
                        tsb = bmb.getSubBlock(SubBlock.PLANE.Y1, b, var19 - 1);
                        bsb = bmb.getSubBlock(SubBlock.PLANE.Y1, b, var19);

                        for(c = 0; c < 4; ++c) {
                            bdest = bsb.getDest();
                            tdest = tsb.getDest();
                            seg = new Segment();
                            seg.P0 = tdest[c][3];
                            seg.P1 = tdest[c][2];
                            seg.P2 = tdest[c][1];
                            seg.P3 = tdest[c][0];
                            seg.Q0 = bdest[c][0];
                            seg.Q1 = bdest[c][1];
                            seg.Q2 = bdest[c][2];
                            seg.Q3 = bdest[c][3];
                            subblock_filter(hev_threshold, interior_limit, sub_bedge_limit, seg);
                            tdest[c][3] = seg.P0;
                            tdest[c][2] = seg.P1;
                            tdest[c][1] = seg.P2;
                            tdest[c][0] = seg.P3;
                            bdest[c][0] = seg.Q0;
                            bdest[c][1] = seg.Q1;
                            bdest[c][2] = seg.Q2;
                            bdest[c][3] = seg.Q3;
                        }
                    }
                }
            }
        }

    }

    private static void simple_segment(int edge_limit, Segment seg) {
        if(abs(seg.P0 - seg.Q0) * 2 + abs(seg.P1 - seg.Q1) / 2 <= edge_limit) {
            System.out.println("True");
            common_adjust(true, seg);
        } else {
            System.out.println(false);
        }

    }

    public static boolean filter_yes(int I, int E, int p3, int p2, int p1, int p0, int q0, int q1, int q2, int q3) {
        return abs(p0 - q0) * 2 + abs(p1 - q1) / 2 <= E && abs(p3 - p2) <= I && abs(p2 - p1) <= I && abs(p1 - p0) <= I && abs(q3 - q2) <= I && abs(q2 - q1) <= I && abs(q1 - q0) <= I;
    }

    public static boolean hev(int threshold, int p1, int p0, int q0, int q1) {
        return abs(p1 - p0) > threshold || abs(q1 - q0) > threshold;
    }

    public static void subblock_filter(int hev_threshold, int interior_limit, int edge_limit, Segment seg) {
        int p3 = u2s(seg.P3);
        int p2 = u2s(seg.P2);
        int p1 = u2s(seg.P1);
        int p0 = u2s(seg.P0);
        int q0 = u2s(seg.Q0);
        int q1 = u2s(seg.Q1);
        int q2 = u2s(seg.Q2);
        int q3 = u2s(seg.Q3);
        if(filter_yes(interior_limit, edge_limit, q3, q2, q1, q0, p0, p1, p2, p3)) {
            boolean hv = hev(hev_threshold, p1, p0, q0, q1);
            int a = common_adjust(hv, seg) + 1 >> 1;
            if(!hv) {
                seg.Q1 = s2u(q1 - a);
                seg.P1 = s2u(p1 + a);
            }
        }

    }

    static void MBfilter(int hev_threshold, int interior_limit, int edge_limit, Segment seg) {
        int p3 = u2s(seg.P3);
        int p2 = u2s(seg.P2);
        int p1 = u2s(seg.P1);
        int p0 = u2s(seg.P0);
        int q0 = u2s(seg.Q0);
        int q1 = u2s(seg.Q1);
        int q2 = u2s(seg.Q2);
        int q3 = u2s(seg.Q3);
        if(filter_yes(interior_limit, edge_limit, q3, q2, q1, q0, p0, p1, p2, p3)) {
            System.out.println("filter_yes");
            System.out.println("p0: " + p0);
            System.out.println("s2u(p0): " + s2u(p0));
            if(!hev(hev_threshold, p1, p0, q0, q1)) {
                System.out.println("hev");
                int w = c(c(p1 - q1) + 3 * (q0 - p0));
                System.out.println("w: " + w);
                int a = 27 * w + 63 >> 7;
                System.out.println("a: " + a);
                System.out.println(27 * w + 63);
                System.out.println(c(27 * w + 63));
                System.out.println(27 * w + 63 >> 7);
                seg.Q0 = s2u(q0 - a);
                seg.P0 = s2u(p0 + a);
                a = 18 * w + 63 >> 7;
                System.out.println("a: " + a);
                seg.Q1 = s2u(q1 - a);
                seg.P1 = s2u(p1 + a);
                a = 9 * w + 63 >> 7;
                System.out.println("a: " + a);
                seg.Q2 = s2u(q2 - a);
                seg.P2 = s2u(p2 + a);
                System.out.println(seg);
            } else {
                common_adjust(true, seg);
            }
        }

    }

    private static int common_adjust(boolean use_outer_taps, Segment seg) {
        int p1 = u2s(seg.P1);
        int p0 = u2s(seg.P0);
        int q0 = u2s(seg.Q0);
        int q1 = u2s(seg.Q1);
        int a = c((use_outer_taps?c(p1 - q1):0) + 3 * (q0 - p0));
        int b = (a & 7) == 4?-1:0;
        a = c(a + 4) >> 3;
        seg.Q0 = s2u(q0 - a);
        seg.P0 = s2u(p0 + c(a + b));
        return a;
    }

    private static int c(int v) {
        int r = v;
        if(v < -128) {
            r = -128;
        }

        if(v > 127) {
            r = 127;
        }

        return r;
    }

    private static int u2s(int v) {
        return v - 128;
    }

    private static int s2u(int v) {
        return c(v) + 128;
    }

    private static int abs(int v) {
        return v < 0?-v:v;
    }
}
