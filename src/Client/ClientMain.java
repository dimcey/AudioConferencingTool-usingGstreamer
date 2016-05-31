package Client;

import org.gstreamer.Gst;
 
import GUI.GUIClient;

public class ClientMain {

	public static void main(String[] args) {
		args = Gst.init("AudioRecord", args);
		
		 java.awt.EventQueue.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                new GUIClient().setVisible(true);
	            }
	        });
	}

}
