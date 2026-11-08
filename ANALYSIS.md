# Assignment 4 — analysis

## Environment and commands

- Language / runtime: Java. OpenJDK 64-Bit Server VM, build 25.0.5.0.2+1-LTS (Microsoft build of OpenJDK 25).
- OS / CPU / RAM: GitHub Codespaces, Linux 6.8.0-1064-azure (Ubuntu 22.04), x86_64, 2 CPUs (`nproc` = 2), 7.8 GiB RAM, no swap.
- Public checks: `python tools/run.py --setup`, then `python tools/run.py --check` (44 public cases and the repeated mixed batch; all pass).
- Own tests: `python tests/student/run_student.py` (fixtures in `tests/student/cases.json`, results in `tests/student/results.md`; 23 of 23 pass).
- Benchmark: `javac -cp Java/build -d tests/student/out tests/student/Bench.java`, then `java -cp Java/build:tests/student/out Bench > tests/student/bench.md`.
- Repository:https://github.com/qallmen/assignment4ik.git. The exact commit URL (with the full 40-character SHA) is submitted in Moodle.

## Algorithm explanations

### lcs
- State: `dp[i][j]` is the length of an LCS of the prefixes `a[0..i)` and `b[0..j)`.
- Base cases: `dp[0][j] = dp[i][0] = 0`, because an empty prefix shares nothing. This also covers empty inputs.
- Transition: if `a[i-1] == b[j-1]` then `dp[i][j] = dp[i-1][j-1] + 1`, otherwise `max(dp[i-1][j], dp[i][j-1])`.
- Order: rows and columns increase, so the upper, left and diagonal cells are ready when a cell is computed.
- Correctness: if the last characters are equal, matching them cannot hurt (exchange argument), so the optimum is the diagonal optimum plus one. If they differ, at least one of them is unused in any common subsequence, so we drop one and take the better result. Induction on `i + j`.
- Reconstruction: start at `(n, m)`. Equal characters are emitted and we move diagonally. Otherwise we move to the larger neighbour (ties go up). The collected characters are reversed. Any optimal sequence is accepted.
- Complexity: Theta((n+1)(m+1)) time and memory for the int table; output O(length of the LCS).
- Boundary test: `axbyc` vs `abc` (subsequence, not substring) detects an implementation that requires contiguity. Two empty strings detect a missing base case.

### merge
- State: `dp[l][r]` is the minimum total cost of merging original blocks `l..r` into one block. `split[l][r]` is the `k` of the last merge.
- Base case: `dp[l][l] = 0`.
- Transition: `dp[l][r] = min over l <= k < r of dp[l][k] + dp[k+1][r]`, plus `S(l, r)`, the total size of blocks `l..r` (prefix sums).
- Order: by increasing interval length, so both sub-intervals are already solved.
- Correctness: because only adjacent blocks merge, the last merge joins `[l,k]` and `[k+1,r]` and always costs `S(l,r)`. The two sides are independent, so the problem has optimal substructure.
- Plan: a recursive walk emits the plan for `[l,k]`, then `[k+1,r]`, then `[l,k,r]`. Children therefore appear before their parent, which makes the plan chronological. It has exactly `n-1` merges, and recursion depth is at most `n <= 200`. Ties: the first `k` with strictly smaller cost is kept.
- Complexity: O(n^3) time, O(n^2) auxiliary memory (`long` costs, `int` splits). `n <= 1` returns immediately. Costs are at most about 4e13, which fits in a 64-bit `long`.
- Boundary test: sizes `[1,100,1,100]` give 404. A greedy merge of the two smallest blocks would join the two non-adjacent 1s, which violates adjacency.

