package Day8;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class Reverse {
    public static void main(String[] args) {
        String s="programming";
        int n=s.length();
        /*LinkedHashSet<Character> hs=new LinkedHashSet<>();

        for(int i=0;i<n;i++){
            hs.add(s.charAt(i));
        }
        System.out.println(hs);*/
        HashSet<Character> hs=new HashSet<>();
        String result="";
        for(int i=0;i<n;i++){
            char ch=s.charAt(i);
            if(!hs.contains(ch)){
                hs.add(ch);
                result=result+ch;
            }
        }
        System.out.println(result);
    }
}
