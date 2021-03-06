/*
 * Charsets.java
 *
 * https://github.com/glad2121/charsets
 */
import static java.nio.charset.StandardCharsets.*;
import static java.text.Normalizer.*;
import static java.text.Normalizer.Form.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.text.BreakIterator;
import java.util.*;
import java.util.regex.*;

class Charsets {

    static final Charset ISO_2022_JP = Charset.forName("ISO-2022-JP");
    static final Charset ISO_2022_JP_X = Charset.forName("ISO-2022-JP-2");
    static final Charset EUC_JP = Charset.forName("EUC-JP");
    static final Charset EUC_JP_X = Charset.forName("EUC-JP");
    static final Charset SHIFT_JIS = Charset.forName("Shift_JIS");
    static final Charset SHIFT_JIS_2004 = Charset.forName("x-SJIS_0213");
    static final Charset WINDOWS_31J = Charset.forName("Windows-31J");
    static final Charset IBM_942 = Charset.forName("x-IBM942");
    static final Charset IBM_943 = Charset.forName("x-IBM943");
    static final Charset IBM_930 = Charset.forName("x-IBM930");
    static final Charset IBM_939 = Charset.forName("x-IBM939");

    static final byte[] EMPTY_BYTES = {};
    static final byte[] BYTES_3F = {(byte) 0x3F};

    static final String[] CHAR_NAMES = {
        "NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL",
        "BS",  "HT",  "LF",  "VT",  "FF",  "CR",  "SO",  "SI",
        "DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
        "CAN", "EM",  "SUB", "ESC", "FS",  "GS",  "RS",  "US"
    };

    static final Map<String, String[]> VARIANT_MAP;
    static {
        Pattern p = Pattern.compile(" *U\\+(\\S+) +U\\+(\\S+)(?: +U\\+(\\S+))?(?: +([^#\\s]+))?.*");
        Map<String, String[]> variantMap = new HashMap<>();
        try (BufferedReader in = Files.newBufferedReader(Paths.get("variants.txt"), UTF_8)) {
            String line;
            while ((line = in.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    String key = cpToString(m.group(1));
                    String variant = cpToString(m.group(2));
                    if (m.group(3) != null) {
                        variant += cpToString(m.group(3));
                    }
                    String[] value = {variant, m.group(4)};
                    variantMap.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VARIANT_MAP = variantMap;
    }

    static final Map<String, Character> KANJI_MAP;
    static {
        Pattern p = Pattern.compile("(\\d) (\\S+)");
        Map<String, Character> kanjiMap = new HashMap<>();
        try (BufferedReader in = Files.newBufferedReader(Paths.get("kanji.txt"), UTF_8)) {
            BreakIterator bi = BreakIterator.getCharacterInstance(Locale.JAPAN);
            String line;
            while ((line = in.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    char kubun = m.group(1).charAt(0);
                    String g = m.group(2);
                    bi.setText(g);
                    for (int start = bi.first(), end = bi.next();
                            end != BreakIterator.DONE; start = end, end = bi.next()) {
                        String s = g.substring(start, end);
                        if (kubun == '3' || !kanjiMap.containsKey(s)) {
                            kanjiMap.put(s, kubun);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        KANJI_MAP = kanjiMap;
    }

    final String option;
    final Charset encoding;
    final String sep;

    public static void main(String[] args) {
        if (args.length > 0 && "-list".equals(args[0])) {
            for (Charset charset : Charset.availableCharsets().values()) {
                System.out.println(charset.name() + ": " + charset.aliases());
            }
            return;
        }
        new Charsets(args).printEncodedLines();
    }

    Charsets(String[] args) {
        this.option = (args.length == 0) ? "-utf8" : args[0];
        this.encoding = optionToEncoding(option);
        this.sep = optionToSeparator(option);
    }

    Charset optionToEncoding(String option) {
        if ("-utf8".equals(option)) {
            return UTF_8;
        } else if ("-jis".equals(option)) {
            return ISO_2022_JP_X;
        } else if ("-euc".equals(option)) {
            return EUC_JP_X;
        } else if ("-sjis".equals(option)) {
            return SHIFT_JIS_2004;
        } else if ("-w31j".equals(option)) {
            return WINDOWS_31J;
        } else {
            return UTF_8;
        }
    }

    String optionToSeparator(String option) {
        if (option.startsWith("-csv")) {
            return ",";
        } else {
            return " ";
        }
    }

    boolean csv() {
        return ",".equals(sep);
    }

    boolean csv1() {
        return "-csv1".equals(option);
    }

    boolean csv2() {
        return "-csv2".equals(option);
    }

    boolean csv3() {
        return "-csv3".equals(option);
    }

    boolean csv4() {
        return "-csv4".equals(option);
    }

    boolean corrects() {
        return !"-utf8-2".equals(option);
    }

    void printEncodedLines() {
        if (csv4()) {
            printHeaderCsv4();
        }
        println("#");
        println("# encoding%s.txt", option);
        println("#");

        if (!csv1() && !csv4()) {
            printKubunDesc();
        }

        println();
        println("# ASCII");
        printHeader();
        printSeparator();
        for (int c = 0x00; c <= 0x1F; ++c) {
            printLines(encodedLines(c));
        }
        List<byte[]> laters = new ArrayList<>();
        printSeparator();
        for (int c = 0x20; c <= 0x7F; ++c) {
            List<byte[]> lines = encodedLines(c);
            printLine(lines.get(0));
            if (lines.size() > 1) {
                laters.addAll(lines.subList(1, lines.size()));
            }
        }

        println();
        println("# JIS X 0201");
        printHeader();
        printSeparator();
        printLines(laters);
        printSeparator();
        for (int c = 0x80; c <= 0x9F; ++c) {
            printLines(encodedLines(c));
        }
        printSeparator();
        for (int c = 0xA0; c <= 0xDF; ++c) {
            printLines(encodedLines(c));
        }
        printSeparator();
        for (int c = 0xE0; c <= 0xFF; ++c) {
            printLines(encodedLines(c));
        }

        if (!csv()) {
            println();
            println("# JIS X 0208 - マッピングが異なるもの");
            printHeaderX0208();
            printSeparator();
            printLines(encodedLines(1, 29));
            printLines(encodedLines(1, 33));
            printLines(encodedLines(1, 34));
            printLines(encodedLines(1, 61));
            printLines(encodedLines(1, 81));
            printLines(encodedLines(1, 82));
            printLines(encodedLines(2, 44));
        }

        println();
        println("# JIS X 0208 - 非漢字");
        printHeaderX0208();
        for (int k = 1; k <= 12; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t));
            }
        }

        println();
        println("# NEC特殊文字");
        printHeaderX0208();
        for (int k = 13; k <= 15; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t));
            }
        }

        println();
        println("# JIS X 0208 - 第1水準漢字");
        printHeaderX0208();
        for (int k = 16; k <= 47; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t));
            }
        }