### tree
- State: for each vertex `v`, `take[v]` is the best weight of its subtree when `v` is selected, and `skip[v]` when `v` is not selected.
- Base: a leaf has `take = weight`, `skip = 0`.
- Transition: `take[p] += skip[c]` and `skip[p] += max(take[c], skip[c])` for every child `c`.
- Order: an iterative DFS (explicit stack) records a preorder. Processing it in reverse guarantees children are finished before their parent, and there is no recursion, so a chain of 100000 vertices is safe.
- Correctness: the only constraint is between a vertex and its children. A chosen vertex forces its children to be skipped, and a skipped vertex lets each child choose freely.
- Reconstruction: in preorder, the root is chosen if `take > skip`. A child is chosen only if its parent is not chosen and `take > skip`. Ties prefer skipping, so zero-weight or all-negative inputs give the empty set and weight 0.
- Complexity: O(n) time and memory (adjacency stored in arrays). `n = 0` is handled up front. Weights are summed in `long`, with a maximum around 1e14.
- Boundary test: a path of 100000 vertices detects stack overflow. `weights [1,10,1]` with child-first edge order detects a dependence on the root or on edge direction.

### naive
- Invariant: for every start `i` in `0..n-m`, compare `text[i+j]` with `pattern[j]` for `j = 0..m-1`. Report `i` if all `m` characters match.
- Last valid start: `n - m`. If `m > n`, nothing is reported. An empty pattern matches every boundary `0..n`, including `n`.
- Overlaps: every start is tested independently, so overlapping matches are all found, in increasing order.
- Complexity: O(n*m + n + 1) time, O(1) auxiliary memory, O(z) output.
- Boundary test: `pattern = text` gives `[0]`, which detects an off-by-one in the last start.

### kmp
- Definition: `pi[i]` is the length of the longest proper prefix of `pattern[0..i]` that is also a suffix of it.
- Construction and search both keep a counter `k` for the number of matched pattern characters. On a mismatch, `k` falls back to `pi[k-1]` until a match or 0. On a match, `k` grows.
- After a full match (`k == m`) we report `i - m + 1` and set `k = pi[k-1]`. This keeps the longest border as already matched text, so overlapping occurrences are found without re-reading text.
- Correctness: `k` is always the longest prefix of the pattern that is a suffix of the text read so far. Fallback only moves to shorter borders, and no valid shorter alignment is skipped.
- Complexity: `k` increases at most once per text character and every fallback decreases it, so the work is O(n + m) plus O(z) output. Auxiliary memory is O(m). The empty pattern is handled before any indexing.
- Boundary test: `text ababcabcabababd`, `pattern ababd` forces several partial matches and fallbacks. `aabaabaab` with `aab` checks fallback after a full match.

### rk
- Hash: polynomial, base 31, modulus 101, ASCII codes. The leftmost character has the highest power. The window hash is `sum code[j] * 31^(m-1-j) mod 101`.
- Rolling update: `h' = ((h - code[i] * 31^(m-1)) mod 101 + 101) mod 101 * 31 + code[i+m]) mod 101`. Negative remainders are normalised by adding the modulus before the final step.
- Verification: equal hashes only make a window a candidate. The window is compared character by character before the start is reported.
- Correctness: equal strings always have equal hashes, so no true match is rejected, and the verification removes false positives.
- Complexity: O(n + m + c*m), where `c` is the number of equal-hash candidate windows. With modulus 101 about 1 in 101 random windows is a false candidate, and in the worst case (for example `a^n` against `a^m`) every window is a candidate, so the bound is O(n*m + n + m + 1). It is not deterministic linear. Auxiliary memory is O(1). `m = 0` and `m > n` are handled before any window is formed.
- Boundary test: `djab` with pattern `ab`. Both `dj` and `ab` hash to 75, so a version that trusts the hash returns `[0, 2]`; the correct result is `[2]`.

