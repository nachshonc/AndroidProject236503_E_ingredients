package il.ac.technion.tessa;

/**
 * Created by nachshonc on 11/30/15.
 */
public enum Options{DANG(0), UNHEALTHY(1), SAFE(2), DEFAULT(3);
    public final int value;
    private Options(int value) {
        this.value = value;
    }
    public static Options getOpt(int v){return (v==0)?DANG:(v==1)?UNHEALTHY:(v==2)?SAFE:DEFAULT;}
};
