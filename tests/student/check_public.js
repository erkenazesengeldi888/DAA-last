// Node-based fallback for environments without the Python launcher dependency.
const fs = require('fs');
const { spawnSync } = require('child_process');
const cases = JSON.parse(fs.readFileSync('tests/public.json', 'utf8'));
const requests = cases.flatMap(c => [c.input, c.input]);
const run = spawnSync('java', ['-Xmx384m', '-cp', 'build', 'Main'], {
  input: requests.map(x => JSON.stringify(x)).join('\n') + '\n', encoding: 'utf8', timeout: 20000
});
if (run.status !== 0) throw new Error(run.stderr || `Java exit ${run.status}`);
const lines = run.stdout.trimEnd().split(/\r?\n/);
if (lines.length !== requests.length) throw new Error(`expected ${requests.length} lines, got ${lines.length}`);
const answers = lines.map(JSON.parse);
const integer = x => Number.isSafeInteger(x);
const ints = x => Array.isArray(x) && x.every(integer);
const subseq = (s, t) => { let j=0; for(const c of t) if(c===s[j]) j++; return j===s.length; };
function valid(x,y,e) {
  switch(x.task) {
    case 'ping': return y.status==='ready';
    case 'lcs': return integer(y.length)&&typeof y.sequence==='string'&&y.length===e.length&&y.length===y.sequence.length&&subseq(y.sequence,x.a)&&subseq(y.sequence,x.b);
    case 'merge': {
      if(!integer(y.cost)||!Array.isArray(y.plan)) return false;
      let blocks=x.sizes.map((_,i)=>[i,i]),cost=0;
      for(const a of y.plan){if(!ints(a)||a.length!==3)return false;const [l,k,r]=a,j=blocks.findIndex(b=>b[0]===l&&b[1]===k);if(j<0||!blocks[j+1]||blocks[j+1][0]!==k+1||blocks[j+1][1]!==r)return false;blocks.splice(j,2,[l,r]);for(let i=l;i<=r;i++)cost+=x.sizes[i];}
      return y.plan.length===Math.max(0,x.sizes.length-1)&&cost===y.cost&&cost===e.cost&&blocks.length<=1;
    }
    case 'tree': {if(!ints(y.selected)||!integer(y.weight))return false;const s=new Set(y.selected);return s.size===y.selected.length&&[...s].every(v=>v>=0&&v<x.n)&&!x.edges.some(([u,v])=>s.has(u)&&s.has(v))&&[...s].reduce((a,v)=>a+x.weights[v],0)===y.weight&&y.weight===e.weight;}
    case 'naive': case 'kmp': case 'rk': return JSON.stringify(y.positions)===JSON.stringify(e.positions);
    case 'suffix': {const s=y.repeated;return ints(y.sa)&&ints(y.lcp)&&typeof s==='string'&&JSON.stringify(y.sa)===JSON.stringify(e.sa)&&JSON.stringify(y.lcp)===JSON.stringify(e.lcp)&&s.length===e.repeated.length&&(!s||Array.from({length:x.text.length},(_,i)=>x.text.startsWith(s,i)).filter(Boolean).length>=2);}
    case 'aho': return Array.isArray(y.counts)&&JSON.stringify(y.counts)===JSON.stringify(e.counts);
    default:return false;
  }
}
let failures=0;
for(let i=0;i<requests.length;i++){const c=cases[Math.floor(i/2)];if(!valid(c.input,answers[i],c.output)){console.error(`FAIL ${i+1}: ${c.name||c.input.task}`,answers[i]);failures++;}}
console.log(`${failures?'FAIL':'PASS'} public cases and repeated mixed batch: ${cases.length} cases, ${requests.length} requests`);
process.exitCode=failures?1:0;
