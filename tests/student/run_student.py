import json, pathlib, subprocess

ROOT = pathlib.Path(__file__).resolve().parents[2]
HERE = pathlib.Path(__file__).resolve().parent

def is_subseq(s, t):
    it = iter(t)
    return all(c in it for c in s)

def check(case, out):
    inp, exp = case["input"], case["output"]
    task = inp["task"]
    if task == "lcs":
        s = out.get("sequence")
        return (out.get("length") == exp["length"] and isinstance(s, str)
                and len(s) == exp["length"] and is_subseq(s, inp["a"]) and is_subseq(s, inp["b"]))
    if task == "merge":
        sizes = inp["sizes"]; n = len(sizes)
        pre = [0]
        for x in sizes: pre.append(pre[-1] + x)
        plan = out.get("plan")
        if out.get("cost") != exp["cost"] or not isinstance(plan, list) or len(plan) != max(n - 1, 0):
            return False
        blocks = {(i, i) for i in range(n)}; total = 0
        for l, k, r in plan:
            if (l, k) not in blocks or (k + 1, r) not in blocks: return False
            blocks -= {(l, k), (k + 1, r)}; blocks.add((l, r)); total += pre[r + 1] - pre[l]
        return total == exp["cost"]
    if task == "tree":
        sel = out.get("selected")
        if out.get("weight") != exp["weight"] or not isinstance(sel, list) or len(set(sel)) != len(sel):
            return False
        chosen = set(sel)
        if any(u in chosen and v in chosen for u, v in inp["edges"]): return False
        return sum(inp["weights"][v] for v in sel) == exp["weight"]
    return out == exp

def short(x, n=80):
    s = json.dumps(x, separators=(",", ":"))
    return s if len(s) <= n else s[:n - 3] + "..."

def expected_text(case):
    t, e = case["input"]["task"], case["output"]
    if t == "lcs": return f"length={e['length']}, any valid LCS"
    if t == "merge": return f"cost={e['cost']}, any valid plan"
    if t == "tree": return f"weight={e['weight']}, any independent set of that weight"
    return short(e)

cases = json.load(open(HERE / "cases.json", encoding="utf-8"))
N = 100000
cases.append({"name": "tree-deep-chain-100000",
              "input": {"task": "tree", "n": N, "weights": [1] * N, "edges": [[i, i + 1] for i in range(N - 1)]},
              "output": {"weight": (N + 1) // 2, "selected": []},
              "checks": "Path of 100000 vertices: recursion would overflow the stack; an iterative DP must not."})

lines = "\n".join(json.dumps(c["input"]) for c in cases) + "\n"
proc = subprocess.run(["java", "-cp", str(ROOT / "Java" / "build"), "Main"],
                      input=lines, capture_output=True, text=True, encoding="utf-8")
outs = [json.loads(l) for l in proc.stdout.splitlines()]
if len(outs) != len(cases):
    print("Expected", len(cases), "result lines, got", len(outs)); print(proc.stderr); raise SystemExit(1)

rows = ["|Command|Input or fixture path|Expected result|Actual result / log|What it checks|", "|---|---|---|---|---|"]
failed = 0
for c, o in zip(cases, outs):
    ok = check(c, o)
    failed += not ok
    print(("PASS " if ok else "FAIL ") + c["name"])
    fixture = "generated in run_student.py" if c["name"] == "tree-deep-chain-100000" else f"tests/student/cases.json: {c['name']}"
    actual = ("PASS: " if ok else "FAIL: ") + (f"weight={o.get('weight')}" if c["name"] == "tree-deep-chain-100000" else short(o))
    rows.append(f"|`python tests/student/run_student.py`|{fixture}|{expected_text(c)}|{actual}|{c['checks']}|")
(HERE / "results.md").write_text("\n".join(rows) + "\n", encoding="utf-8")
print(f"\n{len(cases) - failed}/{len(cases)} passed; table written to tests/student/results.md")