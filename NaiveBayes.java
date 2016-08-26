import java.io.*;
import java.util.*;
/*
 * Name: Yan Pan
 * this program assums the user would enter a integer value for N and 0 < N < number of biographies.
 * Stopwords and Corpus inputs are from fileReader.
 */
public class NaiveBayes {
    static int N;
    static ArrayList<String> stopwords = new ArrayList<String>();
    static Set<String> training_bio_words = new HashSet<String>();     // no repeats of all words in all bio
    static Map<String, ArrayList<String>> nameToBio;
    static Map<String, String> nameToLabel;
    static ArrayList<String> names;
    public static void main(String[] args) throws Exception{
        // read N
        try{
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Type an integer for N and press Enter:");
            N = Integer.parseInt(input.readLine());

        }catch(IOException io){
        }
        //read stopwords
        BufferedReader input2 = new BufferedReader(new FileReader("stopwords.txt"));
        String dic;
        while((dic = input2.readLine())!= null){
            StringTokenizer st= new StringTokenizer(dic);
            while(st.hasMoreTokens()){
                String word = st.nextToken();
                stopwords.add(word);
            }
        }
        // read and parse corpus
        BufferedReader in = new BufferedReader(new FileReader("corpus.txt"));
        String line; int index=0;
        names = new ArrayList<String>();
        nameToLabel = new HashMap<String, String>();  // map name to label
        nameToBio = new HashMap<String, ArrayList<String>>();   // map name to normalized bio
        while((line = in.readLine())!= null){
            while(line.trim().isEmpty()){     // multiple blank lines between bios
                line=in.readLine();
            }
            index++;
            String name = line;
            names.add(name);
            String label = in.readLine();
            nameToLabel.put(name, label.trim());   // trim whitespaces at the end of the category
            String bio,total_bio="";
            while((bio=in.readLine())!=null&&(!bio.trim().isEmpty())){               
                total_bio+=bio.toLowerCase();
            }
            //System.out.println("Original:    "+total_bio);
            //System.out.println("Normalized: "+omitStopwords(omitPunctuation(total_bio)));
            nameToBio.put(name,omitStopwords(omitPunctuation(total_bio)));
            
        }
        countC(nameToLabel);
        int [][] occ_W = countW(nameToBio,nameToLabel);
        //step 1
        double [] freq_C = freqC();
        double [][] freq_W = freqW(occ_W);
        //step 2
        double [] prob_C =prob_C(freq_C);
        double [][] prob_W = prob_W(freq_W);
        //step 3
        double [] log_c = log_probC(prob_C);
        double [][] log_w = log_probW(prob_W); // end of learning
        omitUnseenW();
        // apply classifier to test data
        double [][] predict = computeL(log_w, log_c);
        double [][] real_prob = recoverProb(predict);
        output(real_prob, predict);
    }
    // omits all occurances of comma and periods
    static String omitPunctuation(String bio){
        String output="";
        StringTokenizer s = new StringTokenizer(bio);
        while(s.hasMoreTokens()){
            String word = s.nextToken();
            word = word.replaceAll(",","");
            word = word.replaceAll("\\.","");
            output+=(word+" ");
        }
        return output;
    }
    // omits stopwords and words with one or two letters
    static ArrayList<String> omitStopwords(String bio){
        ArrayList<String> al = new ArrayList<String>();
        StringTokenizer s = new StringTokenizer(bio);
        while(s.hasMoreTokens()){
            String word = s.nextToken();
            if(!stopwords.contains(word)&& word.length()>=3){
                al.add(word);
            }
        }
        return al;
    }
    static Map<String, Integer> labelToNum = new HashMap<String, Integer>();
    // builds labelToNum, Occurance of C in training set
    static Set<String> set = new HashSet<String>();
    static void countC(Map<String, String> nameToLabel){
        for(int i=0;i<N;i++){   // in the range of the training set, find out how many categories and save in a set
            String name = names.get(i);
            set.add(nameToLabel.get(name));  
            
        }
        for(String s : set){
            int count=0;
            for(int i=0;i<N;i++){
                String name = names.get(i);
                if(nameToLabel.get(name).equals(s)){
                    count++;
                }
            }
            labelToNum.put(s, count);
        }
    }
    static int num_categ;
    static int num_word_t;
    static String [] train_words;
    static int [][] countW(Map<String, ArrayList<String>> nameToBio, Map<String, String> nameToLabel){
        // builds a set of different words from training set
        for(int i=0;i<N;i++){
            String name = names.get(i);
            for(String s:nameToBio.get(name)){
                training_bio_words.add(s);
            }
 
        }
        //System.out.println(training_bio_words);
        num_categ = set.size();
        num_word_t = training_bio_words.size();
        // store in array Occ_t(W|C)
        int [][] Occ_t = new int[num_word_t][num_categ];  
        train_words = training_bio_words.toArray(new String[0]);
        for(int i=0;i<num_word_t;i++){
            String W = train_words[i];
            for(int j=0;j<N;j++){
                String name = names.get(j);
                String label = nameToLabel.get(name);
                int idx = categoryID(label);    // For the tiny corpus example: writer -> 0, Music -> 1, Government -> 2
                if(nameToBio.get(name).contains(W)){
                    Occ_t[i][idx]++;
                }
            }
        }
        return Occ_t;
    }
    // return a integer ID for each category
    static String [] categ = new String[set.size()];
    static int categoryID(String C){
        int ID=-1;
        categ = set.toArray(new String[0]);
        for(int i=0;i<categ.length;i++){
            if(categ[i].equals(C)){
                ID=i;
            }
        }
        return ID;
    }
    //step 1: calculate freq_T(C)
    static double [] freqC(){
        double [] freq = new double[num_categ];
        for(String s: categ){
            double occ = labelToNum.get(s);
            int id = categoryID(s);
            freq[id]= occ/(double)N;
            //System.out.println(s+" Freq:"+ freq[id]);
        }
        return freq;
   }
   static double [][] freqW(int [][] occ){
       double [][] freq = new double[num_word_t][num_categ];
       for(int i=0;i<num_word_t;i++){
           for(int j=0;j<num_categ;j++){
               String C = categ[j];
               double num = labelToNum.get(C);
               freq[i][j]= occ[i][j]/num;
           }
       }
       return freq;
   }
   //step 2: compute P(C) and P(W|C) using Laplacian correction
   static double e= 0.1;
   static double [] prob_C(double [] freq){
       double [] prob = new double[num_categ];
       for(int i=0;i<freq.length;i++){
           prob[i]= (freq[i]+e)/(1+num_categ*e);
           //System.out.println("prob:"+ prob[i]);
       }
       return prob;
   }
   static double [][] prob_W(double [][] freq){
       double [][] prob = new double[num_word_t][num_categ];
       for(int i=0;i<num_word_t;i++){
           for(int j=0;j<num_categ;j++){
               prob[i][j]= (freq[i][j]+e)/(1+2*e);
           }
       }
       //System.out.println("prob:"+ prob[10][2]);   // test if P(american|govern) match professor's result
       return prob;
   }
   //step 3: negative log probability
   // we can combine step 2 and step 3 in one function,it would be faster. But this way, we can access P(C) and P(W|C) if we need
   static double log_base2(double x){
       return Math.log(x)/Math.log(2);
   }
   static double [] log_probC(double[] prob){
       double [] log_p = new double[num_categ];
       for(int i=0;i<prob.length;i++){
           log_p[i]= -log_base2(prob[i]);
           //System.out.println("log:"+ log_p[i]);
       }
       return log_p;
   }
   static double [][] log_probW(double [][] prob){
       double [][] log_p = new double[num_word_t][num_categ];
       for(int i=0;i<num_word_t;i++){
           for(int j=0;j<num_categ;j++){
               log_p[i][j]= -log_base2(prob[i][j]);
           }
       }
       //System.out.println("log:"+ log_p[10][2]); // test if match professor's result
       return log_p;
   }
   // end of learning
   
