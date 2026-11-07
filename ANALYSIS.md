# Assignment 4 — analysis

## Environment and commands

- Selected language: Java. The source compiles with `javac --release 17`; the available compiler/runtime reported Java 26.
- Environment used: Windows 11 (build 26200), Intel64 Family 6 Model 186, 8 logical processors. Available RAM was not recorded.
- Build command: `javac --release 17 -encoding UTF-8 -d build Java\\source\\*.java`.
- Launch command: `java -Xmx384m -cp build Main` (one UTF-8 JSON request per line; one JSON result per line).
- The prescribed `python tools/run.py --setup` and `python tools/run.py --check` commands were executed successfully. The setup completed, and all 44 public tests passed, including the state-reset check. Additional student tests were also executed successfully.
- Comparison commands: `javac --release 17 -encoding UTF-8 -cp build -d build Java\\source\\Algorithms.java tests\\student\\Benchmark.java`, then `java -Xms256m -Xmx384m -cp build Benchmark`. It uses seed `20261106+n`, text lengths 600/1200/2400 and pattern length `n/4`. Random text uses Java `Random` over lowercase `a`–`z`; its pattern is a substring at `seed % (n-m+1)`. Repetitive cases use all `a`.

## Algorithm explanations

### LCS

`d[i][j]` is the LCS length of prefixes `a[0..i)` and `b[0..j)`. Row 0 and column 0 are zero. Equal final characters extend `d[i-1][j-1]` by one; otherwise the best solution drops one final character, so the cell is the maximum of `d[i-1][j]` and `d[i][j-1]`. Increasing prefix lengths ensure both dependencies are ready. Reconstruction walks backward: equal characters are kept and both indices decrease; on a mismatch it follows a neighbor with the same optimum value (ties choose the `a` prefix), then reverses the collected characters. This yields an optimal common subsequence, including the empty answer. Time and auxiliary memory are `O((|a|+1)(|b|+1))`; the answer uses `O(L)` output space.

### Ordered adjacent merge

`d[l][r]` is the minimum cost to merge the original contiguous interval `[l,r]` to one block. A one-element interval costs zero. For each longer interval, every possible final split `k` is considered: the two subinterval optima plus the interval sum (the final merge cost). Prefix sums give each interval sum in constant time. Increasing interval length computes child states first. The chosen split is stored; an explicit stack emits the left subtree, right subtree, then parent merge, so every operation applies to two currently adjacent blocks. Ties keep the first minimizing split. Time is `O(n^3)` and auxiliary memory `O(n^2)`; output plan space is `O(n)`.

### Tree independent set

For each vertex `u`, `take[u]` is the best weight in its rooted subtree if `u` is selected, and `skip[u]` is the best if it is omitted. The recurrences are `weight[u] + sum(skip[child])` and `sum(max(take[child],skip[child]))`. Rooting and traversal are iterative; reverse traversal order is a postorder, so children are complete before their parent. Reconstruction uses a stack carrying whether the parent was selected. A selected parent forces a child to skip; otherwise the larger child state is used, with ties choosing skip. This naturally allows the empty set for negative weights. Time and auxiliary space are `O(n)` including the returned selected vertices; adjacency and output storage are linear.

### Naive search

For each candidate start `0..n-m`, compare characters left to right and report the start only if all `m` match. The next start advances by one, preserving overlaps. An empty pattern is handled first and returns all `n+1` boundaries; `m>n` has no candidate. Time is `O(nm+n+1)` including emitted results, output memory `O(z+1)`, and auxiliary working memory `O(1)`.

### KMP

`pi[i]` is the length of the longest proper prefix of `pattern[0..i]` that is also its suffix. The prefix table is built left to right: after a mismatch, repeatedly fall back to `pi[j-1]`; a matching next character extends the border. The text scan applies the same fallback without rescanning already matched text. At a full match it reports the start and falls back to `pi[m-1]`, retaining possible overlaps. Empty pattern is handled before indexing. Time is `O(n+m+z+1)`, prefix-table auxiliary memory `O(m)`, output memory `O(z+1)`.

### Rabin–Karp

