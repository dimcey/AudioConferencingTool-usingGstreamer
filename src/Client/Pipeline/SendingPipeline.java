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
public class SendingPipeline extends Pipeline {
    //+------------------------------------------------------------------------+
    //|************************    VARIABLES     ******************************|
    //+------------------------------------------------------------------------+
    //Variables declaration corresponding to elements of the bin.
    final Element source = ElementFactory.make("alsasrc", "alsasrc");    
    final Element tee = ElementFactory.make("tee", "tee");
    
    //Variables declaration corresponding to sub-bins of this bin.
    private TransmittingBin multicastBin = null;
    private TransmittingBin unicastBin = null;

    //+------------------------------------------------------------------------+
    //|**********************      CONSTRUCTOR     ****************************|
    //+------------------------------------------------------------------------+
    /**
     * Constructor creating the base of the sending pipeline (source, tee).
    */
    public SendingPipeline() {
        // call parent constructor of Pipeline.
        super("TransmittingPipeline"); 
        
        //Adding elements together.
        addMany(source, tee);
        
        //Linking elements together.
        Pipeline.linkMany(source, tee);

    }

    //+------------------------------------------------------------------------+
    //|**********************     METHODS     *********************************|
    //+------------------------------------------------------------------------+
    //*******************    Start/Stop multicast     **************************
    
    /**
     * Plug a multi-cast bin to the sending pipeline.
     * 
     * @param ip is the IP address of the server.
     * @return the SSRC (or -1 if error)
     */
    public long startStreamingToMulticast(String ip) {
        if (multicastBin == null) {
            
            //Manufacture of elements.
            multicastBin = new TransmittingBin(ip, Constants.MULTICAST_PORT, true, "MULTICAST_TB");

            //Adding elements together.
            add(multicastBin);
            
            //States Synchronization with the parent bin.
            multicastBin.syncStateWithParent();

            //Linkaging elements together.
            tee.getRequestPad("src%d").link(multicastBin.getStaticPad("sink"));
            
            //Play the pipeline.
            play();
            
            //Return the SSRC to differentiate different multicast transmission.
            return multicastBin.getSSRC();
        }
        return -1;
    }

    /**
     * Unplug the multi-cast bin to the sending pipeline.
     */
    public void stopStreamingToMulticast() {
        if (multicastBin != null) {
            multicastBin.dropIt();
            multicastBin = null;
        }
    }
    //***************************   end   **************************************
    //*******************    Start/Stop unicast     ****************************
    
    /**
     * Plug an uni-cast bin to the sending pipeline.
     * 
     * @param ip is the IP address of the server.
     */
    public void startStreamingToUnicast(String ip) {
        if (unicastBin == null) {
            
            //Manufacture of elements.
            unicastBin = new TransmittingBin(ip, Constants.UNICAST_PORT, false, "UNICAST_TB");
            
            //Adding elements together.
            add(unicastBin);
            
            //States Synchronization with the parent bin.
            unicastBin.syncStateWithParent();
            
            //Linkaging elements together.
            tee.getRequestPad("src%d").link(unicastBin.getStaticPad("sink"));
            
            //Play the pipeline
            play();
        }
    }

    /**
     * Unplug the uni-cast bin to the sending pipeline.
     */
    public void stopStreamingToUnicast() {
        if (unicastBin != null) {
            unicastBin.dropIt();
            unicastBin = null;
        }
    }
    //***************************   end   **************************************
    //*******************    isMulticast/isUnicast     *************************
    
    /**
     * Test if the sending pipeline is Multi-cast.
     * 
     * @return true or false.
     */
    public boolean isStreamingToMulticast() {
        return (multicastBin != null);
    }

    /**
     * Test if the sending pipeline is Uni-cast.
     * 
     * @return true or false.
     */
    public boolean isStreamingToUnicast() {
        return (unicastBin != null);
    }
    //***************************   end   **************************************
    //*****************    Mute/Unmute the micro    ****************************

    /**
     * Mute the micro.
     */
    public void mutemicro() {
        source.set("volume",0.0);
    }
    
    /**
     * Un-mute the micro.
     */
    public void unmutemicro() {
        source.set("volume",1.0);
    }

    //***************************   end   **************************************
    //*********    Print pipeline for information/debugging     ****************

    /**
     * Print the pipeline in the console for information/debugging purpose.
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
