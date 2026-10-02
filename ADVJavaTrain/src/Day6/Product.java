package Day6;
import java.util.*;
public class Product {

    static class Entr{
        String product;
        int pid;
        float price;

        Entr(String product,int pid,float price){
            this.product=product;
            this.pid=pid;
            this.price=price;
        }
    }
    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        System.out.println("Enter the product,Id,Price");
        for(int i=1;i<=10;i++) {
            String product = sc.next();
            int pid
                    = sc.nextInt();
            float price = sc.nextFloat();
            Entr e = new Entr(product, pid, price);
        }
    }
}
