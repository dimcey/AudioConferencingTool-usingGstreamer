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
import org.gstreamer.PadDirection;

/**
 * @author Dimitar
 */
public class DecoderBin extends Bin {
    
    //+------------------------------------------------------------------------+
    //|************************    VARIABLES     ******************************|
    //+------------------------------------------------------------------------+
    //Variables declaration corresponding to elements of this bin.
    private Element decoder;
    private Element convert;
    private Element rtpPcMuDepay;

    //Variables declaration corresponding to pads of this bin.
    private Pad source;
    private Pad sink;

    //+------------------------------------------------------------------------+
    //|**********************      CONSTRUCTOR     ****************************|
    //+------------------------------------------------------------------------+
    /**
     * Constructor creating a DecoderBin (rtpPcMuDepay, decoder, converter).
     *
     * @param cleanlyRelease true if multicast decoder, otherwise false.
     */
    public DecoderBin(boolean cleanlyRelease) {
        // call parent constructor of Pipeline.
        super();
        
        //Manufacture of elements
        rtpPcMuDepay = ElementFactory.make("rtppcmudepay", null);
        decoder = ElementFactory.make("mulawdec", null);
        convert = ElementFactory.make("audioconvert", null);
        
        //Adding elements together.
        this.addMany(rtpPcMuDepay, decoder, convert);
       
        //Linking elements together.
        Bin.linkMany(rtpPcMuDepay, decoder, convert);
        

        //Creation of Pads for this bin.
        sink = new GhostPad("sink", rtpPcMuDepay.getStaticPad("sink"));
        source = new GhostPad("src", convert.getStaticPad("src"));
        
        //Adding new Pads.
        this.addPad(sink);
        this.addPad(source);
        
        //Unlink listener under check control (need to be multicast).
        if(cleanlyRelease) {
            sink.connect(new GhostPad.UNLINKED() {
                @Override
                public void unlinked(Pad complainer, Pad gonePad) {
                    if (gonePad.getDirection().equals(PadDirection.SRC)) {
                        if(DecoderBin.this != null)
                            DecoderBin.this.dropIt();
                    }
                }
            });
        }
    }

    //+------------------------------------------------------------------------+
    //|**********************     METHODS     *********************************|
    //+------------------------------------------------------------------------+
    /**
     * Unlink the bin and remove it.
     */
    public void dropIt() {
        Pad sourcePad = source.getPeer();
        sourcePad.getParentElement().releaseRequestPad(sourcePad);
        ((Bin) this.getParent()).remove(this);
    }

}
