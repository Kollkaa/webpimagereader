/*
 * Copyright (c) https://sourceforge.net/projects/javavp8decoder/
 *
 * 2016/9/28
 */

package org.vp8Decoder;

import java.io.IOException;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VP8Frame {
    private static int MAX_REF_LF_DELTAS = 4;
    private static int MAX_MODE_LF_DELTAS = 4;
    private static int BLOCK_TYPES = 4;
    private static int COEF_BANDS = 8;
    private static int PREV_COEF_CONTEXTS = 3;
    private static int MAX_ENTROPY_TOKENS = 12;
    private int[] frame;
    private int[][][][] coefProbs;
    private int qIndex;
    private int mb_no_coeff_skip;
    private int macroBlockRows;
    private int macroBlockCols;
    private int multiTokenPartition = 0;
    private int segmentation_enabled;
    private BoolDecoder tokenBoolDecoder;
    private Vector<BoolDecoder> tokenBoolDecoders;
    private MacroBlock[][] macroBlocks;
    private int filterLevel;
    private int filterType;
    private int sharpnessLevel;
    private int frameType;
    private int width;
    private int height;
    private int mb_segement_abs_delta;
    private int[] mb_segment_tree_probs;
    private int update_mb_segmentation_map;
    private int update_mb_segmentaton_data;

    public int getSharpnessLevel() {
        return this.sharpnessLevel;
    }

    public int getFrameType() {
        return this.frameType;
    }

    public VP8Frame(int[] frame, int[][][][] coef_probs) {
        this.frame = frame;
        this.coefProbs = coef_probs;
        this.tokenBoolDecoders = new Vector();
    }

    private void createMacroBlocks() {
        this.macroBlocks = new MacroBlock[this.macroBlockCols + 2][this.macroBlockRows + 2];

        for(int x = 0; x < this.macroBlockCols + 2; ++x) {
            for(int y = 0; y < this.macroBlockRows + 2; ++y) {
                this.macroBlocks[x][y] = new MacroBlock(x, y);
            }
        }

    }

    private static DeltaQ get_delta_q(BoolDecoder bc, int prev) {
        DeltaQ ret = new DeltaQ();
        ret.v = 0;
        ret.update = false;
        if(bc.read_bit() > 0) {
            ret.v = bc.read_literal(4);
            if(bc.read_bit() > 0) {
                ret.v = -ret.v;
            }
        }

        if(ret.v != prev) {
            ret.update = true;
        }

        return ret;
    }

    public boolean decodeFrame(boolean debug) throws IOException {
        int[] ref_lf_deltas = new int[MAX_REF_LF_DELTAS];
        int[] mode_lf_deltas = new int[MAX_MODE_LF_DELTAS];
        byte offset = 0;
        int var29 = offset + 1;
        int c = this.frame[offset];
        this.frameType = this.getBitAsInt(c, 0);

        if (log.isDebugEnabled()) {
            log.debug("frame.length: " + this.frame.length + " Frame type: " + this.frameType);
        }

        if(this.frameType != 0) {
            return false;
        } else {
            int versionNumber = this.getBitAsInt(c, 1) << 1;
            versionNumber += this.getBitAsInt(c, 2) << 1;
            versionNumber += this.getBitAsInt(c, 3);
            if (log.isDebugEnabled()) {
                log.debug("Version Number: " + versionNumber + " show_frame: " + this.getBit(c, 4));
            }
            int first_partition_length_in_bytes = this.getBitAsInt(c, 5) << 0;
            first_partition_length_in_bytes += this.getBitAsInt(c, 6) << 1;
            first_partition_length_in_bytes += this.getBitAsInt(c, 7) << 2;
            c = this.frame[var29++];
            first_partition_length_in_bytes += c << 3;
            c = this.frame[var29++];
            first_partition_length_in_bytes += c << 11;
            if (log.isDebugEnabled()) {
                log.debug("first_partition_length_in_bytes: " + first_partition_length_in_bytes);
            }
            c = this.frame[var29++];
            if (log.isDebugEnabled()) {
                log.debug("StartCode: " + c);
            }
            c = this.frame[var29++];
            if (log.isDebugEnabled()) {
                log.debug(" " + c);
            }
            c = this.frame[var29++];
            if (log.isDebugEnabled()) {
                log.debug(" " + c);
            }
            c = this.frame[var29++];
            int hBytes = c;
            c = this.frame[var29++];
            hBytes += c << 8;
            this.width = hBytes & 16383;
            if (log.isDebugEnabled()) {
                log.debug("width: " + this.width + " hScale: " + (hBytes >> 14));
            }
            c = this.frame[var29++];
            int vBytes = c;
            c = this.frame[var29++];
            vBytes += c << 8;
            this.height = vBytes & 16383;
            if (log.isDebugEnabled()) {
                log.debug("height: " + this.height + " vScale: " + (vBytes >> 14));
            }
            int tWidth = this.width;
            int tHeight = this.height;
            if((tWidth & 15) != 0) {
                tWidth += 16 - (tWidth & 15);
            }

            if((tHeight & 15) != 0) {
                tHeight += 16 - (tHeight & 15);
            }

            this.macroBlockRows = tHeight >> 4;
            this.macroBlockCols = tWidth >> 4;
            if (log.isDebugEnabled()) {
                log.debug("macroBlockCols: " + this.macroBlockCols + " macroBlockRows: " + this.macroBlockRows);
            }
            this.createMacroBlocks();
            BoolDecoder bc = new BoolDecoder(this.frame, var29);
            int mode_ref_lf_delta_enabled;
            int Qindex;
            if(this.frameType == 0) {
                mode_ref_lf_delta_enabled = bc.read_bit();
                if (log.isDebugEnabled()) {
                    log.debug("clr_type: " + mode_ref_lf_delta_enabled + " bc: " + bc);
                }
                Qindex = bc.read_bit();
                if (log.isDebugEnabled()) {
                    log.debug("clamp_type: " + Qindex);
                }
            }

            this.segmentation_enabled = bc.read_bit();
            if (log.isDebugEnabled()) {
                log.debug("segmentation_enabled: " + this.segmentation_enabled);
            }
            int q_update;
            if(this.segmentation_enabled > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("TODO");
                }
                this.update_mb_segmentation_map = bc.read_bit();
                this.update_mb_segmentaton_data = bc.read_bit();
                if (log.isDebugEnabled()) {
                    log.debug("update_mb_segmentaton_map: " + this.update_mb_segmentation_map + "update_mb_segmentaton_data: " + this.update_mb_segmentaton_data);
                }
                if(this.update_mb_segmentaton_data > 0) {
                    if(this.update_mb_segmentaton_data > 0) {
                        this.mb_segement_abs_delta = bc.read_bit();

                        for(mode_ref_lf_delta_enabled = 0; mode_ref_lf_delta_enabled < 2; ++mode_ref_lf_delta_enabled) {
                            for(Qindex = 0; Qindex < 4; ++Qindex) {
                                if(bc.read_bit() > 0) {
                                    q_update = bc.read_literal(Globals.vp8_mb_feature_data_bits[mode_ref_lf_delta_enabled]);
                                    if(bc.read_bit() > 0) {
                                        q_update = -q_update;
                                    }
                                }
                            }
                        }
                    }

                    if(this.update_mb_segmentation_map > 0) {
                        this.mb_segment_tree_probs = new int[3];

                        for(mode_ref_lf_delta_enabled = 0; mode_ref_lf_delta_enabled < 3; ++mode_ref_lf_delta_enabled) {
                            boolean var30 = true;
                            if(bc.read_bit() > 0) {
                                Qindex = bc.read_literal(8);
                            } else {
                                Qindex = 255;
                            }

                            this.mb_segment_tree_probs[mode_ref_lf_delta_enabled] = Qindex;
                        }
                    }
                }
            }

            this.filterType = bc.read_bit();
            if (log.isDebugEnabled()) {
                log.debug("filter_type: " + this.filterType);
            }
            this.filterLevel = bc.read_literal(6);
            if (log.isDebugEnabled()) {
                log.debug( "filter_level: " + this.filterLevel);
            }
            this.sharpnessLevel = bc.read_literal(3);
            if (log.isDebugEnabled()) {
                log.debug("sharpness_level: " + this.sharpnessLevel);
            }
            mode_ref_lf_delta_enabled = bc.read_bit();
            if (log.isDebugEnabled()) {
                log.debug("mode_ref_lf_delta_enabled: " + mode_ref_lf_delta_enabled);
            }
            if(mode_ref_lf_delta_enabled > 0) {
                Qindex = bc.read_bit();
                if (log.isDebugEnabled()) {
                    log.debug("mode_ref_lf_delta_update: " + Qindex);
                }
                if(Qindex > 0) {
                    for(q_update = 0; q_update < MAX_REF_LF_DELTAS; ++q_update) {
                        if(bc.read_bit() > 0) {
                            ref_lf_deltas[q_update] = bc.read_literal(6);
                            if(bc.read_bit() > 0) {
                                ref_lf_deltas[q_update] *= -1;
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("ref_lf_deltas[i]: " + ref_lf_deltas[q_update]);
                            }
                        }
                    }

                    for(q_update = 0; q_update < MAX_MODE_LF_DELTAS; ++q_update) {
                        if(bc.read_bit() > 0) {
                            mode_lf_deltas[q_update] = bc.read_literal(6);
                            if(bc.read_bit() > 0) {
                                mode_lf_deltas[q_update] *= -1;
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("mode_lf_deltas[i]: " + mode_lf_deltas[q_update]);
                            }
                        }
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("offset: " + var29);
            }
            this.setupTokenDecoder(bc, this.frame, first_partition_length_in_bytes, var29);
            Qindex = bc.read_literal(7);
            if (log.isDebugEnabled()) {
                log.debug("Q: " + Qindex);
            }
            this.qIndex = Qindex;
            boolean var31 = false;
            DeltaQ v = get_delta_q(bc, 0);
            int y1dc_delta_q = v.v;
            var31 = var31 || v.update;
            if (log.isDebugEnabled()) {
                log.debug("y1dc_delta_q: " + y1dc_delta_q + " q_update: " + var31);
            }
            v = get_delta_q(bc, 0);
            int y2dc_delta_q = v.v;
            var31 = var31 || v.update;
            if (log.isDebugEnabled()) {
                log.debug("y2dc_delta_q: " + y2dc_delta_q + " q_update: " + var31);
            }
            v = get_delta_q(bc, 0);
            int y2ac_delta_q = v.v;
            var31 = var31 || v.update;
            if (log.isDebugEnabled()) {
                log.debug("y2ac_delta_q: " + y2ac_delta_q + " q_update: " + var31);
            }
            v = get_delta_q(bc, 0);
            int uvdc_delta_q = v.v;
            var31 = var31 || v.update;
            if (log.isDebugEnabled()) {
                log.debug("uvdc_delta_q: " + uvdc_delta_q + " q_update: " + var31);
            }
            v = get_delta_q(bc, 0);
            int uvac_delta_q = v.v;
            var31 = var31 || v.update;
            if (log.isDebugEnabled()) {
                log.debug("uvac_delta_q: " + uvac_delta_q + " q_update: " + var31);
            }

            if(this.frameType != 0) {
                throw new IllegalArgumentException("bad input: not intra");
            } else {
                int refresh_entropy_probs = bc.read_bit();
                if (log.isDebugEnabled()) {
                    log.debug("refresh_entropy_probs: " + refresh_entropy_probs);
                }
                boolean refresh_last_frame = false;
                int var33;
                if(this.frameType == 0) {
                    var33 = 1;
                } else {
                    var33 = bc.read_bit();
                }
                if (log.isDebugEnabled()) {
                    log.debug("refresh_last_frame: " + var33);
                }
                int ibc;
                int num_part;
                int mb_row;
                for(ibc = 0; ibc < BLOCK_TYPES; ++ibc) {
                    for(num_part = 0; num_part < COEF_BANDS; ++num_part) {
                        for(mb_row = 0; mb_row < PREV_COEF_CONTEXTS; ++mb_row) {
                            for(int l = 0; l < MAX_ENTROPY_TOKENS - 1; ++l) {
                                if(bc.read_bool(Globals.coef_update_probs[ibc][num_part][mb_row][l]) > 0) {
                                    int newp = bc.read_literal(8);
                                    this.coefProbs[ibc][num_part][mb_row][l] = newp;
                                }
                            }
                        }
                    }
                }

                this.mb_no_coeff_skip = bc.read_bit();
                if (log.isDebugEnabled()) {
                    log.debug("mb_no_coeff_skip: " + this.mb_no_coeff_skip);
                }
                if(this.frameType == 0) {
                    this.readModes(bc);
                    ibc = 0;
                    num_part = 1 << this.multiTokenPartition;

                    for(mb_row = 0; mb_row < this.macroBlockRows; ++mb_row) {
                        if(num_part > 1) {
                            this.tokenBoolDecoder = (BoolDecoder)this.tokenBoolDecoders.elementAt(ibc);
                            this.decodeMacroBlockRow(mb_row);
                            ++ibc;
                            if(ibc == num_part) {
                                ibc = 0;
                            }
                        } else {
                            this.decodeMacroBlockRow(mb_row);
                        }
                    }

                    if(debug) {
                        this.drawDebug();
                    }

                    return true;
                } else {
                    throw new IllegalArgumentException("bad input: not intra");
                }
            }
        }
    }

    private void drawDebug() {
        for(int mb_row = 0; mb_row < this.macroBlockRows; ++mb_row) {
            for(int mb_col = 0; mb_col < this.macroBlockCols; ++mb_col) {
                this.macroBlocks[mb_col + 1][mb_row + 1].drawDebug();
            }
        }

    }

    public int getFilterType() {
        return this.filterType;
    }

    public int getFilterLevel() {
        return this.filterLevel;
    }

    private void decodeMacroBlockRow(int mbRow) throws IOException {
        for(int mb_col = 0; mb_col < this.macroBlockCols; ++mb_col) {
            MacroBlock mb = this.getMacroBlock(mb_col, mbRow);
            mb.decodeMacroBlock(this);
            mb.dequantMacroBlock(this);
        }

    }

    public SubBlock getAboveRightSubBlock(SubBlock sb, SubBlock.PLANE plane) {
        MacroBlock mb = sb.getMacroBlock();
        int x = mb.getSubblockX(sb);
        int y = mb.getSubblockY(sb);
        if(plane == SubBlock.PLANE.Y1) {
            SubBlock r;
            MacroBlock var12;
            if(y == 0 && x < 3) {
                var12 = this.getMacroBlock(mb.getX(), mb.getY() - 1);
                r = var12.getSubBlock(plane, x + 1, 3);
                return r;
            } else if(y == 0 && x == 3) {
                var12 = this.getMacroBlock(mb.getX() + 1, mb.getY() - 1);
                r = var12.getSubBlock(plane, 0, 3);
                if(var12.getX() == this.getMacroBlockCols()) {
                    int[][] dest = new int[4][4];

                    for(int b = 0; b < 4; ++b) {
                        for(int a = 0; a < 4; ++a) {
                            if(var12.getY() < 0) {
                                dest[a][b] = 127;
                            } else {
                                dest[a][b] = this.getMacroBlock(mb.getX(), mb.getY() - 1).getSubBlock(SubBlock.PLANE.Y1, 3, 3).getDest()[3][3];
                            }
                        }
                    }

                    r = new SubBlock(var12, null, null, SubBlock.PLANE.Y1);
                    r.setDest(dest);
                }

                return r;
            } else if(y > 0 && x < 3) {
                r = mb.getSubBlock(plane, x + 1, y - 1);
                return r;
            } else {
                SubBlock sb2 = mb.getSubBlock(sb.getPlane(), 3, 0);
                return this.getAboveRightSubBlock(sb2, plane);
            }
        } else {
            throw new IllegalArgumentException("bad input: getAboveRightSubBlock()");
        }
    }

    public SubBlock getAboveSubBlock(SubBlock sb, SubBlock.PLANE plane) {
        SubBlock r = sb.getAbove();
        if(r == null) {
            MacroBlock mb = sb.getMacroBlock();
            int x = mb.getSubblockX(sb);

            MacroBlock mb2;
            for(mb2 = this.getMacroBlock(mb.getX(), mb.getY() - 1); plane == SubBlock.PLANE.Y2 && mb2.getYMode() == 4; mb2 = this.getMacroBlock(mb2.getX(), mb2.getY() - 1)) {
                ;
            }

            r = mb2.getBottomSubBlock(x, sb.getPlane());
        }

        return r;
    }

    private boolean getBit(int data, int bit) {
        int r = data & 1 << bit;
        return r > 0;
    }

    private int getBitAsInt(int data, int bit) {
        int r = data & 1 << bit;
        return r > 0?1:0;
    }

    public int[][][][] getCoefProbs() {
        return this.coefProbs;
    }

    public SubBlock getLeftSubBlock(SubBlock sb, SubBlock.PLANE plane) {
        SubBlock r = sb.getLeft();
        if(r == null) {
            MacroBlock mb = sb.getMacroBlock();
            int y = mb.getSubblockY(sb);

            MacroBlock mb2;
            for(mb2 = this.getMacroBlock(mb.getX() - 1, mb.getY()); plane == SubBlock.PLANE.Y2 && mb2.getYMode() == 4; mb2 = this.getMacroBlock(mb2.getX() - 1, mb2.getY())) {
                ;
            }

            r = mb2.getRightSubBlock(y, sb.getPlane());
        }

        return r;
    }

    public MacroBlock getMacroBlock(int mbCol, int mbRow) {
        return this.macroBlocks[mbCol + 1][mbRow + 1];
    }

    public int getMacroBlockCols() {
        return this.macroBlockCols;
    }

    public int getMacroBlockRows() {
        return this.macroBlockRows;
    }

    public int getQIndex() {
        return this.qIndex;
    }

    public BoolDecoder getTokenBoolDecoder() {
        return this.tokenBoolDecoder;
    }

    public int[][] getUBuffer() {
        int[][] r = new int[this.macroBlockCols * 8][this.macroBlockRows * 8];

        for(int y = 0; y < this.macroBlockRows; ++y) {
            for(int x = 0; x < this.macroBlockCols; ++x) {
                MacroBlock mb = this.macroBlocks[x + 1][y + 1];

                for(int b = 0; b < 2; ++b) {
                    for(int a = 0; a < 2; ++a) {
                        SubBlock sb = mb.getUSubBlock(a, b);

                        for(int d = 0; d < 4; ++d) {
                            for(int c = 0; c < 4; ++c) {
                                r[x * 8 + a * 4 + c][y * 8 + b * 4 + d] = sb.getDest()[c][d];
                            }
                        }
                    }
                }
            }
        }

        return r;
    }

    public int[][] getVBuffer() {
        int[][] r = new int[this.macroBlockCols * 8][this.macroBlockRows * 8];

        for(int y = 0; y < this.macroBlockRows; ++y) {
            for(int x = 0; x < this.macroBlockCols; ++x) {
                MacroBlock mb = this.macroBlocks[x + 1][y + 1];

                for(int b = 0; b < 2; ++b) {
                    for(int a = 0; a < 2; ++a) {
                        SubBlock sb = mb.getVSubBlock(a, b);

                        for(int d = 0; d < 4; ++d) {
                            for(int c = 0; c < 4; ++c) {
                                r[x * 8 + a * 4 + c][y * 8 + b * 4 + d] = sb.getDest()[c][d];
                            }
                        }
                    }
                }
            }
        }

        return r;
    }

    public int[][] getYBuffer() {
        int[][] r = new int[this.macroBlockCols * 16][this.macroBlockRows * 16];

        for(int y = 0; y < this.macroBlockRows; ++y) {
            for(int x = 0; x < this.macroBlockCols; ++x) {
                MacroBlock mb = this.macroBlocks[x + 1][y + 1];

                for(int b = 0; b < 4; ++b) {
                    for(int a = 0; a < 4; ++a) {
                        SubBlock sb = mb.getYSubBlock(a, b);

                        for(int d = 0; d < 4; ++d) {
                            for(int c = 0; c < 4; ++c) {
                                r[x * 16 + a * 4 + c][y * 16 + b * 4 + d] = sb.getDest()[c][d];
                            }
                        }
                    }
                }
            }
        }

        return r;
    }

    private void readModes(BoolDecoder bc) {
        int mb_row = -1;
        int prob_skip_false = 0;
        if(this.mb_no_coeff_skip > 0) {
            prob_skip_false = bc.read_literal(8);
        }

        while(true) {
            ++mb_row;
            if(mb_row >= this.macroBlockRows) {
                return;
            }

            int mb_col = -1;

            while(true) {
                ++mb_col;
                if(mb_col >= this.macroBlockCols) {
                    break;
                }

                MacroBlock mb = this.getMacroBlock(mb_col, mb_row);
                if(this.segmentation_enabled > 0 && this.update_mb_segmentation_map > 0) {
                    bc.treed_read(Globals.mb_segment_tree, this.mb_segment_tree_probs);
                }

                int var14;
                if(this.mb_no_coeff_skip > 0) {
                    var14 = bc.read_bool(prob_skip_false);
                } else {
                    var14 = 0;
                }

                mb.setMb_skip_coeff(var14);
                int y_mode = this.readYMode(bc);
                mb.setYMode(y_mode);
                int x;
                SubBlock sb;
                int var15;
                if(y_mode == 4) {
                    for(var15 = 0; var15 < 4; ++var15) {
                        for(x = 0; x < 4; ++x) {
                            SubBlock var16 = mb.getYSubBlock(x, var15);
                            sb = this.getAboveSubBlock(var16, SubBlock.PLANE.Y1);
                            SubBlock L = this.getLeftSubBlock(var16, SubBlock.PLANE.Y1);
                            int mode1 = this.readSubBlockMode(bc, sb.getMode(), L.getMode());
                            var16.setMode(mode1);
                        }
                    }
                } else {
                    byte mode;
                    switch(y_mode) {
                    case 0:
                        mode = 0;
                        break;
                    case 1:
                        mode = 2;
                        break;
                    case 2:
                        mode = 3;
                        break;
                    case 3:
                        mode = 1;
                        break;
                    default:
                        mode = 0;
                    }

                    for(x = 0; x < 4; ++x) {
                        for(int y = 0; y < 4; ++y) {
                            sb = mb.getYSubBlock(x, y);
                            sb.setMode(mode);
                        }
                    }
                }

                var15 = this.readUvMode(bc);
                mb.setUvMode(var15);
            }
        }
    }

    private int readSubBlockMode(BoolDecoder bc, int A, int L) {
        int i = bc.treed_read(Globals.bmode_tree, Globals.kf_bmode_prob[A][L]);
        return i;
    }

    private int readUvMode(BoolDecoder bc) {
        int i = bc.treed_read(Globals.uv_mode_tree, Globals.kf_uv_mode_prob);
        return i;
    }

    private int readYMode(BoolDecoder bc) {
        int i = bc.treed_read(Globals.vp8_kf_ymode_tree, Globals.kf_ymode_prob);
        return i;
    }

    private int readPartitionSize(int[] data, int offset) {
        int size = data[offset + 0] + (data[offset + 1] << 8) + (data[offset + 2] << 16);
        return size;
    }

    private void setupTokenDecoder(BoolDecoder bc, int[] data, int first_partition_length_in_bytes, int offset) {
        int partitionsStart = offset + first_partition_length_in_bytes;
        int partition = partitionsStart;
        this.multiTokenPartition = bc.read_literal(2);
        if (log.isDebugEnabled()) {
            log.debug("multi_token_partition: " + this.multiTokenPartition);
        }
        int num_part = 1 << this.multiTokenPartition;
        if (log.isDebugEnabled()) {
            log.debug("num_part: " + num_part);
        }
        if(num_part > 1) {
            partition = partitionsStart + 3 * (num_part - 1);
        }

        for(int i = 0; i < num_part; ++i) {
            int var10;
            if(i < num_part - 1) {
                var10 = this.readPartitionSize(data, partitionsStart + i * 3);
            } else {
                var10 = data.length - partition;
            }

            this.tokenBoolDecoders.add(new BoolDecoder(this.frame, partition));
            partition += var10;
        }

        this.tokenBoolDecoder = this.tokenBoolDecoders.elementAt(0);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
