/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Client.Pipeline;

import org.gstreamer.Bin;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;

/**
 * @author Dimitar
 */
public class EncoderBin extends Bin {
    //+------------------------------------------------------------------------+
    //|************************    VARIABLES     ******************************|
    //+------------------------------------------------------------------------+
    //Variables declaration corresponding to elements of the bin.
    private Element queue;
    private Element mulawEncoder;
    private Element rtpPcMuPay;
    
    //Variables declaration corresponding to pads of the bin.
    private Pad source;
    private Pad sink;

    //+------------------------------------------------------------------------+
    //|**********************      CONSTRUCTOR     ****************************|
    //+------------------------------------------------------------------------+
    /**
     * Constructor creating a EncooderBin (queue,mulawEncoder, rtpPcMuPay).
     */
    public EncoderBin() {
        // call parent constructor of Pipeline.
        super();
        
        // Manufacture of elements.
        queue    = ElementFactory.make("queue", null);
        mulawEncoder =  ElementFactory.make("mulawenc", "mulawenc");
        rtpPcMuPay = ElementFactory.make("rtppcmupay", null);
        
        //Adding elements together.
        this.addMany(queue, mulawEncoder, rtpPcMuPay);
        
        //Linking elements together.
        Bin.linkMany(queue, mulawEncoder, rtpPcMuPay);
        
        //Creation of Pads for this bin.
        sink = new GhostPad("sink", queue.getStaticPad("sink"));
        source = new GhostPad("src", rtpPcMuPay.getStaticPad("src"));
        
        //Test if active
        sink.setActive(true);
        source.setActive(true);  
        
        //Adding new Pads.
        this.addPad(sink);
        this.addPad(source);   
    }
    //***************************   end   **************************************
}
