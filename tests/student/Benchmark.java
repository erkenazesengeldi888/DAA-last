import java.util.*;

/** Reproducible method-level naive/KMP/RK comparison; run with a warmed JVM. */
public final class Benchmark {
    private static final String[] NAMES={"naive","kmp","rk"};
    private static Map<String,Object> request(String task,String text,String pattern){Map<String,Object> x=new HashMap<>();x.put("task",task);x.put("text",text);x.put("pattern",pattern);return x;}
    private static Object invoke(int a,Map<String,Object> x){return switch(a){case 0->Algorithms.naive(x).get("positions");case 1->Algorithms.kmp(x).get("positions");default->Algorithms.rk(x).get("positions");};}
    private static long median(long[] a){Arrays.sort(a);return a[a.length/2];}
    private static String randomText(int n,long seed){Random r=new Random(seed);StringBuilder b=new StringBuilder(n);for(int i=0;i<n;i++)b.append((char)('a'+r.nextInt(26)));return b.toString();}
    private static void one(String kind,int n,long seed){int m=Math.max(1,n/4);String text,pattern;
        if(kind.equals("random")){text=randomText(n,seed);int at=(int)(seed%(n-m+1));pattern=text.substring(at,at+m);}
        else{text="a".repeat(n);pattern="a".repeat(m);}
        Map<String,Object>[] req=new Map[3];Object[] expected=new Object[3];
        for(int a=0;a<3;a++)req[a]=request(NAMES[a],text,pattern);
        for(int a=0;a<3;a++)expected[a]=invoke(a,req[a]);
        if(!expected[0].equals(expected[1])||!expected[0].equals(expected[2]))throw new AssertionError("different outputs");
        for(int a=0;a<3;a++)for(int w=0;w<2;w++)invoke(a,req[a]); // warm-up each implementation
        long[][] times=new long[3][3];Random order=new Random(seed^0x5eedL);
        for(int run=0;run<3;run++){int[] methods={0,1,2};for(int i=2;i>0;i--){int j=order.nextInt(i+1),t=methods[i];methods[i]=methods[j];methods[j]=t;}
            for(int a:methods){long start=System.nanoTime();Object got=invoke(a,req[a]);times[a][run]=System.nanoTime()-start;if(!expected[a].equals(got))throw new AssertionError("unstable output");}}
        for(int a=0;a<3;a++)System.out.printf(Locale.ROOT,"%s,%s,%d,%d,%d,%d,%d,%d,%d,%d%n",kind,NAMES[a],n,m,seed,times[a][0],times[a][1],times[a][2],median(times[a]),expected[a].hashCode());
    }
    public static void main(String[] args){System.out.println("case,algorithm,n,m,seed,run1_ns,run2_ns,run3_ns,median_ns,output_hash");for(int n:new int[]{600,1200,2400}){one("random",n,20261106L+n);one("repetitive",n,20261106L+n);}}
}
