/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Pipeline;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.Bin;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.GhostPad;
import org.gstreamer.Pad;
import org.gstreamer.State;
import org.gstreamer.elements.FakeSink;

/**
 * @author Dimitar
 */
public class MulticastReceivingBin extends Bin {
    //+------------------------------------------------------------------------+
    //|************************    VARIABLES     ******************************|
    //+------------------------------------------------------------------------+
    //Variables declaration corresponding to elements of the bin.
    private final Element udpSource;
    private final Element rtpBin;
    private final Element adder;
    
    //Variable declaration corresponding to pads of the bin.
    private final Pad source;

    //+------------------------------------------------------------------------+
    //|**********************      CONSTRUCTOR     ****************************|
    //+------------------------------------------------------------------------+

    /**
     * Constructor creating a BinMulticastReceiving (udpReceiver, rtpBin, adder).
     * 
     * @param ip is the multi-cast IP address.
     * @param port is the multi-cast port number on the server.
     * @param ssrcIgnore is a variable to manage echo.
     */
    public MulticastReceivingBin(String ip, int port, final long ssrcIgnore) {
        // call parent constructor of Pipeline.
        super("Room_" + ip);

        //Manufacture of elements.
        udpSource = ElementFactory.make("udpsrc", null);
        udpSource.set("multicast-group", ip);
        udpSource.set("auto-multicast", true);
        udpSource.set("port", port);

        udpSource.getStaticPad("src").setCaps(
                Caps.fromString("application/x-rtp,"
                        + "media=(string)audio,"
                        + "clock-rate=(int)8000,"
                        + "encoding-name=(string)PCMU, "
                        + "payload=(int)0, "
                        + "ssrc=(guint)1350777638, "
                        + "clock-base=(guint)2942119800, "
                        + "seqnum-base=(guint)47141"
                ));

        rtpBin = ElementFactory.make("gstrtpbin", null);
        adder = ElementFactory.make("liveadder", null);

        rtpBin.connect(new Element.PAD_ADDED() {
            @Override
            public synchronized void padAdded(Element element, Pad pad) {

                if (pad.getName().startsWith("recv_rtp_src")) {

                    if (pad.getName().contains(String.valueOf(ssrcIgnore))) {
                        //Route our echo sound in a fake sink.
                    	System.out.println("echoo1");
                        Element fakesink = new FakeSink((String) null);
                        MulticastReceivingBin.this.add(fakesink);
                        fakesink.syncStateWithParent();
                        pad.link(fakesink.getStaticPad("sink"));
                    } 
                    //else {
                    	System.out.println("echoo2");
                        //link a decoder to the new client flux.
                        DecoderBin decoder = new DecoderBin(true);
                        MulticastReceivingBin.this.add(decoder);
                        decoder.syncStateWithParent();
                        pad.link(decoder.getStaticPad("sink"));
                        Pad adderPad = adder.getRequestPad("sink%d");
                        decoder.getStaticPad("src").link(adderPad);
                    //}
                }
            }
        });

        //Adding elements together.
        addMany(udpSource, rtpBin, adder);

        //Creation of Pads for this bin.
        source = new GhostPad("src", adder.getStaticPad("src"));
        
        //Adding new Pads.
        addPad(source);

        //Linking elements together.
        Pad pad = rtpBin.getRequestPad("recv_rtp_sink_0");
        udpSource.getStaticPad("src").link(pad);

        //Pause the bin.
        pause();
    }

    //+------------------------------------------------------------------------+
    //|**********************     METHODS     *********************************|
    //+------------------------------------------------------------------------+
    /**
     * Unlink the bin and remove it.
     */
    public void dropIt() {
      try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            Logger.getLogger(SendingPipeline.class.getName()).log(Level.SEVERE, null, ex);
        }
        Pad downstreamPeer = source.getPeer();
        this.setState(State.NULL);
        ((Bin) this.getParent()).remove(this);
        downstreamPeer.getParentElement().releaseRequestPad(downstreamPeer);
    }
    //***************************   end   **************************************
}
