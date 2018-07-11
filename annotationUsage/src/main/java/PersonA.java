import com.company.TypeScriptAnnotation;

@TypeScriptAnnotation
public class PersonA {
    private final String name;
    private final String lastname;
    public String temp;
    private final int num1;
    public int num2;

    public PersonA (String name, String lastname, int num1) {
        this.name = name;
        this.lastname = lastname;
        this.num1 = num1;
    }

    public static void main(String[] args) {

    }
}
