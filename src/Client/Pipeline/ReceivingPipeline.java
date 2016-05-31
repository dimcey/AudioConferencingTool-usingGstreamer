/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Pipeline;

import java.util.Iterator;
import java.util.List;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import Middleware.Constants;

/**
 * @author Dimitar
 */
public class ReceivingPipeline extends Pipeline {
    //+------------------------------------------------------------------------+
    //|************************    VARIABLES     ******************************|
    //+------------------------------------------------------------------------+
    //Variables declaration corresponding to elements of this bin.
    private final Element adder = ElementFactory.make("liveadder", "liveadder");
    private final Element sink = ElementFactory.make("alsasink", "alsasink");
    
    //Variables declaration corresponding to sub-bins of this bin.
    private MulticastReceivingBin multicast = null;
    private UnicastReceivingBin unicast = null;
    
    //+------------------------------------------------------------------------+
    //|**********************      CONSTRUCTOR     ****************************|
    //+------------------------------------------------------------------------+
    /**
     * Constructor creating the base of the receiving pipeline (adder, sink).
     */
    public ReceivingPipeline() {
        // call parent constructor of Pipeline.
        super("ReceivingPipeline");
        
        //Adding elements together.
        addMany(adder, sink);
        
        //Linking elements together.
        linkMany(adder, sink);
        
        //Play the Bin
        play();
    }
    
    //+------------------------------------------------------------------------+
    //|**********************     METHODS     *********************************|
    //+------------------------------------------------------------------------+
    //*******************    Start/Stop multicast     **************************
    /**
     * Plug a multi-cast bin to the receiving pipeline.
     * 
     * @param ip is the IP address of the server.
     * @param ssrcIgnore is a variable to manage echo effect.
     */
    public void startReceivingFromMulticast(String ip, long ssrcIgnore) {
        if(multicast == null) {
            //Manufacture of elements.
            multicast = new MulticastReceivingBin(ip, Constants.MULTICAST_PORT, ssrcIgnore);

            //Adding elements together.
            add(multicast);
            
            //States Synchronization with the parent bin.
            multicast.syncStateWithParent();

            //Linking elements together.
            multicast.link(adder);
        }
    }

    /**
     * Unplug the multi-cast bin to the receiving pipeline.
     */
    public void stopReceivingFromMulticast() {
        if(multicast != null) {
            multicast.dropIt();
            multicast = null;            
        }
    }
    //***************************   end   **************************************
    //*******************    Start/Stop unicast     ****************************
    
    /**
     * Plug an uni-cast bin to the receiving pipeline.
    */
    public void startReceivingFromUnicast() {
        if(unicast == null) {
            //Manufacture of elements.
            unicast = new UnicastReceivingBin(Constants.UNICAST_PORT, adder);

            //Adding elements together.
            add(unicast);
            
            //States Synchronization with the parent bin.
            unicast.syncStateWithParent();
        }
    }
    
    /**
     * Unplug the uni-cast bin to the receiving pipeline.
     */
    public void stopReceivingFromUnicast() {
        if(unicast != null) {
            unicast.dropIt();            
            unicast = null;
        }
    }
    //***************************   end   **************************************
    //**********************    Check state     ********************************

    /**
     * Test is you are receiving from Multi-cast
     * @return true or false
     */
        public boolean isReceivingFromMulticast() {
        return (multicast != null);
    }

    /**
     * Test is you are receiving from Uni-cast
     * @return true or false
     */
    public boolean isReceivingFromUnicast() {
        return (unicast != null);
    }
    
    /**
     * Mute the sound of the speakers.
     */
    public void muteSound() {
        sink.set("volume", "mute");
    }
    
    /**
     * Un-mute the sound of the speakers.
     */
    public void unmuteSound() {
        sink.set("volume",1.0);
    }
    //***************************   end   **************************************
    //*********    Print pipeline for information/debugging     ****************
    
    /**
     * Print the Pipeline in the console for information/debugging purpose.
     */
    public void printPipeline() {

        List<Element> elements = this.getElements();

        if (elements.size() > 0) {
            Iterator<Element> elemiter = elements.iterator();
            Element e = null;
            while (elemiter.hasNext()) {
                e = (Element) elemiter.next();

                List<Pad> pads = e.getPads();

                if (pads.size() > 0) {
                    Iterator<Pad> paditer = pads.iterator();
                    Pad pad = null;
                    while (paditer.hasNext()) {
                        pad = (Pad) paditer.next();
                        System.out.print(e + " " + pad.getDirection());
                        System.out.println("\t" + pad.getCaps());
                    }
                }
            }
        }
    }
    //***************************   end   **************************************
}
