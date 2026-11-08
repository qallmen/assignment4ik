import java.util.*;

public class Bench {
    interface Algo { Map<String, Object> run(Map<String, Object> in); }
    static final int WARMUP = 30, RUNS = 3, REPS = 20;
    static long sink = 0;

    static List<Long> oracle(String t, String p) {
        List<Long> r = new ArrayList<>();
        for (int i = t.indexOf(p); i >= 0; i = t.indexOf(p, i + 1)) r.add((long) i);
        return r;
    }

    static String[] gen(String kind, int n, int m) {
        if (kind.equals("random")) {
            Random rnd = new Random(12345L + n);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append((char) ('a' + rnd.nextInt(26)));
            String t = sb.toString();
            int s = rnd.nextInt(n - m + 1);
            return new String[]{t, t.substring(s, s + m)};
        }
        String t = "a".repeat(n);
        if (kind.equals("repetitive-allmatch")) return new String[]{t, "a".repeat(m)};
        return new String[]{t, "a".repeat(m - 1) + "b"};
    }

    static String describe(String kind, int n) {
        if (kind.equals("random")) return "Random(12345+" + n + "), letters a-z, pattern = random substring of text";
        if (kind.equals("repetitive-allmatch")) return "text a^n, pattern a^m";
        return "text a^n, pattern a^(m-1)b";
    }

    public static void main(String[] args) {
        String[] kinds = {"random", "repetitive-nomatch", "repetitive-allmatch"};
        int[][] sizes = {{500, 50}, {1500, 150}, {3000, 300}};
        String[] names = {"naive", "kmp", "rk"};
        Algo[] algos = {Algorithms::naive, Algorithms::kmp, Algorithms::rk};
        System.out.println("|Case|n|m|Seed/generator|Algorithm|Run 1 (us)|Run 2 (us)|Run 3 (us)|Median (us)|Correct outputs checked|");
        System.out.println("|---|---:|---:|---|---|---:|---:|---:|---:|---|");
        for (String kind : kinds) {
            for (int[] sz : sizes) {
                int n = sz[0], m = sz[1];
                String[] tp = gen(kind, n, m);
                Map<String, Object> in = new LinkedHashMap<>();
                in.put("text", tp[0]);
                in.put("pattern", tp[1]);
                List<Long> exp = oracle(tp[0], tp[1]);
                for (int k = 0; k < 3; k++) {
                    Algo al = algos[k];
                    boolean ok = exp.equals(al.run(in).get("positions"));
                    for (int w = 0; w < WARMUP; w++) sink += al.run(in).size();
                    double[] t = new double[RUNS];
                    for (int r = 0; r < RUNS; r++) {
                        long s0 = System.nanoTime();
                        for (int q = 0; q < REPS; q++) sink += al.run(in).size();
                        t[r] = (System.nanoTime() - s0) / 1000.0 / REPS;
                    }
                    double[] sorted = t.clone();
                    Arrays.sort(sorted);
                    System.out.println(String.format(Locale.ROOT, "|%s|%d|%d|%s|%s|%.1f|%.1f|%.1f|%.1f|%s|",
                            kind, n, m, describe(kind, n), names[k], t[0], t[1], t[2], sorted[1],
                            ok ? "yes (" + exp.size() + " starts, equals String.indexOf oracle)" : "NO"));
                }
            }
        }
        if (sink == 42) System.out.println();
    }
}