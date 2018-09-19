package xmltest;

import java.util.*;
import org.dom4j.*;

public class findDevice
{
    Map<Element, String> T2D_map;   //存放端子与对应节点
    List<String> NodeIDList;    //存放需要查看的节点名称
    private int NodeIDListsize;
    Document document;
    private Element result;
    private Map<String, Element> ResultMap; //搜索结果集
    private Xmlclassify xmlclassify;

    Map<device, String> DNmap=new HashMap<device,String>();    //存放设备与节点的映射关系：[(设备类型,设备列表下表),节点ID]
    Map<String,node> nodeAttrMap=new HashMap<String,node>();    //存放节点信息[nodeID,node]
    private int level=0;    //设备树层数

    public findDevice(Xmlclassify xmlclassify, Element Device) {

        this.T2D_map = new HashMap<Element, String>();
        this.NodeIDList = new ArrayList<String>();
        this.NodeIDListsize = 0;
        this.document = DocumentHelper.createDocument();
        this.result = this.document.addElement("nobreaker");
        this.ResultMap = new HashMap<String, Element>();
        this.xmlclassify = xmlclassify;
        this.ResultMap.put("Breaker", null);
        this.ResultMap.put("BusbarSection", null);
        //找出设备对应的端子及节点
        for (Element fooElement : this.Device2Terminal(null, Device)) {
            this.Terminal2Node(fooElement, Device);
        }

    }

    public findDevice(Xmlclassify xmlclassify, Element Device, List<String> searchobject) {
        this.T2D_map = new HashMap<Element, String>();
        this.NodeIDList = new ArrayList<String>();
        this.NodeIDListsize = 0;
        this.document = DocumentHelper.createDocument();
        this.result = this.document.addElement("nobreaker");
        this.ResultMap = new HashMap<String, Element>();
        this.xmlclassify = xmlclassify;
        for (String SOname : searchobject) {
            this.ResultMap.put(SOname, null);
        }
        for (Element fooElement : this.Device2Terminal(null, Device)) {
            this.Terminal2Node(fooElement, Device);
        }
    }

    //找出Device对应的端子，并排除上一个端子
    public List<Element> Device2Terminal(Element Terminallast, Element Device) {
        List<Element> Terminalnext = new ArrayList<Element>();
        if (Device != null) {
            if (Device.element("ConductingEquipment.Terminals") == null) {
                if ("".equals(Device.attributeValue("ID"))) {
                    if (Device.element("IdentifiedObject.mRID") != null) {
                        for (Element terminal : this.xmlclassify.getchildElementList("Terminal")) {
                            if (Device.elementText("IdentifiedObject.mRID").equals(terminal.element("Terminal.ConductingEquipment").attributeValue("resource").replace("#", ""))) {
                                Terminalnext.add(terminal);
                            }
                        }
                    }
                    else {
                        for (Element terminal : this.xmlclassify.getchildElementList("Terminal")) {
                            if (terminal.elementText("Naming.name").contains(Device.elementText("Naming.name"))) {
                                Terminalnext.add(terminal);
                            }
                        }
                    }
                }
                else {
                    for (Element terminal : this.xmlclassify.getchildElementList("Terminal")) {
                        if (Device.attributeValue("ID").equals(terminal.element("Terminal.ConductingEquipment").attributeValue("resource").replace("#", ""))) {
                            Terminalnext.add(terminal);
                        }
                    }
                }
            }
            else {
                for (Iterator i=Device.selectNodes("cim:ConductingEquipment.Terminals").iterator();i.hasNext();) {
                    Element fooElement=(Element)i.next();
                    String terminalID = fooElement.attributeValue("resource").replace("#", "");
                    for (Element terminal2 : this.xmlclassify.getchildElementList("Terminal")) {
                        if (terminalID.equals(terminal2.attributeValue("ID"))) {
                            Terminalnext.add(terminal2);
                        }
                    }
                }
            }
            if (Terminallast != null) {
                for (Iterator i = Terminalnext.iterator();i.hasNext();) {
                    Element fooElement = (Element) i.next();
                    if (fooElement.attributeValue("ID").equals(Terminallast.attributeValue("ID"))) {
                        i.remove();
                        break;
                    }
                }
            }
        }
        return Terminalnext;
    }