The pattern and first window use polynomial hashes with base 31 and modulus 101, folding ASCII codes left to right. The highest power `31^(m-1)` is removed from the outgoing character; the remaining value is multiplied by 31 and the incoming code is added. Remainders are normalized to `[0,100]`. A matching fingerprint is only a candidate: all `m` characters are checked before reporting. This is why the deliberate `ab`/`dj` collision returns only the true match. Empty and too-long patterns are handled before hashing. Time is `O(n+m+c*m)` for `c` equal-hash candidates (worst case `O(nm+n+m+1)`); working memory is `O(1)` beyond the output list, which is `O(z+1)`.

### Suffix array and Kasai LCP

Prefix doubling starts with character ranks and sorts suffix indices by `(rank[i], rank[i+k])`, using `-1` when the second half is past the text. It assigns new equivalence ranks until all suffixes are distinct; it never constructs suffix strings and includes no empty suffix. Comparison sorting gives `O(n log^2(n+1))` time and `O(n)` auxiliary/output memory. Kasai records each suffix's inverse rank and compares it to the preceding suffix in sorted order. The carried common-prefix length decreases by at most one between adjacent text positions; it is reset when the current suffix is first in suffix order. The maximum adjacent LCP is the longest repeated substring because any repeated substring's two suffixes have a common-prefix interval whose adjacent suffixes share at least that prefix. The reported substring is cut from one matching start. Kasai takes `O(n)` time and `O(n)` memory. Ties select the first maximum in text-index iteration.

### Aho–Corasick

Patterns share trie prefixes; duplicate dictionary entries share a terminal trie state but retain separate dictionary IDs. Breadth-first construction sets each state's failure link to the longest proper suffix that is also a trie prefix, while completing all 95 printable-ASCII transitions from the failure state. The text scan then follows one transition per character. Each reached state increments a visit count. Processing states in reverse BFS order propagates visits to failure parents, adding suffix-pattern occurrences without walking every output for every character. Terminal visit counts are copied to each original pattern ID, so duplicates receive equal counts. Visit arrays are fresh per text, which counts overlaps and resets independent queries. Empty dictionaries preserve one empty row per text. Construction takes `O(L+95S)` time and `O(95S+L+p)` memory for the dense transition table, trie construction and terminal IDs. Per text, scanning and failure propagation take `O(|text|+S+p)` time; output is `O(p)` per row.

## Own tests

|Input / command|Expected output|Actual output|Defect targeted|
|---|---|---|---|
|`{"task":"lcs","a":"","b":"abc"}`|`{"length":0,"sequence":""}`|Same; also present in `mixed.jsonl` output.|Empty prefix base case.|
|`{"task":"merge","sizes":[2,3,4]}`|`{"cost":14,"plan":[[0,0,1],[0,1,2]]}`|Same.|Adjacent merges and chronological child-before-parent plan.|
|`{"task":"tree","n":5,"weights":[-3,0,5,5,5],"edges":[[0,1],[1,2],[2,3],[3,4]]}`|Optimal weight 10; selected `[2,4]` is one valid witness.|`{"weight":10,"selected":[2,4]}`|Nonpositive weights, ties and adjacency restriction.|
|`{"task":"naive","text":"aaaaa","pattern":"aaa"}`|`{"positions":[0,1,2]}`|Same.|Overlapping matches.|
|`{"task":"kmp","text":"abc","pattern":""}`|`{"positions":[0,1,2,3]}`|Same.|Empty pattern boundary count.|
|`{"task":"rk","text":"abdj","pattern":"ab"}`|`{"positions":[0]}`|Same.|Hash collision must be verified character by character.|
|`{"task":"suffix","text":"banana"}`|`sa=[5,3,1,0,4,2]`, `lcp=[0,1,3,0,0,2]`, repeated length 3.|Same arrays and `repeated="ana"`.|No sentinel; overlap allowed; Kasai order.|
|`{"task":"aho","patterns":["he","she","he"],"texts":["sheshe","sheshe",""]}`|`{"counts":[[2,2,2],[2,2,2],[0,0,0]]}`|Same.|Suffix matches, duplicate IDs and fresh counts per text.|
|`node tests/student/check_public.js`|44 published cases plus a second full batch in the same Java process.|`PASS public cases and repeated mixed batch: 44 cases, 88 requests`|Published examples/boundaries, output schema, task mixing and request-state reset.|
|`node tests/student/check_random.js` (seed `0x4a2026`)|Independent brute-force checks across all commands.|`PASS deterministic brute-force checks: 260 requests (seed 0x4a2026)`|Small randomized edge combinations and independent-oracle agreement.|
|`java -Xms256m -Xmx512m -cp build Stress`|Boundary sizes: LCS 1000×1000, merge 200, tree 100000, KMP 200000/100000, RK 10000/2000, suffix 50000, Aho pattern length 100000 and 100 texts.|`PASS boundary-size smoke checks` (exit code 0)|Published maxima and nonrecursive handling of the deep tree.|

