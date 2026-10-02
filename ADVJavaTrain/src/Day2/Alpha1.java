package Day2;

public class Alpha1 {
    static void sum() throws ArithmeticException
    {
        System.out.println(10/5);
    }

    public static void main(String[] args) {
        try{
            sum();
        }catch (ArithmeticException e){
            System.out.println("problem");
        }
    }
}
