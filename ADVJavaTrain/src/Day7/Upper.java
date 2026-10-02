package Day7;

public class Upper {
    public static void main(String[] args) {
        String s="jAva iS fuN";
        StringBuilder sb=new StringBuilder();


        String ans=s.toLowerCase();
        int n=ans.length();
        sb.append(Character.toUpperCase(ans.charAt(n-1)));
        for(int i=n-1;i>=0;i--) {
            if (s.charAt(i-1)== ' ') {
                sb.append(Character.toUpperCase(ans.charAt(i)));
            } else {
                sb.append(ans.charAt(i));
            }
        }
        System.out.println(sb);
    }
}
