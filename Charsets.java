import static java.nio.charset.StandardCharsets.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;

class Charsets {

    static final Charset ISO_2022_JP = Charset.forName("ISO-2022-JP");
    static final Charset EUC_JP = Charset.forName("EUC-JP");
    static final Charset SHIFT_JIS = Charset.forName("Shift_JIS");
    static final Charset WINDOWS_31J = Charset.forName("Windows-31J");
    static final Charset SHIFT_JISX0213 = Charset.forName("x-SJIS_0213");

    final String option;
    final Charset encoding;

    public static void main(String[] args) {
        new Charsets(args).printEncodedLines();
    }

    static Charset optionToEncoding(String option) {
        if ("-jis".equals(option)) {
            return ISO_2022_JP;
        } else if ("-euc".equals(option)) {
            return EUC_JP;
        } else if ("-sjis".equals(option)) {
            return SHIFT_JISX0213;
        } else if ("-w31j".equals(option)) {
            return WINDOWS_31J;
        } else if ("-utf8".equals(option)) {
            return UTF_8;
        } else {
            return Charset.defaultCharset();
        }
    }

    Charsets(String[] args) {
        option = (args.length == 0) ? "" : args[0];
        encoding = optionToEncoding(option);
    }

    void printEncodedLines() {
        println("#");
        println("# encoding%s.txt", option);
        println("#");
        
        println();
        println("# US-ASCII");
        println();
        printHeader();
        printSeparator();
        for (int c = 0x00; c <= 0x1F; ++c) {
            printLines(encodedLines(c, option));
        }
        List<byte[]> laters = new ArrayList<>();
        printSeparator();
        for (int c = 0x20; c <= 0x7F; ++c) {
            List<byte[]> lines = encodedLines(c, option);
            printLine(lines.get(0));
            if (lines.size() > 1) {
                laters.addAll(lines.subList(1, lines.size()));
            }
        }
        
        println();
        println("# JIS X 0201");
        println();
        printHeader();
        printSeparator();
        printLines(laters);
        printSeparator();
        for (int c = 0x80; c <= 0x9F; ++c) {
            printLines(encodedLines(c, option));
        }
        printSeparator();
        for (int c = 0xA0; c <= 0xDF; ++c) {
            printLines(encodedLines(c, option));
        }
        printSeparator();
        for (int c = 0xE0; c <= 0xFF; ++c) {
            printLines(encodedLines(c, option));
        }
        
        println();
        println("# JIS X 0208 - マッピングが異なるもの");
        println();
        printHeaderX0208();
        printSeparator();
        printLines(encodedLines(1, 29, option));
        printLines(encodedLines(1, 33, option));
        printLines(encodedLines(1, 34, option));
        printLines(encodedLines(1, 61, option));
        printLines(encodedLines(1, 81, option));
        printLines(encodedLines(1, 82, option));
        printLines(encodedLines(2, 44, option));
        
        println();
        println("# JIS X 0208 - 非漢字");
        println();
        printHeaderX0208();
        for (int k = 1; k <= 12; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        println();
        println("# NEC特殊文字");
        println();
        printHeaderX0208();
        for (int k = 13; k <= 15; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        println();
        println("# JIS X 0208 - 第1水準漢字");
        println();
        printHeaderX0208();
        for (int k = 16; k <= 47; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        println();
        println("# JIS X 0208 - 第2水準漢字");
        println();
        printHeaderX0208();
        for (int k = 48; k <= 88; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        println();
        println("# NEC選定IBM拡張文字");
        println();
        printHeaderX0208();
        for (int k = 89; k <= 94; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        println();
        println("# ユーザー外字領域");
        println();
        printHeaderX0208();
        for (int k = 95; k <= 114; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        println();
        println("# IBM拡張文字");
        println();
        printHeaderX0208();
        for (int k = 115; k <= 120; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        println();
        println("# JIS X 0213 - 非漢字");
        println();
        printHeaderX0213();
        for (int k = 1; k <= 13; ++k) {
            if (k == 1) continue;
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLinesX0213(1, k, t, option));
            }
        }
        
        println();
        println("# JIS X 0213 - 第3水準漢字");
        println();
        printHeaderX0213();
        for (int k = 14; k <= 94; ++k) {
            // 第1水準。
            if (16 <= k && k <= 46) continue;
            // 第2水準。
            if (48 <= k && k <= 83) continue;
            
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLinesX0213(1, k, t, option));
            }
        }
        
        println();
        println("# JIS X 0213 - 第4水準漢字");
        println();
        printHeaderX0213();
        for (int k = 1; k <= 94; ++k) {
            // 補助漢字。
            if (k == 2) continue;
            if ( 6 <= k && k <=  7) continue;
            if ( 9 <= k && k <= 11) continue;
            if (16 <= k && k <= 77) continue;
            
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLinesX0213(2, k, t, option));
            }
        }
    }

    void printHeader() {
        println("         JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
    }

    void printHeaderX0208() {
        println("   区-点 JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
    }

    void printHeaderX0213() {
        println("面-区-点 JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
    }

    void printSeparator() {
        println("-------- ---- ------ ---- ---- -------- -------- ------------ ----");
    }

    static final byte[] EMPTY_BYTES = {};

    void println() {
        printLine(EMPTY_BYTES);
    }

    void println(String s) {
        printLine(s == null ? EMPTY_BYTES : s.getBytes(encoding));
    }

    void println(String format, Object... args) {
        println(String.format(format, args));
    }

    void printLine(byte[] bytes) {
        try {
            System.out.write(bytes);
            System.out.write((byte) '\n');
            System.out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void printLines(Iterable<byte[]> c) {
        for (byte[] bytes : c) {
            printLine(bytes);
        }
    }

    List<byte[]> encodedLines(int c, String option) {
        byte[] bs = {(byte) c};
        String ss = toString(bs, SHIFT_JIS);
        
        ByteArrayBuilder bab = new ByteArrayBuilder();
        bab.append("      %02X ", c);
        if (ss.equals("\uFFFD")) {
            bab.append("-    -      -    ");
        } else {
            bab.append("%02X   ", c & 0x7F);
            if (c < 0x80) {
                bab.append("%02X     ", c);
            } else {
                bab.append("8E%02X   ", c);
            }
            if (c == 0x5C || c == 0x7E) {
                bab.append("-    ");
            } else {
                bab.append("%02X   ", c);
            }
        }
        bab.append("%02X   ", c);
        bab.append("U+%04X   ", ss.codePointAt(0));
        bab.append("%-8s ", toHexString(ss.toCharArray()));
        bab.append("%-12s ", toHexString(ss.getBytes(UTF_8)));
        if (!ss.equals("\uFFFD")) {
            if (0x20 <= c && c != 0x7F) {
                bab.append("[%s]", ss);
            }
            byte[] bs2 = ss.getBytes(SHIFT_JIS);
            if (!Arrays.equals(bs, bs2)) {
                bab.append(" => %s", toHexString(bs2));
            }
        }
        byte[] line = bab.toByteArray();
        if (c != 0x5C && c != 0x7E) {
            return Collections.singletonList(line);
        }
        
        ss = (c == 0x5C) ? "\u00A5" : "\u203E";
        bab = new ByteArrayBuilder();
        bab.append("      %02X ", c);
        bab.append("%02X   ", c & 0x7F);
        bab.append("-      ");
        bab.append("%02X   ", c);
        bab.append("-    ");
        bab.append("U+%04X   ", ss.codePointAt(0));
        bab.append("%-8s ", toHexString(ss.toCharArray()));
        bab.append("%-12s ", toHexString(ss.getBytes(UTF_8)));
        if (!ss.equals("\uFFFD")) {
                bab.append("[%s]", ss);
            byte[] bs2 = ss.getBytes(SHIFT_JIS);
            if (!Arrays.equals(bs, bs2)) {
                bab.append(" => %s", toHexString(bs2));
            } else {
                bab.append(" -> %s (SJIS)", toHexString(bs2));
            }
            byte[] bw2 = ss.getBytes(WINDOWS_31J);
            if (!Arrays.equals(bs, bw2)) {
                bab.append(" -> %s (W31J)", toHexString(bw2));
            }
        }
        return Arrays.asList(line, bab.toByteArray());
    }

    List<byte[]> encodedLines(int k, int t, String option) {
        int jis = (94 < k) ? 0 : kutenToJis(k, t);
        int euc = (94 < k) ? 0 : kutenToEuc(k, t);
        int sjis = kutenToSjis(k, t);
        byte[] bs = bytes(sjis, 2);
        String ss = toString(bs, SHIFT_JIS);
        String sw = toString(bs, WINDOWS_31J);
        
        List<byte[]> lines = new ArrayList<>();
        ByteArrayBuilder bab = new ByteArrayBuilder();
        
        boolean showSjis = (!ss.startsWith("\uFFFD") && !ss.equals(sw));
        if (showSjis) {
            bab.append("   %02d-%02d ", k, t);
            bab.append("%04X ", jis);
            bab.append("%04X   ", euc);
            bab.append("%04X ", sjis);
            bab.append("-    ");
            bab.append("U+%04X   ", ss.codePointAt(0));
            bab.append("%-8s ", toHexString(ss.toCharArray()));
            bab.append("%-12s ", toHexString(ss.getBytes(UTF_8)));
            if ("-sjis".equals(option) || "-w31j".equals(option)) {
                bab.append("[").append(bs).append("]");
            } else {
                bab.append("[%s]", ss);
            }
            byte[] bs2 = ss.getBytes(SHIFT_JIS);
            if (!Arrays.equals(bs, bs2)) {
                bab.append(" => %s", toHexString(bs2));
            } else {
                byte[] bw2 = ss.getBytes(WINDOWS_31J);
                if (!Arrays.equals(bs, bw2) || showSjis) {
                    bab.append(" -> %s (W31J)", toHexString(bw2));
                }
            }
            lines.add(bab.toByteArray());
            bab = new ByteArrayBuilder();
        }
        
        if (k < 100) {
            bab.append("   %02d-%02d ", k, t);
        } else {
            bab.append("  %3d-%02d ", k, t);
        }
        if (showSjis || ss.startsWith("\uFFFD")) {
            bab.append("-    -      -    ");
        } else {
            bab.append("%04X ", jis);
            bab.append("%04X   ", euc);
            bab.append("%04X ", sjis);
        }
        bab.append("%04X ", sjis);
        bab.append("U+%04X   ", sw.codePointAt(0));
        bab.append("%-8s ", toHexString(sw.toCharArray()));
        bab.append("%-12s ", toHexString(sw.getBytes(UTF_8)));
        if (!sw.startsWith("\uFFFD")) {
            if ("-sjis".equals(option) || "-w31j".equals(option)) {
                bab.append("[").append(bs).append("]");
            } else {
                bab.append("[%s]", sw);
            }
            byte[] bw2 = sw.getBytes(WINDOWS_31J);
            if (!Arrays.equals(bs, bw2)) {
                bab.append(" => %s", toHexString(bw2));
            } else {
                byte[] bs2 = sw.getBytes(SHIFT_JISX0213);
                if (!Arrays.equals(bs, bs2) || showSjis) {
                    bab.append(" -> %s (SJIS0213)", toHexString(bs2));
                }
            }
        }
        lines.add(bab.toByteArray());
        
        return lines;
    }

    List<byte[]> encodedLinesX0213(int m, int k, int t, String option) {
        // 補助漢字。
        if (m == 2 && (k == 2
                || ( 6 <= k && k <=  7)
                || ( 9 <= k && k <= 11)
                || (16 <= k && k <= 77))) {
            return Collections.emptyList();
        }
        
        int jis = kutenToJis(k, t);
        int euc = kutenToEuc(m, k, t);
        int sjis = kutenToSjis(m, k, t);
        byte[] bs = bytes(sjis, 2);
        String sx = toString(bs, SHIFT_JISX0213);
        String ss = toString(bs, SHIFT_JIS);
        String sw = toString(bs, WINDOWS_31J);
        
        if (sx.charAt(0) == '\uFFFD' || sx.equals(ss)) {
            return Collections.emptyList();
        }
        
        ByteArrayBuilder bab = new ByteArrayBuilder();
        bab.append("%2d-%02d-%02d ", m, k, t);
        bab.append("%04X ", jis);
        if (euc <= 0xFFFF) {
            bab.append("%04X   ", euc);
        } else {
            bab.append("%06X ", euc);
        }
        bab.append("%04X ", sjis);
        if (!sw.equals(sx)) {
            bab.append("-    ");
        } else {
            bab.append("%04X ", sjis);
        }
        if (sx.codePointCount(0, sx.length()) > 1) {
            bab.append("-        ");
        } else {
            int cpx = sx.codePointAt(0);
            if (cpx <= 0xFFFF) {
                bab.append("U+%04X   ", cpx);
            } else {
                bab.append("U+%06X ", cpx);
            }
        }
        bab.append("%-8s ", toHexString(sx.toCharArray()));
        bab.append("%-12s ", toHexString(sx.getBytes(UTF_8)));
        bab.append("[%s]", sx);
        byte[] bx2 = sx.getBytes(SHIFT_JISX0213);
        if (!Arrays.equals(bs, bx2)) {
            bab.append(" => %s", toHexString(bx2));
        } else {
            byte[] bw2 = sx.getBytes(WINDOWS_31J);
            if (!Arrays.equals(bs, bw2)) {
                bab.append(" -> %s (W31J)", toHexString(bw2));
            }
        }
        return Collections.singletonList(bab.toByteArray());
    }

    static int kutenToJis(int k, int t) {
        assertRange("k", k, 1, 94);
        assertRange("t", t, 1, 94);
        return word(k + 0x20, t + 0x20);
    }

    static int kutenToEuc(int k, int t) {
        assertRange("k", k, 1, 94);
        assertRange("t", t, 1, 94);
        return word(k + 0xA0, t + 0xA0);
    }

    static int kutenToEuc(int m, int k, int t) {
        assertRange("m", m, 1, 2);
        if (m == 1) {
            return kutenToEuc(k, t);
        }
        return 0x8F0000 | kutenToEuc(k, t);
    }

    static int kutenToSjis(int k, int t) {
        assertRange("k", k, 1, 120);
        assertRange("t", t, 1, 94);
        int c1 = (k + (k <= 62 ? 0x101 : 0x181)) / 2;
        int c2 = t + (k % 2 == 1 ? (t <= 63 ? 0x3F : 0x40) : 0x9E);
        return word(c1, c2);
    }

    static int kutenToSjis(int m, int k, int t) {
        assertRange("m", m, 1, 2);
        assertRange("k", k, 1, 94);
        if (m == 1) {
            return kutenToSjis(k, t);
        }
        
        if (k == 2
                || ( 6 <= k && k <=  7)
                || ( 9 <= k && k <= 11)
                || (16 <= k && k <= 77)) {
            throw new IllegalArgumentException("m = 2, k = " + k);
        }
        int c1 = (k <= 15)
            ? ((k + 0x1DF) / 2 - (k / 8) * 3)
            : ((k + 0x19B) / 2);
        int c2 = t + (k % 2 == 1 ? (t <= 63 ? 0x3F : 0x40) : 0x9E);
        return word(c1, c2);
    }

    static void assertRange(String name, int value, int min, int max) {
        if (value < min || max < value) {
            throw new IllegalArgumentException(name + " = " + value);
        }
    }

    static int kuten(int k, int t) {
        return k * 100 + t;
    }

    static int word(int hi, int lo) {
        return hi << 8 | lo;
    }

    static byte[] bytes(int word, int len) {
        byte[] bytes = new byte[len];
        for (int i = len - 1; 0 <= i; --i) {
            bytes[i] = (byte) (word & 0xFF);
            word >>= 8;
        }
        return bytes;
    }

    static String toString(byte[] bytes, Charset encoding) {
        return new String(bytes, encoding);
    }

    static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte c : bytes) {
            sb.append(String.format("%02X", c));
        }
        return sb.toString();
    }

    static String toHexString(char[] chars) {
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(String.format("%04X", (int) c));
        }
        return sb.toString();
    }

    class ByteArrayBuilder {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ByteArrayBuilder append(byte[] bytes) {
            baos.write(bytes, 0, bytes.length);
            return this;
        }

        ByteArrayBuilder append(String s) {
            return append(s.getBytes(encoding));
        }

        ByteArrayBuilder append(String format, Object... args) {
            return append(String.format(format, args));
        }

        byte[] toByteArray() {
            return baos.toByteArray();
        }

    }

}
