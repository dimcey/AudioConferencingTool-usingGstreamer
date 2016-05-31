/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Pipeline;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gstreamer.Bin;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;
import org.gstreamer.State;
import org.gstreamer.elements.Tee;
import org.gstreamer.elements.good.RTPBin;

/**
 * @author Dimitar
 */
public class TransmittingBin extends Bin {
    //+------------------------------------------------------------------------+
    //|************************    VARIABLES     ******************************|
    //+------------------------------------------------------------------------+
    //Variables declaration corresponding to subbin of this bin.
    private final EncoderBin encoder = new EncoderBin();
    private final RTPBin rtpBin = new RTPBin("RTPBin");
    
    //Variables declaration corresponding to elements of this bin.
    private final Element udpSink;
    
    //Variables declaration corresponding to pads of this bin.
    private final Pad sink;

    //+------------------------------------------------------------------------+
    //|**********************      CONSTRUCTOR     ****************************|
    //+------------------------------------------------------------------------+
    /**
     * Constructor creating a BinTransmitting (encoderBin, rtpBin, udpSink).
     * 
     * @param ip is the IP address of the server.
     * @param port is the port number of the server.
     * @param isMulticast true or false this bin is for multi-cast.
     * @param name is the name of this bin.
     */
    public TransmittingBin(String ip, int port, boolean isMulticast, String name) {
        // call parent constructor of Pipeline.
        super(name);
        
        //States Synchronization with the parent bin.
        encoder.syncStateWithParent();
        
        
        Pad rtpSink = rtpBin.getRequestPad("send_rtp_sink_0");
        
        //Manufacture of elements.
        udpSink = ElementFactory.make("udpsink", null);
        udpSink.set("host", ip);
        udpSink.set("port", port);
        if (isMulticast) {
            udpSink.set("auto-multicast", true);
        }
        
        //Adding elements together.
        addMany(encoder, rtpBin, udpSink);
        
        //Creation of Pads for this bin.
        sink = new GhostPad("sink", encoder.getStaticPad("sink"));
        
        //Check if active
        sink.setActive(true);

        //Adding new Pads.
        addPad(sink);
        
        //Linking elements together.
        encoder.getStaticPad("src").link(rtpSink);
        rtpBin.getStaticPad("send_rtp_src_0").link(udpSink.getStaticPad("sink"));

    }

    //+------------------------------------------------------------------------+
    //|**********************     METHODS     *********************************|
    //+------------------------------------------------------------------------+
    /**
     * Get the unique ID of a client.
     * 
     * @return the unique SSRC ID of the client.
     */
    public long getSSRC() {
        String test = null;
        for (Element element : rtpBin.getElements()) {
            if (element.getName().startsWith("rtpsession")) {
                try {
                    test = element.getSinkPads().get(0).getCaps().toString();
                } catch (Exception e) {
                }
            }
        }
        if (test != null) {
            Pattern pattern = Pattern.compile("ssrc=(.*?)([0-9]+);");
            Matcher matcher = pattern.matcher(test);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(2));
            }
        }
        return -1;
    }

    /**
     * Unlink the bin and remove it.
     */
    public void dropIt() {
        //wait for 1,5sec to stabilize states
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            Logger.getLogger(SendingPipeline.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //get (src) pad connected to -> sink
        Pad sourcePad = sink.getPeer();
        //get the parent tee element
        Tee teeElement = ((Tee) sink.getPeer().getParent());     
        
        //transmitting bin element
        Bin parentBin = ((Bin) this.getParent()); 
        
        //upstreamPeer.setActive(false);
        sourcePad.setBlocked(true);
        ((Bin) this.getParent()).unlink(this);

        this.setState(State.NULL);
        ((Bin) this.getParent()).remove(this);
        
        if (teeElement.getSrcPads().size() == 1) {
            parentBin.setState(State.NULL);
        }
 
        teeElement.releaseRequestPad(sourcePad);
    }
    //***************************   end   **************************************
}
