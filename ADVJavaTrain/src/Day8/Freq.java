package Day8;

import java.util.HashMap;
import java.util.Map;

public class Freq {
    public static void main(String[] args) {
        String s="aabbccde";
        int [] arr=new int[256];
        int n=s.length();
        for(int i=0;i<n;i++){
            arr[s.charAt(i)]+=1;
        }
        for(int i=0;i<256;i++){
            if(arr[i]==1){
                System.out.println((char)i+"="+arr[i]);
            }
        }
    }
}
