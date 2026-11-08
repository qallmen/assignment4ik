import java.util.*;

/**
 * Assignment 4 algorithms. Each method is independent and keeps no state between calls.
 * Call these from the template's TODO methods (the template handles the JSON Lines I/O).
 */
public final class Algorithms {
    private Algorithms() {}

    // ---------------------------------------------------------------- Part 1: LCS
    public record LcsResult(int length, String sequence) {}

    /** dp[i][j] = LCS length of a[0..i) and b[0..j). O(|a||b|) time and memory. */
    public static LcsResult lcs(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1]; // row/col 0 stay 0: empty prefix
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1] + 1;
                else dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
        StringBuilder sb = new StringBuilder();
        int i = n, j = m;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) { sb.append(a.charAt(i - 1)); i--; j--; }
            else if (dp[i - 1][j] >= dp[i][j - 1]) i--;
            else j--;
        }
        return new LcsResult(dp[n][m], sb.reverse().toString());
    }

    // ---------------------------------------------------------------- Part 2: ordered merge
    public record MergeResult(long cost, List<int[]> plan) {}

    /** Interval DP: cost[l][r] = min over split k of cost[l][k] + cost[k+1][r] + sum(l..r). O(n^3). */
    public static MergeResult merge(long[] sizes) {
        int n = sizes.length;
        List<int[]> plan = new ArrayList<>();
        if (n <= 1) return new MergeResult(0, plan);
        long[] pre = new long[n + 1];
        for (int i = 0; i < n; i++) pre[i + 1] = pre[i] + sizes[i];
        long[][] cost = new long[n][n];
        int[][] split = new int[n][n];
        for (int len = 2; len <= n; len++) {
            for (int l = 0; l + len - 1 < n; l++) {
                int r = l + len - 1;
                long best = Long.MAX_VALUE;
                int bestK = l;
                for (int k = l; k < r; k++) {
                    long c = cost[l][k] + cost[k + 1][r];
                    if (c < best) { best = c; bestK = k; }
                }
                cost[l][r] = best + (pre[r + 1] - pre[l]);
                split[l][r] = bestK;
            }
        }
        emitPlan(0, n - 1, split, plan);
        return new MergeResult(cost[0][n - 1], plan);
    }

    // post-order: both children are merged before the parent merge. Depth <= n <= 200.
    private static void emitPlan(int l, int r, int[][] split, List<int[]> out) {
        if (l == r) return;
        int k = split[l][r];
        emitPlan(l, k, split, out);
        emitPlan(k + 1, r, split, out);
        out.add(new int[] {l, k, r});
    }

    // ---------------------------------------------------------------- Part 3: tree independent set
    public record TreeResult(long weight, int[] selected) {}

    /** take/skip DP, fully iterative (no recursion, so a 100000-vertex chain is fine). */
    public static TreeResult tree(int n, long[] w, int[][] edges) {
        if (n == 0) return new TreeResult(0, new int[0]);
        int[] head = new int[n], next = new int[2 * (n - 1)], to = new int[2 * (n - 1)];
        Arrays.fill(head, -1);
        int ec = 0;
        for (int[] e : edges) {
            to[ec] = e[1]; next[ec] = head[e[0]]; head[e[0]] = ec++;
            to[ec] = e[0]; next[ec] = head[e[1]]; head[e[1]] = ec++;
        }
        int[] parent = new int[n], order = new int[n];
        Arrays.fill(parent, -1);
        int qh = 0, qt = 0;
        order[qt++] = 0;
        parent[0] = 0; // root marks itself so it is never re-added
        while (qh < qt) {
            int v = order[qh++];
            for (int e = head[v]; e != -1; e = next[e]) {
                int u = to[e];
                if (parent[u] == -1) { parent[u] = v; order[qt++] = u; }
            }
        }
        long[] take = new long[n], skip = new long[n];
        for (int i = n - 1; i >= 0; i--) { // children are processed before their parent
            int v = order[i];
            take[v] += w[v];
            if (i > 0) {
                int p = parent[v];
                take[p] += skip[v];
                skip[p] += Math.max(take[v], skip[v]);
            }
        }
        boolean[] chosen = new boolean[n];
        int count = 0;
        for (int i = 0; i < n; i++) { // parents are decided before their children
            int v = order[i];
            boolean parentTaken = i > 0 && chosen[parent[v]];
            chosen[v] = !parentTaken && take[v] > skip[v];
            if (chosen[v]) count++;
        }
        int[] sel = new int[count];
        int idx = 0;
        for (int v = 0; v < n; v++) if (chosen[v]) sel[idx++] = v;
        return new TreeResult(Math.max(take[0], skip[0]), sel);
    }

    // ---------------------------------------------------------------- Part 4: exact search
    private static int[] toArray(List<Integer> list) {
        int[] r = new int[list.size()];
        for (int i = 0; i < r.length; i++) r[i] = list.get(i);
        return r;
    }

    private static int[] allBoundaries(int n) {
        int[] r = new int[n + 1];
        for (int i = 0; i <= n; i++) r[i] = i;
        return r;
    }

    public static int[] naive(String text, String pattern) {
        int n = text.length(), m = pattern.length();
        if (m == 0) return allBoundaries(n);
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i + m <= n; i++) { // last valid start is n - m
            int j = 0;
            while (j < m && text.charAt(i + j) == pattern.charAt(j)) j++;
            if (j == m) res.add(i);
        }
        return toArray(res);
    }

    /** pi[i] = length of the longest proper prefix of pattern[0..i] that is also its suffix. */
    private static int[] prefixFunction(String p) {
        int m = p.length();
        int[] pi = new int[m];
        for (int i = 1, k = 0; i < m; i++) {
            while (k > 0 && p.charAt(i) != p.charAt(k)) k = pi[k - 1];
            if (p.charAt(i) == p.charAt(k)) k++;
            pi[i] = k;
        }
        return pi;
    }

    public static int[] kmp(String text, String pattern) {
        int n = text.length(), m = pattern.length();
        if (m == 0) return allBoundaries(n);
        if (m > n) return new int[0];
        int[] pi = prefixFunction(pattern);
        List<Integer> res = new ArrayList<>();
        for (int i = 0, k = 0; i < n; i++) {
            while (k > 0 && text.charAt(i) != pattern.charAt(k)) k = pi[k - 1];
            if (text.charAt(i) == pattern.charAt(k)) k++;
            if (k == m) {
                res.add(i - m + 1);
                k = pi[k - 1]; // fall back so overlapping matches are kept
            }
        }
        return toArray(res);
    }

    private static final int RK_BASE = 31, RK_MOD = 101;

    /** Rolling hash result: positions plus (optional) number of equal-hash candidate windows. */
    public static int[] rk(String text, String pattern) { return rk(text, pattern, null); }

    /** If stats != null, stats[0] receives the number of hash-equal candidate windows (for ANALYSIS.md). */
    public static int[] rk(String text, String pattern, long[] stats) {
        int n = text.length(), m = pattern.length();
        if (m == 0) return allBoundaries(n);
        if (m > n) return new int[0];
        int pow = 1; // BASE^(m-1) mod MOD: weight of the leftmost character
        for (int i = 1; i < m; i++) pow = pow * RK_BASE % RK_MOD;
        int hp = 0, ht = 0;
        for (int i = 0; i < m; i++) {
            hp = (hp * RK_BASE + pattern.charAt(i)) % RK_MOD;
            ht = (ht * RK_BASE + text.charAt(i)) % RK_MOD;
        }
        List<Integer> res = new ArrayList<>();
        long candidates = 0;
        for (int i = 0; ; i++) {
            if (hp == ht) {
                candidates++;
                int j = 0; // equal hashes are only a candidate: verify real characters
                while (j < m && text.charAt(i + j) == pattern.charAt(j)) j++;
                if (j == m) res.add(i);
            }
            if (i + m >= n) break;
            ht = (ht - text.charAt(i) * pow % RK_MOD + RK_MOD) % RK_MOD; // drop leftmost
            ht = (ht * RK_BASE + text.charAt(i + m)) % RK_MOD;           // add new rightmost
        }
        if (stats != null) stats[0] = candidates;
        return toArray(res);
    }

    // ---------------------------------------------------------------- Part 5: suffix array + Kasai
    public record SuffixResult(int[] sa, int[] lcp, String repeated) {}

    public static SuffixResult suffix(String s) {
        int n = s.length();
        if (n == 0) return new SuffixResult(new int[0], new int[0], "");
        int[] sa = buildSuffixArray(s);
        int[] lcp = kasai(s, sa);
        int best = 0, bestPos = 0;
        for (int i = 1; i < n; i++) if (lcp[i] > best) { best = lcp[i]; bestPos = i; }
        String rep = best == 0 ? "" : s.substring(sa[bestPos], sa[bestPos] + best);
        return new SuffixResult(sa, lcp, rep);
    }

    /** Prefix doubling: rank by first k chars -> by first 2k chars, sorting (rank[i], rank[i+k]) pairs. */
    static int[] buildSuffixArray(String s) {
        int n = s.length();
        int[] sa = new int[n], rank = new int[n], tmp = new int[n];
        long[] keys = new long[n];
        for (int i = 0; i < n; i++) rank[i] = s.charAt(i);
        for (int k = 1; ; k <<= 1) {
            long mod = 0;
            for (int r : rank) mod = Math.max(mod, r);
            mod += 2;
            for (int i = 0; i < n; i++) {
                long second = i + k < n ? rank[i + k] + 1 : 0; // 0 = "past the end" sorts first
                keys[i] = ((rank[i] * mod + second) << 16) | i; // n <= 50000 < 2^16
            }
            Arrays.sort(keys);
            for (int j = 0; j < n; j++) sa[j] = (int) (keys[j] & 0xFFFF);
            tmp[sa[0]] = 0;
            for (int j = 1; j < n; j++) {
                boolean diff = (keys[j] >> 16) != (keys[j - 1] >> 16);
                tmp[sa[j]] = tmp[sa[j - 1]] + (diff ? 1 : 0);
            }
            System.arraycopy(tmp, 0, rank, 0, n);
            if (rank[sa[n - 1]] == n - 1 || k >= n) break; // all ranks distinct
        }
        return sa;
    }

    /** Kasai: lcp[i] = LCP(suffix sa[i-1], suffix sa[i]); lcp[0] = 0. Reuses h-1 when moving to i+1. */
    static int[] kasai(String s, int[] sa) {
        int n = s.length();
        int[] rank = new int[n], lcp = new int[n];
        for (int i = 0; i < n; i++) rank[sa[i]] = i;
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1];
                while (i + h < n && j + h < n && s.charAt(i + h) == s.charAt(j + h)) h++;
                lcp[rank[i]] = h;
                if (h > 0) h--;
            } else {
                h = 0;
            }
        }
        return lcp;
    }

    // ---------------------------------------------------------------- Part 6: Aho-Corasick
    private static final int ALPHA = 95; // printable ASCII 32..126

    /** counts[t][p] = number of (overlapping) occurrences of patterns[p] in texts[t]. */
    public static long[][] aho(String[] patterns, String[] texts) {
        int p = patterns.length;
        int maxStates = 1;
        for (String s : patterns) maxStates += s.length();
        int[] go = new int[maxStates * ALPHA];
        Arrays.fill(go, -1);
        int states = 1;
        int[] term = new int[p]; // terminal state of each dictionary position (duplicates share one)
        for (int i = 0; i < p; i++) {
            int v = 0;
            for (int c = 0; c < patterns[i].length(); c++) {
                int ch = patterns[i].charAt(c) - 32;
                if (go[v * ALPHA + ch] == -1) go[v * ALPHA + ch] = states++;
                v = go[v * ALPHA + ch];
            }
            term[i] = v;
        }
        // BFS: failure links and full automaton transitions
        int[] fail = new int[states], order = new int[states];
        int qh = 0, qt = 0;
        order[qt++] = 0;
        while (qh < qt) {
            int v = order[qh++];
            for (int ch = 0; ch < ALPHA; ch++) {
                int u = go[v * ALPHA + ch];
                if (u != -1) {
                    fail[u] = v == 0 ? 0 : go[fail[v] * ALPHA + ch];
                    order[qt++] = u;
                } else {
                    go[v * ALPHA + ch] = v == 0 ? 0 : go[fail[v] * ALPHA + ch];
                }
            }
        }
        long[][] counts = new long[texts.length][p];
        long[] visits = new long[states];
        for (int t = 0; t < texts.length; t++) {
            Arrays.fill(visits, 0); // independent text: fresh state and counts
            int v = 0;
            String s = texts[t];
            for (int i = 0; i < s.length(); i++) {
                v = go[v * ALPHA + (s.charAt(i) - 32)];
                visits[v]++;
            }
            // push visit counts up the failure tree: deeper states first (reverse BFS order)
            for (int i = states - 1; i > 0; i--) visits[fail[order[i]]] += visits[order[i]];
            for (int i = 0; i < p; i++) counts[t][i] = visits[term[i]];
        }
        return counts;
    }

    // ============ Adapters called by Main.java: JSON map in, JSON-writable object out ============
    @SuppressWarnings("unchecked")
    private static long[] longs(Object o) {
        List<Object> l = (List<Object>) o;
        long[] r = new long[l.size()];
        for (int i = 0; i < r.length; i++) r[i] = ((Number) l.get(i)).longValue();
        return r;
    }

    @SuppressWarnings("unchecked")
    private static String[] strings(Object o) {
        List<Object> l = (List<Object>) o;
        String[] r = new String[l.size()];
        for (int i = 0; i < r.length; i++) r[i] = (String) l.get(i);
        return r;
    }

    public static Object lcs(Map<String, Object> x) {
        LcsResult r = lcs((String) x.get("a"), (String) x.get("b"));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("length", r.length());
        out.put("sequence", r.sequence());
        return out;
    }

    public static Object merge(Map<String, Object> x) {
        MergeResult r = merge(longs(x.get("sizes")));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("cost", r.cost());
        out.put("plan", r.plan());
        return out;
    }

    @SuppressWarnings("unchecked")
    public static Object tree(Map<String, Object> x) {
        int n = ((Number) x.get("n")).intValue();
        long[] w = longs(x.get("weights"));
        List<Object> el = (List<Object>) x.get("edges");
        int[][] edges = new int[el.size()][];
        for (int i = 0; i < edges.length; i++) {
            List<Object> e = (List<Object>) el.get(i);
            edges[i] = new int[] {((Number) e.get(0)).intValue(), ((Number) e.get(1)).intValue()};
        }
        TreeResult r = tree(n, w, edges);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("weight", r.weight());
        out.put("selected", r.selected());
        return out;
    }

    private static Object positions(int[] pos) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("positions", pos);
        return out;
    }

    public static Object naive(Map<String, Object> x) {
        return positions(naive((String) x.get("text"), (String) x.get("pattern")));
    }

    public static Object kmp(Map<String, Object> x) {
        return positions(kmp((String) x.get("text"), (String) x.get("pattern")));
    }

    public static Object rk(Map<String, Object> x) {
        return positions(rk((String) x.get("text"), (String) x.get("pattern")));
    }

    public static Object suffix(Map<String, Object> x) {
        SuffixResult r = suffix((String) x.get("text"));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sa", r.sa());
        out.put("lcp", r.lcp());
        out.put("repeated", r.repeated());
        return out;
    }

    public static Object aho(Map<String, Object> x) {
        long[][] counts = aho(strings(x.get("patterns")), strings(x.get("texts")));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("counts", counts);
        return out;
    }
}
