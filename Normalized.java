import static java.nio.charset.StandardCharsets.*;
import static java.text.Normalizer.*;
import static java.text.Normalizer.Form.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;

class Normalized {

    static final Charset SHIFT_JIS = Charset.forName("Shift_JIS");
    static final Charset WINDOWS_31J = Charset.forName("Windows-31J");
    static final Charset SHIFT_JISX0213 = Charset.forName("x-SJIS_0213");

    public static void main(String[] args) {
        new Normalized().printNormalizedLines();
    }

    void printNormalizedLines() {
        println("#");
        println("# normalized.txt");
        println("#");
        
        println();
        println("# US-ASCII");
        println();
        printHeader();
        printSeparator();
        for (int c = 0x00; c <= 0x1F; ++c) {
            printLines(normalizedLines(c));
        }
        List<String> laters = new ArrayList<>();
        printSeparator();
        for (int c = 0x20; c <= 0x7F; ++c) {
            List<String> lines = normalizedLines(c);
            println(lines.get(0));
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
            printLines(normalizedLines(c));
        }
        printSeparator();
        for (int c = 0xA0; c <= 0xDF; ++c) {
            printLines(normalizedLines(c));
        }
        printSeparator();
        for (int c = 0xE0; c <= 0xFF; ++c) {
            printLines(normalizedLines(c));
        }
        
        println();
        println("# JIS X 0208 - NFC による正規化で値が変わるもの");
        println();
        printHeaderX0208();
        printSeparator();
        printLines(normalizedLines(  2, 82));
        printSeparator();
        printLines(normalizedLines(115, 92));
        printLines(normalizedLines(116, 58));
        printLines(normalizedLines(116, 66));
        printLines(normalizedLines(117, 25));
        printLines(normalizedLines(117, 31));
        printLines(normalizedLines(117, 54));
        printLines(normalizedLines(117, 62));
        printLines(normalizedLines(117, 63));
        printLines(normalizedLines(117, 64));
        printLines(normalizedLines(117, 66));
        printLines(normalizedLines(117, 70));
        printLines(normalizedLines(117, 73));
        printLines(normalizedLines(117, 82));
        printLines(normalizedLines(118,  1));
        printLines(normalizedLines(118, 11));
        printLines(normalizedLines(118, 22));
        printLines(normalizedLines(118, 25));
        printLines(normalizedLines(118, 75));
        printLines(normalizedLines(118, 88));
        printLines(normalizedLines(118, 89));
        printLines(normalizedLines(118, 91));
        printLines(normalizedLines(119, 10));
        
        println();
        println("# JIS X 0208 - 非漢字");
        println();
        printHeaderX0208();
        for (int k = 1; k <= 12; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(normalizedLines(k, t));
            }
        }
        
