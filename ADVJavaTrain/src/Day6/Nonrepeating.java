package Day6;

import java.util.HashMap;

public class Nonrepeating {
    public static void main(String[] args) {
        int [] arr={1,2,4,3,2,1};
        int n=arr.length;
        HashMap<Integer,Integer>mp=new HashMap<>();
        for(int i=0;i<n;i++){
            mp.put(arr[i],mp.getOrDefault(arr[i],0)+1);
        }

        for(int i=0;i<n;i++){
            if(mp.get(arr[i])==1){
                System.out.println(arr[i]);
                return;
            }
        }

    }
}
