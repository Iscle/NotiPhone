package me.iscle.notiphone.Handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import me.iscle.notiphone.Model.OnlineClockSkin;

public class ClockSkinXMLHandler extends DefaultHandler {

    private ArrayList<OnlineClockSkin> onlineClockSkins;
    private OnlineClockSkin tempOnlineClockSkin;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();

        this.onlineClockSkins = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        switch (qName) {
            case "clockskin":
                tempOnlineClockSkin = new OnlineClockSkin();
                break;
            case "name":
                tempOnlineClockSkin.setName(attributes.getValue("id"));
                break;
            case "skinid":
                tempOnlineClockSkin.setSkinid(attributes.getValue("id"));
                break;
            case "file":
                tempOnlineClockSkin.setFile(attributes.getValue("id"));
                break;
            case "customer":
                tempOnlineClockSkin.setCustomer(attributes.getValue("id"));
                break;
            case "type":
                tempOnlineClockSkin.setType(attributes.getValue("id"));
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (qName.equals("clockskin")) {
            onlineClockSkins.add(tempOnlineClockSkin);
        }
    }

    public ArrayList<OnlineClockSkin> getOnlineClockSkins() {
        return onlineClockSkins;
    }
}
