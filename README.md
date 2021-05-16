# charsets

文字コード関連の情報。

ASCII、JIS X 0201、JIS X 0208、JIS X 0213、Windows-31J の収録文字について、  
Unicode、ISO-2022-JP、EUC-JP、Shift_JIS、Windows-31J、EBCDIC 等のコード値、  
正規化による安定性、異体字、JIS規格の水準、常用漢字、人名用漢字等の情報を整理しています。

## ファイル一覧

```
+ README.md         : このファイル
+ Charsets.java     : 文字コード情報出力プログラム
+ Normalized.java   : 正規化情報出力プログラム
+ kanji.txt         : 法令等による漢字の分類
+ variants.txt      : 異体字一覧
+ encoding-utf8.txt : 文字コード情報詳細 (UTF-8 版)
+ encoding-jis.txt  : 文字コード情報詳細 (ISO-2022-JP 版)
+ encoding-euc.txt  : 文字コード情報詳細 (EUC-JP 版)
+ encoding-sjis.txt : 文字コード情報詳細 (Shift_JIS 版)
+ encoding-w31j.txt : 文字コード情報詳細 (Windows-31J 版)
+ encoding-csv.txt  : 文字コード情報詳細 (CSV 版)
+ encoding.xlsx     : 文字コード情報詳細 (Excel 版)
+ charset.txt       : 文字コード情報概要 (プログラム処理用)
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
$ java Charsets -csv4 > encoding-csv.txt
$ java Charsets -csv3 > charset.txt
$ java Normalized > normalized.txt
```