        println();
        println("# NEC特殊文字");
        println();
        printHeaderX0208();
        for (int k = 13; k <= 15; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(normalizedLines(k, t));
            }
        }
        
        println();
        println("# JIS X 0208 - 第1水準漢字");
        println();
        printHeaderX0208();
        for (int k = 16; k <= 47; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(normalizedLines(k, t));
            }
        }
        
        println();
        println("# JIS X 0208 - 第2水準漢字");
        println();
        printHeaderX0208();
        for (int k = 48; k <= 88; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(normalizedLines(k, t));
            }
        }
        
        println();
        println("# NEC選定IBM拡張文字");
        println();
        printHeaderX0208();
        for (int k = 89; k <= 94; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(normalizedLines(k, t));
            }
        }
        
        println();
        println("# ユーザー外字領域");
        println();
        printHeaderX0208();
        for (int k = 95; k <= 114; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(normalizedLines(k, t));
            }
        }
        
        println();
        println("# IBM拡張文字");
        println();
        printHeaderX0208();
        for (int k = 115; k <= 120; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(normalizedLines(k, t));
            }
        }
        
        println();
        println("# JIS X 0213 - NFC のよる正規化で値が変わるもの");
        println();
        printHeaderX0213();
        printSeparator();
        printLines(normalizedLinesX0213(1, 11, 39));
        printLines(normalizedLinesX0213(1, 11, 49));
        printSeparator();
        printLines(normalizedLinesX0213(1, 14, 24));
        printLines(normalizedLinesX0213(1, 14, 41));
        printLines(normalizedLinesX0213(1, 14, 48));
        printLines(normalizedLinesX0213(1, 14, 67));
        printLines(normalizedLinesX0213(1, 14, 72));
        printLines(normalizedLinesX0213(1, 14, 78));
        printLines(normalizedLinesX0213(1, 15, 12));
        printLines(normalizedLinesX0213(1, 15, 15));
        printLines(normalizedLinesX0213(1, 15, 22));
        printLines(normalizedLinesX0213(1, 15, 55));
        printLines(normalizedLinesX0213(1, 15, 58));
        printLines(normalizedLinesX0213(1, 15, 62));
        printLines(normalizedLinesX0213(1, 47, 65));
        printLines(normalizedLinesX0213(1, 47, 66));
        printLines(normalizedLinesX0213(1, 84, 14));
        printLines(normalizedLinesX0213(1, 84, 48));
        printLines(normalizedLinesX0213(1, 84, 60));
        printLines(normalizedLinesX0213(1, 84, 62));
        printLines(normalizedLinesX0213(1, 84, 65));
        printLines(normalizedLinesX0213(1, 85,  8));
        printLines(normalizedLinesX0213(1, 85, 11));
        printLines(normalizedLinesX0213(1, 85, 35));
        printLines(normalizedLinesX0213(1, 85, 46));
        printLines(normalizedLinesX0213(1, 85, 69));
        printLines(normalizedLinesX0213(1, 86, 27));
        printLines(normalizedLinesX0213(1, 86, 41));
        printLines(normalizedLinesX0213(1, 86, 73));
        printLines(normalizedLinesX0213(1, 86, 87));
        printLines(normalizedLinesX0213(1, 87,  5));
        printLines(normalizedLinesX0213(1, 87, 53));
        printLines(normalizedLinesX0213(1, 87, 58));
        printLines(normalizedLinesX0213(1, 87, 79));
        printLines(normalizedLinesX0213(1, 89,  7));
        printLines(normalizedLinesX0213(1, 89, 19));
        printLines(normalizedLinesX0213(1, 89, 20));
        printLines(normalizedLinesX0213(1, 89, 23));
        printLines(normalizedLinesX0213(1, 89, 24));
        printLines(normalizedLinesX0213(1, 89, 25));
        printLines(normalizedLinesX0213(1, 89, 27));
        printLines(normalizedLinesX0213(1, 89, 28));
        printLines(normalizedLinesX0213(1, 89, 29));
        printLines(normalizedLinesX0213(1, 89, 31));
        printLines(normalizedLinesX0213(1, 89, 32));
        printLines(normalizedLinesX0213(1, 89, 33));
        printLines(normalizedLinesX0213(1, 89, 45));
        printLines(normalizedLinesX0213(1, 89, 49));
        printLines(normalizedLinesX0213(1, 89, 68));
        printLines(normalizedLinesX0213(1, 90, 14));
        printLines(normalizedLinesX0213(1, 90, 19));
        printLines(normalizedLinesX0213(1, 90, 26));
        printLines(normalizedLinesX0213(1, 90, 36));
        printLines(normalizedLinesX0213(1, 90, 56));
        printLines(normalizedLinesX0213(1, 91,  7));
        printLines(normalizedLinesX0213(1, 91, 47));
        printLines(normalizedLinesX0213(1, 91, 79));
        printLines(normalizedLinesX0213(1, 91, 89));
        printLines(normalizedLinesX0213(1, 92, 14));
        printLines(normalizedLinesX0213(1, 92, 15));
        printLines(normalizedLinesX0213(1, 92, 16));
        printLines(normalizedLinesX0213(1, 92, 24));
        printLines(normalizedLinesX0213(1, 92, 29));
        printLines(normalizedLinesX0213(1, 92, 57));
        printLines(normalizedLinesX0213(1, 92, 74));
        printLines(normalizedLinesX0213(1, 93, 61));
        printLines(normalizedLinesX0213(1, 93, 67));
        printLines(normalizedLinesX0213(1, 93, 86));
        printLines(normalizedLinesX0213(1, 93, 91));
        printLines(normalizedLinesX0213(1, 94,  4));
        printSeparator();
        printLines(normalizedLinesX0213(2, 80,  9));
        printLines(normalizedLinesX0213(2, 84, 48));
        printLines(normalizedLinesX0213(2, 85, 84));
        printLines(normalizedLinesX0213(2, 85, 85));
        printLines(normalizedLinesX0213(2, 87, 24));
        printLines(normalizedLinesX0213(2, 89, 73));
        
        println();
        println("# JIS X 0213 - 非漢字");
        println();
        printHeaderX0213();
        for (int k = 1; k <= 13; ++k) {
            if (k == 1) continue;
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(normalizedLinesX0213(1, k, t));
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
                printLines(normalizedLinesX0213(1, k, t));
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
                printLines(normalizedLinesX0213(2, k, t));
            }
        }
    }

    void printHeader() {
        println("         SJIS W31J UTF-16   NFC      NFD          NFKC             NFKD");
    }

    void printHeaderX0208() {
        println("   区-点 SJIS W31J UTF-16   NFC      NFD          NFKC             NFKD");
    }

    void printHeaderX0213() {
        println("面-区-点 SJIS W31J UTF-16   NFC      NFD          NFKC             NFKD");
    }

    void printSeparator() {
        println("-------- ---- ---- -------- -------- ------------ ---------------- ---------------- ----");
    }

    static final byte[] EMPTY_BYTES = {};

    void println() {
        println(EMPTY_BYTES);
    }

    void println(String s) {
        println(s == null ? EMPTY_BYTES : s.getBytes(UTF_8));
    }

    void println(String format, Object... args) {
        println(String.format(format, args));
    }

    void println(byte[] bytes) {
        try {
            System.out.write(bytes);
            System.out.write((byte) '\n');
            System.out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void printLines(Iterable<String> c) {
        for (String s : c) {
            println(s);
        }
    }

    List<String> normalizedLines(int c) {
        byte[] bs = {(byte) c};
        String ss = toString(bs, SHIFT_JIS);
        
        StringBuilder sb = new StringBuilder();
        append(sb, "      %02X ", c);
        if (ss.equals("\uFFFD")) {
            append(sb, "-    ");
        } else {
            if (c == 0x5C || c == 0x7E) {
                append(sb, "-    ");
            } else {
                append(sb, "%02X   ", c);
            }
        }
        append(sb, "%02X   ", c);
        append(sb, "%-8s ", toHexString(ss));
        if (ss.equals("\uFFFD")) {
            append(sb, "-        -            -                -                ");
        } else {
            String nfc = normalize(ss, NFC);
            append(sb, "%-8s ", toHexString(nfc));
            append(sb, "%-12s ", toHexString(normalize(ss, NFD)));
            append(sb, "%-16s ", toHexString(normalize(ss, NFKC)));
            append(sb, "%-16s ", toHexString(normalize(ss, NFKD)));
            if (0x20 <= c && c != 0x7F) {
                append(sb, "[%s]", ss);
            }
            byte[] bs2 = nfc.getBytes(SHIFT_JIS);
            if (!Arrays.equals(bs, bs2)) {
                append(sb, " => %s", toHexString(bs2));
                if (!ss.equals(nfc)) {
                    append(sb, " (NFC) [%s]", nfc);
                }
            }
        }
        String line = sb.toString();
        if (c != 0x5C && c != 0x7E) {
            return Collections.singletonList(line);
        }
        
        ss = (c == 0x5C) ? "\u00A5" : "\u203E";
        sb = new StringBuilder();
        append(sb, "      %02X ", c);
        append(sb, "%02X   ", c);
        append(sb, "-    ");
        append(sb, "%-8s ", toHexString(ss));
        if (ss.equals("\uFFFD")) {
            append(sb, "-        -            -                -                ");
        } else {
            String nfc = normalize(ss, NFC);
            append(sb, "%-8s ", toHexString(nfc));
            append(sb, "%-12s ", toHexString(normalize(ss, NFD)));
            append(sb, "%-16s ", toHexString(normalize(ss, NFKC)));
            append(sb, "%-16s ", toHexString(normalize(ss, NFKD)));
            append(sb, "[%s]", ss);
            byte[] bs2 = nfc.getBytes(SHIFT_JIS);
            if (!Arrays.equals(bs, bs2)) {
                append(sb, " => %s", toHexString(bs2));
                if (!ss.equals(nfc)) {
                    append(sb, " (NFC) [%s]", nfc);
                }
            } else {
                append(sb, " -> %s (SJIS)", toHexString(bs2));
            }
            byte[] bw2 = nfc.getBytes(WINDOWS_31J);
            if (!Arrays.equals(bs, bw2)) {
                append(sb, " -> %s (W31J)", toHexString(bw2));
            }
        }
        return Arrays.asList(line, sb.toString());
    }

    List<String> normalizedLines(int k, int t) {
        int sjis = kutenToSjis(k, t);
        byte[] bs = bytes(sjis, 2);
        String ss = toString(bs, SHIFT_JIS);
        String sw = toString(bs, WINDOWS_31J);
        
        List<String> lines = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        
        boolean showSjis = (!ss.startsWith("\uFFFD") && !ss.equals(sw));
        if (showSjis) {
            append(sb, "   %02d-%02d ", k, t);
            append(sb, "%04X ", sjis);
            append(sb, "-    ");
            append(sb, "%-8s ", toHexString(ss));
            String nfc = normalize(ss, NFC);
            append(sb, "%-8s ", toHexString(nfc));
            append(sb, "%-12s ", toHexString(normalize(ss, NFD)));
            append(sb, "%-16s ", toHexString(normalize(ss, NFKC)));
            append(sb, "%-16s ", toHexString(normalize(ss, NFKD)));
            append(sb, "[%s]", ss);
            byte[] bs2 = nfc.getBytes(SHIFT_JIS);
            if (!Arrays.equals(bs, bs2)) {
                append(sb, " => %s", toHexString(bs2));
                if (!ss.equals(nfc)) {
                    append(sb, " (NFC) [%s]", nfc);
                }
            } else {
                byte[] bw2 = nfc.getBytes(WINDOWS_31J);
                if (!Arrays.equals(bs, bw2) || showSjis) {
                    append(sb, " -> %s (W31J)", toHexString(bw2));
                }
            }
            lines.add(sb.toString());
            sb = new StringBuilder();
        }
        
        if (k < 100) {
            append(sb, "   %02d-%02d ", k, t);
        } else {
            append(sb, "  %3d-%02d ", k, t);
        }
        if (showSjis || ss.startsWith("\uFFFD")) {
            append(sb, "-    ");
        } else {
            append(sb, "%04X ", sjis);
        }
        append(sb, "%04X ", sjis);
        append(sb, "%-8s ", toHexString(sw));
        if (sw.startsWith("\uFFFD")) {
            append(sb, "-        -            -                -                ");
        } else {
            String nfc = normalize(sw, NFC);
            append(sb, "%-8s ", toHexString(nfc));
            append(sb, "%-12s ", toHexString(normalize(sw, NFD)));
            append(sb, "%-16s ", toHexString(normalize(sw, NFKC)));
            append(sb, "%-16s ", toHexString(normalize(sw, NFKD)));
            append(sb, "[%s]", sw);
            byte[] bw2 = nfc.getBytes(WINDOWS_31J);
            if (!Arrays.equals(bs, bw2)) {
                append(sb, " => %s", toHexString(bw2));
                if (!sw.equals(nfc)) {
                    append(sb, " (NFC) [%s]", nfc);
                }
            } else {
                byte[] bs2 = nfc.getBytes(SHIFT_JISX0213);
                if (!Arrays.equals(bs, bs2) || showSjis) {
                    append(sb, " -> %s (SJIS0213)", toHexString(bs2));
                }
            }
        }
        lines.add(sb.toString());
        
        return lines;
    }

    List<String> normalizedLinesX0213(int m, int k, int t) {
        // 補助漢字。
        if (m == 2 && (k == 2
                || ( 6 <= k && k <=  7)
                || ( 9 <= k && k <= 11)
                || (16 <= k && k <= 77))) {
            return Collections.emptyList();
        }
        
        int sjis = kutenToSjis(m, k, t);
        byte[] bs = bytes(sjis, 2);
        String sx = toString(bs, SHIFT_JISX0213);
        String ss = toString(bs, SHIFT_JIS);
        String sw = toString(bs, WINDOWS_31J);
        
        if (sx.charAt(0) == '\uFFFD' || sx.equals(ss)) {
            return Collections.emptyList();
        }
        
        StringBuilder sb = new StringBuilder();
        append(sb, "%2d-%02d-%02d ", m, k, t);
        append(sb, "%04X ", sjis);
        if (!sw.equals(sx)) {
            append(sb, "-    ");
        } else {
            append(sb, "%04X ", sjis);
        }
        append(sb, "%-8s ", toHexString(sx));
        String nfc = normalize(sx, NFC);
        append(sb, "%-8s ", toHexString(nfc));
        append(sb, "%-12s ", toHexString(normalize(sx, NFD)));
        append(sb, "%-16s ", toHexString(normalize(sx, NFKC)));
        append(sb, "%-16s ", toHexString(normalize(sx, NFKD)));
        append(sb, "[%s]", sx);
        byte[] bx2 = nfc.getBytes(SHIFT_JISX0213);
        if (!Arrays.equals(bs, bx2)) {
            append(sb, " => %s", toHexString(bx2));
            if (!sx.equals(nfc)) {
                append(sb, " (NFC) [%s]", nfc);
            }
        } else {
            byte[] bw2 = nfc.getBytes(WINDOWS_31J);
            if (!Arrays.equals(bs, bw2)) {
                append(sb, " -> %s (W31J)", toHexString(bw2));
            }
        }
        return Collections.singletonList(sb.toString());
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

    static String toHexString(String s) {
        return toHexString(s.toCharArray());
    }

    static void append(StringBuilder sb, String s) {
        sb.append(s);
    }

    static void append(StringBuilder sb, String format, Object... args) {
        append(sb, String.format(format, args));
    }

}
