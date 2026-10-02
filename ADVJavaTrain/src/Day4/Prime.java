package Day4;
import java.util.*;
public class Prime {
    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        System.out.println("Enter the no:");
        int n=sc.nextInt();
        int ans=nthprime(n);
        System.out.println(ans);
    }
    public static boolean isprime(int n){

        if(n<=1){
            return false;
        }
        else{
            for(int i=2;i<=n/2;i++){
                if(n%i==0){
                    return false;
                }
            }
        }
        return true;
    }
    public static int nthprime(int n){
        int count=0;
        for(int i=1;i<=n;i++){
            if(isprime(i)){
                count++;
            }
        }
        return count ;
    }
}
