import static java.nio.charset.StandardCharsets.*;
import static java.text.Normalizer.*;
import static java.text.Normalizer.Form.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

class Charsets {

    static final Charset ISO_2022_JP = Charset.forName("ISO-2022-JP");
    static final Charset EUC_JP = Charset.forName("EUC-JP");
    static final Charset SHIFT_JIS = Charset.forName("Shift_JIS");
    static final Charset SHIFT_JIS_2004 = Charset.forName("x-SJIS_0213");
    static final Charset WINDOWS_31J = Charset.forName("Windows-31J");
    static final Charset IBM_930 = Charset.forName("x-IBM930");
    static final Charset IBM_939 = Charset.forName("x-IBM939");

    static final byte[] EMPTY_BYTES = {};
    static final byte[] BYTES_3F = {(byte) 0x3F};

    static final Map<String, String> VARIANT_MAP;
    static {
        Pattern p = Pattern.compile(" *U\\+(\\S+) +U\\+(\\S+).*");
        Map<String, String> variantMap = new HashMap<>();
        try (BufferedReader in = Files.newBufferedReader(Paths.get("variants.txt"), UTF_8)) {
            String line;
            while ((line = in.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    variantMap.put(cpToString(m.group(1)), cpToString(m.group(2)));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VARIANT_MAP = variantMap;
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
        this.option = (args.length == 0) ? "" : args[0];
        this.encoding = optionToEncoding(option);
        this.sep = optionToSeparator(option);
    }

    static Charset optionToEncoding(String option) {
        if ("-utf8".equals(option)) {
            return UTF_8;
        } else if ("-jis".equals(option)) {
            return ISO_2022_JP;
        } else if ("-euc".equals(option)) {
            return EUC_JP;
        } else if ("-sjis".equals(option)) {
            return SHIFT_JIS_2004;
        } else if ("-w31j".equals(option)) {
            return WINDOWS_31J;
        } else {
            return UTF_8;
        }
    }

    static String optionToSeparator(String option) {
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

    void printEncodedLines() {
        println("#");
        println("# encoding%s.txt", option);
        println("#");
        
        printKubunDesc();
        
        println();
        println("# US-ASCII");
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
        
        if (!csv()) {
            println();
            println("# JIS X 0208 - マッピングが異なるもの");
            printHeaderX0208();
            printSeparator();
            printLines(encodedLines(1, 29, option));
            printLines(encodedLines(1, 33, option));
            printLines(encodedLines(1, 34, option));
            printLines(encodedLines(1, 61, option));
            printLines(encodedLines(1, 81, option));
            printLines(encodedLines(1, 82, option));
            printLines(encodedLines(2, 44, option));
        }
        
        println();
        println("# JIS X 0208 - 非漢字");
        printHeaderX0208();
        for (int k = 1; k <= 12; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        println();
        println("# NEC特殊文字");
        printHeaderX0208();
        for (int k = 13; k <= 15; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        println();
        println("# JIS X 0208 - 第1水準漢字");
        printHeaderX0208();
        for (int k = 16; k <= 47; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        println();
        println("# JIS X 0208 - 第2水準漢字");
        printHeaderX0208();
        for (int k = 48; k <= 88; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        if (!csv1()) {
            println();
            println("# NEC選定IBM拡張文字");
            printHeaderX0208();
            for (int k = 89; k <= 94; ++k) {
                printSeparator();
                for (int t = 1; t <= 94; ++t) {
                    printLines(encodedLines(k, t, option));
                }
            }
            
            println();
            println("# ユーザー外字領域");
            printHeaderX0208();
            for (int k = 95; k <= 114; ++k) {
                printSeparator();
                for (int t = 1; t <= 94; ++t) {
                    printLines(encodedLines(k, t, option));
                }
            }
        }
        
        println();
        println("# IBM拡張文字");
        printHeaderX0208();
        for (int k = 115; k <= 120; ++k) {
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLines(k, t, option));
            }
        }
        
        println();
        println("# JIS X 0213 - 非漢字");
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
        printHeaderX0213();
        for (int k = 14; k <= 94; ++k) {
            // 第1水準漢字。
            if (16 <= k && k <= 46) continue;
            // 第2水準漢字。
            if (48 <= k && k <= 83) continue;
            
            printSeparator();
            for (int t = 1; t <= 94; ++t) {
                printLines(encodedLinesX0213(1, k, t, option));
            }
        }
        
        println();
        println("# JIS X 0213 - 第4水準漢字");
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
        
        println();
        println("# JIS X 0213 - 結合文字");
        printHeaderX0213();
        printSeparator();
        printLines(encodedLinesCombining('\u0300'));
        printLines(encodedLinesCombining('\u0301'));
        printLines(encodedLinesCombining('\u3099'));
        printLines(encodedLinesCombining('\u309A'));
    }

    void printKubunDesc() {
        if (csv()) {
            return;
        }
        println();
        println("# 区分:");
        println("# No Unicode      正規化       規格等       水準         Windows-31J       ");
        println("# -- ------------ ------------ ------------ ------------ ------------------");
        println("#  0 -            変換なし     -            非漢字       標準規格          ");
        println("#  1 基本多言語面 NFDで変換    US-ASCII     第1水準漢字  NEC特殊文字       ");
        println("#  2 追加面       NFKCで変換   JIS X 0201   第2水準漢字  NEC選定IBM拡張文字");
        println("#  3 結合文字列   NFCで変換    JIS X 0208   第3水準漢字  IBM拡張文字       ");
        println("#  4 結合文字     -            JIS X 0213   第4水準漢字  -                 ");
        println("#  5 -            -            JIS X 0212   補助漢字     -                 ");
        println("#  7 Decodeのみ可 -            ベンダー外字 ベンダー外字 Encodeのみ可      ");
        println("#  8 ユーザー外字 ユーザー外字 ユーザー外字 ユーザー外字 ユーザー外字      ");
        println("#  9 未定義       未定義       未定義       未定義       未定義            ");
    }

    void printHeader() {
        if (csv()) {
            return;
        }
        println();
        println("Unicode  区分   UTF-16   UTF-8        VAR      CODE JIS  EUC    2004   SJIS W31J ALT  I930 I939 ALT  備考");
    }

    void printHeaderX0208() {
        if (csv()) {
            return;
        }
        println();
        println("Unicode  区分   UTF-16   UTF-8        VAR     区-点 JIS  EUC    2004   SJIS W31J ALT  I930 I939 ALT  備考");
    }

    void printHeaderX0213() {
        if (csv()) {
            return;
        }
        println();
        println("Unicode  区分   UTF-16   UTF-8        VAR  面-区-点 JIS  EUC    2004   SJIS W31J ALT  I930 I939 ALT  備考");
    }

    void printSeparator() {
        if (csv()) {
            println();
            return;
        }
        println("-------- ------ -------- ------------ ---- -------- ---- ------ ------ ---- ---- ---- ---- ---- ---- ----");
    }

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
            System.out.write((byte) '\r');
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
        JisX0201Info info = new JisX0201Info(c);
        if (csv1() && info.kubun().charAt(0) >= '7') {
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

    List<byte[]> encodedLines(int k, int t, String option) {
        Windows31jInfo info = new Windows31jInfo(k, t, option);
        if (csv1() && info.kubun().charAt(0) >= '7') {
            return Collections.emptyList();
        }
        if (info.showSjis) {
            return Arrays.asList(new JisX0208Info(info).encodedLine(), info.encodedLine());
        }
        return Collections.singletonList(info.encodedLine());
    }

    List<byte[]> encodedLinesX0213(int m, int k, int t, String option) {
        // 補助漢字。
        if (m == 2 && (k == 2
                || ( 6 <= k && k <=  7)
                || ( 9 <= k && k <= 11)
                || (16 <= k && k <= 77))) {
            return Collections.emptyList();
        }
        
        JisX0213Info info = new JisX0213Info(m, k, t);
        if (csv1() && info.kubun().charAt(4) != '9') {
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
        bab.append("U+%04X  " + sep, cp);
        if (csv()) {
            bab.append("1" + sep);
        }
        if (csv1()) {
            return Collections.singletonList(bab.toByteArray());
        }
        // 区分。
        bab.append("%-6s" + sep, "40407");
        // UTF-16
        bab.append("%-8s" + sep, toHexString(s));
        // UTF-8
        bab.append("%-12s" + sep, toHexString(s.getBytes(UTF_8)));
        // VARIANT
        bab.append("-   " + sep);

        // JIS
        bab.append("       -" + sep);
        // ISO-2022-JP
        bab.append("-   " + sep);
        // EUC-JP
        bab.append("-     " + sep);
        bab.append("-     " + sep);
        // Shift_JIS
        bab.append("-   " + sep);
        // Windows-31J
        bab.append("-   " + sep);
        bab.append("-   " + sep);

        // EBCDIC
        bab.append("-   %1$s-   %1$s-   %1$s", sep);

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

    static String cpToString(String cp) {
        return new StringBuilder().appendCodePoint(Integer.parseInt(cp, 16)).toString();
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

    class CodeInfo {

        String option;

        String s;
        int cp;

        String nfc;
        String nfkc;
        String nfd;
        String variant;

        int jis;
        int euc;
        int sjis;

        byte[] b;
        byte[] be2;
        byte[] bs2;
        byte[] bx2;
        byte[] bw2;

        byte[] i930;
        byte[] i939;

        void initUnicode(String s) {
            this.s = s;
            this.cp = (s.codePointCount(0, s.length()) != 1) ? -1 : s.codePointAt(0);
            this.nfc  = normalize(s, NFC);
            this.nfkc = normalize(s, NFKC);
            this.nfd  = normalize(s, NFD);
            this.variant = VARIANT_MAP.get(s);
            this.be2 = s.getBytes(EUC_JP);
            this.bs2 = s.getBytes(SHIFT_JIS);
            this.bx2 = s.getBytes(SHIFT_JIS_2004);
            this.bw2 = s.getBytes(WINDOWS_31J);
            this.i930 = s.getBytes(IBM_930);
            this.i939 = s.getBytes(IBM_939);
        }

        boolean undefined() {
            return s.contains("\uFFFD");
        }

        boolean encodableToEuc() {
            return s.equals("?") || !contains(be2, 0x3F);
        }

        boolean encodableToSjis() {
            return Arrays.equals(bs2, b);
        }

        boolean encodableToSjis2004() {
            return Arrays.equals(bx2, b);
        }

        boolean encodableToW31j() {
            return Arrays.equals(bw2, b);
        }

        boolean encodableToI930() {
            return s.equals("?") || !contains(i930, 0x6F) || isEbcdicKanji(i930);
        }

        boolean encodableToI939() {
            return s.equals("?") || !contains(i939, 0x6F) || isEbcdicKanji(i939);
        }

        boolean decodableFromEuc() {
            return Charsets.toString(be2, EUC_JP).equals(s);
        }

        boolean decodableFromSjis() {
            return Charsets.toString(bs2, SHIFT_JIS_2004).equals(s);
        }

        boolean decodableFromSjis2004() {
            return Charsets.toString(bx2, SHIFT_JIS_2004).equals(s);
        }

        boolean decodableFromW31j() {
            return Charsets.toString(bw2, WINDOWS_31J).equals(s);
        }

        boolean decodableFromI930() {
            return Charsets.toString(i930, IBM_930).equals(s);
        }

        boolean decodableFromI939() {
            return Charsets.toString(i939, IBM_939).equals(s);
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

        char kubunNormalization() {
            if (!nfc.equals(s)) {
                return '3';
            } else if (!nfkc.equals(s)) {
                return '2';
            } else if (!nfd.equals(s)) {
                return '1';
            } else {
                return '0';
            }
        }

    }

    class JisX0201Info extends CodeInfo {

        int c;

        JisX0201Info(int c) {
            this.c = c;
            this.jis = c & 0x7F;
            this.euc = (c < 0x80) ? c : (0x8E00 | c);
            this.sjis = (c == 0x5C || c == 0x7E) ? -1 : c;
            this.b = new byte[] {(byte) c};
            initUnicode(Charsets.toString(b, SHIFT_JIS));
        }

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
            if (cp < 0) {
                bab.append("-       " + sep);
            } else {
                bab.append("U+%04X  " + sep, cp);
            }
            if (csv()) {
                if (undefined() || cp < 0x20 || cp == 0x7F) {
                    bab.append("0" + sep);
                } else {
                    bab.append("1" + sep);
                }
            }
            if (csv1()) {
                if (0x20 <= c && c != 0x7F) {
                    bab.append("[%s]", s);
                }
                return bab.toByteArray();
            }
            // 区分。
            bab.append("%-6s" + sep, kubun());

            // UTF-16
            bab.append("%-8s" + sep, utf16());
            // UTF-8
            bab.append("%-12s" + sep, utf8());
            // VARIANT
            if (!nfc.equals(s)) {
                bab.append("%-4s" + sep, toHexString(nfc));
            } else if (!nfkc.equals(s) && nfkc.length() == 1) {
                bab.append("%-4s" + sep, toHexString(nfkc));
            } else if (variant != null && !variant.isEmpty()) {
                bab.append("%-4s" + sep, toHexString(variant));
            } else {
                bab.append("-   " + sep);
            }

            // JIS
            bab.append("      %02X" + sep, c);
            if (undefined()) {
                bab.append("-   %1$s-     %1$s-     %1$s-   %1$s", sep);
                // Windows-31J
                bab.append("%02X  " + sep, c);
                bab.append("-   " + sep);
            } else {
                // ISO-2022-JP
                if (0x80 <= c) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%02X  " + sep, jis);
                }
                // EUC-JP
                if (!encodableToEuc() || !decodableFromEuc()) {
                    bab.append("-     " + sep);
                } else {
                    bab.append("%-6s" + sep, toHexString(be2));
                }
                if (euc < 0) {
                    bab.append("-     " + sep);
                } else if (euc < 0x80) {
                    bab.append("%02X    " + sep, euc);
                } else {
                    bab.append("%04X  " + sep, euc);
                }
                // Shift_JIS
                if (sjis < 0) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%02X  " + sep, sjis);
                }
                // Windows-31J
                if (!encodableToW31j() || !decodableFromW31j()) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%-4s" + sep, toHexString(bw2));
                }
                if (!encodableToSjis()) {
                    bab.append("%-4s" + sep, toHexString(bs2));
                } else if (!contains(bw2, 0x3F) && !decodableFromW31j()) {
                    bab.append("%-4s" + sep, toHexString(bw2));
                } else {
                    bab.append("-   " + sep);
                }
            }

            // EBCDIC
            if (undefined() /*|| !encodableToSjis()*/) {
                bab.append("-   %1$s-   %1$s-   %1$s", sep);
            } else {
                // IBM 930
                if (!encodableToI930() || !decodableFromI930()) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%-4s" + sep, i930());
                }
                // IBM 939
                if (!encodableToI939() || !decodableFromI939()) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%-4s" + sep, i939());
                }
                if (encodableToI930() && !decodableFromI930()) {
                    bab.append("%-4s" + sep, i930());
                } else if (encodableToI939() && !decodableFromI939()) {
                    bab.append("%-4s" + sep, i939());
                } else {
                    bab.append("-   " + sep);
                }
            }

            // 備考。
            if (!undefined()) {
                if (0x20 <= c && c != 0x7F) {
                    bab.append("[%s]", s);
                }
                if (encodableToSjis()) {
                    if (sjis < 0) {
                        bab.append(" -> %s (SJIS)", toHexString(bs2));
                    }
                    if (!decodableFromSjis2004()) {
                        bab.append(" -> %s (SJIS2004)", toHexString(bx2));
                    }
                    if (!nfc.equals(s)) {
                        bab.append(" -> [%s] (NFC)", nfc);
                    } else if (!nfkc.equals(s) && nfkc.length() == 1) {
                        if ("\u3099".equals(nfkc) || "\u309A".equals(nfkc)) {
                            bab.append(" -> (NFKC)");
                        } else {
                            bab.append(" -> [%s] (NFKC)", nfkc);
                        }
                    }
                    if (variant != null && !variant.isEmpty()
                            && !variant.equals(nfc) && !variant.equals(nfkc)) {
                        bab.append(" -> [%s]", variant);
                    }
                }
            }
            return bab.toByteArray();
        }

        String kubun() {
            if (undefined()) {
                // 未定義文字。
                return "99999";
            }
            StringBuilder sb = new StringBuilder();
            sb.append('1');
            sb.append(kubunNormalization());
            if (s.equals("\u00A5") || s.equals("\u203E")) {
                sb.append("207");
            } else if (c < 0x80) {
                // US-ASCII
                sb.append("100");
            } else {
                // JIS X 0201
                sb.append("200");
            }
            return sb.toString();
        }

    }

    class JisX0208Info extends CodeInfo {

        int k;
        int t;

        JisX0208Info(Windows31jInfo w31j) {
            this.option = w31j.option;
            this.k = w31j.k;
            this.t = w31j.t;
            this.jis = w31j.jis;
            this.euc = w31j.euc;
            this.sjis = w31j.sjis;
            this.b = w31j.b;
            initUnicode(Charsets.toString(b, SHIFT_JIS));
        }

        byte[] encodedLine() {
            ByteArrayBuilder bab = new ByteArrayBuilder();

            // Unicode
            if (cp < 0) {
                bab.append("-       " + sep);
            } else {
                bab.append("U+%04X  " + sep, cp);
            }
            if (csv()) {
                if (undefined()) {
                    bab.append("0" + sep);
                } else {
                    bab.append("1" + sep);
                }
            }
            if (csv1()) {
                bab.append("[%s]", s);
                return bab.toByteArray();
            }
            // 区分。
            bab.append("%-6s" + sep, kubun());
            // UTF-16
            bab.append("%-8s" + sep, utf16());
            // UTF-8
            bab.append("%-12s" + sep, utf8());
            // VARIANT
            if (!nfc.equals(s)) {
                bab.append("%-4s" + sep, toHexString(nfc));
            } else if (!nfkc.equals(s) && nfkc.length() == 1) {
                bab.append("%-4s" + sep, toHexString(nfkc));
            } else if (variant != null && !variant.isEmpty()) {
                bab.append("%-4s" + sep, toHexString(variant));
            } else {
                bab.append("-   " + sep);
            }

            // JIS
            bab.append("   %02d-%02d" + sep, k, t);
            // ISO-2022-JP
            bab.append("%04X" + sep, jis);
            // EUC-JP
            bab.append("%-6s" + sep, toHexString(be2));
            bab.append("%04X  " + sep, euc);
            // Shift_JIS
            bab.append("%04X" + sep, sjis);
            // Windows-31J
            bab.append("-   " + sep);
            if (!encodableToSjis()) {
                bab.append("%-4s" + sep, toHexString(bs2));
            } else if (!contains(bw2, 0x3F)) {
                bab.append("%-4s" + sep, toHexString(bw2));
            } else {
                bab.append("-   " + sep);
            }

            // EBCDIC
            if (undefined() /*|| !encodableToSjis()*/) {
                bab.append("-   %1$s-   %1$s-   %1$s", sep);
            } else {
                // IBM 930
                if (!encodableToI930() || !decodableFromI930()) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%-4s" + sep, i930());
                }
                // IBM 939
                if (!encodableToI939() || !decodableFromI939()) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%-4s" + sep, i939());
                }
                if (encodableToI930() && !decodableFromI930()) {
                    bab.append("%-4s" + sep, i930());
                } else if (encodableToI939() && !decodableFromI939()) {
                    bab.append("%-4s" + sep, i939());
                } else {
                    bab.append("-   " + sep);
                }
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
                    } else if (!nfkc.equals(s) && nfkc.length() == 1) {
                        bab.append(" -> [%s] (NFKC)", nfkc);
                    }
                    if (variant != null && !variant.isEmpty()
                            && !variant.equals(nfc) && !variant.equals(nfkc)) {
                        bab.append(" -> [%s]", variant);
                    }
                }
            }
            return bab.toByteArray();
        }

        String kubun() {
            if (undefined()) {
                // 未定義文字。
                return "99999";
            }
            StringBuilder sb = new StringBuilder();
            if (!encodableToSjis()) {
                sb.append('7');
            } else {
                sb.append('1');
            }
            sb.append(kubunNormalization());
            if (k < 16) {
                // 非漢字。
                if (contains(bw2, 0x3F)) {
                    sb.append("309");
                } else {
                    sb.append("307");
                }
            } else if (k < 48) {
                // 第1水準漢字。
                sb.append("310");
            } else {
                // 第2水準漢字。
                sb.append("320");
            }
            return sb.toString();
        }

    }

    class Windows31jInfo extends CodeInfo {

        int k;
        int t;

        String ss;
        boolean showSjis;

        Windows31jInfo(int k, int t, String option) {
            this.option = option;
            this.k = k;
            this.t = t;
            this.jis = (94 < k) ? -1 : kutenToJis(k, t);
            this.euc = (94 < k) ? -1 : kutenToEuc(k, t);
            this.sjis = kutenToSjis(k, t);
            this.b = bytes(sjis, 2);
            this.ss = Charsets.toString(b, SHIFT_JIS);
            initUnicode(Charsets.toString(b, WINDOWS_31J));
            this.showSjis = showSjis();
        }

        boolean showSjis() {
            return !ss.contains("\uFFFD") && !ss.equals(s);
        }

        byte[] encodedLine() {
            ByteArrayBuilder bab = new ByteArrayBuilder();

            // Unicode
            if (cp < 0) {
                bab.append("-       " + sep);
            } else {
                bab.append("U+%04X  " + sep, cp);
            }
            if (csv()) {
                if (undefined() || "78".indexOf(kubun().charAt(0)) != -1) {
                    bab.append("0" + sep);
                } else {
                    bab.append("1" + sep);
                }
            }
            if (csv1()) {
                bab.append("[%s]", s);
                return bab.toByteArray();
            }
            // 区分。
            bab.append("%-6s" + sep, kubun());
            // UTF-16
            bab.append("%-8s" + sep, utf16());
            // UTF-8
            bab.append("%-12s" + sep, utf8());
            // VARIANT
            if (!nfc.equals(s)) {
                bab.append("%-4s" + sep, toHexString(nfc));
            } else if (!nfkc.equals(s) && nfkc.length() == 1
                    /*&& !(0x2160 <= cp && cp <= 0x217F)*/) {
                bab.append("%-4s" + sep, toHexString(nfkc));
            } else if (variant != null && !variant.isEmpty()) {
                bab.append("%-4s" + sep, toHexString(variant));
            } else {
                bab.append("-   " + sep);
            }

            // JIS
            if (k < 100) {
                bab.append("   %02d-%02d" + sep, k, t);
            } else {
                bab.append("  %3d-%02d" + sep, k, t);
            }
            if (ss.contains("\uFFFD") || !ss.equals(s)) {
                bab.append("-   " + sep);
                if (!encodableToEuc() || !decodableFromEuc()) {
                    bab.append("-     " + sep);
                } else {
                    bab.append("%-6s" + sep, toHexString(be2));
                }
                bab.append("-     " + sep);
                if (contains(bx2, 0x3F) || !decodableFromSjis2004()) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%-4s" + sep, toHexString(bx2));
                }
            } else {
                // ISO-2022-JP
                bab.append("%04X" + sep, jis);
                // EUC-JP
                bab.append("%-6s" + sep, toHexString(be2));
                bab.append("%04X  " + sep, euc);
                // Shift_JIS
                bab.append("%04X" + sep, sjis);
            }
            // Windows-31J
            bab.append("%04X" + sep, sjis);
            if (undefined()) {
                bab.append("-   " + sep);
            } else {
                if (!encodableToW31j()) {
                    bab.append("%-4s" + sep, toHexString(bw2));
                } else {
                    bab.append("-   " + sep);
                }
            }

            // EBCDIC
            if (undefined() /*|| !encodableToW31j()*/) {
                bab.append("-   %1$s-   %1$s-   %1$s", sep);
            } else {
                // IBM 930
                if (!encodableToI930() || !decodableFromI930()) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%-4s" + sep, i930());
                }
                // IBM 939
                if (!encodableToI939() || !decodableFromI939()) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%-4s" + sep, i939());
                }
                if (encodableToI930() && !decodableFromI930()) {
                    bab.append("%-4s" + sep, i930());
                } else if (encodableToI939() && !decodableFromI939()) {
                    bab.append("%-4s" + sep, i939());
                } else {
                    bab.append("-   " + sep);
                }
            }