   // omit words not in training set
   static void omitUnseenW(){
       for(int i=N;i<names.size();i++){
           String name = names.get(i);
           ArrayList<String> bio = nameToBio.get(name);
           ArrayList<String> normal= new ArrayList<String>();
           for(String s: bio){
               if(training_bio_words.contains(s)){
                   normal.add(s);
               }
           }
           nameToBio.put(name, normal);
       }
   }
   static double [][] computeL(double [][] log_w, double [] log_c){
       double [][] predict = new double[names.size()-N][num_categ+2]; // store prediction in  predict [x][num_categ]
                                                                      // store min L(C|B) in predict [x][num_categ+1]
       for(int i=N;i<names.size();i++){
           String name = names.get(i);
           ArrayList<String> bio = nameToBio.get(name);
           double sum;
           int prediction=-1;
           double min= Integer.MIN_VALUE;
           for(int j=0;j<num_categ;j++){
               sum=0;
               for(String s: bio){
                   int idx = Arrays.asList(train_words).indexOf(s);
                   //System.out.println(s+" "+log_w[idx][j]);
                   sum+= log_w[idx][j];
               }
               sum+= log_c[j];   // add L(C)
               //System.out.println(j+" "+sum);
               predict[i-N][j]=sum;
 
               if(j==0){
                   min=sum;
                   prediction =0;
               }
               else{
                   if(sum<min){
                       min=sum;
                       prediction=j;
                   }
               }               
           }
           predict[i-N][num_categ]=prediction;
           predict[i-N][num_categ+1]= min;
           //System.out.println("prediction: "+prediction+ " min:"+min);

       }
       return predict;
   }
   static double [][] recoverProb( double [][] predict){
       double [][] real_prob = new double [names.size()-N][num_categ+1];
        for(int i=N;i<names.size();i++){  // test set range
            double sum=0;
            for(int j=0;j<num_categ;j++){
                double expo = predict[i-N][num_categ+1]-predict[i-N][j];
                real_prob[i-N][j]= Math.pow(2,expo);
                sum+=real_prob[i-N][j];
            }
            real_prob[i-N][num_categ]=sum;
            
        }
        for(int i=N;i<names.size();i++){
            for(int j=0;j<num_categ;j++){
                real_prob[i-N][j]=(real_prob[i-N][j])/(real_prob[i-N][num_categ]);
                //System.out.println("P:   "+real_prob[i-N][j]);
            }
        }
       return real_prob;
   }
   static void output(double [][] real_prob,double [][] predict){
       StringBuilder sb = new StringBuilder();
       int accurat_count=0;
       for(int i=N;i<names.size();i++){
           String name = names.get(i);
           String p = categ[(int)predict[i-N][num_categ]].trim();
           String correct = nameToLabel.get(name).trim();
           String outcome="Wrong";
           if(p.equals(correct)){
               outcome="Right";
               accurat_count++;
           }
           sb.append(name).append("   Prediction: ").append(p).append("   ").append(outcome).append("\n");
           for(int j=0;j<num_categ;j++){
               sb.append(categ[j]).append(": ").append(String.format("%.2f",real_prob[i-N][j])).append("   ");
           }
           sb.append("\n").append("\n");
       }
       int sizeTest = names.size()-N;
       sb.append("Overall accuracy: ").append(accurat_count).append(" out of ").append(sizeTest).append(" = ").append(String.format("%.2f",accurat_count/(double)sizeTest)).append("\n");
       System.out.print(sb);
   }
    
}
