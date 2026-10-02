package Day3;

public class Pattern2 {
    public static void main(String[] args) {
        int n=5;

        for(int i=0;i<=n;i++){

            for(int j=0;j<n-i;j++){
                System.out.print(" ");
            }
            for(int k=1;k<=i;k++) {
                System.out.print(k);
            }
            System.out.println();
        }
    }
}
