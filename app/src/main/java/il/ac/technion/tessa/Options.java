package il.ac.technion.tessa;

public enum Options{DANG(0), UNHEALTHY(1), SAFE(2), DEFAULT(3);
    public final int value;
    Options(int value) {
        this.value = value;
    }
    public static Options getOpt(int v){return (v==0)?DANG:(v==1)?UNHEALTHY:(v==2)?SAFE:DEFAULT;}
    public int getPic(){
        if(this==DANG) return R.drawable.danger;
        else if(this==UNHEALTHY) return R.drawable.caution;
        else return R.drawable.v;
    }
}
