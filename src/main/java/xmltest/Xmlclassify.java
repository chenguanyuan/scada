package xmltest;

import org.dom4j.io.*;
import java.io.*;
import org.dom4j.*;
import java.util.*;

public class Xmlclassify {
    private SAXReader reader;
    Document doc;
    private Map<String, List> childElementsmap=new HashMap<String, List>();

    public Xmlclassify(File FileCim) throws FileNotFoundException, UnsupportedEncodingException, DocumentException {
        this.reader = new SAXReader();
        BufferedReader bufferreader = new BufferedReader(new InputStreamReader(new FileInputStream(FileCim), "GBK"));
        this.reader.setEncoding("GBK");
        this.doc = this.reader.read(bufferreader);
        Element root = this.doc.getRootElement();
        List<Element> childElements = root.elements();
        for (Element ele : childElements) {
            if (!this.childElementsmap.containsKey(ele.getName())) {
                this.childElementsmap.put(ele.getName(), new ArrayList());
            }
            List<Element> childElementList = this.childElementsmap.get(ele.getName());
            childElementList.add(ele);
        }
    }

    public List<Element> getchildElementList(String childElementName) {
        List<Element> childElementList = this.childElementsmap.get(childElementName);
        return childElementList;
    }

    public Map<String, List> getchildElementsmap() {
        return this.childElementsmap;
    }
}
