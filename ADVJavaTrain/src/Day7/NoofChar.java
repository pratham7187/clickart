package Day7;

public class NoofChar {
    public static void main(String[] args) {
        String s = "abcF@12#45jk";
        s=s.trim();
        String [] arr=s.split("\\s+");
        int n=arr.length;
        System.out.println(n);

    }
}
