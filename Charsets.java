import static java.nio.charset.StandardCharsets.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;

class Charsets {

    static final Charset ISO_2022_JP = Charset.forName("ISO-2022-JP");
    static final Charset EUC_JP = Charset.forName("EUC-JP");
    static final Charset SHIFT_JIS = Charset.forName("Shift_JIS");
    static final Charset WINDOWS_31J = Charset.forName("Windows-31J");

    String option;
    Charset encoding;

    public static void main(String[] args) {
        Charsets o = new Charsets();
        o.option = (args.length == 0) ? "" : args[0];
        o.encoding = optionToEncoding(o.option);
        o.printEncodingLines();
    }

    static Charset optionToEncoding(String option) {
        if ("-jis".equals(option)) {
            return ISO_2022_JP;
        } else if ("-euc".equals(option)) {
            return EUC_JP;
        } else if ("-sjis".equals(option)) {
            return SHIFT_JIS;
        } else if ("-w31j".equals(option)) {
            return WINDOWS_31J;
        } else if ("-utf8".equals(option)) {
            return UTF_8;
        } else {
            return Charset.defaultCharset();
        }
    }

    void printEncodingLines() {
        println("#");
        println(String.format("# encoding%s.txt", option));
        println("#");
        
        println();
        println("# US-ASCII");
        println();
        println("         JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
        println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
        for (int c = 0x00; c <= 0x1F; ++c) {
            printLines(encodingLines(c, option));
        }
        List<byte[]> laters = new ArrayList<>();
        println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
        for (int c = 0x20; c <= 0x7F; ++c) {
            List<byte[]> lines = encodingLines(c, option);
            printLine(lines.get(0));
            if (lines.size() > 1) {
                laters.addAll(lines.subList(1, lines.size()));
            }
        }
        
        println();
        println("# JIS X 0201");
        println();
        println("         JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
        println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
        printLines(laters);
        println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
        for (int c = 0x80; c <= 0x9F; ++c) {
            printLines(encodingLines(c, option));
        }
        println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
        for (int c = 0xA0; c <= 0xDF; ++c) {
            printLines(encodingLines(c, option));
        }
        println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
        for (int c = 0xE0; c <= 0xFF; ++c) {
            printLines(encodingLines(c, option));
        }
        
        println();
        println("# JIS X 0208 - マッピングが異なるもの");
        println();
        println("   区-点 JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
        println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
        printLines(encodingLines(01, 33, option));
        printLines(encodingLines(01, 34, option));
        printLines(encodingLines(01, 61, option));
        printLines(encodingLines(01, 81, option));
        printLines(encodingLines(01, 82, option));
        printLines(encodingLines(02, 44, option));
        
        println();
        println("# JIS X 0208 - 非漢字");
        println();
        println("   区-点 JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
        for (int k = 01; k <= 12; ++k) {
            println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
            for (int t = 01; t <= 94; ++t) {
                printLines(encodingLines(k, t, option));
            }
        }
        
        println();
        println("# NEC特殊文字");
        println();
        println("   区-点 JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
        for (int k = 13; k <= 15; ++k) {
            println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
            for (int t = 01; t <= 94; ++t) {
                printLines(encodingLines(k, t, option));
            }
        }
        
        println();
        println("# JIS X 0208 - 第1水準漢字");
        println();
        println("   区-点 JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
        for (int k = 16; k <= 47; ++k) {
            println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
            for (int t = 01; t <= 94; ++t) {
                printLines(encodingLines(k, t, option));
            }
        }
        
        println();
        println("# JIS X 0208 - 第2水準漢字");
        println();
        println("   区-点 JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
        for (int k = 48; k <= 88; ++k) {
            println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
            for (int t = 01; t <= 94; ++t) {
                printLines(encodingLines(k, t, option));
            }
        }
        
        println();
        println("# NEC選定IBM拡張文字");
        println();
        println("   区-点 JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
        for (int k = 89; k <= 94; ++k) {
            println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
            for (int t = 01; t <= 94; ++t) {
                printLines(encodingLines(k, t, option));
            }
        }
        
        println();
        println("# ユーザー外字領域");
        println();
        println("   区-点 JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
        for (int k = 95; k <= 114; ++k) {
            println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
            for (int t = 01; t <= 94; ++t) {
                printLines(encodingLines(k, t, option));
            }
        }
        
        println();
        println("# IBM拡張文字");
        println();
        println("   区-点 JIS  EUC    SJIS W31J Unicode  UTF-16   UTF-8");
        for (int k = 115; k <= 120; ++k) {
            println("-------- ---- ------ ---- ---- -------- -------- -------- ----");
            for (int t = 01; t <= 94; ++t) {
                printLines(encodingLines(k, t, option));
            }
        }
    }

    static final byte[] EMPTY_BYTES = {};

    void println() {
        printLine(EMPTY_BYTES);
    }

    void println(String s) {
        printLine(s == null ? EMPTY_BYTES : s.getBytes(encoding));
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

    List<byte[]> encodingLines(int c, String option) {
        byte[] bs = {(byte) c};
        String ss = toString(bs, SHIFT_JIS);
        
        ByteArrayBuilder bab = new ByteArrayBuilder();
        bab.append(String.format("      %02X ", c));
        if (ss.equals("\uFFFD")) {
            bab.append("-    -      -    ");
        } else {
            bab.append(String.format("%02X   ", c & 0x7F));
            if (c < 0x80) {
                bab.append(String.format("%02X     ", c));
            } else {
                bab.append(String.format("8E%02X   ", c));
            }
            if (c == 0x5C || c == 0x7E) {
                bab.append("-    ");
            } else {
                bab.append(String.format("%02X   ", c));
            }
        }
        bab.append(String.format("%02X   ", c));
        bab.append(String.format("U+%04X   ", ss.codePointAt(0)));
        bab.append(String.format("%-8s ", toHexString(ss.toCharArray())));
        bab.append(String.format("%-8s ", toHexString(ss.getBytes(UTF_8))));
        if (!ss.equals("\uFFFD")) {
            if (0x20 <= c && c != 0x7F) {
                bab.append(String.format("[%s]", ss));
            }
            byte[] bs2 = ss.getBytes(SHIFT_JIS);
            if (!Arrays.equals(bs, bs2)) {
                bab.append(String.format(" => %s", toHexString(bs2)));
            }
        }
        byte[] line = bab.toByteArray();
        if (c != 0x5C && c != 0x7E) {
            return Collections.singletonList(line);
        }
        
        ss = (c == 0x5C) ? "\u00A5" : "\u203E";
        bab = new ByteArrayBuilder();
        bab.append(String.format("      %02X ", c));
        bab.append(String.format("%02X   ", c & 0x7F));
        bab.append("-      ");
        bab.append(String.format("%02X   ", c));
        bab.append("-    ");
        bab.append(String.format("U+%04X   ", ss.codePointAt(0)));
        bab.append(String.format("%-8s ", toHexString(ss.toCharArray())));
        bab.append(String.format("%-8s ", toHexString(ss.getBytes(UTF_8))));
        if (!ss.equals("\uFFFD")) {
                bab.append(String.format("[%s]", ss));
            byte[] bs2 = ss.getBytes(SHIFT_JIS);
            if (!Arrays.equals(bs, bs2)) {
                bab.append(String.format(" => %s", toHexString(bs2)));
            }
        }
        return Arrays.asList(line, bab.toByteArray());
    }

    List<byte[]> encodingLines(int k, int t, String option) {
        int jis = kutenToJis(k, t);
        int euc = kutenToEuc(k, t);
        int sjis = kutenToSjis(k, t);
        byte[] bs = bytes(sjis, 2);
        String ss = toString(bs, SHIFT_JIS);
        String sw = toString(bs, WINDOWS_31J);
        
        List<byte[]> lines = new ArrayList<>();
        ByteArrayBuilder bab = new ByteArrayBuilder();
        
        boolean showSjis = (!ss.equals("\uFFFD") && !ss.equals(sw));
        if (showSjis) {
            bab.append(String.format("   %02d-%02d ", k, t));
            bab.append(String.format("%04X ", jis));
            bab.append(String.format("%04X   ", euc));
            bab.append(String.format("%04X ", sjis));
            bab.append("-    ");
            bab.append(String.format("U+%04X   ", ss.codePointAt(0)));
            bab.append(String.format("%-8s ", toHexString(ss.toCharArray())));
            bab.append(String.format("%-8s ", toHexString(ss.getBytes(UTF_8))));
            if ("-sjis".equals(option) || "-w31j".equals(option)) {
                bab.append("[").append(bs).append("]");
            } else {
                bab.append(String.format("[%s]", ss));
            }
            byte[] bs2 = ss.getBytes(SHIFT_JIS);
            if (!Arrays.equals(bs, bs2)) {
                bab.append(String.format(" => %s", toHexString(bs2)));
            } else {
                byte[] bw2 = ss.getBytes(WINDOWS_31J);
                if (!Arrays.equals(bs, bw2)) {
                    bab.append(String.format(" -> %s (W31J)", toHexString(bw2)));
                }
            }
            lines.add(bab.toByteArray());
            bab = new ByteArrayBuilder();
        }
        
        if (k < 100) {
            bab.append(String.format("   %02d-%02d ", k, t));
        } else {
            bab.append(String.format("  %3d-%02d ", k, t));
        }
        if (showSjis || ss.equals("\uFFFD")) {
            bab.append("-    -      -    ");
        } else {
            bab.append(String.format("%04X ", jis));
            bab.append(String.format("%04X   ", euc));
            bab.append(String.format("%04X ", sjis));
        }
        bab.append(String.format("%04X ", sjis));
        bab.append(String.format("U+%04X   ", sw.codePointAt(0)));
        bab.append(String.format("%-8s ", toHexString(sw.toCharArray())));
        bab.append(String.format("%-8s ", toHexString(sw.getBytes(UTF_8))));
        if (!sw.equals("\uFFFD")) {
            if ("-sjis".equals(option) || "-w31j".equals(option)) {
                bab.append("[").append(bs).append("]");
            } else {
                bab.append(String.format("[%s]", sw));
            }
            byte[] bw2 = sw.getBytes(WINDOWS_31J);
            if (!Arrays.equals(bs, bw2)) {
                bab.append(String.format(" => %s", toHexString(bw2)));
            } else if (showSjis) {
                byte[] bs2 = ss.getBytes(SHIFT_JIS);
                if (!Arrays.equals(bs, bs2)) {
                    bab.append(String.format(" -> %s (SJIS)", toHexString(bs2)));
                }
            }
        }
        lines.add(bab.toByteArray());
        
        return lines;
    }

    static int kutenToJis(int k, int t) {
        return word(k + 0x20, t + 0x20);
    }

    static int kutenToEuc(int k, int t) {
        return word(k + 0xA0, t + 0xA0);
    }

    static int kutenToSjis(int k, int t) {
        int c1 = (k - 1) / 2 + (k <= 62 ? 0x81 : 0xC1);
        int c2 = t + (k % 2 == 1 ? (t <= 63 ? 0x3F : 0x40) : 0x9E);
        return word(c1, c2);
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

        public ByteArrayBuilder append(byte[] bytes) {
            baos.write(bytes, 0, bytes.length);
            return this;
        }

        public ByteArrayBuilder append(String s) {
            return append(s.getBytes(encoding));
        }

        public byte[] toByteArray() {
            return baos.toByteArray();
        }

    }

}
