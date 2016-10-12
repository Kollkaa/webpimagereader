/*
 * Copyright (c) https://sourceforge.net/projects/javavp8decoder/
 *
 * 2016/9/28
 */

package org.vp8Decoder;

public class Segment {
    int P0;
    int P1;
    int P2;
    int P3;
    int Q0;
    int Q1;
    int Q2;
    int Q3;

    public Segment() {
    }

    public String toString() {
        return this.P3 + " " + this.P2 + " " + this.P1 + " " + this.P0 + " " + this.Q0 + " " + this.Q1 + " " + this.Q2 + " " + this.Q3;
    }
}
