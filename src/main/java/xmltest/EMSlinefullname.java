package xmltest;

import java.io.*;
import org.dom4j.*;
import java.util.concurrent.*;
import java.util.*;

public class EMSlinefullname {
    private Writer TXTout;
    private Xmlclassify xmlclassify;

    public EMSlinefullname(File fileout, Xmlclassify xmlclassify) {
        this.xmlclassify = xmlclassify;
        try {
            (this.TXTout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileout), "GBK"))).write("变电站名称     线路名称    线路ID    开关名称    开关ID    母线名称    母线ID\r\n");
            this.TXTout.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //多线程，用线程池管理，每条线路解析为一个线程
    public void energyconsumerThread() {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(8); //Runtime.getRuntime().availableProcessors()
        ThreadPoolExecutor tpe = (ThreadPoolExecutor)fixedThreadPool;
        for (Element fooElement : this.xmlclassify.getchildElementList("EnergyConsumer")) {
            String EnergyConsumerName = fooElement.elementText("Naming.name").replaceAll("\\s*", "");
            if (!EnergyConsumerName.toLowerCase().contains("eq")) {
//                try {
//                    EnergyConsumerTXT(fooElement);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EnergyConsumerTXT(fooElement);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        int activeCount;
        do {
            activeCount = tpe.getActiveCount();
        } while (activeCount != 0);
        fixedThreadPool.shutdown();
        try {
            this.TXTout.flush();
            this.TXTout.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void EnergyConsumerTXT(final Element fooElement) throws IOException {
        String substation_name = null;
        String EnergyConsumer_NamingName = null;
        String EnergyConsumer_ID = null;
        Element breaker = null;
        String breakername = null;
        String breakerID = null;
        Element BusbarSection = null;
        String BusbarSectionname = null;
        String BusbarSectionID = null;
        EnergyConsumer_NamingName = fooElement.elementText("Naming.name").replaceAll("\\s*", "");
        Element substation = this.findSubstation(fooElement);
        //获取变电站名称
        if (substation != null) {
            substation_name = substation.elementText("Naming.name").replaceAll("\\s*", "");
        }
        if ("".equals(fooElement.attributeValue("ID"))) {
            if (fooElement.element("IdentifiedObject.mRID") != null) {
                EnergyConsumer_ID = fooElement.elementText("IdentifiedObject.mRID");
            }
        }
        else {
            EnergyConsumer_ID = fooElement.attributeValue("ID");
        }
        findDevice findbreaker = new findDevice(this.xmlclassify, fooElement);
        findbreaker.StartFindDevice();
        Map<String, Element> ResultMap = new HashMap<String, Element>();
        ResultMap = findbreaker.getResultMap();
        breaker = ResultMap.get("Breaker");
        BusbarSection = ResultMap.get("BusbarSection");
        if (breaker != null) {
            breakername = breaker.elementText("Naming.name");
            breakerID = breaker.attributeValue("ID");
        }
        if (BusbarSection != null) {
            BusbarSectionname = BusbarSection.elementText("Naming.name");
            BusbarSectionID = BusbarSection.attributeValue("ID");
        }
        this.TXTout.write(substation_name + "    " + EnergyConsumer_NamingName + "    " + EnergyConsumer_ID + "    " + breakername + "    " + breakerID + "    " + BusbarSectionname + "    " + BusbarSectionID + "\r\n");
        //this.TXTout.flush();
    }

    //寻找设备对应的变电站
    public Element findSubstation(Element Device) {
        Element Substaion = null;
        String memberID =null;
        if (Device != null) {
            if(Device.element("Equipment.MemberOf_EquipmentContainer") != null){
                memberID = Device.element("Equipment.MemberOf_EquipmentContainer").attributeValue("resource").replace("#", "");
            }else if(Device.element(".MemberOf_EquipmentContainer") != null){
                memberID = Device.element(".MemberOf_EquipmentContainer").attributeValue("resource").replace("#", "");
            }
            //String memberID = Device.element("Equipment.MemberOf_EquipmentContainer").attributeValue("resource").replace("#", "");
            for(Element bay:this.xmlclassify.getchildElementList("Bay")){
                if (memberID.equals(bay.attributeValue("ID"))) {
                    memberID=bay.element("Bay.MemberOf_Substation").attributeValue("resource").replace("#", "");
                }
            }
            for (Element substation : this.xmlclassify.getchildElementList("Substation")) {
                if (memberID.equals(substation.attributeValue("ID"))) {
                    Substaion = substation;
                    return Substaion;
                }
            }
            for (Element VoltageLevel : this.xmlclassify.getchildElementList("VoltageLevel")) {
                if (memberID.equals(VoltageLevel.attributeValue("ID"))) {
                    memberID = VoltageLevel.element("VoltageLevel.MemberOf_Substation").attributeValue("resource").replace("#", "");
                }
                for (Element substation2 : this.xmlclassify.getchildElementList("Substation")) {
                    if (memberID.equals(substation2.attributeValue("ID"))) {
                        Substaion = substation2;
                        return Substaion;
                    }
                }
            }

        }
        return Substaion;
    }

    public Element findVoltageLevel(String VolID) {
        Element VoltageLevel = null;
        return VoltageLevel;
    }
}
