|Case|n|m|Seed/generator|Algorithm|Run 1 (us)|Run 2 (us)|Run 3 (us)|Median (us)|Correct outputs checked|
|---|---:|---:|---|---|---:|---:|---:|---:|---|
|random|500|50|Random(12345+500), letters a-z, pattern = random substring of text|naive|38.0|42.0|37.8|38.0|yes (1 starts, equals String.indexOf oracle)|
|random|500|50|Random(12345+500), letters a-z, pattern = random substring of text|kmp|53.9|42.1|38.6|42.1|yes (1 starts, equals String.indexOf oracle)|
|random|500|50|Random(12345+500), letters a-z, pattern = random substring of text|rk|83.8|63.5|70.7|70.7|yes (1 starts, equals String.indexOf oracle)|
|random|1500|150|Random(12345+1500), letters a-z, pattern = random substring of text|naive|50.0|53.9|52.1|52.1|yes (1 starts, equals String.indexOf oracle)|
|random|1500|150|Random(12345+1500), letters a-z, pattern = random substring of text|kmp|218.9|119.7|99.1|119.7|yes (1 starts, equals String.indexOf oracle)|
|random|1500|150|Random(12345+1500), letters a-z, pattern = random substring of text|rk|175.2|124.5|75.0|124.5|yes (1 starts, equals String.indexOf oracle)|
|random|3000|300|Random(12345+3000), letters a-z, pattern = random substring of text|naive|125.5|131.1|133.1|131.1|yes (1 starts, equals String.indexOf oracle)|
|random|3000|300|Random(12345+3000), letters a-z, pattern = random substring of text|kmp|119.8|122.6|120.4|120.4|yes (1 starts, equals String.indexOf oracle)|
|random|3000|300|Random(12345+3000), letters a-z, pattern = random substring of text|rk|153.6|140.6|151.1|151.1|yes (1 starts, equals String.indexOf oracle)|
|repetitive-nomatch|500|50|text a^n, pattern a^(m-1)b|naive|88.0|89.5|89.7|89.5|yes (0 starts, equals String.indexOf oracle)|
|repetitive-nomatch|500|50|text a^n, pattern a^(m-1)b|kmp|50.4|44.8|45.7|45.7|yes (0 starts, equals String.indexOf oracle)|
|repetitive-nomatch|500|50|text a^n, pattern a^(m-1)b|rk|23.0|23.6|22.7|23.0|yes (0 starts, equals String.indexOf oracle)|
|repetitive-nomatch|1500|150|text a^n, pattern a^(m-1)b|naive|579.4|332.2|343.2|343.2|yes (0 starts, equals String.indexOf oracle)|
|repetitive-nomatch|1500|150|text a^n, pattern a^(m-1)b|kmp|8.0|9.6|8.1|8.1|yes (0 starts, equals String.indexOf oracle)|
|repetitive-nomatch|1500|150|text a^n, pattern a^(m-1)b|rk|95.9|95.3|95.1|95.3|yes (0 starts, equals String.indexOf oracle)|
|repetitive-nomatch|3000|300|text a^n, pattern a^(m-1)b|naive|1478.3|1489.7|1290.3|1478.3|yes (0 starts, equals String.indexOf oracle)|
|repetitive-nomatch|3000|300|text a^n, pattern a^(m-1)b|kmp|17.3|16.1|15.6|16.1|yes (0 starts, equals String.indexOf oracle)|
|repetitive-nomatch|3000|300|text a^n, pattern a^(m-1)b|rk|49.9|50.2|49.8|49.9|yes (0 starts, equals String.indexOf oracle)|
|repetitive-allmatch|500|50|text a^n, pattern a^m|naive|51.2|47.6|47.7|47.7|yes (451 starts, equals String.indexOf oracle)|
|repetitive-allmatch|500|50|text a^n, pattern a^m|kmp|14.3|12.9|12.7|12.9|yes (451 starts, equals String.indexOf oracle)|
|repetitive-allmatch|500|50|text a^n, pattern a^m|rk|95.8|95.7|98.4|95.8|yes (451 starts, equals String.indexOf oracle)|
|repetitive-allmatch|1500|150|text a^n, pattern a^m|naive|363.1|360.9|360.5|360.9|yes (1351 starts, equals String.indexOf oracle)|
|repetitive-allmatch|1500|150|text a^n, pattern a^m|kmp|68.0|51.2|120.1|68.0|yes (1351 starts, equals String.indexOf oracle)|
|repetitive-allmatch|1500|150|text a^n, pattern a^m|rk|761.2|851.1|761.4|761.4|yes (1351 starts, equals String.indexOf oracle)|
|repetitive-allmatch|3000|300|text a^n, pattern a^m|naive|1400.6|1310.8|1298.4|1310.8|yes (2701 starts, equals String.indexOf oracle)|
|repetitive-allmatch|3000|300|text a^n, pattern a^m|kmp|74.8|74.1|74.3|74.3|yes (2701 starts, equals String.indexOf oracle)|
|repetitive-allmatch|3000|300|text a^n, pattern a^m|rk|3252.3|3109.7|3634.3|3252.3|yes (2701 starts, equals String.indexOf oracle)|