    //找出端子对应的node,并将node添加至addNodeIDList，T2D_map
    public void Terminal2Node(Element Terminal,Element Device) {
        if (Terminal != null && !this.Tisused(Terminal) && Terminal.element("Terminal.ConnectivityNode") != null) {
            for (Iterator i=Terminal.selectNodes("cim:Terminal.ConnectivityNode").iterator();i.hasNext();) {
                Element fooElement=(Element)i.next();
                String NodeID = fooElement.attributeValue("resource").replace("#", "");
                this.putTerminal(Terminal, NodeID);
                this.addNodeIDList(NodeID);

                nodeAttrMap.put(NodeID,new node());
            }
        }
    }

    //找出Node连接的所有端子
    public List<Element> Node2Terminal(String NodeID) {
        List<Element> terminallist = new ArrayList<Element>();
        for (Element terminal : this.xmlclassify.getchildElementList("Terminal")) {
            if (terminal.element("Terminal.ConnectivityNode") !=null && NodeID.equals(terminal.element("Terminal.ConnectivityNode").attributeValue("resource").replace("#", ""))) {
                if(!this.Tisused(terminal)){
                    terminallist.add(terminal);
                }
            }
        }
//        for (Iterator i = terminallist.iterator();i.hasNext();) {
//            Element terminal = (Element) i.next();
//            if (this.Tisused(terminal)) {
//                i.remove();
//            }
//        }
        return terminallist;
    }

    //找出端子所属的设备
    public Element Terminal2Device(Element Terminallast) {
        Element Device = null;
        if (Terminallast != null) {
            String DeviceID = Terminallast.element("Terminal.ConductingEquipment").attributeValue("resource").replace("#", "");
            for (String childkey : this.xmlclassify.getchildElementsmap().keySet()) {
                if (!childkey.equals("Terminal") && !childkey.equals("ConnectivityNode") && !childkey.equals("BaseVoltage") && !childkey.equals("VoltageLevel")) {
                    List<Element> childlist = this.xmlclassify.getchildElementList(childkey);
                    for (int i=0;i<childlist.size();i++) {
                        Element device=childlist.get(i);
                        if (DeviceID.equals(device.attributeValue("ID"))) {
                            Device= device;

                            break;
                        }else {
                            if (DeviceID.equals(device.elementText("IdentifiedObject.mRID"))) {
                                Device= device;
                                break;
                            }
                        }
                    }
                }
                if(Device !=null){
                    break;
                }
            }
            if (Device.getName().equals("Breaker") || Device.getName().equals("BusbarSection")) {
                this.result = Device;
                if(this.ResultMap.get(Device.getName()) == null){
                    this.ResultMap.put(Device.getName(), Device);
                }

            }
        }
        return Device;
    }

    //一个T-D-T-N步骤
    public void onethread(Element Terminallast,String NodeID) {
        Element Device = this.Terminal2Device(Terminallast);
        List<Element> Terminalnext = this.Device2Terminal(Terminallast, Device);
        for (Element fooElement : Terminalnext) {
            this.Terminal2Node(fooElement,Device);
        }
    }

    //开始查找设备
    public void StartFindDevice() {
        if (this.Notend() && this.NodeIDList.size() > 0) {
            while (this.NodeIDListsize < this.NodeIDList.size()) {
                String NodeID = this.NodeIDList.get(this.NodeIDListsize);
                if (NodeID != null) {
//                    if(unexpectNode(NodeID)){
//
//                    }
                    for (Iterator j = this.Node2Terminal(NodeID).iterator();j.hasNext() && this.Notend();) {
                        Element terminal = (Element)j.next();
                        this.onethread(terminal,NodeID);
                    }
                }
                ++this.NodeIDListsize;
            }
        }
    }

    public void addNodeIDList(String NodeID) {
        if (!this.NodeIDList.contains(NodeID)) {
            this.NodeIDList.add(NodeID);
        }
    }

    public boolean Tisused(Element Terminal) {
        return this.T2D_map.containsKey(Terminal);
    }

    public void putTerminal(Element Terminal, String NodeID) {
        this.T2D_map.put(Terminal, NodeID);
    }

    public Element getResult() {
        return this.result;
    }

    public Map<String, Element> getResultMap() {
        return this.ResultMap;
    }

    public boolean Notend() {
        return !this.result.getName().equals("BusbarSection");
    }

    public boolean allisend() {
        return !this.ResultMap.containsValue(null);
    }

    public boolean unexpectNode(String NodeID){
        for (Iterator j = this.Node2Terminal(NodeID).iterator();j.hasNext();) {
            Element terminal = (Element)j.next();
            if("BusbarSection".equals(terminal.getName()) && !this.ResultMap.containsKey("Breaker")){
                return false;
            }
        }
        return true;
    }

}
