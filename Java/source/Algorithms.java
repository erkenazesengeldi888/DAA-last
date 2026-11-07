import java.util.*;

/** Implementations for the eight JSONL assignment commands. */
public final class Algorithms {
    private Algorithms() {}
    private static Map<String,Object> out(Object... kv) {
        Map<String,Object> m = new LinkedHashMap<>();
        for (int i=0;i<kv.length;i+=2) m.put((String)kv[i], kv[i+1]);
        return m;
    }
    private static String str(Map<String,Object> x,String k){return (String)x.get(k);}
    private static int num(Object x){return ((Number)x).intValue();}
    private static long lng(Object x){return ((Number)x).longValue();}

    static Map<String,Object> lcs(Map<String,Object> in) {
        String a=str(in,"a"), b=str(in,"b"); int n=a.length(),m=b.length();
        int[][] d=new int[n+1][m+1];
        for(int i=1;i<=n;i++) for(int j=1;j<=m;j++)
            d[i][j]=a.charAt(i-1)==b.charAt(j-1)?d[i-1][j-1]+1:Math.max(d[i-1][j],d[i][j-1]);
        StringBuilder s=new StringBuilder(); int i=n,j=m;
        while(i>0&&j>0) {
            if(a.charAt(i-1)==b.charAt(j-1)){s.append(a.charAt(i-1));i--;j--;}
            else if(d[i-1][j]>=d[i][j-1]) i--; else j--;
        }
        return out("length",d[n][m],"sequence",s.reverse().toString());
    }

    static Map<String,Object> merge(Map<String,Object> in) {
        List<?> raw=(List<?>)in.get("sizes"); int n=raw.size();
        if(n==0)return out("cost",0L,"plan",new ArrayList<int[]>());
        long[] p=new long[n+1]; for(int i=0;i<n;i++)p[i+1]=p[i]+lng(raw.get(i));
        long[][] d=new long[n][n]; int[][] split=new int[n][n];
        for(int len=2;len<=n;len++) for(int l=0;l+len<=n;l++){
            int r=l+len-1; long best=Long.MAX_VALUE; int bk=l;
            for(int k=l;k<r;k++){long v=d[l][k]+d[k+1][r]+p[r+1]-p[l];if(v<best){best=v;bk=k;}}
            d[l][r]=best;split[l][r]=bk;
        }
        List<int[]> plan=new ArrayList<>();
        if(n>1){ArrayDeque<int[]> st=new ArrayDeque<>();st.push(new int[]{0,n-1,0});
            while(!st.isEmpty()){int[] f=st.pop();int l=f[0],r=f[1];if(l==r)continue;
                if(f[2]==1){plan.add(new int[]{l,split[l][r],r});continue;}
                int k=split[l][r];st.push(new int[]{l,r,1});st.push(new int[]{k+1,r,0});st.push(new int[]{l,k,0});
            }
        }
        return out("cost",d[0][n-1],"plan",plan);
    }

    static Map<String,Object> tree(Map<String,Object> in) {
        int n=num(in.get("n")); List<?> w=(List<?>)in.get("weights"), edges=(List<?>)in.get("edges");
        if(n==0)return out("weight",0L,"selected",new ArrayList<Integer>());
        @SuppressWarnings("unchecked") List<Integer>[] g=new List[n];for(int i=0;i<n;i++)g[i]=new ArrayList<>();
        for(Object e:edges){List<?> a=(List<?>)e;int u=num(a.get(0)),v=num(a.get(1));g[u].add(v);g[v].add(u);}
        int[] par=new int[n],order=new int[n];Arrays.fill(par,-2);par[0]=-1;int sz=0;int[] stack=new int[n];stack[sz++]=0;
        for(int q=0;q<sz;q++){int u=stack[q];order[q]=u;for(int v:g[u])if(v!=par[u]){par[v]=u;stack[sz++]=v;}}
        long[] take=new long[n],skip=new long[n];
        for(int z=n-1;z>=0;z--){int u=order[z];take[u]=lng(w.get(u));for(int v:g[u])if(par[v]==u){take[u]+=skip[v];skip[u]+=Math.max(take[v],skip[v]);}}
        List<Integer> chosen=new ArrayList<>();ArrayDeque<int[]> todo=new ArrayDeque<>();todo.push(new int[]{0,0});long best=Math.max(take[0],skip[0]);
        while(!todo.isEmpty()){int[] f=todo.pop();int u=f[0];boolean forceSkip=f[1]!=0;boolean pick=!forceSkip&&take[u]>skip[u];if(pick)chosen.add(u);
            for(int v:g[u])if(par[v]==u)todo.push(new int[]{v,pick?1:0});}
        return out("weight",best,"selected",chosen);
    }

