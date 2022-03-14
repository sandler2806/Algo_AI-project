import java.util.ArrayList;
import java.util.HashMap;

public class myNode {

    public ArrayList<String> outcome=new ArrayList<>();
    public String name;
    public ArrayList<myNode> given=new ArrayList<>();
    public ArrayList<myNode> child=new ArrayList<>();
    public ArrayList<HashMap<String,String>> tables=new ArrayList<>();
    public ArrayList<String> givenS=new ArrayList<>();
    public String[] Table;


    public myNode(String name,String table,ArrayList<String> outcome,ArrayList<String> given)
    {   //copy the name,outcome and given
        this.name=name;
        Table=table.split(" ");
        this.outcome.addAll(outcome);
        this.givenS.addAll(given);
    }
    public void makeTable(){
        //create the table after initialize the parents
        HashMap<String,String>map1=new HashMap<>();
        for (myNode myNode : given) map1.put(myNode.name, myNode.name);
        map1.put(name,name);
        tables.add(map1);
        for (int i = 0; i < Table.length; i++) {
            int count=1;
            HashMap<String,String>map=new HashMap<>();
            map.put("pot",Table[i]);
            for (int j = 0; j <= given.size(); j++) {
                if(j==given.size()){
                    map.put(name,outcome.get(i%outcome.size()));
                }
                else
                {
                    count*=given.get(j).outcome.size();
                    map.put(given.get(j).name,given.get(j).outcome.get((i/(Table.length/count))%given.get(j).outcome.size()));
                }
            }
            tables.add(map);
        }
    }
//    public String getGiven() {
//        String str="";
//        for (int i = 0; i < given.size(); i++) {
//            str+=given.get(i).name+",";
//        }
//        return str;
//    }
//    public String getChild() {
//        String str="";
//        for (int i = 0; i < child.size(); i++) {
//            str+=child.get(i).name+",";
//        }
//        return str;
//    }

//    @Override
//    public String toString() {
//        return "myNode{" +
//                "outcome=" + outcome +
//                ", name='" + name + '\'' +
//                ", given=" + this.getGiven() +
//                ", child=" + this.getChild() +
//                ", tables=" + tables.toString() +
//                '}';
//    }

}
