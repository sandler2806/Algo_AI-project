import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class netReader {


    public static HashMap<String,myNode> netRead(String filename) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        ArrayList<String> given=new ArrayList<>();
        ArrayList<String> outcome=new ArrayList<>();
        ArrayList<String> names=new ArrayList<>();
        HashMap<String, myNode> nodes = new HashMap<>();

        try {

            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(filename));
            doc.getDocumentElement().normalize();
            NodeList list = doc.getElementsByTagName("VARIABLE");

            for (int temp = 0; temp < list.getLength(); temp++) {
                //read all the variable and definition for each node
                list = doc.getElementsByTagName("VARIABLE");
                Node node = list.item(temp);
                String name="";
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    name = element.getElementsByTagName("NAME").item(0).getTextContent();
                    names.add(name);
                     outcome.clear();
                    for (int i = 0; i <element.getElementsByTagName("OUTCOME").getLength(); i++) {
                        outcome.add(element.getElementsByTagName("OUTCOME").item(i).getTextContent());
                    }
                }
                NodeList list2 = doc.getElementsByTagName("DEFINITION");
                node = list2.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String table = element.getElementsByTagName("TABLE").item(0).getTextContent();
                    given.clear();
                        for (int j = 0; j <element.getElementsByTagName("GIVEN").getLength() ; j++) {
                            given.add(element.getElementsByTagName("GIVEN").item(j).getTextContent());
                        }
                    //create and put the node in hashmap
                    nodes.put(name,new myNode(name,table,outcome,given));
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        //after creating all the nodes I add them their children,parents and create their table
        for (String myName : names) {
            myNode current = nodes.get(myName);
            for (int j = 0; j < current.givenS.size(); j++) {
                String parentName = current.givenS.get(j);
                myNode parent = nodes.get(parentName);
                current.given.add(parent);
                if (parent != null)
                parent.child.add(current);
            }
            current.makeTable();
        }
        return nodes;
    }

}