package Day7;

public class AllPali {
    public static boolean pali(String s){
        int i=0;
        int n=s.length();
        int j=n-1;
        while(i<j){
            if(s.charAt(i)!=s.charAt(j)){
                return false;
            }
            i++;
            j--;
        }
        return true;
    }
    public static void main(String[] args) {
        String [] arr={"madam","hello","level"};
        int n=arr.length;
        for(int i=0;i<n;i++){
            boolean ans=pali(arr[i]);
               if(ans){
                   System.out.println(arr[i]);
               }
        }
    }
}