The local randomized checker has small domains by design. The boundary smoke checks exercise the largest individual limits. The public `--check` launcher could not be invoked here because Python is absent; the official grader may use additional combinations and time limits.

## Search comparison

Timing scope is one call to the selected algorithm method, including its result-list allocation and output positions, but excluding JSON parsing, request construction and JVM startup. All outputs were compared for equality before timing and checked again after every timed call. Two unmeasured warm-up calls per algorithm/case were used; three measured calls followed in seeded shuffled method order. Times below are nanoseconds from `System.nanoTime`; medians are the middle of the three. Runs used the available Java 26 runtime with `-Xms256m -Xmx384m` on the Windows environment described above. The small, shared-runtime sample has visible JVM/OS noise, so it is descriptive only.

|Case|n|m|Seed|Algorithm|Run 1|Run 2|Run 3|Median|Outputs checked|
|---|---:|---:|---:|---|---:|---:|---:|---:|---|
|Random|600|150|20261706|naive|88,300|86,500|90,500|88,300|equal|
|Random|600|150|20261706|KMP|121,000|140,500|119,400|121,000|equal|
|Random|600|150|20261706|RK|144,600|144,000|144,400|144,400|equal|
|Repetitive|600|150|20261706|naive|727,700|669,200|565,600|669,200|equal|
|Repetitive|600|150|20261706|KMP|452,300|400,400|258,800|400,400|equal|
|Repetitive|600|150|20261706|RK|2,793,900|5,636,500|2,183,600|2,793,900|equal|
|Random|1,200|300|20262306|naive|40,700|55,700|127,700|55,700|equal|
|Random|1,200|300|20262306|KMP|255,000|324,400|273,300|273,300|equal|
|Random|1,200|300|20262306|RK|166,800|89,500|95,300|95,300|equal|
|Repetitive|1,200|300|20262306|naive|1,173,300|2,960,900|1,918,400|1,918,400|equal|
|Repetitive|1,200|300|20262306|KMP|716,200|828,100|511,700|716,200|equal|
|Repetitive|1,200|300|20262306|RK|1,939,200|1,732,200|2,546,000|1,939,200|equal|
|Random|2,400|600|20263506|naive|19,900|100,200|15,900|19,900|equal|
|Random|2,400|600|20263506|KMP|430,000|398,000|360,500|398,000|equal|
|Random|2,400|600|20263506|RK|122,400|239,200|226,300|226,300|equal|
|Repetitive|2,400|600|20263506|naive|6,430,300|3,458,900|3,863,800|3,863,800|equal|
|Repetitive|2,400|600|20263506|KMP|960,600|1,194,500|762,900|960,600|equal|
|Repetitive|2,400|600|20263506|RK|3,994,600|3,506,400|4,203,400|3,994,600|equal|

Random cases had one guaranteed occurrence because the pattern was drawn from the text; KMP and RK avoid most character-by-character work there, while timings at these sizes are noisy. Repetitive inputs create many matches and overlapping output positions. Naive and KMP have predictable scans, while RK's modulus 101 creates many candidate collisions and exact verification can dominate. This small-modulus exercise is intentionally collision-prone and does not characterize production hash choices. Timing data does not prove asymptotic bounds.

## Contribution and materials

I implemented the eight required algorithms in `Java/source/Algorithms.java`, added deterministic checks and a search benchmark under `tests/student`, and completed this analysis from the published contracts and rubric. No external algorithm libraries or built-in substring-search routines are used. The local verification performed is recorded above; no Python launcher result is claimed.
