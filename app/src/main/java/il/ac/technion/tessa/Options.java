package il.ac.technion.tessa;

public enum Options{DANG(0), UNHEALTHY(1), SAFE(2), DEFAULT(3);
    public final int value;
    Options(int value) {
        this.value = value;
    }
    public static Options getOpt(int v){return (v==0)?DANG:(v==1)?UNHEALTHY:(v==2)?SAFE:DEFAULT;}
    public int getPic(){
        if(this==DANG) return R.drawable.poisonicon;
        else if(this==UNHEALTHY) return R.drawable.warningicon;
        else return R.drawable.okicon;
    }
}
