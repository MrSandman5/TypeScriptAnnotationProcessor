import com.company.TypeScriptAnnotation;

@TypeScriptAnnotation
public class PersonA {
    private String name;
    private char lastname;
    public String temp;
    private int num1;
    public int num2;

    public String None(){ return "a";}
    private float Something(double b){ return (float)b;}
    public boolean More (long a, long b, long c) {return a>b || b<c;}
}
