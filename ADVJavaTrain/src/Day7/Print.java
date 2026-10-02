package Day7;

public class Print {
    public static void main(String[] args) {
        String s="Hello";
        String s1="Bhavish";
        int m=s1.length();
        String ans="";
        int n=s.length();
        StringBuilder sb=new StringBuilder();

        for(int i=0;i<n;i++){
            char ch=s.charAt(i);
            sb.append(ch);
        }
        sb.reverse();
        System.out.println(sb);
        int j=0;
        for(int i=m-1;i>=0;i--){
            ans=ans+s1.charAt(i);
        }
        System.out.println(ans);

    }
}
