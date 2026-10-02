package Day5;

import java.util.HashMap;

public class Myprg {
    public static void main(String[] args) {
        int[] arr = {10,20,30,40,10,20,60,10,54,10,52,30};
        int n=arr.length;
        HashMap<Integer,Integer>mp=new HashMap<>();
        for(int i=0;i<n;i++){
            mp.put(arr[i], mp.getOrDefault(0,+1));
        }
        int max=0;
        for(int i=0;i<n;i++){
            int val=mp.get(arr[i]);
            if(val>max){
                max=val;
            }
        }
        System.out.println(max);
    }
}