### suffix
- Prefix doubling: initial rank is the character code. In round `k`, suffixes are sorted by the pair `(rank[i], rank[i+k])`, where a missing second rank is -1. New ranks are assigned in sorted order, with equal pairs sharing a rank. Doubling stops when all ranks are distinct (`rank[sa[n-1]] == n-1`). After a round with step `k`, ranks describe the first `2k` characters.
- Complexity of doubling: about `log2(n)` rounds, each a comparison sort of `Integer[]`, so O(n log^2 n) time and O(n) memory. This uses boxed integers and is simple rather than the fastest option.
- Kasai: `inv[sa[i]] = i`. Suffixes are processed in text order, with `h` the current LCP. If suffix `i` has LCP `h` with its predecessor in the suffix array, then suffix `i+1` has LCP at least `h-1` with its own predecessor, so `h` only decreases by at most one per step. Total work is O(n). `lcp[0] = 0`.
- Repeated substring: the maximum value in `lcp` is the length of the longest substring that occurs twice. If a substring of length `L` occurs at two starts, the two suffixes share a prefix of length `L`, and all suffixes with that prefix are consecutive in sorted order, so some adjacent pair has `lcp >= L`. The answer is `text.substring(sa[i], sa[i] + lcp[i])` for the maximising `i`, or the empty string when the maximum is 0. Overlap is allowed.
- Edge cases: empty text returns empty arrays and `""`. A single character is a one-element array with `lcp [0]`.
- Boundary test: `mississippi` needs several doubling rounds, and the unique maximum LCP 4 gives `issi`. `abc` (no repeat) returns `""`.

### aho
- Automaton: one trie per request over the 95 printable ASCII characters (code minus 32), stored in a flat `int` array. Each pattern ends in a terminal state; duplicate patterns end in the same state and keep separate output columns through `patState[i]`.
- Failure links: BFS from the root. `fail[v]` is the state of the longest proper suffix of `v`'s string that is a prefix of some pattern. Missing transitions are filled with `go[fail[v]][c]`, so a text character is processed in O(1).
- Counting per text: reset state to the root and use a fresh `vis` array. For each text character move to `go[state][c]` and increment `vis[state]`. Then walk the states in reverse BFS order and add `vis[v]` to `vis[fail[v]]`. A pattern ends at a position exactly when its terminal state is the current state or lies on its failure chain, so after this propagation `vis[terminal]` is the number of occurrences. Overlaps and patterns that are suffixes of other patterns are counted automatically.
- Reverse BFS order is correct because `fail[v]` is shallower than `v`. All deeper failure-children are added before `v` pushes its total upward.
- Output: row `t` has one count per dictionary position in original order. No texts gives `[]`. An empty dictionary gives an empty row per text. An empty text gives zeros.
- Complexity: construction O(L + 95*S + p) time and O(95*S + p) memory, with `L` total pattern length, `S` states and `p` patterns. Each text costs O(|text| + S + p). Counts are `long`.
- Boundary test: `ab` against texts `["a", "b"]` must give `[[0],[0]]`; carrying the state over would wrongly count one match.

## Own tests

All 23 cases pass (22 fixtures in `tests/student/cases.json` plus one generated case, a path of 100000 vertices). For `lcs`, `merge` and `tree`, several optimal answers exist, so the runner checks the optimum value and validates the returned witness (subsequence of both strings, a legal adjacent merge plan, an independent set with the stated weight). All other cases are compared exactly.

