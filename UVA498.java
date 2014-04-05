import java.io.*;
import java.util.*;

public class UVA498 {
    public static void main(String[] args) throws Exception{
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));    
        String line1;
        String line2;
        while((line1 = in.readLine())!= null && (line2 =in.readLine())!= null){
            ArrayList<Integer> al = new ArrayList<Integer>();
            al.clear();
            StringTokenizer st = new StringTokenizer(line1);
            while (st.hasMoreTokens()) {
                al.add(Integer.parseInt(st.nextToken()));
            }
            
            ArrayList<Integer> al2 = new ArrayList<Integer>();
            al2.clear();
            StringTokenizer xValue = new StringTokenizer(line2);
            while(xValue.hasMoreTokens()){
                al2.add(Integer.parseInt(xValue.nextToken()));
            }
            String output = calculate(al, al2);
            System.out.println(output);
        }
    }
    // Hornor's Rule
    public static String calculate(ArrayList<Integer> coef, ArrayList<Integer> x){
        int num_coef = coef.size();
        int num_x = x.size();
        int result= 0;
        int value = 0;
        StringBuilder output = new StringBuilder();
        for(int i=0;i<num_x;i++){
            value = x.get(i);
            result = coef.get(0);
            for(int j=1;j<num_coef;j++){
                result = (result*value)+ coef.get(j);
            }
            output.append(result);
            result=0;
            output.append(" ");
        }
        String line = output.toString().trim();
        return line;       
    }
    
}
