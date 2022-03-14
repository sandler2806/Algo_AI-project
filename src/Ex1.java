import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class Ex1 {
    static HashMap<myNode,Boolean> memory=new HashMap<>();
    static ArrayList<String> evidence =new ArrayList<>();
    static HashMap<String,myNode> Nodes=new HashMap<>();

    /**
     * implements bayesball algorithm
     * @return true if the variables is independent
     */
    public static boolean bayes(String question){
        memory.clear();//make a memory that save node and child/parent, so I won't check the same case twice
        evidence.clear();
        String dep=question.substring(question.indexOf("|")+1);
        String var=question.substring(0,question.indexOf("|"));
        String[] variables=var.split("-");//put the variables in array
        String[] arr=dep.split(",");

        if(!dep.equals(""))
        for (String s:arr){//make a list of the evidence
            evidence.add(s.substring(0,s.indexOf("=")));
        }
//check if there is a path between the variables given if we came from child or parent
        return (path(Nodes.get(variables[0]),Nodes.get(variables[1]),true));
    }

    /**
     * return if there is a path from a' to b' given the evidence
     * @param child a boolean that sign if we came from parent or child
     */
    static private boolean path(myNode a,myNode b,boolean child) {
        if(a==b)return true;
        // I will check which case this is and check by recursion the path by the laws of bayes ball
        if(evidence.contains(a.name))//the node is one of the evidence
        {
            if (child){//came from child , can't go anywhere
                return false;
            }
            else{//came from parent
                for (int i = 0; i < a.given.size(); i++) {//check recursion all the parents if I didn't check it already
                    if (!memory.containsKey(a.given.get(i))||!memory.get(a.given.get(i)))
                    {
                        memory.put(a.given.get(i),true);
                        if(path(a.given.get(i),b,true))return true;//if one of them true then I can reach from one node to other
                    }
                }
            }
        }
        else
        {//the node is not one of the evidence
            if (child){//came from child then I check path from all children and parents
                for (int i = 0; i < a.child.size(); i++) {
                    if (!memory.containsKey(a.child.get(i))||memory.get(a.child.get(i)))
                    {
                        memory.put(a.child.get(i),false);
                        if(path(a.child.get(i),b,false))return true;
                    }
                }
                for (int i = 0; i < a.given.size(); i++) {
                    if (!memory.containsKey(a.given.get(i))||!memory.get(a.given.get(i)))
                    {
                        memory.put(a.given.get(i),true);
                        if(path(a.given.get(i),b,true))return true;
                    }
                }
            }
            else{//came from parent then I check path from all children
                for (int i = 0; i < a.child.size(); i++) {
                    if (!memory.containsKey(a.child.get(i))||memory.get(a.child.get(i)))
                    {
                        memory.put(a.child.get(i),false);
                        if(path(a.child.get(i),b,false))return true;
                    }
                }

            }

        }
        return false;
    }

    /**
     * implements variable elimination algorithm and return the answer and the number of Multiplication and addition
     */
    public static double[] eliminate(String question){//for example P(B=T|J=T,M=T) A-E
        {
            double multiplication=0;
            double addition=0;
            String queryF=question.substring(2,question.indexOf('|')); //B=T
            String query=queryF.substring(0,queryF.indexOf("="));//B
            String queryState=queryF.substring(queryF.indexOf("=")+1);///T
            String EvidenceS=question.substring(question.indexOf('|')+1,question.indexOf(')'));//J=T,M=T
            String[]Evidence=EvidenceS.split(",");
            HashMap<String,String>Evi=new HashMap<>();
            if(!EvidenceS.equals(""))
            for (String value : Evidence) {
                Evi.put(value.substring(0, value.indexOf("=")), value.substring(value.indexOf("=") + 1));//hashMap of the evidence names
            }
            String[]hideTemp;
            ArrayList<String> hide;
            if(question.indexOf(')')+2<=question.length()) {
                hideTemp = question.substring(question.indexOf(')') + 2).split("-");//A-E
                hide = new ArrayList<>(Arrays.asList(hideTemp));
            }
            else
                hide=new ArrayList<>();
                hide.add(query);

            boolean cpt=true;
            myNode a=Nodes.get(query);
            for (String evidence:Evi.keySet()){// checking if the question is in the cpt
                if (!a.givenS.contains(evidence)) {
                    cpt = false;
                    break;
                }
            }
            for (String given:a.givenS){// checking if the question is in the cpt
                if (!Evi.containsKey(given)) {
                    cpt = false;
                    break;
                }
            }
            if(cpt){// if the question is in the cpt it returns it
                for (int i = 1; i <a.tables.size(); i++) {
                    boolean cell=a.tables.get(i).get(a.name).equals(queryState);
                    for (int j = 0; j < a.given.size()&&cell; j++) {
                        myNode n=a.given.get(j);
                        if(!Evi.get(n.name).equals(a.tables.get(i).get(n.name)))cell=false;
                    }
                    if (cell){
                        return new double[]{Double.parseDouble(a.tables.get(i).get("pot")),0,0};
                    }
                }
            }
            else
            {
                HashMap<String,ArrayList<HashMap<String,String>>>NodesCopy=new HashMap<>();
                for (myNode n:Nodes.values()) {// making a copy of all the factors that are ancestor of the evidence or query include them
                    ArrayList <HashMap<String,String>>tableCopy=new ArrayList<>();
                    boolean ancestor=isAncestor(Nodes.get(query),n);
                    for (String s:Evi.keySet())if(isAncestor(Nodes.get(s),n))ancestor=true;
                    for (int i = 0; i < n.tables.size()&&ancestor; i++) {
                        HashMap<String,String> row =new HashMap<>();
                        for (String r:n.tables.get(i).keySet()){
                            row.put(r,n.tables.get(i).get(r));
                        }
                        tableCopy.add(row);
                    }
                    if(ancestor)NodesCopy.put(n.name,tableCopy);
                }
                //delete all the rows(hashmaps) in the table which the evidence state is not correct(not the same as given)
                ArrayList<String>delete=new ArrayList<>();
                for (String name:NodesCopy.keySet()){
                    for (String evi:Evi.keySet()){
                        if(NodesCopy.get(name).get(0).containsKey(evi)) {
                            NodesCopy.get(name).get(0).remove(evi);
                            for (int i = 1; i < NodesCopy.get(name).size(); i++) {
                                if (!NodesCopy.get(name).get(i).get(evi).equals(Evi.get(evi)))
                                    NodesCopy.get(name).remove(i--);
                                else
                                    NodesCopy.get(name).get(i).remove(evi);
                            }
                        }
                    }
//                           delete all factors that are independent with the query given the evidence
                            boolean independent=false;
                            for (Object x:NodesCopy.get(name).get(0).keySet()){
                                if(hide.contains(x)&&!bayes(query+"-"+x+"|"+EvidenceS+","))independent=true;
                            }
                            if (independent||NodesCopy.get(name).size()<3){
                                for (String s:NodesCopy.keySet()){
                                    if(NodesCopy.get(s).equals(NodesCopy.get(name)))
                                        delete.add(s);
                                }
                            }

                }
                for (String value : delete) {
                    NodesCopy.remove(value);
                }
                int countEli=0;
                for (String h : hide) {// pick a hidden variable,join all his factors and then eliminate him
                    HashMap<String,ArrayList<HashMap<String,String>>> hidden=new HashMap<>();
                    delete.clear();
                    for (String name : NodesCopy.keySet()) {//delete all the factors from the copy hashmap and put them in hidden hashmap
                        if (NodesCopy.get(name).get(0).containsKey(h)) {
                            hidden.put(name,NodesCopy.get(name));
                            delete.add(name);
                        }
                    }
                    for (String value : delete) {
                        NodesCopy.remove(value);
                    }
                    int count=0;
                    while (hidden.size()>1){//keep join all the factors of the hidden variable until I get one factor only
                        count++;
                        String[]min=findMin(hidden);//a function that return the 2 minimum factors
                        ArrayList<String>common=new ArrayList<>();
                        ArrayList<HashMap<String,String>>multi=new ArrayList<>();
                        multi.add(new HashMap<>());//making new table "multi" where I put the multiplication result of both factors
                        for (String s:hidden.get(min[0]).get(0).keySet()){//take the common variable in both of the factors
                            if(hidden.get(min[1]).get(0).containsKey(s))common.add(s);
                        }//put in the first line all the factors that at least in one of the factors
                        for (String s:hidden.get(min[0]).get(0).keySet())multi.get(0).put(s,s);
                        for (String s:hidden.get(min[1]).get(0).keySet())multi.get(0).put(s,s);

                        for (int i = 1; i < hidden.get(min[0]).size(); i++) {
                            HashMap<String, String> mapA = hidden.get(min[0]).get(i);//take one row from first factor
                            for (int j = 1; j < hidden.get(min[1]).size(); j++) {//running all the second factor
                                HashMap<String, String> mapB = hidden.get(min[1]).get(j);
                                HashMap<String, String> map = new HashMap<>();
                                boolean same = true;//multiplication the rows if the common variables the same
                                for (String s : common) if (!mapA.get(s).equals(mapB.get(s))) same = false;
                                if (same) {//put all the variables of the row except the "pot"
                                    for (String s : mapA.keySet())if(!s.equals("pot")) map.put(s, mapA.get(s));
                                    for (String s : mapB.keySet())if(!s.equals("pot")) map.put(s, mapB.get(s));
                                    map.put("pot", "" + (Double.parseDouble(mapA.get("pot")) * Double.parseDouble(mapB.get("pot"))));
                                    multi.add(map);
                                    multiplication++;
                                }//multiplication both "pot" add to the new table and count it
                            }
                        }//after multiplication all the relevant rows I delete those factors and put the new one
                        hidden.remove(min[0]);
                        hidden.remove(min[1]);
                        hidden.put("multi"+count,multi);
                    }

                    ArrayList<HashMap<String,String>>eliminate=new ArrayList<>();
                    eliminate.add(new HashMap<>());
                    //if this is the query variable I don't eliminate him
                    if(h.equals(query))for (ArrayList<HashMap<String,String>> arrayList:hidden.values())eliminate=arrayList;
                    else//this loop running once because this the condition of the previous while loop
                    for (ArrayList<HashMap<String,String>> eliminationTable:hidden.values()){
                        for (String s:eliminationTable.get(0).keySet()){
                            if(!s.equals(h))eliminate.get(0).put(s,s);//put in the first row all the variables except the one I wish to eliminate
                        }
                        while (eliminationTable.size()>1){//I addition all the relevant rows and then delete them
                            HashMap<String,String>map=new HashMap<>();
                            for (String s:eliminationTable.get(1).keySet()){
                            if(!s.equals(h))map.put(s,eliminationTable.get(1).get(s));//insert the row all the variables except the hidden
                        }
                            eliminate.add(map);
                            double sum=Double.parseDouble(eliminationTable.get(1).get("pot"));
                            //take the first row and addition it with those who the variable except the hidden are the same(the hidden will be different)
                            for (int i = 2; i < eliminationTable.size(); i++) {
                                boolean add=true;
                                delete.clear();
                                for (String k:eliminationTable.get(i).keySet()){
                                    if(!k.equals("pot")&&!k.equals(h)){
                                        if (!eliminationTable.get(1).get(k).equals(eliminationTable.get(i).get(k)))add=false;
                                    }
                                }
                                if(add){//addition the relevant row,delete it and count
                                    sum+=Double.parseDouble(eliminationTable.get(i).get("pot"));
                                    eliminationTable.remove(i--);
                                    addition++;
                                }
                            }
                            eliminationTable.remove(1);//delete the first row too(the same as all the rows that I addition already)
                            eliminate.get(eliminate.size()-1).put("pot",""+sum);
                        }
                    }

                    if(eliminate.size()>2)//if the factor with one value I delete him
                    NodesCopy.put("eli"+countEli++,eliminate);
                }
                for (String s:NodesCopy.keySet()){//addition all the potential
                    double sum=Double.parseDouble(NodesCopy.get(s).get(1).get("pot"));
                    for (int i = 2; i < NodesCopy.get(s).size(); i++) {
                        sum+=Double.parseDouble(NodesCopy.get(s).get(i).get("pot"));
                        addition++;
                    }
                    for (int i = 1; i < NodesCopy.get(s).size(); i++) {//normalize the answer
                        if(NodesCopy.get(s).get(i).get(query).equals(queryState)){
                            double ans=Double.parseDouble(NodesCopy.get(s).get(i).get("pot"))/sum;
                            NumberFormat formatter = new DecimalFormat("#0.00000");
                            String ans1=formatter.format(ans);
                            return new double[]{Double.parseDouble(ans1),addition,multiplication};
                        }
                    }

                }
            }
            return new double[]{};
        }
    }

    /**
     * find the two minimum factors in the list
     * @param hidden all the hidden factors
     */
    public static String[]findMin(HashMap<String,ArrayList<HashMap<String,String>>> hidden){
        String[] ans=new String[2];
        ans[1]="";ans[0]="";
        for (String s:hidden.keySet()){
            if(ans[0].equals(""))ans[0]=s;
            else{
                if(ans[1].equals("")){
                    if(hidden.get(ans[0]).size()>hidden.get(s).size()){
                        ans[1]=ans[0];
                        ans[0]=s;
                    }
                    else if(hidden.get(ans[0]).size()<hidden.get(s).size())ans[1]=s;
                    else if(hidden.get(ans[0]).size()==hidden.get(s).size()){
                      if(equal(hidden,ans,s,0)){
                          ans[1]=ans[0];
                          ans[0]=s;
                      }
                      else ans[1]=s;
                    }
                }
                else{
                    if(hidden.get(s).size()<hidden.get(ans[0]).size()||(hidden.get(s).size()==hidden.get(ans[0]).size()&&equal(hidden,ans,s,0))){
                        ans[1]=ans[0];
                        ans[0]=s;
                    }
                    else if(hidden.get(s).size()<hidden.get(ans[1]).size()||(hidden.get(s).size()==hidden.get(ans[1]).size()&&equal(hidden,ans,s,1)))
                        ans[1]=s;
                }
            }
        }
        return ans;
    }
    /**
     *find the lower factor by their ascii size
     */
    public static boolean equal(HashMap<String,ArrayList<HashMap<String,String>>> hidden,String[] ans,String s,int k){

        int sumAns=0,sumS=0;
        for(String names:hidden.get(ans[k]).get(0).keySet()){
            for (int i = 0; i < names.length(); i++) {
                sumAns+=names.charAt(i);
            }
        }
        for(String names:hidden.get(s).get(0).keySet()){
            for (int i = 0; i < names.length(); i++) {
                sumS+=names.charAt(i);
            }
        }
        return sumAns >= sumS;
    }

    /**
     * return if b is ancestor of a
     */
    public static boolean isAncestor(myNode a,myNode b){

        if(a.name.equals(b.name))return true;
        boolean ans=false;
        for (myNode n:a.given){
            if(isAncestor(n,b))ans=true;
        }
        return ans;
    }

    /**
     * read from file the network and questions and write to output the answers
     */
    public static void main(String[] args) {
        String net="src/";
        ArrayList<String>questions=new ArrayList<>();
        try {
            File text = new File("src/input.txt");
            //scan at first the network name
            Scanner scanner = new Scanner(text);
            net+=scanner.nextLine();
            //scan the questions
            while (scanner.hasNext()){
                questions.add(scanner.nextLine());
            }
        }
        catch (Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        //create all the nodes from the network
        Nodes = netReader.netRead(net);

        boolean first=true;
        try {
            //write all the outputs and filter the answer by the type of the question
            FileWriter myWriter = new FileWriter("output.txt");
            for (String question : questions) {
                if(!first)myWriter.write("\n");
                first=false;
                if (question.charAt(0) == 'P') {
                    double[] Ans = eliminate(question);
                    try{
                        String ans = "" + Ans[0];
                        String ans1=""+(int) Ans[1];
                        String ans2=""+(int) Ans[2];
                        ans=ans +","+ans1;
                        ans+=","+ans2;
                        myWriter.write(ans);
                    }
                    catch (Exception e){
                        myWriter.write("failed");
                    }
                }
                else
                if(!bayes(question))
                    myWriter.write("yes");
                else
                    myWriter.write("no");
            }

            myWriter.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