    static Map<String,Object> naive(Map<String,Object> in){return out("positions",naivePositions(str(in,"text"),str(in,"pattern")));}
    private static List<Integer> naivePositions(String t,String p){int n=t.length(),m=p.length();List<Integer> a=new ArrayList<>();if(m==0){for(int i=0;i<=n;i++)a.add(i);return a;}for(int i=0;i+m<=n;i++){int j=0;while(j<m&&t.charAt(i+j)==p.charAt(j))j++;if(j==m)a.add(i);}return a;}
    static Map<String,Object> kmp(Map<String,Object> in){return out("positions",kmpPositions(str(in,"text"),str(in,"pattern")));}
    private static List<Integer> kmpPositions(String t,String p){int n=t.length(),m=p.length();List<Integer> a=new ArrayList<>();if(m==0){for(int i=0;i<=n;i++)a.add(i);return a;}int[] pi=new int[m];for(int i=1,j=0;i<m;i++){while(j>0&&p.charAt(i)!=p.charAt(j))j=pi[j-1];if(p.charAt(i)==p.charAt(j))j++;pi[i]=j;}for(int i=0,j=0;i<n;i++){while(j>0&&t.charAt(i)!=p.charAt(j))j=pi[j-1];if(t.charAt(i)==p.charAt(j))j++;if(j==m){a.add(i-m+1);j=pi[j-1];}}return a;}
    static Map<String,Object> rk(Map<String,Object> in){return out("positions",rkPositions(str(in,"text"),str(in,"pattern")));}
    private static List<Integer> rkPositions(String t,String p){int n=t.length(),m=p.length();List<Integer>a=new ArrayList<>();if(m==0){for(int i=0;i<=n;i++)a.add(i);return a;}if(m>n)return a;final long mod=101,base=31;long pow=1,ph=0,wh=0;for(int i=1;i<m;i++)pow=pow*base%mod;for(int i=0;i<m;i++){ph=(ph*base+p.charAt(i))%mod;wh=(wh*base+t.charAt(i))%mod;}for(int s=0;s+m<=n;s++){if(ph==wh){int j=0;while(j<m&&p.charAt(j)==t.charAt(s+j))j++;if(j==m)a.add(s);}if(s+m<n){wh=(wh-(t.charAt(s)*pow)%mod+mod)%mod;wh=(wh*base+t.charAt(s+m))%mod;}}return a;}

    static Map<String,Object> suffix(Map<String,Object> in){String s=str(in,"text");int n=s.length();if(n==0)return out("sa",new int[0],"lcp",new int[0],"repeated","");Integer[] ix=new Integer[n];int[] rank=new int[n];for(int i=0;i<n;i++){ix[i]=i;rank[i]=s.charAt(i);}for(int k=1;k<n;k*=2){final int step=k;final int[] rr=rank;Arrays.sort(ix,(a,b)->{int c=Integer.compare(rr[a],rr[b]);if(c!=0)return c;int ra=a+step<n?rr[a+step]:-1,rb=b+step<n?rr[b+step]:-1;return Integer.compare(ra,rb);});int[] nr=new int[n];nr[ix[0]]=0;for(int i=1;i<n;i++){int a=ix[i-1],b=ix[i];nr[b]=nr[a]+((rank[a]!=rank[b]||(a+k<n?rank[a+k]:-1)!=(b+k<n?rank[b+k]:-1))?1:0);}rank=nr;if(rank[ix[n-1]]==n-1)break;if(k>n/2)break;}
        int[] sa=new int[n];for(int i=0;i<n;i++)sa[i]=ix[i];int[] pos=new int[n],lcp=new int[n];for(int i=0;i<n;i++)pos[sa[i]]=i;int h=0,best=0,start=0;for(int i=0;i<n;i++){int r=pos[i];if(r==0){h=0;continue;}int j=sa[r-1];while(i+h<n&&j+h<n&&s.charAt(i+h)==s.charAt(j+h))h++;lcp[r]=h;if(h>best){best=h;start=i;}if(h>0)h--;}
        return out("sa",sa,"lcp",lcp,"repeated",best==0?"":s.substring(start,start+best));}

    static Map<String,Object> aho(Map<String,Object> in){List<?> patterns=(List<?>)in.get("patterns"),texts=(List<?>)in.get("texts");int p=patterns.size();
        List<Map<Character,Integer>> trie=new ArrayList<>();List<List<Integer>> terminal=new ArrayList<>();List<Integer> fail=new ArrayList<>();trie.add(new HashMap<>());terminal.add(new ArrayList<>());fail.add(0);
        for(int id=0;id<p;id++){String w=(String)patterns.get(id);int u=0;for(int i=0;i<w.length();i++){char c=w.charAt(i);Integer v=trie.get(u).get(c);if(v==null){v=trie.size();trie.get(u).put(c,v);trie.add(new HashMap<>());terminal.add(new ArrayList<>());fail.add(0);}u=v;}terminal.get(u).add(id);}
        int states=trie.size();int[][] go=new int[states][95];List<Integer> bfs=new ArrayList<>(states);ArrayDeque<Integer> q=new ArrayDeque<>();bfs.add(0);
        for(Map.Entry<Character,Integer> e:trie.get(0).entrySet()){int c=e.getKey()-32,v=e.getValue();go[0][c]=v;fail.set(v,0);q.add(v);}
        while(!q.isEmpty()){int u=q.remove();bfs.add(u);int f=fail.get(u);for(int c=0;c<95;c++){Integer v=trie.get(u).get((char)(c+32));if(v!=null){go[u][c]=v;fail.set(v,go[f][c]);q.add(v);}else go[u][c]=go[f][c];}}
        trie.clear();
        List<List<Long>> rows=new ArrayList<>();for(Object textObj:texts){String text=(String)textObj;long[] visits=new long[states];int state=0;for(int i=0;i<text.length();i++){state=go[state][text.charAt(i)-32];visits[state]++;}for(int z=bfs.size()-1;z>0;z--){int u=bfs.get(z);visits[fail.get(u)]+=visits[u];}long[] counts=new long[p];for(int u=0;u<terminal.size();u++)for(int id:terminal.get(u))counts[id]=visits[u];List<Long> row=new ArrayList<>(p);for(long x:counts)row.add(x);rows.add(row);}
        return out("counts",rows);}
}