        println();
        println("# JIS X 0208 - 第2水準漢字");
        printHeaderX0208();
        for (int k = 48; k <= 88; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t));
            }
        }

        if (!csv1() && !csv2() && !csv3()) {
            println();
            println("# NEC選定IBM拡張文字");
            printHeaderX0208();
            for (int k = 89; k <= 94; ++k) {
                printSeparator();
                for (int t = 1; t <= 94; ++t) {
                    printLines(encodedLines(k, t));
                }
            }

            println();
            println("# ユーザー外字領域");
            printHeaderX0208();
            for (int k = 95; k <= 114; ++k) {
                printSeparator();
                for (int t = 1; t <= 94; ++t) {
                    printLines(encodedLines(k, t));
                }
            }
        }

        println();
        println("# IBM拡張文字");
        printHeaderX0208();
        for (int k = 115; k <= 120; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t));
            }
        }

        println();
        println("# JIS X 0213 - 非漢字");
        printHeaderX0213();
        for (int k = 1; k <= 13; ++k) {
            // JIS X 0208 で充填。
            if (k == 1) continue;

            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLinesX0213(1, k, t));
            }
        }

        println();
        println("# JIS X 0213 - 第3水準漢字");
        printHeaderX0213();
        for (int k = 14; k <= 94; ++k) {
            // 第1水準漢字で充填。
            if (16 <= k && k <= 46) continue;
            // 第2水準漢字で充填。
            if (48 <= k && k <= 83) continue;

            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLinesX0213(1, k, t));
            }
        }

        println();
        println("# JIS X 0213 - 第4水準漢字");
        printHeaderX0213();
        for (int k = 1; k <= 94; ++k) {
            // 補助漢字の領域。
            if (k == 2) continue;
            if ( 6 <= k && k <=  7) continue;
            if ( 9 <= k && k <= 11) continue;
            if (16 <= k && k <= 77) continue;

            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLinesX0213(2, k, t));
            }
        }

        println();
        println("# JIS X 0213 - 結合文字");
        printHeaderX0213();
        printSeparator();
        printLines(encodedLinesCombining('\u3099'));
        printLines(encodedLinesCombining('\u309A'));

        if (!csv() || csv4()) {
            println();
            println("# JIS X 0212 - 非漢字");
            printHeaderX0213();
            for (int k = 2; k <= 11; ++k) {
                // 第4水準漢字の領域。
                if (3 <= k && k <= 5) continue;
                if (k == 8) continue;

                printSeparator();
                for (int t = 1; t <= 94; ++t) {
                    printLines(encodedLinesX0212(2, k, t));
                }
            }

            println();
            println("# JIS X 0212 - 補助漢字");
            printHeaderX0213();
            for (int k = 16; k <= 77; ++k) {
                printSeparator();
                for (int t = 1; t <= 94; ++t) {
                    printLines(encodedLinesX0212(2, k, t));
                }
            }
        }
    }

    void printKubunDesc() {
        if (csv2()) {
            println();
            println("# コード区分:");
            println("# 0: 制御文字");
            println("# 1: ASCII");
            println("# 2: JIS X 0201");
            println("# 3: JIS X 0208");
            println("# 4: NEC特殊文字");
            println("# 5: IBM拡張文字");
            println("# 6: JIS X 0213 1面");
            println("# 7: JIS X 0213 2面");
        }
        if (csv3()) {
            println();
            println("# 文字区分:");
            println("# No 規格等         補足情報   ");
            println("# -- -------------- -----------");
            println("#  0 制御文字       制御文字   ");
            println("#  1 ASCII          BasicJ     ");
            println("#  2 JIS X 0201     互換文字   ");
            println("#  3 JIS X 0208     NEC特殊文字");
            println("#  4 JIS X 0213 1面 IBM拡張文字");
            println("#  5 JIS X 0213 2面 その他BMP  ");
            println("#  6 JIS X 0212     追加面     ");
            println("#  7 ベンダー外字   結合文字   ");
            println("#  9 未定義         結合文字列 ");
        }
        println();
        println("# 詳細区分:");
        println("# No Unicode      正規化       規格等       水準         Windows-31J        法令等        ");
        println("# -- ------------ ------------ ------------ ------------ ------------------ --------------");
        println("#  0 -            変換なし     制御文字     非漢字       制御文字           非漢字        ");
        println("#  1 基本多言語面 NFDで変換    ASCII        第1水準漢字  ASCII              常用漢字      ");
        println("#  2 追加面       NFKCで変換   JIS X 0201   第2水準漢字  JIS X 0201         常用漢字(旧字)");
        println("#  3 結合文字     半角・全角形 JIS X 0208   第3水準漢字  JIS X 0208         人名用漢字(二)");
        println("#  4 結合文字列   NFCで変換    JIS X 0213   第4水準漢字  NEC特殊文字        人名用漢字(一)");
        println("#  5 -            -            JIS X 0212   補助漢字     NEC選定IBM拡張文字 表外漢字(印標)");
        println("#  6 -            -            -            -            IBM拡張文字        表外漢字(簡慣)");
        println("#  7 Decodeのみ可 -            ベンダー外字 ベンダー外字 Encodeのみ可       その他        ");
        println("#  8 ユーザー外字 ユーザー外字 ユーザー外字 ユーザー外字 ユーザー外字       ユーザー外字  ");
        println("#  9 未定義       未定義       未定義       未定義       未定義             未定義        ");
    }

    void printHeaderCsv4() {
        println("Unicode,CT,T1,T2,T3,T4,T5,T6,UTF-16,UTF-8,VAR,ID,JIS,JIS2,EUC,2004,SJIS,W31J,I942,I943,I930,I939,備考");
    }

    void printHeader() {
        if (csv()) return;
        println();
        println("Unicode  区分   UTF-16   UTF-8        VAR          CODE JIS  JIS2    EUC     2004   SJIS  W31J  I942  I943  I930  I939  備考");
    }

    void printHeaderX0208() {
        if (csv()) return;
        println();
        println("Unicode  区分   UTF-16   UTF-8        VAR         区-点 JIS  JIS2    EUC     2004   SJIS  W31J  I942  I943  I930  I939  備考");
    }

    void printHeaderX0213() {
        if (csv()) return;
        println();
        println("Unicode  区分   UTF-16   UTF-8        VAR      面-区-点 JIS  JIS2    EUC     2004   SJIS  W31J  I942  I943  I930  I939  備考");
    }

    void printSeparator() {
        if (csv()) {
            println();
            return;
        }
        println("-------- ------ -------- ------------ -------- -------- ---- ------- ------- ------ ----- ----- ----- ----- ----- ----- ----");
    }

    void println() {
        if (csv()) {
            println(" ");
            return;
        }
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
            //System.out.write((byte) '\r');
            //System.out.write((byte) '\n');
            //System.out.flush();
            System.out.println();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void printLines(Iterable<byte[]> c) {
        for (byte[] bytes : c) {
            printLine(bytes);
        }
    }

    List<byte[]> encodedLines(int c) {
        JisX0201Info info = new JisX0201Info(c);
        // CSV は相互変換可能な文字のみ。
        if ((csv()) && info.kubun().charAt(0) >= '7') {
            return Collections.emptyList();
        }
        if (c == 0x5C) {
            return Arrays.asList(info.encodedLine(), new JisX0201Info(c, "\u00A5").encodedLine());
        }
        if (c == 0x7E) {
            return Arrays.asList(info.encodedLine(), new JisX0201Info(c, "\u203E").encodedLine());
        }
        return Collections.singletonList(info.encodedLine());
    }

    List<byte[]> encodedLines(int k, int t) {
        Windows31jInfo info = new Windows31jInfo(k, t);
        // CSV は相互変換可能な文字のみ。
        if ((csv()) && info.kubun().charAt(0) >= '7') {
            return Collections.emptyList();
        }
        if (info.showSjis) {
            return Arrays.asList(new JisX0208Info(info).encodedLine(), info.encodedLine());
        }
        return Collections.singletonList(info.encodedLine());
    }

    List<byte[]> encodedLinesX0213(int m, int k, int t) {
        // 補助漢字の領域。
        if (m == 2 && (k == 2
                || ( 6 <= k && k <=  7)
                || ( 9 <= k && k <= 11)
                || (16 <= k && k <= 77))) {
            return Collections.emptyList();
        }

        JisX0213Info info = new JisX0213Info(m, k, t);
        // CSV は Windows-31J で未定義な文字のみ。
        if ((csv()) && info.kubun().charAt(4) < '7') {
            return Collections.emptyList();
        }
        if (info.undefined() || info.s.equals(info.ss)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(info.encodedLine());
    }

    List<byte[]> encodedLinesCombining(int cp) {
        ByteArrayBuilder bab = new ByteArrayBuilder();

        String s = new String(new int[] {cp}, 0, 1);

        // Unicode
        if (csv3()) {
            bab.append("%-8s" + sep, toHexString(s));
        } else {
            bab.append("U+%04X  " + sep, cp);
        }

        // 区分。
        if (csv()) {
            if (csv2()) {
                bab.append("6" + sep);
            } else {
                bab.append("97" + sep);
            }
            if (csv1()) {
                return Collections.singletonList(bab.toByteArray());
            }
        }
        if (csv4()) {
            bab.append("3,0,9,0,9,0" + sep);
        } else {
            bab.append("%-6s" + sep, "309090");
        }

        if (!csv2() && !csv3()) {
            // UTF-16
            bab.append("%-8s" + sep, toHexString(s));
            // UTF-8
            bab.append("%-12s" + sep, toHexString(s.getBytes(UTF_8)));
            // VARIANT
            bab.append("-       " + sep);

            // 面区点。
            bab.append("       -" + sep);
            // ISO-2022-JP
            bab.append("-   %1$s-      %1$s", sep);
            // EUC-JP
            bab.append("-      %1$s-     %1$s", sep);
            // Shift_JIS
            bab.append("-    " + sep);
            // Windows-31J
            bab.append("-    %1$s", sep);
            // IBM 94X
            bab.append("-    %1$s-    %1$s", sep);

            // EBCDIC
            bab.append("-    %1$s-    %1$s", sep);
        }

        return Collections.singletonList(bab.toByteArray());
    }

    List<byte[]> encodedLinesX0212(int m, int k, int t) {
        // 第4水準漢字の領域。
        if (m != 2 || (k != 2
                && !( 6 <= k && k <=  7)
                && !( 9 <= k && k <= 11)
                && !(16 <= k && k <= 77))) {
            return Collections.emptyList();
        }

        JisX0212Info info = new JisX0212Info(m, k, t);
        // CSV は Windows-31J で未定義な文字のみ。
        if ((csv()) && (info.kubun().charAt(3) != '5' || info.kubun().charAt(4) < '7')) {
            return Collections.emptyList();
        }
        if (info.undefined()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(info.encodedLine());
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
        int c1 = (k - 1) / 2 + (k <= 62 ? 0x81 : 0xC1);
        //int c1 = (k + (k <= 62 ? 0x101 : 0x181)) / 2;
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

    static int[] sjis2004ToKuten(byte[] sjis2004) {
        int c1 = sjis2004[0] & 0xFF;
        int c2 = sjis2004[1] & 0xFF;
        int word = word(c1, c2);

        int m = c1 < 0xF0 ? 1 : 2;
        int k = (c1 - (c1 < 0xE0 ? 0x80 : (c1 < 0xF0 ? 0xC0 :
                (word < 0xF09F ? 0xEF :
                (word < 0xF140 ? 0xEC :
                (word < 0xF29F ? 0xEF :
                (word < 0xF49F ? 0xEC : 0xCD))))))) * 2
                - (c2 < 0x9F ? 1 : 0);
        int t = c2 - (c2 < 0x80 ? 0x3F : (c2 < 0x9F ? 0x40 : 0x9E));
        return new int[] {m, k, t};
    }

    static int sjis2004ToEuc(byte[] sjis2004) {
        int[] kuten = sjis2004ToKuten(sjis2004);
        return kutenToEuc(kuten[0], kuten[1], kuten[2]);
    }

    static void assertRange(String name, int value, int min, int max) {
        if (value < min || max < value) {
            throw new IllegalArgumentException(name + ": " + value);
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

    static boolean contains(byte[] bytes, int value) {
        for (byte x : bytes) {
            if (x == value) {
                return true;
            }
        }
        return false;
    }

    static String decode(byte[] bytes, Charset encoding) {
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

    static String cpToString(String cp) {
        return new StringBuilder().appendCodePoint(Integer.parseInt(cp, 16)).toString();
    }

    static String jisToHexString(byte[] jis) {
        String s = toHexString(jis);
        if (s.startsWith("1B284A")) {
            return "1-" + s.substring(6, 8);
        } else if (s.startsWith("1B2849")) {
            return "2-" + s.substring(6, 8);
        } else if (s.startsWith("1B2442")) {
            return "4-" + s.substring(6, 10);
        } else if (s.startsWith("1B242844")) {
            return "5-" + s.substring(8, 12);
        } else {
            return "0-" + s;
        }
    }

    static boolean isEbcdicKanji(byte[] ebcdic) {
        int length = ebcdic.length;
        return length == 4 && ebcdic[0] == 0x0E && ebcdic[length - 1] == 0x0F;
    }

    static String ebcdicToHexString(byte[] ebcdic) {
        if (isEbcdicKanji(ebcdic)) {
            return toHexString(Arrays.copyOfRange(ebcdic, 1, ebcdic.length - 1));
        }
        return toHexString(ebcdic);
    }

    static boolean isHalfwidthAndFullwidthForms(int c) {
        return (0xFF00 <= c) && (c <= 0xFFEF);
    }

    static String getCharName(int c) {
        if (c < 0x20) {
            return CHAR_NAMES[c];
        } else if (c == 0x7F) {
            return "DEL";
        } else {
            return Character.getName(c);
        }
    }

    class CodeInfo {

        String s;
        int cp;

        String nfc;
        String nfkc;
        String nfd;
        String[] variant;

        int jis;
        int euc;
        int sjis;

        byte[] b;
        byte[] bj2;
        byte[] be2;
        byte[] bs2;
        byte[] bx2;
        byte[] bw2;

        byte[] i942;
        byte[] i943;

        byte[] i930;
        byte[] i939;

        void initUnicode(String s) {
            // Java 8 の変更点を吸収。
            if (corrects() && s.startsWith("\uFFFD")) {
                s = "\uFFFD";
            }
            this.s = s;
            this.cp = (s.codePointCount(0, s.length()) != 1) ? -1 : s.codePointAt(0);
            this.nfc  = normalize(s, NFC);
            this.nfkc = normalize(s, NFKC);
            this.nfd  = normalize(s, NFD);
            this.variant = VARIANT_MAP.get(s);
            this.bj2 = s.getBytes(ISO_2022_JP_X);
            this.be2 = s.getBytes(EUC_JP_X);
            this.bs2 = s.getBytes(SHIFT_JIS);
            this.bx2 = s.getBytes(SHIFT_JIS_2004);
            this.bw2 = s.getBytes(WINDOWS_31J);
            this.i942 = s.getBytes(IBM_942);
            this.i943 = s.getBytes(IBM_943);
            this.i930 = s.getBytes(IBM_930);
            this.i939 = s.getBytes(IBM_939);
        }

        boolean undefined() {
            return s.contains("\uFFFD");
        }

        boolean encodableToJis2() {
            if (s.equals("?") || s.equals("？")) {
                return true;
            }
            String hex = toHexString(bj2);
            return (!hex.contains("3F") || hex.startsWith("1B")) && !hex.contains("2129");
        }

        boolean decodableFromJis2() {
            return decode(bj2, ISO_2022_JP_X).equals(s);
        }

        boolean encodableToEuc() {
            return s.equals("?") || !contains(be2, 0x3F);
        }

        boolean decodableFromEuc() {
            return decode(be2, EUC_JP_X).equals(s);
        }

        boolean encodableToSjis() {
            return Arrays.equals(bs2, b);
        }

        boolean decodableFromSjis() {
            return decode(bs2, SHIFT_JIS_2004).equals(s);
        }

        boolean encodableToSjis2004() {
            return Arrays.equals(bx2, b);
        }

        boolean decodableFromSjis2004() {
            return decode(bx2, SHIFT_JIS_2004).equals(s);
        }

        boolean encodableToW31j() {
            return Arrays.equals(bw2, b);
        }

        boolean decodableFromW31j() {
            return decode(bw2, WINDOWS_31J).equals(s);
        }

        boolean encodableToI942() {
            return s.equals("?") || !contains(i942, 0x3F);
        }

        boolean decodableFromI942() {
            return decode(i942, IBM_942).equals(s);
        }

        boolean encodableToI943() {
            return s.equals("?") || !contains(i943, 0x3F);
        }

        boolean decodableFromI943() {
            return decode(i943, IBM_943).equals(s);
        }

        boolean encodableToI930() {
            return s.equals("?") || !contains(i930, 0x6F) || isEbcdicKanji(i930);
        }

        boolean decodableFromI930() {
            return decode(i930, IBM_930).equals(s);
        }

        boolean encodableToI939() {
            return s.equals("?") || !contains(i939, 0x6F) || isEbcdicKanji(i939);
        }

        boolean decodableFromI939() {
            return decode(i939, IBM_939).equals(s);
        }

        String utf16() {
            return toHexString(s);
        }

        String utf8() {
            return toHexString(s.getBytes(UTF_8));
        }

        String i930() {
            return ebcdicToHexString(i930);
        }

        String i939() {
            return ebcdicToHexString(i939);
        }

        void appendVariant(ByteArrayBuilder bab) {
            if (variant != null) {
                bab.append("%-8s" + sep, toHexString(variant[0]));
            } else if (!nfc.equals(s)) {
                bab.append("%-8s" + sep, toHexString(nfc));
            } else if (!nfkc.equals(s)) {
                if (nfkc.length() <= 2) {
                    bab.append("%-8s" + sep, toHexString(nfkc));
                } else {
                    bab.append("%04X... " + sep, (int) nfkc.charAt(0));
                }
            } else if (!nfd.equals(s)) {
                if (nfd.length() <= 2) {
                    bab.append("%-8s" + sep, toHexString(nfd));
                } else {
                    bab.append("%04X... " + sep, (int) nfd.charAt(0));
                }
            } else {
                bab.append("-       " + sep);
            }
        }

        void appendIbm94x(ByteArrayBuilder bab) {
            if (undefined()) {
                bab.append("-    %1$s-    %1$s", sep);
            } else {
                // IBM 942
                if (!encodableToI942()) {
                    bab.append("-    " + sep);
                } else if (!decodableFromI942()) {
                    bab.append(">%-4s" + sep, toHexString(i942));
                } else {
                    bab.append("%-4s " + sep, toHexString(i942));
                }
                // IBM 943
                if (!encodableToI943()) {
                    bab.append("-    " + sep);
                } else if (!decodableFromI943()) {
                    bab.append(">%-4s" + sep, toHexString(i943));
                } else {
                    bab.append("%-4s " + sep, toHexString(i943));
                }
            }
        }

        void appendIbm93x(ByteArrayBuilder bab) {
            if (undefined()) {
                bab.append("-    %1$s-    %1$s", sep);
            } else {
                // IBM 930
                if (!encodableToI930()) {
                    bab.append("-    " + sep);
                } else if (!decodableFromI930()) {
                    bab.append(">%-4s" + sep, i930());
                } else {
                    bab.append("%-4s " + sep, i930());
                }
                // IBM 939
                if (!encodableToI939()) {
                    bab.append("-    " + sep);
                } else if (!decodableFromI939()) {
                    bab.append(">%-4s" + sep, i939());
                } else {
                    bab.append("%-4s " + sep, i939());
                }
            }
        }

        char kubunUnicode() {
            if (cp < 0) {
                // 結合文字列。
                return '4';
            } else if (0x0300 <= cp && cp <= 0x036F) {
                // 結合文字。
                return '3';
            } else if (0x10000 <= cp) {
                // 追加面。
                return '2';
            } else {
                // 基本多言語面。
                return '1';
            }
        }

        char kubunNormalization() {
            if (!s.equals(nfc)) {
                // NFC で変換 (非正規形)。
                return '4';
            } else if (!s.equals(nfkc)) {
                // NFKC で変換 (互換文字)。
                if (isHalfwidthAndFullwidthForms(s.charAt(0))) {
                    return '3';
                } else {
                    return '2';
                }
            } else if (!s.equals(nfd)) {
                // NFD で変換 (合成済み)。
                return '1';
            } else {
                // 変換なし。
                return '0';
            }
        }

        char kubunLevel() {
            if (decodableFromSjis2004()) {
                String sjis2004 = toHexString(bx2);
                if (sjis2004.compareTo("879F") < 0) {
                    // 非漢字。
                    return '0';
                } else if (sjis2004.compareTo("889F") < 0) {
                    // 第3水準漢字。
                    return '3';
                } else if (sjis2004.compareTo("9873") < 0) {
                    // 第1水準漢字。
                    return '1';
                } else if (sjis2004.compareTo("989F") < 0) {
                    // 第3水準漢字。
                    return '3';
                } else if (sjis2004.compareTo("EAA5") < 0) {
                    // 第2水準漢字。
                    return '2';
                } else if (sjis2004.compareTo("F040") < 0) {
                    // 第3水準漢字。
                    return '3';
                } else {
                    // 第4水準漢字。
                    return '4';
                }
            } else if (decodableFromEuc()) {
                // 補助漢字。
                return '5';
            } else {
                // ベンダー外字。
                return '7';
            }
        }

        char kubunW31j() {
            if (decodableFromW31j()) {
                String w31j = toHexString(bw2);
                if (w31j.compareTo("8740") < 0) {
                    // JIS S 0208 (非漢字)
                    return '3';
                } else if (w31j.compareTo("889F") < 0) {
                    // NEC特殊文字。
                    return '4';
                } else if (w31j.compareTo("ED40") < 0) {
                    // JIS S 0208 (第1水準・第2水準漢字)
                    return '3';
                } else if (w31j.compareTo("F040") < 0) {
                    // NEC選定IBM拡張文字。
                    return '5';
                } else if (w31j.compareTo("FA40") < 0) {
                    // ユーザー外字。
                    return '8';
                } else {
                    // IBM拡張文字。
                    return '6';
                }
            } else if (!contains(bw2, 0x3F)) {
                // エンコードのみ可。
                return '7';
            } else {
                // 未定義。
                return '9';
            }
        }

        char kubunKanji() {
            Character c = KANJI_MAP.get(s);
            if (c != null) {
                // 常用漢字、人名用漢字、表外漢字。
                return c;
            } else if (0x3400 <= cp && cp <= 0x9FFF) {
                // CJK統合漢字。
                return '7';
            } else if (0xF900 <= cp && cp <= 0xFAFF) {
                // CJK互換漢字。
                return '7';
            } else if (0x20000 <= cp && cp <= 0x2FA1F) {
                // CJK統合漢字拡張、CJK互換漢字補助。
                return '7';
            } else {
                // 非漢字。
                return '0';
            }
        }

    }

    class JisX0201Info extends CodeInfo {

        int c;

        // ASCII
        // JIS X 0201 (片仮名)
        JisX0201Info(int c) {
            this.c = c;
            this.jis = c & 0x7F;
            this.euc = (c < 0x80) ? c : (0x8E00 | c);
            this.sjis = (c == 0x5C || c == 0x7E) ? -1 : c;
            this.b = new byte[] {(byte) c};
            initUnicode(decode(b, SHIFT_JIS));
        }

        // JIS X 0201 (ラテン文字)
        JisX0201Info(int c, String s) {
            this.c = c;
            this.jis = c & 0x7F;
            this.euc = -1;
            this.sjis = c;
            this.b = new byte[] {(byte) c};
            initUnicode(s);
        }

        byte[] encodedLine() {
            ByteArrayBuilder bab = new ByteArrayBuilder();

            // Unicode
            if (csv3()) {
                bab.append("%-8s" + sep, utf16());
            } else if (cp < 0) {
                bab.append("-       " + sep);
            } else {
                bab.append("U+%04X  " + sep, cp);
            }

            // 区分。
            if (csv()) {
                if (csv2()) {
                    if (undefined()) {
                        // 未定義。
                        bab.append("9" + sep);
                    } else if (cp < 0x20 || cp == 0x7F) {
                        // 制御文字。
                        bab.append("0" + sep);
                    } else if (cp < 0x80) {
                        // US-ASCII
                        bab.append("1" + sep);
                    } else {
                        // JIS X 0201
                        bab.append("2" + sep);
                    }
                } else {
                    if (undefined()) {
                        // 未定義。
                        bab.append("99" + sep);
                    } else if (cp < 0x20 || cp == 0x7F) {
                        // 制御文字。
                        bab.append("00" + sep);
                    } else if (cp < 0x80) {
                        // US-ASCII
                        bab.append("11" + sep);
                    } else if (cp < 0xFF00){
                        // JIS X 0201 (ラテン文字)
                        bab.append("21" + sep);
                    } else {
                        // JIS X 0201 (片仮名)
                        bab.append("22" + sep);
                    }
                }
                if (csv1()) {
                    if (c < 0x20 || c == 0x7F) {
                        bab.append(getCharName(c));
                    } else {
                        bab.append("[%s]", s);
                    }
                    return bab.toByteArray();
                }
            }
            if (csv4()) {
                bab.append(String.join(",", kubun().split(" *")) + sep);
            } else {
                bab.append("%-6s" + sep, kubun());
            }

            if (!csv2() && !csv3()) {
                // UTF-16
                bab.append("%-8s" + sep, utf16());
                // UTF-8
                bab.append("%-12s" + sep, utf8());
                // VARIANT
                appendVariant(bab);

                // CODE
                bab.append("      %02X" + sep, c);
                if (undefined()) {
                    bab.append("-   %1$s-     %1$s-     %1$s-     %1$s-    %1$s", sep);
                    // Windows-31J
                    bab.append("%02X   " + sep, c);
                } else {
                    // ISO-2022-JP
                    if (0x80 <= c) {
                        bab.append("-   " + sep);
                    } else {
                        bab.append("%02X  " + sep, jis);
                    }
                    if (!encodableToJis2()) {
                        bab.append("-      " + sep);
                    } else if (!decodableFromJis2()) {
                        bab.append(">%-6s" + sep, jisToHexString(bj2));
                    } else {
                        bab.append("%-6s " + sep, jisToHexString(bj2));
                    }
                    // EUC-JP
                    if (!encodableToEuc()) {
                        bab.append("-      " + sep);
                    } else if (!decodableFromEuc()) {
                        bab.append(">%-6s" + sep, toHexString(be2));
                    } else {
                        bab.append("%-6s " + sep, toHexString(be2));
                    }
                    if (euc < 0) {
                        bab.append("-     " + sep);
                    } else if (euc < 0x80) {
                        bab.append("%02X    " + sep, euc);
                    } else {
                        bab.append("%04X  " + sep, euc);
                    }
                    // Shift_JIS
                    if (!encodableToSjis()) {
                        bab.append("-    " + sep);
                    } else if (!decodableFromSjis()) {
                        bab.append(">%-4s" + sep, toHexString(bs2));
                    } else if (sjis < 0) {
                        bab.append(":%-4s" + sep, toHexString(bs2));
                    } else {
                        bab.append("%02X   " + sep, sjis);
                    }
                    // Windows-31J
                    if (!encodableToW31j()) {
                        bab.append("-    " + sep);
                    } else if (!decodableFromW31j()) {
                        bab.append(">%-4s" + sep, toHexString(bw2));
                    } else {
                        bab.append("%-4s " + sep, toHexString(bw2));
                    }
                }

                appendIbm94x(bab);

                // EBCDIC
                appendIbm93x(bab);
            }

            // 備考。
            if (!undefined()) {
                if (c < 0x20 || c == 0x7F) {
                    bab.append(getCharName(c));
                } else {
                    bab.append("[%s]", s);
                }
                if (encodableToSjis()) {
                    if (!nfc.equals(s)) {
                        bab.append(" -> [%s] (NFC)", nfc);
                    } else if (!nfkc.equals(s)) {
                        if ("\u3099".equals(nfkc) || "\u309A".equals(nfkc)) {
                            bab.append(" -> (NFKC)");
                        } else {
                            bab.append(" -> [%s] (NFKC)", nfkc);
                        }
                    }
                    if (variant != null
                            && !variant[0].startsWith(nfc) && !variant[0].equals(nfkc)) {
                        bab.append(" -> [%s]", variant[0]);
                        if (variant[1] != null) {
                            bab.append(" (%s)", variant[1]);
                        }
                    }
                    if (!decodableFromSjis2004()) {
                        bab.append(" -> %s (SJIS2004)", toHexString(bx2));
                    }
                }
            }
            return bab.toByteArray();
        }

        String kubun() {
            if (undefined()) {
                // 未定義文字。
                return "999999";
            }
            StringBuilder sb = new StringBuilder();
            // 基本多言語面。
            sb.append('1');
            sb.append(kubunNormalization());
            if (s.equals("\u00A5") || s.equals("\u203E")) {
                // JIS X 0201 (ラテン文字)
                sb.append("207");
            } else if (c < 0x20 || c == 0x7F) {
                // 制御文字。
                sb.append("000");
            } else if (c < 0x80) {
                // US-ASCII
                sb.append("101");
            } else {
                // JIS X 0201 (半角カナ)
                sb.append("202");
            }
            // 非漢字。
            sb.append('0');
            return sb.toString();
        }

    }

    class JisX0208Info extends CodeInfo {

        int k;
        int t;

        JisX0208Info(Windows31jInfo w31j) {
            this.k = w31j.k;
            this.t = w31j.t;
            this.jis = w31j.jis;
            this.euc = w31j.euc;
            this.sjis = w31j.sjis;
            this.b = w31j.b;
            initUnicode(decode(b, SHIFT_JIS));
        }

        byte[] encodedLine() {
            ByteArrayBuilder bab = new ByteArrayBuilder();

            // Unicode
            if (csv3()) {
                bab.append("%-8s" + sep, utf16());
            } else if (cp < 0) {
                bab.append("-       " + sep);
            } else {
                bab.append("U+%04X  " + sep, cp);
            }

            // 区分。
            if (csv()) {
                if (csv2()) {
                    if (undefined()) {
                        // 未定義。
                        bab.append("9" + sep);
                    } else {
                        // JIS X 0208
                        bab.append("3" + sep);
                    }
                } else {
                    if (undefined()) {
                        // 未定義。
                        bab.append("99" + sep);
                    } else if (cp < 0xFF00) {
                        // JIS X 0208 (BasicJ)
                        bab.append("31" + sep);
                    } else {
                        // JIS X 0208 (CommonJ)
                        bab.append("32" + sep);
                    }
                }
                if (csv1()) {
                    bab.append("[%s]", s);
                    return bab.toByteArray();
                }
            }
            if (csv4()) {
                bab.append(String.join(",", kubun().split(" *")) + sep);
            } else {
                bab.append("%-6s" + sep, kubun());
            }

            if (!csv2() && !csv3()) {
                // UTF-16
                bab.append("%-8s" + sep, utf16());
                // UTF-8
                bab.append("%-12s" + sep, utf8());
                // VARIANT
                appendVariant(bab);

                // 区点。
                bab.append("   %02d-%02d" + sep, k, t);
                // ISO-2022-JP
                bab.append("%04X" + sep, jis);
                if (!encodableToJis2()) {
                    bab.append("-      " + sep);
                } else if (!decodableFromJis2()) {
                    bab.append(">%-6s" + sep, jisToHexString(bj2));
                } else {
                    bab.append("%-6s " + sep, jisToHexString(bj2));
                }
                // EUC-JP
                if (!encodableToEuc()) {
                    bab.append("-      " + sep);
                } else if (!decodableFromEuc()) {
                    bab.append(">%-6s" + sep, toHexString(be2));
                } else {
                    bab.append("%-6s " + sep, toHexString(be2));
                }
                bab.append("%04X  " + sep, euc);
                // Shift_JIS
                bab.append("%04X " + sep, sjis);
                // Windows-31J
                if (contains(bw2, 0x3F)) {
                    bab.append("-    " + sep);
                } else {
                    bab.append(">%-4s" + sep, toHexString(bw2));
                }

                appendIbm94x(bab);

                // EBCDIC
                appendIbm93x(bab);
            }

            // 備考。
            if (!undefined()) {
                if ("-sjis".equals(option) || "-w31j".equals(option)) {
                    bab.append("[").append(b).append("]");
                } else {
                    bab.append("[%s]", s);
                }
                if (encodableToSjis()) {
                    if (!nfc.equals(s)) {
                        bab.append(" -> [%s] (NFC)", nfc);
                    } else if (!nfkc.equals(s)) {
                        bab.append(" -> [%s] (NFKC)", nfkc);
                    }
                    if (variant != null
                            && !variant[0].startsWith(nfc) && !variant[0].equals(nfkc)) {
                        bab.append(" -> [%s]", variant[0]);
                        if (variant[1] != null) {
                            bab.append(" (%s)", variant[1]);
                        }
                    }
                }
            }
            return bab.toByteArray();
        }

        String kubun() {
            if (undefined()) {
                // 未定義文字。
                return "999999";
            }
            StringBuilder sb = new StringBuilder();
            if (!encodableToSjis()) {
                // デコードのみ可。
                sb.append('7');
            } else {
                // 基本多言語面。
                sb.append('1');
            }
            sb.append(kubunNormalization());
            if (k < 16) {
                // 非漢字。
                if (contains(bw2, 0x3F)) {
                    // Windows-31J 未定義。
                    sb.append("309");
                } else {
                    // Windows-31J エンコードのみ可。
                    sb.append("307");
                }
            } else if (k < 48) {
                // 第1水準漢字。
                sb.append("313");
            } else {
                // 第2水準漢字。
                sb.append("323");
            }
            sb.append(kubunKanji());
            return sb.toString();
        }

    }

    class Windows31jInfo extends CodeInfo {

        int k;
        int t;

        String ss;
        boolean showSjis;

        Windows31jInfo(int k, int t) {
            this.k = k;
            this.t = t;
            this.jis = (94 < k) ? -1 : kutenToJis(k, t);
            this.euc = (94 < k) ? -1 : kutenToEuc(k, t);
            this.sjis = kutenToSjis(k, t);
            this.b = bytes(sjis, 2);
            this.ss = decode(b, SHIFT_JIS);
            initUnicode(decode(b, WINDOWS_31J));
            this.showSjis = showSjis();
        }

        boolean showSjis() {
            return !ss.contains("\uFFFD") && !ss.equals(s);
        }

        byte[] encodedLine() {
            ByteArrayBuilder bab = new ByteArrayBuilder();

            // Unicode
            if (csv3()) {
                bab.append("%-8s" + sep, utf16());
            } else if (cp < 0) {
                bab.append("-       " + sep);
            } else {
                bab.append("U+%04X  " + sep, cp);
            }

            // 区分。
            String kubun = kubun();
            if (csv()) {
                if (csv2()) {
                    if (undefined() || "78".indexOf(kubun.charAt(0)) != -1) {
                        // 未定義。
                        bab.append("9" + sep);
                    } else if (k < 13) {
                        // 非漢字。
                        bab.append("3" + sep);
                    } else if (k < 16) {
                        // NEC特殊文字。
                        bab.append("4" + sep);
                    } else if (k < 89) {
                        // 第1・2水準漢字。
                        bab.append("3" + sep);
                    } else {
                        // IBM拡張漢字。
                        bab.append("5" + sep);
                    }
                } else {
                    if (undefined() || "78".indexOf(kubun.charAt(0)) != -1) {
                        // 未定義。
                        bab.append("99" + sep);
                    } else if (k < 13) {
                        // 非漢字。
                        if (cp < 0xFF00 && kubun.charAt(2) == '3') {
                            bab.append("31" + sep);
                        } else {
                            bab.append(kubun.charAt(2) + "2" + sep);
                        }
                    } else if (k < 16) {
                        // NEC特殊文字。
                        bab.append(kubun.charAt(2) + "3" + sep);
                    } else if (k < 89) {
                        // 第1・2水準漢字。
                        bab.append("31" + sep);
                    } else {
                        // IBM拡張漢字。
                        if (kubun.charAt(3) == '4') {
                            bab.append("54" + sep);
                        } else if (kubun.charAt(2) == '5') {
                            bab.append("64" + sep);
                        } else {
                            bab.append(kubun.charAt(2) + "4" + sep);
                        }
                    }
                }
                if (csv1()) {
                    bab.append("[%s]", s);
                    return bab.toByteArray();
                }
            }
            if (csv4()) {
                bab.append(String.join(",", kubun.split(" *")) + sep);
            } else {
                bab.append("%-6s" + sep, kubun);
            }

            if (!csv2() && !csv3()) {
                // UTF-16
                bab.append("%-8s" + sep, utf16());
                // UTF-8
                bab.append("%-12s" + sep, utf8());
                // VARIANT
                appendVariant(bab);

                // 区点。
                if (k < 100) {
                    bab.append("   %02d-%02d" + sep, k, t);
                } else {
                    bab.append("  %3d-%02d" + sep, k, t);
                }
                if (ss.contains("\uFFFD") || !ss.equals(s)) {
                    // ISO-2022-JP
                    bab.append("-   " + sep);
                    if (!encodableToJis2()) {
                        bab.append("-      " + sep);
                    } else if (!decodableFromJis2()) {
                        bab.append(">%-6s" + sep, jisToHexString(bj2));
                    } else {
                        bab.append("%-6s " + sep, jisToHexString(bj2));
                    }
                    // EUC-JP
                    if (!encodableToEuc()) {
                        bab.append("-      " + sep);
                    } else if (!decodableFromEuc()) {
                        bab.append(">%-6s" + sep, toHexString(be2));
                    } else {
                        bab.append("%-6s " + sep, toHexString(be2));
                    }
                    bab.append("-     " + sep);
                    // Shift_JIS
                    if (contains(bx2, 0x3F) || !decodableFromSjis2004()) {
                        bab.append("-    " + sep);
                    } else {
                        bab.append("%-4s " + sep, toHexString(bx2));
                    }
                } else {
                    // ISO-2022-JP
                    bab.append("%04X" + sep, jis);
                    if (!encodableToJis2()) {
                        bab.append("-      " + sep);
                    } else if (!decodableFromJis2()) {
                        bab.append(">%-6s" + sep, jisToHexString(bj2));
                    } else {
                        bab.append("%-6s " + sep, jisToHexString(bj2));
                    }
                    // EUC-JP
                    if (!encodableToEuc()) {
                        bab.append("-      " + sep);
                    } else if (!decodableFromEuc()) {
                        bab.append(">%-6s" + sep, toHexString(be2));
                    } else {
                        bab.append("%-6s " + sep, toHexString(be2));
                    }
                    bab.append("%04X  " + sep, euc);
                    // Shift_JIS
                    bab.append("%04X " + sep, sjis);
                }
                // Windows-31J
                if (!undefined() && !encodableToW31j()) {
                    bab.append("<%04X" + sep, sjis);
                } else {
                    bab.append("%04X " + sep, sjis);
                }

                appendIbm94x(bab);

                // EBCDIC
                appendIbm93x(bab);
            }

            // 備考。
            if (!undefined()) {
                if ("-sjis".equals(option) || "-w31j".equals(option)) {
                    bab.append("[").append(b).append("]");
                } else {
                    bab.append("[%s]", s);
                }
                if (encodableToW31j()) {
                    if (!nfc.equals(s)) {
                        bab.append(" -> [%s] (NFC)", nfc);
                    } else if (!nfkc.equals(s)) {
                        bab.append(" -> [%s] (NFKC)", nfkc);
                    }
                    if (variant != null
                            && !variant[0].startsWith(nfc) && !variant[0].equals(nfkc)) {
                        bab.append(" -> [%s]", variant[0]);
                        if (variant[1] != null) {
                            bab.append(" (%s)", variant[1]);
                        }
                    }
                    if ((showSjis || !encodableToSjis2004())
                            && !contains(bx2, 0x3F) && !decodableFromSjis2004()) {
                        bab.append(" -> %s (SJIS2004)", toHexString(bx2));
                    }
                }
            }
            return bab.toByteArray();
        }

        @Override
        void appendIbm94x(ByteArrayBuilder bab) {
            if (undefined()) {
                bab.append("-    %1$s-    %1$s", sep);
            } else {
                // IBM 942
                if (decode(b, IBM_942).equals(s) && !Arrays.equals(i942, b)) {
                    bab.append("<%-4s" + sep, toHexString(b));
                } else if (!encodableToI942()) {
                    bab.append("-    " + sep);
                } else if (!decodableFromI942()) {
                    bab.append(">%-4s" + sep, toHexString(i942));
                } else {
                    bab.append("%-4s " + sep, toHexString(i942));
                }
                // IBM 943
                if (decode(b, IBM_943).equals(s) && !Arrays.equals(i943, b)) {
                    bab.append("<%-4s" + sep, toHexString(b));
                } else if (!encodableToI943()) {
                    bab.append("-    " + sep);
                } else if (!decodableFromI943()) {
                    bab.append(">%-4s" + sep, toHexString(i943));
                } else {
                    bab.append("%-4s " + sep, toHexString(i943));
                }
            }
        }

        String kubun() {
            if (undefined()) {
                // 未定義文字。
                return "999999";
            }
            StringBuilder sb = new StringBuilder();
            if (!encodableToW31j()) {
                // デコードのみ可。
                sb.append('7');
            } else {
                // 基本多言語面。
                sb.append('1');
            }
            sb.append(kubunNormalization());
            if (k < 13) {
                // 非漢字。
                char level = kubunLevel();
                if (ss.equals(s)) {
                    // JIS X 0208
                    sb.append('3');
                } else if (!csv() || level > '4') {
                    // ベンダー外字。
                    sb.append('7');
                } else {
                    // JIS X 0213
                    sb.append('4');
                }
                sb.append(level);
                // JIS X 0208
                sb.append('3');
            } else if (k < 16) {
                // NEC特殊文字。
                char level = kubunLevel();
                if (!csv() || level > '4') {
                    // ベンダー外字。
                    sb.append('7');
                } else {
                    // JIS X 0213
                    sb.append('4');
                }
                sb.append(level);
                // NEC特殊文字。
                sb.append('4');
            } else if (k < 48) {
                // 第1水準漢字。
                sb.append("313");
            } else if (k < 89) {
                // 第2水準漢字。
                sb.append("323");
            } else if (k < 95) {
                // NEC選定IBM拡張文字。
                // ベンダー外字。
                sb.append('7');
                sb.append(kubunLevel());
                // NEC選定IBM拡張文字。
                sb.append('5');
            } else if (k < 115) {
                // ユーザー外字領域。
                return "888888";
            } else {
                // IBM拡張漢字。
                char level = kubunLevel();
                if (!csv() || level > '5') {
                    // ベンダー外字。
                    sb.append('7');
                } else if (level > '4') {
                    // JIS X 0212
                    sb.append('5');
                } else {
                    // JIS X 0213
                    sb.append('4');
                }
                sb.append(level);
                // IBM拡張文字。
                sb.append('6');
            }
            sb.append(kubunKanji());
            return sb.toString();
        }

    }

    class JisX0213Info extends CodeInfo {

        int m;
        int k;
        int t;

        String ss;
        String sw;

        JisX0213Info(int m, int k, int t) {
            this.m = m;
            this.k = k;
            this.t = t;
            this.jis = kutenToJis(k, t);
            this.euc = kutenToEuc(m, k, t);
            this.sjis = kutenToSjis(m, k, t);
            this.b = bytes(sjis, 2);
            this.ss = decode(b, SHIFT_JIS);
            this.sw = decode(b, WINDOWS_31J);
            initUnicode(decode(b, SHIFT_JIS_2004));
        }

        byte[] encodedLine() {
            ByteArrayBuilder bab = new ByteArrayBuilder();

            // Unicode
            if (csv3()) {
                bab.append("%-8s" + sep, utf16());
            } else if (cp < 0) {
                bab.append("-       " + sep);
            } else if (cp <= 0xFFFF) {
                bab.append("U+%04X  " + sep, cp);
            } else {
                bab.append("U+%06X" + sep, cp);
            }

            // 区分。
            String kubun = kubun();
            if (csv()) {
                if (csv2()) {
                    if (undefined() || "478".indexOf(kubun.charAt(0)) != -1
                            || kubun.charAt(4) < '7') {
                        // 未定義。
                        bab.append("9" + sep);
                    } else if (kubun.charAt(3) == '4') {
                        // JIS X 0213 2面
                        bab.append("7" + sep);
                    } else {
                        // JIS X 0213 1面
                        bab.append("6" + sep);
                    }
                } else {
                    if (undefined() || "78".indexOf(kubun.charAt(0)) != -1
                            || kubun.charAt(4) < '7') {
                        // 未定義。
                        bab.append("99" + sep);
                    } else if (kubun.charAt(3) == '4') {
                        // JIS X 0213 (2面)
                        if (kubun.charAt(0) == '1') {
                            bab.append("55" + sep);
                        } else {
                            bab.append("56" + sep);
                        }
                    } else {
                        // JIS X 0213 (1面)
                        if (kubun.charAt(0) == '1') {
                            bab.append("45" + sep);
                        } else if (kubun.charAt(0) == '2') {
                            bab.append("46" + sep);
                        } else if (kubun.charAt(0) == '3') {
                            bab.append("47" + sep);
                        } else {
                            bab.append("49" + sep);
                        }
                    }
                }
                if (csv1()) {
                    bab.append("[%s]", s);
                    return bab.toByteArray();
                }
            }
            if (csv4()) {
                bab.append(String.join(",", kubun.split(" *")) + sep);
            } else {
                bab.append("%-6s" + sep, kubun);
            }

            if (!csv2() && !csv3()) {
                // UTF-16
                bab.append("%-8s" + sep, utf16());
                // UTF-8
                bab.append("%-12s" + sep, utf8());
                // VARIANT
                appendVariant(bab);

                // 面区点。
                bab.append("%2d-%02d-%02d" + sep, m, k, t);
                // ISO-2022-JP
                bab.append("%04X" + sep, jis);
                if (!encodableToJis2()) {
                    bab.append("-      " + sep);
                } else if (!decodableFromJis2()) {
                    bab.append(">%-6s" + sep, jisToHexString(bj2));
                } else {
                    bab.append("%-6s " + sep, jisToHexString(bj2));
                }
                // EUC-JP
                if (!encodableToEuc()) {
                    bab.append("-      " + sep);
                } else if (!decodableFromEuc()) {
                    bab.append(">%-6s" + sep, toHexString(be2));
                } else {
                    bab.append("%-6s " + sep, toHexString(be2));
                }
                if (euc <= 0xFFFF) {
                    bab.append("%04X  " + sep, euc);
                } else {
                    bab.append("%06X" + sep, euc);
                }
                // Shift_JIS
                if (!encodableToSjis2004()) {
                    bab.append("<%04X" + sep, sjis);
                } else {
                    bab.append("%04X " + sep, sjis);
                }
                // Windows-31J
                if (!sw.equals(s)) {
                    if (!encodableToSjis2004() || contains(bw2, 0x3F)) {
                        bab.append("-    " + sep);
                    } else if (!decodableFromW31j()) {
                        bab.append(">%-4s" + sep, toHexString(bw2));
                    } else {
                        bab.append("%-4s " + sep, toHexString(bw2));
                    }
                } else {
                    bab.append("%04X " + sep, sjis);
                }

                appendIbm94x(bab);

                // EBCDIC
                appendIbm93x(bab);
            }

            // 備考。
            if (!undefined()) {
                bab.append("[%s]", s);
                if (encodableToSjis2004()) {
                    if (!nfc.equals(s)) {
                        bab.append(" -> [%s] (NFC)", nfc);
                    } else if (!nfkc.equals(s)) {
                        bab.append(" -> [%s] (NFKC)", nfkc);
                    }
                    if (variant != null
                            && !variant[0].startsWith(nfc) && !variant[0].equals(nfkc)) {
                        bab.append(" -> [%s]", variant[0]);
                        if (variant[1] != null) {
                            bab.append(" (%s)", variant[1]);
                        }
                    }
                    if (!contains(bw2, 0x3F) && !decodableFromW31j()) {
                        bab.append(" -> [%s] (W31J)", decode(bw2, WINDOWS_31J));
                    }
                }
            }
            return bab.toByteArray();
        }

        String kubun() {
            if (undefined()) {
                // 未定義文字。
                return "999999";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(kubunUnicode());
            sb.append(kubunNormalization());
            if (m == 2) {
                // 第4水準漢字。
                sb.append("44");
            } else if (k < 14) {
                // 非漢字。
                sb.append("40");
            } else {
                // 第3水準漢字。
                sb.append("43");
            }
            sb.append(kubunW31j());
            sb.append(kubunKanji());
            return sb.toString();
        }

        char kubunUnicode() {
            if (!encodableToSjis2004()) {
                // デコードのみ可。
                return '7';
            }
            return super.kubunUnicode();
        }

    }

    class JisX0212Info extends CodeInfo {

        int m;
        int k;
        int t;

        JisX0212Info(int m, int k, int t) {
            this.m = m;
            this.k = k;
            this.t = t;
            this.jis = kutenToJis(k, t);
            this.euc = kutenToEuc(m, k, t);
            this.sjis = -1;
            this.b = bytes(euc, 3);
            initUnicode(decode(b, EUC_JP));
        }

        @Override
        boolean encodableToEuc() {
            return Arrays.equals(be2, b);
        }

        @Override
        boolean encodableToSjis2004() {
            return s.equals("?") || !contains(bx2, 0x3F);
        }

        @Override
        boolean encodableToW31j() {
            return s.equals("?") || !contains(bw2, 0x3F);
        }

        byte[] encodedLine() {
            ByteArrayBuilder bab = new ByteArrayBuilder();

            // Unicode
            if (csv3()) {
                bab.append("%-8s" + sep, utf16());
            } else if (cp < 0) {
                bab.append("-       " + sep);
            } else if (cp <= 0xFFFF) {
                bab.append("U+%04X  " + sep, cp);
            } else {
                bab.append("U+%06X" + sep, cp);
            }

            // 区分。
            String kubun = kubun();
            if (csv()) {
                if (csv2()) {
                    if (undefined() || "78".indexOf(kubun.charAt(0)) != -1
                            || kubun.charAt(3) != '5' || kubun.charAt(4) < '7') {
                        // 未定義。
                        bab.append("9" + sep);
                    } else {
                        // 未定義。
                        bab.append("9" + sep);
                    }
                } else {
                    if (undefined() || "78".indexOf(kubun.charAt(0)) != -1
                            || kubun.charAt(3) != '5' || kubun.charAt(4) < '7') {
                        // 未定義。
                        bab.append("99" + sep);
                    } else {
                        // 未定義。
                        bab.append("65" + sep);
                    }
                }
                if (csv1()) {
                    bab.append("[%s]", s);
                    return bab.toByteArray();
                }
            }
            if (csv4()) {
                bab.append(String.join(",", kubun.split(" *")) + sep);
            } else {
                bab.append("%-6s" + sep, kubun);
            }

            if (!csv2() && !csv3()) {
                // UTF-16
                bab.append("%-8s" + sep, utf16());
                // UTF-8
                bab.append("%-12s" + sep, utf8());
                // VARIANT
                appendVariant(bab);

                // 面区点。
                bab.append(" S-%02d-%02d" + sep, k, t);
                // ISO-2022-JP
                bab.append("%04X" + sep, jis);
                if (!encodableToJis2()) {
                    bab.append("-      " + sep);
                } else if (!decodableFromJis2()) {
                    bab.append(">%-6s" + sep, jisToHexString(bj2));
                } else {
                    bab.append("%-6s " + sep, jisToHexString(bj2));
                }
                // EUC-JP
                if (!encodableToEuc()) {
                    bab.append("-      " + sep);
                } else if (!decodableFromEuc()) {
                    bab.append(">%-6s" + sep, toHexString(be2));
                } else {
                    bab.append("%-6s " + sep, toHexString(be2));
                }
                if (!encodableToSjis2004() || !decodableFromSjis2004()) {
                    bab.append("-     " + sep);
                } else {
                    int euc2004 = sjis2004ToEuc(bx2);
                    if (euc2004 <= 0xFFFF) {
                        bab.append("%04X  " + sep, euc2004);
                    } else {
                        bab.append("%06X" + sep, euc2004);
                    }
                }
                // Shift_JIS
                if (!encodableToSjis2004() || !decodableFromSjis2004()) {
                    bab.append("-    " + sep);
                } else {
                    bab.append("%-4s " + sep, toHexString(bx2));
                }
                // Windows-31J
                if (!encodableToW31j()) {
                    bab.append("-    " + sep);
                } else if (!decodableFromW31j()) {
                    bab.append(">%-4s" + sep, toHexString(bw2));
                } else {
                    bab.append("%-4s " + sep, toHexString(bw2));
                }

                appendIbm94x(bab);

                // EBCDIC
                appendIbm93x(bab);
            }

            // 備考。
            if (!undefined()) {
                bab.append("[%s]", s);
                if (encodableToEuc()) {
                    if (!nfc.equals(s)) {
                        bab.append(" -> [%s] (NFC)", nfc);
                    } else if (!nfkc.equals(s)) {
                        bab.append(" -> [%s] (NFKC)", nfkc);
                    }
                    if (variant != null
                            && !variant[0].startsWith(nfc) && !variant[0].equals(nfkc)) {
                        bab.append(" -> [%s]", variant[0]);
                        if (variant[1] != null) {
                            bab.append(" (%s)", variant[1]);
                        }
                    }
                }
            }
            return bab.toByteArray();
        }

        String kubun() {
            if (undefined()) {
                // 未定義文字。
                return "999999";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(kubunUnicode());
            sb.append(kubunNormalization());
            // JIS X 0202
            sb.append('5');
            sb.append(kubunLevel());
            sb.append(kubunW31j());
            sb.append(kubunKanji());
            return sb.toString();
        }

        char kubunUnicode() {
            if (!encodableToEuc()) {
                // デコードのみ可。
                return '7';
            }
            return super.kubunUnicode();
        }

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
