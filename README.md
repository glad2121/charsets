# charsets

文字コード関連の情報

## ファイル一覧

```
+ README.md         : このファイル
+ Charsets.java     : 文字コード情報出力プログラム
+ Normalized.java   : 正規化情報出力プログラム
+ kanji.txt         : 法令等による漢字の分類
+ variants.txt      : 異体字一覧
+ charset.txt       : 文字コード情報概要
+ encoding-csv.txt  : 文字コード情報詳細 (CSV 版)
+ encoding-utf8.txt : 文字コード情報詳細 (UTF-8 版)
+ encoding-jis.txt  : 文字コード情報詳細 (ISO-2022-JP 版)
+ encoding-euc.txt  : 文字コード情報詳細 (EUC-JP 版)
+ encoding-sjis.txt : 文字コード情報詳細 (Shift_JIS 版)
+ encoding-w31j.txt : 文字コード情報詳細 (Windows-31J 版)
+ normalized.txt    : 正規化情報
+ new-line.txt      : 改行コードの確認ファイル
```

## ビルド方法

```
$ javac -encoding UTF-8 Charsets.java
$ javac -encoding UTF-8 Normalized.java
```

## 実行方法

```
$ java Charsets -utf8 > encoding-utf8.txt
$ java Charsets -jis  > encoding-jis.txt
$ java Charsets -euc  > encoding-euc.txt
$ java Charsets -sjis > encoding-sjis.txt
$ java Charsets -w31j > encoding-w31j.txt
$ java Charsets -csv2 > charset.txt
$ java Normalized > normalized.txt
```