|Command|Input or fixture path|Expected result|Actual result / log|What it checks|
|---|---|---|---|---|
|`python tests/student/run_student.py`|tests/student/cases.json: lcs-identical|length=3, any valid LCS|PASS: {"length":3,"sequence":"abc"}|Equal strings: the diagonal branch is taken at every step.|
|`python tests/student/run_student.py`|tests/student/cases.json: lcs-no-common|length=0, any valid LCS|PASS: {"length":0,"sequence":""}|No shared characters: wrong base case or reconstruction loop would emit garbage.|
|`python tests/student/run_student.py`|tests/student/cases.json: lcs-classic|length=4, any valid LCS|PASS: {"length":4,"sequence":"GTAB"}|Non-contiguous optimum with mismatch transitions on both sides.|
|`python tests/student/run_student.py`|tests/student/cases.json: lcs-both-empty|length=0, any valid LCS|PASS: {"length":0,"sequence":""}|Both inputs empty.|
|`python tests/student/run_student.py`|tests/student/cases.json: merge-two|cost=12, any valid plan|PASS: {"cost":12,"plan":[[0,0,1]]}|Smallest non-trivial interval; plan has exactly n-1 = 1 merge.|
|`python tests/student/run_student.py`|tests/student/cases.json: merge-adjacency|cost=404, any valid plan|PASS: {"cost":404,"plan":[[0,0,1],[2,2,3],[0,1,3]]}|Optimum 404 = (1+100)+(1+100)+202; a greedy merge of the two smallest (non-adjacent) blocks violates adjacency.|
|`python tests/student/run_student.py`|tests/student/cases.json: tree-single|weight=7, any independent set of that weight|PASS: {"weight":7,"selected":[0]}|Single vertex, no edges.|
|`python tests/student/run_student.py`|tests/student/cases.json: tree-star|weight=15, any independent set of that weight|PASS: {"weight":15,"selected":[1,2,3]}|Leaves beat the centre (15 vs 1); take/skip at the root.|
|`python tests/student/run_student.py`|tests/student/cases.json: tree-chain-four|weight=6, any independent set of that weight|PASS: {"weight":6,"selected":[0,3]}|Reconstruction must respect the parent decision (0 and 3, not 1 and 3).|
|`python tests/student/run_student.py`|tests/student/cases.json: tree-inner-optimum|weight=10, any independent set of that weight|PASS: {"weight":10,"selected":[1]}|Best vertex is not vertex 0 and edges are listed child-first: detects dependence on root or edge direction.|
|`python tests/student/run_student.py`|tests/student/cases.json: naive-no-match|{"positions":[]}|PASS: {"positions":[]}|No occurrence.|
|`python tests/student/run_student.py`|tests/student/cases.json: naive-whole-text|{"positions":[0]}|PASS: {"positions":[0]}|Last valid start equals 0 when pattern length equals text length.|
|`python tests/student/run_student.py`|tests/student/cases.json: kmp-fallback|{"positions":[10]}|PASS: {"positions":[10]}|Several partial matches that need prefix-function fallback before the real match.|
|`python tests/student/run_student.py`|tests/student/cases.json: kmp-period|{"positions":[0,3,6]}|PASS: {"positions":[0,3,6]}|Fallback after a full match keeps the periodic occurrences.|
|`python tests/student/run_student.py`|tests/student/cases.json: rk-hash-equal-not-match|{"positions":[2]}|PASS: {"positions":[2]}|'dj' and 'ab' both hash to 75 (base 31, mod 101): reporting on equal hash alone would also return 0.|
|`python tests/student/run_student.py`|tests/student/cases.json: rk-rolling|{"positions":[2,5]}|PASS: {"positions":[2,5]}|Many rolling updates, including negative remainders after subtracting the leftmost character.|
|`python tests/student/run_student.py`|tests/student/cases.json: suffix-distinct|{"sa":[0,1,2],"lcp":[0,0,0],"repeated":""}|PASS: {"sa":[0,1,2],"lcp":[0,0,0],"repeated":""}|No repeated substring: repeated must be the empty string.|
|`python tests/student/run_student.py`|tests/student/cases.json: suffix-mississippi|{"sa":[10,7,4,1,0,9,8,6,3,5,2],"lcp":[0,1,1,4,0,0,1,0,2,1,3],"repeated":"issi"}|PASS: {"sa":[10,7,4,1,0,9,8,6,3,5,2],"lcp":[0,1,1,4,0,0,1,0,2,1,3],"repeated":"issi"}|Several doubling rounds and Kasai reuse; the unique maximum LCP 4 gives 'issi'.|
|`python tests/student/run_student.py`|tests/student/cases.json: aho-nested|{"counts":[[2,2,0,1,0,2,0]]}|PASS: {"counts":[[2,2,0,1,0,2,0]]}|Patterns that are prefixes/suffixes of each other; failure-link propagation.|
|`python tests/student/run_student.py`|tests/student/cases.json: aho-overlap|{"counts":[[3]]}|PASS: {"counts":[[3]]}|Overlapping matches are all counted.|
|`python tests/student/run_student.py`|tests/student/cases.json: aho-duplicates|{"counts":[[2,1,2],[0,0,0]]}|PASS: {"counts":[[2,1,2],[0,0,0]]}|Duplicate patterns share a state but keep separate columns; empty text gives zeros.|
|`python tests/student/run_student.py`|tests/student/cases.json: aho-reset|{"counts":[[0],[0]]}|PASS: {"counts":[[0],[0]]}|Automaton state must reset between texts: 'a' then 'b' must not form 'ab'.|
|`python tests/student/run_student.py`|generated in run_student.py|weight=50000, any independent set of that weight|PASS: weight=50000|Path of 100000 vertices: recursion would overflow the stack; an iterative DP must not.|

