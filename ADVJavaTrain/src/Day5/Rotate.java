package Day5;

import java.util.LinkedHashSet;

public class Rotate {
    public static void main(String[] args) {
        int [] arr={6,2,4,4,4,6,7,9,9,10};
        int n=arr.length;
        LinkedHashSet<Integer> hs = new LinkedHashSet<>();
        for(int i=0;i<n;i++) {
            hs.add(arr[i]);
        }
        System.out.println(hs);
    }
}
