package Day6;

import java.util.PriorityQueue;

public class Kthlarge {
    public static void main(String[] args) {
        int []arr={2,5,3,8,9,6,4};
        int k=1;
        int n=arr.length;
        PriorityQueue<Integer>pq=new PriorityQueue<>();
        for(int i=0;i<n;i++){
            pq.add(arr[i]);
            if(pq.size()>k){
                pq.remove();
            }
        }
        System.out.println(pq.peek());
    }
}
