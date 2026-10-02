package Day8;

public class Anagram {
    public static void main(String[] args) {


        String s1 = "abcdbcea";

        int n = s1.length();

        int[] freq = new int[26];
        int[] freq1 = new int[26];

        for (int i = 0; i < n; i++) {
            freq[s1.charAt(i) - 'a']++;
        }
        for(int i=0;i<n;i++) {
            if (freq[s1.charAt(i) - 'a']==1) {
                System.out.println(s1.charAt(i));
                return;
            }
        }

    }
}
