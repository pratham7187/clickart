package Day4;
import java.util.*;

public class Smallno {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        /*int [] arr={2,5,90,43,9};
        int small=arr[0];
        for(int i=1;i<arr.length;i++){
            if(arr[i]<small){
                small=arr[i];
            }
        }
        System.out.println("smallest is:"+small);*/
        int[] arr = {1,1,0,1,0,1,1,1};
        int n=arr.length;
        int i=0;
        int j=n-1;
        while(i<j){
            int temp=arr[i];
            arr[i]=arr[j];
            arr[j]=temp;
            i++;
            j--;
        }
        for(int k=0;k<n;k++){
            System.out.print(arr[k]+" ");
        }
    }
}