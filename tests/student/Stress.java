import java.util.*;

/** Boundary-size smoke checks for the published limits; assertions are algorithm-specific. */
public final class Stress {
    private static Map<String,Object> req(Object... kv){Map<String,Object>x=new HashMap<>();for(int i=0;i<kv.length;i+=2)x.put((String)kv[i],kv[i+1]);return x;}
    private static void need(boolean ok,String what){if(!ok)throw new AssertionError(what);}
    public static void main(String[] args){
        String thousand="a".repeat(1000);need(Algorithms.lcs(req("a",thousand,"b",thousand)).get("length").equals(1000),"lcs");
        List<Integer> sizes=new ArrayList<>();for(int i=0;i<200;i++)sizes.add(1);Map<String,Object> merged=Algorithms.merge(req("sizes",sizes));need(((List<?>)merged.get("plan")).size()==199,"merge");
        int n=100000;List<Integer> weights=new ArrayList<>(n);List<List<Integer>> edges=new ArrayList<>(n-1);for(int i=0;i<n;i++){weights.add(1);if(i>0)edges.add(List.of(i-1,i));}Map<String,Object> tree=Algorithms.tree(req("n",n,"weights",weights,"edges",edges));need(tree.get("weight").equals(50000L),"tree");
        String text="a".repeat(200000),pattern="b".repeat(100000);need(((List<?>)Algorithms.kmp(req("text",text,"pattern",pattern)).get("positions")).isEmpty(),"kmp");
        need(((List<?>)Algorithms.rk(req("text","a".repeat(10000),"pattern","b".repeat(2000))).get("positions")).isEmpty(),"rk");
        String repeated="a".repeat(50000);Map<String,Object> suffix=Algorithms.suffix(req("text",repeated));need(((int[])suffix.get("sa"))[0]==49999&&((int[])suffix.get("lcp"))[49999]==49999&&((String)suffix.get("repeated")).length()==49999,"suffix");
        List<String> texts=new ArrayList<>();for(int i=0;i<100;i++)texts.add("a".repeat(2000));Map<String,Object> aho=Algorithms.aho(req("patterns",List.of("a".repeat(100000)),"texts",texts));need(((List<?>)aho.get("counts")).size()==100,"aho");
        System.out.println("PASS boundary-size smoke checks: LCS 1000x1000, merge 200, tree 100000, KMP 200000/100000, RK 10000/2000, suffix 50000, Aho 100000 states plus 100 texts");
    }
}
