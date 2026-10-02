package Day3;
import java.util.*;
public class Problems {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = 666;
        //boolean ans=isperfect(n);
        //boolean ans1=isamstrong(n);

        // boolean ans2=ishappy(n);
        boolean ans2 = smith(n);
        if (ans2) {
            System.out.println("is smith");
        } else {
            System.out.println("not smith");
        }


    }

    public static int count(int n) {
        int sum = 0;
        while (n != 0) {
            int mod = n % 10;
            sum = sum + mod;
            n = n / 10;
        }
        return sum;
    }

    public static boolean smith(int n) {

        int sum = 0;
        int i = 2;
        int temp=n;
        while (n >1) {

            if (n % i == 0) {
                int cnt = count(i);
                sum =sum+cnt;
                n=n/i;
                i=2;

            }
            else{
                i++;
            }
        }

        return count(temp)==sum;
    }
}