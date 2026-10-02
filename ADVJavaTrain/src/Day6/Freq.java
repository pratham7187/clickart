package Day6;

import java.util.HashMap;
import java.util.*;
public class Freq {
    public static void main(String[] args) {
        int [] arr={1,2,3,3,5,5,1,2,3,6};
        int n =arr.length;
        HashMap<Integer,Integer>mp=new HashMap<>();
        for(int i=0;i<n;i++){
            mp.put(arr[i], mp.getOrDefault(arr[i],0)+1);
        }
        for(Map.Entry<Integer,Integer>entry:mp.entrySet()){
            System.out.println(entry.getKey()+"="+ entry.getValue());
        }
    }
}
