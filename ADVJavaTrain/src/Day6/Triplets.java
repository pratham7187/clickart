package Day6;

public class Triplets {
    public static void main(String[] args) {
        int [] arr={1,2,4,5,7,8,10};
        int n=arr.length;
        int d=3;

        int trip=0;
        for(int i=0;i<n;i++){
           boolean first=false;
           boolean second=false;

          for(int j=i+1;j<n;j++){
              if(arr[i]+d==arr[j]){
                  first=true;

              }
              if(arr[i]+d*2==arr[j]){
                  second=true;
              }
              if(first&&second){
                  trip++;
                  break;
              }
          }
        }
        System.out.println(trip);
    }
}