## Search comparison

Inputs: the same text and pattern are given to naive, KMP and RK in each row (common domain, n and m at most 3000). Sizes: (n, m) = (500, 50), (1500, 150), (3000, 300). Cases are generated separately for: random text (seed 12345 + n, letters a-z, pattern is a random substring of the text), repetitive text with no match (text `a^n`, pattern `a^(m-1)b`), and repetitive text where every window matches (text `a^n`, pattern `a^m`).

Correctness is checked first: for each row the positions returned by the algorithm are compared with a reference list produced by `String.indexOf` in the benchmark harness (the harness only, not the algorithms). All 27 rows are correct.

Timing scope: only the call to `Algorithms.naive/kmp/rk` on an already built input map (including creating the output list). Compilation is excluded. Warm-up: 30 untimed calls per algorithm and input. One reported run is the mean of 20 consecutive calls in microseconds. Three runs per row, and the median of the three is reported. Commands are in the environment section.

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

Observations (medians in microseconds):

1. Random text: all three algorithms stay in the same range (about 38 to 151). Naive is not slower than KMP here. With 26 letters almost every window fails at the first or second comparison, so naive does about one comparison per window, while KMP and RK pay for the prefix table and the hash arithmetic. This is the average case and does not contradict the O(n*m) worst case.
2. Repetitive text, no match (`a^n` against `a^(m-1)b`): this is the worst case for naive, since every window matches `m-1` characters before failing. Naive went from 89.5 to 343.2 to 1478.3 while `n*m` grew 9 times and then 4 times (the first step grew less than 9 times, probably because of fixed overhead). KMP stayed between 8.1 and 45.7 and RK between 23.0 and 95.3. The KMP value 45.7 at the smallest size and the non-monotonic RK values (95.3 at n=1500, 49.9 at n=3000) are consistent within each row but differ between rows, so they are most likely JIT warm-up effects. I did not investigate this further. A longer warm-up or a fresh JVM per row would test it. RK has no equal-hash candidates in this case, because the hashes of `a^m` and `a^(m-1)b` differ by 97 - 98 = -1 mod 101, so RK is linear here.
3. Repetitive text, all match (`a^n` against `a^m`): every window is an equal-hash candidate and is verified with `m` comparisons, so RK falls to the naive bound. RK was about two to two and a half times slower than naive (95.8 vs 47.7, 761.4 vs 360.9 and 3252.3 vs 1310.8), presumably because it also updates the hash for every window. KMP stayed fastest (12.9, 68.0, 74.3), including the cost of emitting z = 451, 1351 and 2701 starts.
4. Modulus 101: on random text about 1 window in 101 is expected to have a false equal hash (roughly n/101 windows). I did not count candidates in the benchmark. Verification rejects them, usually at the first character, so the cost stays small for random text. The all-match row shows the worst case. The known collision `ab` / `dj` is part of the own tests.

Times are wall-clock measurements on a JVM with 2 CPUs and are noisy, so they illustrate the algorithms but do not prove asymptotic bounds. The small modulus is deliberate in this assignment. The remarks above do not describe production hash implementations that use a large modulus or a randomised base.

## Sources and AI assistance

- Sources: assignment README and specification (`docs/guide/CONTRACTS.md`), the provided template and public test cases, Week 8 lecture materials.
- AI assistance: I used Claude (Anthropic). Claude wrote the first version of `Algorithms.java`, the own-test fixtures and runner (`cases.json`, `run_student.py`), the benchmark harness (`Bench.java`) and a draft of this analysis, including the explanations of the algorithms.
- My own verification: I ran the public checks, my own tests and the benchmark myself in GitHub Codespaces. All numbers, outputs and environment details in this file are the real outputs of those runs.