            // 備考。
            if (!undefined()) {
                if ("-sjis".equals(option) || "-w31j".equals(option)) {
                    bab.append("[").append(b).append("]");
                } else {
                    bab.append("[%s]", s);
                }
                if (encodableToW31j()) {
                    if ((showSjis || !encodableToSjis2004())
                            && !contains(bx2, 0x3F) && !decodableFromSjis2004()) {
                        bab.append(" -> %s (SJIS2004)", toHexString(bx2));
                    }
                    if (!nfc.equals(s)) {
                        bab.append(" -> [%s] (NFC)", nfc);
                    } else if (!nfkc.equals(s) && nfkc.length() == 1
                            /*&& !(0x2160 <= cp && cp <= 0x217F)*/) {
                        bab.append(" -> [%s] (NFKC)", nfkc);
                    }
                    if (variant != null && !variant.isEmpty()
                            && !variant.equals(nfc) && !variant.equals(nfkc)) {
                        bab.append(" -> [%s]", variant);
                    }
                }
            }
            return bab.toByteArray();
        }

        String kubun() {
            if (undefined()) {
                // 未定義文字。
                return "99999";
            }
            StringBuilder sb = new StringBuilder();
            if (!encodableToW31j()) {
                sb.append('7');
            } else {
                sb.append('1');
            }
            sb.append(kubunNormalization());
            if (k < 13) {
                // 非漢字。
                if (!ss.equals(s)) {
                    sb.append("770");
                } else {
                    sb.append("300");
                }
            } else if (k < 16) {
                // NEC特殊文字。
                if (!encodableToSjis2004()) {
                    sb.append("771");
                } else {
                    sb.append("701");
                }
            } else if (k < 48) {
                // 第1水準漢字。
                sb.append("310");
            } else if (k < 89) {
                // 第2水準漢字。
                sb.append("320");
            } else if (k < 95) {
                // NEC選定IBM拡張文字。
                if (decodableFromSjis2004()) {
                    String sjis2004 = toHexString(bx2);
                    if (sjis2004.compareTo("879F") < 0) {
                        sb.append("702");
                    } else if (sjis2004.compareTo("F000") < 0) {
                        sb.append("732");
                    } else {
                        sb.append("742");
                    }
                } else if (decodableFromEuc()) {
                    sb.append("752");
                } else {
                    sb.append("772");
                }
            } else if (k < 115) {
                // ユーザー外字領域。
                return "88888";
            } else {
                // IBM拡張漢字。
                if (decodableFromSjis2004()) {
                    String sjis2004 = toHexString(bx2);
                    if (sjis2004.compareTo("879F") < 0) {
                        sb.append("703");
                    } else if (sjis2004.compareTo("F000") < 0) {
                        sb.append("733");
                    } else {
                        sb.append("743");
                    }
                } else if (decodableFromEuc()) {
                    sb.append("753");
                } else {
                    sb.append("773");
                }
            }
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
            this.ss = Charsets.toString(b, SHIFT_JIS);
            this.sw = Charsets.toString(b, WINDOWS_31J);
            initUnicode(Charsets.toString(b, SHIFT_JIS_2004));
        }

        byte[] encodedLine() {
            ByteArrayBuilder bab = new ByteArrayBuilder();

            // Unicode
            if (cp < 0) {
                bab.append("-       " + sep);
            } else if (cp <= 0xFFFF) {
                bab.append("U+%04X  " + sep, cp);
            } else {
                bab.append("U+%06X" + sep, cp);
            }
            if (csv()) {
                if (undefined() || "378".indexOf(kubun().charAt(0)) != -1 || kubun().charAt(4) != '9') {
                    bab.append("0" + sep);
                } else {
                    bab.append("1" + sep);
                }
            }
            if (csv1()) {
                bab.append("[%s]", s);
                return bab.toByteArray();
            }
            // 区分。
            bab.append("%-6s" + sep, kubun());
            // UTF-16
            bab.append("%-8s" + sep, utf16());
            // UTF-8
            bab.append("%-12s" + sep, utf8());
            // VARIANT
            if (!nfc.equals(s)) {
                bab.append("%-4s" + sep, toHexString(nfc));
            } else if (!nfkc.equals(s) && nfkc.length() == 1
                    /*&& !(0x2160 <= cp && cp <= 0x217F)*/) {
                bab.append("%-4s" + sep, toHexString(nfkc));
            } else if (variant != null && !variant.isEmpty()) {
                bab.append("%-4s" + sep, toHexString(variant));
            } else {
                bab.append("-   " + sep);
            }

            // JIS
            bab.append("%2d-%02d-%02d" + sep, m, k, t);
            // ISO-2022-JP
            bab.append("%04X" + sep, jis);
            // EUC-JP
            if (!encodableToEuc() || !decodableFromEuc()) {
                bab.append("-     " + sep);
            } else {
                bab.append("%-6s" + sep, toHexString(be2));
            }
            if (euc <= 0xFFFF) {
                bab.append("%04X  " + sep, euc);
            } else {
                bab.append("%06X" + sep, euc);
            }
            // Shift_JIS
            bab.append("%04X" + sep, sjis);
            // Windows-31J
            if (!sw.equals(s)) {
                if (!encodableToSjis2004()
                        || contains(bw2, 0x3F) || !decodableFromW31j()) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%-4s" + sep, toHexString(bw2));
                }
            } else {
                bab.append("%04X" + sep, sjis);
            }
            if (undefined()) {
                bab.append("-   " + sep);
            } else {
                if (!encodableToSjis2004()) {
                    bab.append("%-4s" + sep, toHexString(bx2));
                } else if (!encodableToW31j()
                        && !contains(bw2, 0x3F) && !decodableFromW31j()) {
                    bab.append("%-4s" + sep, toHexString(bw2));
                } else {
                    bab.append("-   " + sep);
                }
            }

            // EBCDIC
            if (undefined() /*|| !encodableToSjis2004()*/) {
                bab.append("-   %1$s-   %1$s-   %1$s", sep);
            } else {
                // IBM 930
                if (!encodableToI930() || !decodableFromI930()) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%-4s" + sep, i930());
                }
                // IBM 939
                if (!encodableToI939() || !decodableFromI939()) {
                    bab.append("-   " + sep);
                } else {
                    bab.append("%-4s" + sep, i939());
                }
                if (encodableToI930() && !decodableFromI930()) {
                    bab.append("%-4s" + sep, i930());
                } else if (encodableToI939() && !decodableFromI939()) {
                    bab.append("%-4s" + sep, i939());
                } else {
                    bab.append("-   " + sep);
                }
            }

            // 備考。
            if (!undefined()) {
                bab.append("[%s]", s);
                if (encodableToSjis2004()) {
                    if (!nfc.equals(s)) {
                        bab.append(" -> [%s] (NFC)", nfc);
                    } else if (!nfkc.equals(s) && nfkc.length() == 1
                            /*&& !(0x2160 <= cp && cp <= 0x217F)*/) {
                        bab.append(" -> [%s] (NFKC)", nfkc);
                    }
                    if (variant != null && !variant.isEmpty()
                            && !variant.equals(nfc) && !variant.equals(nfkc)) {
                        bab.append(" -> [%s]", variant);
                    }
                }
            }
            return bab.toByteArray();
        }

        String kubun() {
            if (undefined()) {
                // 未定義文字。
                return "99999";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(kubunUnicode());
            sb.append(kubunNormalization());
            if (m == 2) {
                // 第4水準漢字。
                sb.append("44");
                if (contains(bw2, 0x3F)) {
                    sb.append('9');
                } else {
                    sb.append('7');
                }
            } else if (k < 14) {
                // 非漢字。
                sb.append("40");
                if (contains(bw2, 0x3F)) {
                    sb.append('9');
                } else if (encodableToW31j()) {
                    sb.append('1');
                } else {
                    sb.append('7');
                }
            } else {
                // 第3水準漢字。
                sb.append("43");
                if (contains(bw2, 0x3F)) {
                    sb.append('9');
                } else {
                    sb.append('7');
                }
            }
            return sb.toString();
        }

        char kubunUnicode() {
            if (!encodableToSjis2004()) {
                return '7';
            } else if (cp < 0) {
                return '3';
            } else if (s.length() > 1) {
                return '2';
            } else {
                return '1';
            }
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
