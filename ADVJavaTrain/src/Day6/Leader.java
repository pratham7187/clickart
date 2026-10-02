package Day6;

import java.util.ArrayList;

public class Leader {
    public static void main(String[] args) {
        int [] arr={5,4,3,2,1};
        int n=arr.length;
        ArrayList<Integer>al=new ArrayList<>();
        for(int i=0;i<n;i++){
            int lead=arr[i];
            boolean ans=true;
            for(int j=i+1;j<n;j++){
                if(arr[j]>lead){
                    ans=false;
                    break;
                }
                else{
                    ans=true;
                }
            }
            if(ans){
                al.add(lead);
            }
        }
        System.out.println(al);
    }
}
