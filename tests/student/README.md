# Student checks

`mixed.jsonl` is a compact manual JSON Lines fixture covering all eight commands and consecutive requests. With Java already compiled, run it with:

```bat
java -Xmx384m -cp build Main < tests\student\mixed.jsonl
```

When Python 3.12+ is available, use the official launcher from the repository root:

```bat
python tools\run.py --check
```

This workspace did not have Python installed. The Node fallback runs all 44 public cases twice in the same Java process, and the deterministic oracle checker exercises small random cases:

```bat
node tests\student\check_public.js
node tests\student\check_random.js
```

Largest-domain smoke cases are in `Stress.java`; compile and run them with:

```bat
javac --release 17 -encoding UTF-8 -cp build -d build tests\student\Stress.java
java -Xms256m -Xmx512m -cp build Stress
```

For the naive/KMP/RK comparison, compile `Benchmark.java` with `Algorithms.java` into `build` and run `java -Xms256m -Xmx384m -cp build Benchmark`. The generator, warm-up policy, raw timings, and timing scope are recorded in `ANALYSIS.md`.
