import java.io.*;
import java.util.*;

public class UVA11340 {
        public static void main(String[] args) throws Exception{
        
        
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "ISO-8859-1"));  // input uses extended ASCII
        int nCases = Integer.parseInt(in.readLine());
      
        for(int caseNum = 0; caseNum < nCases; caseNum++){
            int nKeys = Integer.parseInt(in.readLine());
            int[] ascii_table = new int[280];
            //inserting value into the ascii_table
            for(int keyNum = 0; keyNum<nKeys;keyNum++){
                String line = in.readLine();
                StringTokenizer st = new StringTokenizer(line);
                int position = (int)st.nextToken().charAt(0);
                ascii_table[position] = Integer.parseInt(st.nextToken());

            }
            
            int nLines = Integer.parseInt(in.readLine());
            double payment = 0;
            for(int lineNum = 0;lineNum<nLines;lineNum++){
                String article = in.readLine();
                for(int i = 0; i<article.length();i++){
                    int index = (int)article.charAt(i);
                    payment = payment + ascii_table[index];

                }
            }
            System.out.printf("%.2f$\n",payment/100.0);
        }

    }
    
}
