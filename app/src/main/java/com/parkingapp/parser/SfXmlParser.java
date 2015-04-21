package com.parkingapp.parser;

import com.parkingapp.utility.Constants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * class for XML parsing
 * Created by pooja on 4/17/2015.
 */
public class SfXmlParser {

    private DocumentBuilder db;

    private Document document;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public DocumentBuilder getParser() {
        return db;
    }

    public void createParser() {

        Document dom = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            // dom = db.parse(inputStream);

        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }
    }

    /*
     * parser given XML and returns the ArrayList containing parsed data
     * @return an arraylist containing list of parking locations
     */
    public List<SFParkBean> parseXML() {

        List<SFParkBean> sfParkLocations = new ArrayList<SFParkBean>();

        //get the root element
        Element docEle = document.getDocumentElement();

        //get a nodelist of elements
        NodeList nl = docEle.getElementsByTagName(Constants.XML_TAG_AVL);
        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) {

                //get the element
                Element el = (Element)nl.item(i);

                sfParkLocations.add(getBean(el));

            }
        }
    return sfParkLocations;
    }

    private SFParkBean getBean(Element sfparkEl) {

        String type = getTextValue(sfparkEl,Constants.XML_TAG_TYPE);
        String  name = getTextValue(sfparkEl,Constants.XML_TAG_NAME);
        String address  = getTextValue(sfparkEl,Constants.XML_TAG_DESC);
        String contactNumber  = getTextValue(sfparkEl,Constants.XML_TAG_TEL);
        String location = getTextValue(sfparkEl,Constants.XML_TAG_LOC);
        double latitude = 0;
        double longitude = 0;
        if(location != null || !location.isEmpty())
        {
            String[] locations = location.split(",");

            if(locations[0] != null || !locations[0].isEmpty()) {
                latitude = Double.parseDouble(locations[0]);
            }

            if(locations[1] != null || !locations[1].isEmpty()) {
                longitude = Double.parseDouble(locations[1]);
            }
        }
        List<OperationHoursBean> oprHourList = getOprHours(sfparkEl);
    /*    System.out.println("name  " + name + " address " + address + "contact   " + contactNumber + " latitude " + latitude
                + " longitude " + longitude);
*/
        SFParkBean sfParkBean = new SFParkBean();
        sfParkBean.setType(type);
        sfParkBean.setName(name);
        sfParkBean.setAddress(address);
        sfParkBean.setContactNumber(contactNumber);
        sfParkBean.setLatitude(latitude);
        sfParkBean.setLongitude(longitude);
        sfParkBean.setOperationHours(oprHourList);

        return sfParkBean;
    }
    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if(nl != null && nl.getLength() > 0) {
            Element el = (Element)nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        return textVal;
    }

    private List<OperationHoursBean> getOprHours(Element ele) {

       List<OperationHoursBean> oprBeanList = new ArrayList<OperationHoursBean>();
        NodeList nl = ele.getElementsByTagName("OPS");
        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) {
                //get the element
                Element e = (Element)nl.item(i);
                String from = getTextValue(e,Constants.XML_TAG_FROM);
                String to = getTextValue(e,Constants.XML_TAG_TO);
                String begin = getTextValue(e,Constants.XML_TAG_BEG);
                String end = getTextValue(e,Constants.XML_TAG_END);
                OperationHoursBean oprBean = new OperationHoursBean();
                oprBean.setFromDay(from);
                oprBean.setToDay(to);
                oprBean.setStartTime(begin);
                oprBean.setEndTime(end);
                oprBeanList.add(oprBean);
            }
        }
        return oprBeanList;
    }

}
