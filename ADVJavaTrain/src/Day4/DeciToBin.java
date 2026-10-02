package Day4;
import java.util.*;
public class DeciToBin {
    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        System.out.println("Enter a no:");
        int n=sc.nextInt();
        int [] binary=new int[32];
        int i=0;
        while(n>1){

            int mod=n%2;
            binary[i]=mod;
            n=n/2;
            i++;
        }
        binary[i]=n;
        for(int j=i;j>=0;j--){
            System.out.print("["+binary[j]+",]");
        }
    }

}
