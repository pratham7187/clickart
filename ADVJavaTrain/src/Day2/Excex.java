package Day2;
import java.util.*;
public class Excex {
    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        System.out.println("Enter the no:");
        int n=sc.nextInt();
        System.out.println("Enter the no:");
        int m=sc.nextInt();
        try{
            System.out.println(n/m);
        }catch (Exception e){
            System.out.println("Not Divided By Zero");
        }
        finally{
            sc.close();
        }

    }
}
