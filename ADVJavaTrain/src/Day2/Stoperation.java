package Day2;
import java.util.*;
public class Stoperation {
    public static void main(String[] args) {
        Stack st=new Stack<>();
        st.push(10);

        st.push(20);
        st.push("Hello");
        st.push(5.6);
        st.add(null);
        System.out.println(st);
        System.out.println(st.empty());

    }
}
