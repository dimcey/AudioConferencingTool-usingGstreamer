package Client;

import java.util.ArrayList;

import GUI.GUICallReceiver;
import GUI.GUICalling;
import GUI.GUIClient;
import GUI.GUILoggin;
import GUI.GUIMulticast;
import Middleware.Constants;
import Middleware.EventListener;
import Middleware.Network;
import Middleware.Network.Client;
import Middleware.Network.Message;
import Middleware.Network.Room;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import Client.ClientHandler;
import Client.ClientOperations;
import Client.Pipeline.ReceivingPipeline;
import Client.Pipeline.SendingPipeline;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList; 
import javax.swing.*;
import org.gstreamer.Gst;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.util.ArrayList;
import Middleware.Network;
import Middleware.Network.*;
import Middleware.Constants;

public class ClientOperations implements EventListener {

	static GUIClient myGui;
    static ClientHandler client;
    String clientName = null;
    String ServerAddress = null;
    static Network.Room[] rooms = null;
    int lastSelected = -1;
    static ArrayList<Network.Client> list;
    static GUICallReceiver callReciever = null;
    static GUICallReceiver callCaller = null;
    static GUICalling calling = null;
    static GUICalling recieving = null;
    static GUIMulticast multicast = null;
    static long ssrc;
    static SendingPipeline transmitterpipe;
    static ReceivingPipeline receiverpipe;
//    Object refreshMsg = null;
	
	public ClientOperations(GUIClient myGui) {
		this.myGui = myGui;
		initClient();
        transmitterpipe = new SendingPipeline();
        receiverpipe = new ReceivingPipeline();
	}

	private void initClient() {
		final GUILoggin initClient = new GUILoggin();

        int result = JOptionPane.showConfirmDialog(null, initClient,
                "Please enter Server Address and Username", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            ServerAddress = initClient.txtSAddress.getText();
            clientName = initClient.txtUsername.getText();
            myGui.setGuiTitle(clientName);
            connect();
        } else {
            System.exit(0);
        }
	}

	private void connect() {
		client = new ClientHandler(ServerAddress, clientName);
        client.setListener(this);
        client.connect();
        try {
            Thread.sleep(100);
            //client.sendMessage(c, "TEST MESSAGE");
        } catch (InterruptedException ex) {
            Logger.getLogger(GUIClient.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

	
	@Override
	public void fireEvent(int message, Object o) {
		switch (message){
		 //new clients liste received
        case Constants.EVT_CLIENT_LIST_RECEIVED:
            list = (ArrayList<Network.Client>) o;
            int i = 0;
            myGui.clearClientList();
            for (Network.Client c : list) {
                if (c.id != client.getID()) {
                	myGui.addNewClients(i, c.name);
                	System.out.println("Client: "+c.name+" added to the ClientList");
                    i++;
                }
            }
            break;
            
            //new rooms array received   
        case Constants.EVT_ROOM_ARRAY_RECEIVED:
            rooms = (Network.Room[]) o;
            refreshList(rooms);
            if (lastSelected != -1) {
                updateRoomClient(lastSelected);
            }
            break;
            
          //new message from another client received 
        case Constants.EVT_MESSAGE_FROM_ANOTHER_CLIENT:
            try {
                Network.Message msg = (Network.Message) o;
                System.out.println(msg.text + "from " + msg.sender.name);
                if (msg.text.equals("calling")) {
                	System.out.println("CALLING MSG");
                    reciever(msg);
                } else if (msg.text.equals("dropped")) {
                	System.out.println("DROPPED MSG");
                    callDropped();
//                    Window w = SwingUtilities.getWindowAncestor(calling);
//                    closeDialog(w);
                } else if (msg.text.equals("ended")) {
                	System.out.println("ENDED MSG");
                    callDropped();
//                    Window w = SwingUtilities.getWindowAncestor(recieving);
//                    closeDialog(w);
                } else if (msg.text.equals("recieved")) {
                	System.out.println("RECEIVED MSG");
                    if (!receiverpipe.isReceivingFromUnicast()) {
                    	System.out.println("The sender is starting to receive stream...");
                        receiverpipe.startReceivingFromUnicast();
                        receiverpipe.printPipeline();
                    }
                } else if (msg.text.equals("rejected")) {
                    if (transmitterpipe.isStreamingToUnicast()) {
                        transmitterpipe.stopStreamingToUnicast();
                        transmitterpipe.printPipeline();
//                        Window w = SwingUtilities.getWindowAncestor(calling);
//                        closeDialog(w);
                    }
                }

            } catch (Exception e) {
                System.out.println(e);
            }

            break;
		}
		
	}

	private void refreshList(Room[] rooms) {
        int i;
        System.out.println("ROOMS:");
        i = 0;
        myGui.clearRooms();
        for (Network.Room r : rooms) {
        	myGui.addNewRooms(i, r.name);
            System.out.print("ROOM: " + r.name +" | ");
            for (Network.Client c : r.clients) {
                System.out.println("-- " + c.name + "BITEN DEBUG MISLAM DEKA E DALI IMA KLIENTI VO SOBATA");
            }
            i++;
        }
	}
	
	public void updateRoomClient(int i) {
		System.out.println("USHTE PO BITEN DEBUG, NE ZNAM KOGA VLAGA TUKA, AMA BI TREBALO OD KOGA KJE BIDE KLIKNATO JOINROOM");
        myGui.setSelectedRoom(i);
		int j = 0;
		myGui.clearMulticastClientList();
        for (Network.Client c : rooms[i].clients) {
        	myGui.addRoomClient(j, c.name);
            System.out.println("-- " + c.name);
            j++;
        }
	}

	public static void makeCall() {
		String selectedClient = myGui.getSelectedClient();
        Network.Client callHim = null;
       
        for (Network.Client c : list) {
        	Object w = c.name;
        	if(list.contains(c.name)){System.out.println("woop");}
            if (c.name == selectedClient) {
                callHim = c;
            }
        }
        //if (callHim.id != client.getID()) {
            callReceiver(callHim);
//        } else {
//            String infoMessage = "You cannot Dial Yourself or Same IP";
//            String titleBar = "Select Another User";
//            JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);
//        }
	}

	private static void callReceiver(final Network.Client callHim) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	            	callCaller = new GUICallReceiver();
	            	callCaller.setCallerCredentials(callHim.name, callHim.id, callHim.ip);
	                final Object[] options = {"Place Call"};
	                int result = JOptionPane.showOptionDialog(null, callCaller, "Call " + callHim.name, JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	                if (result == JOptionPane.YES_OPTION) {
	                    calling(callHim);
	                } else {
	                    //cmdCall.setEnabled(true);
//	                    Window w = SwingUtilities.getWindowAncestor(callCaller);
//	                    closeDialog(w);
	                }
	            }
	        });
	}
	
	private static void calling(final Network.Client callHim) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				client.sendMessage(callHim, "calling");
                transmitterpipe.startStreamingToUnicast(callHim.ip);
                transmitterpipe.printPipeline();
                //receiverpipe.startReceivingFromUnicast();
                calling = new GUICalling();
                calling.lblUser.setText(callHim.name);
                calling.cmdMute2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        if (calling.cmdMute2.isSelected()) {
                            //receiverpipe.stopReceivingFromUnicast();
                            receiverpipe.muteSound();
                        } else {
                            //receiverpipe.startReceivingFromUnicast();
                            receiverpipe.unmuteSound();
                        }
                    }
                });
                Object[] options = {"End Call"};
                int result = JOptionPane.showOptionDialog(null, calling, "Calling " + callHim.name, JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                try {
                    if (result == JOptionPane.YES_OPTION) {
                        calling.timer.stop();
                        if (transmitterpipe.isStreamingToUnicast() && transmitterpipe != null) {
                            client.sendMessage(callHim, "ended");
                            transmitterpipe.stopStreamingToUnicast();
                        }
                        if (receiverpipe.isReceivingFromUnicast() && receiverpipe != null) {
                            receiverpipe.stopReceivingFromUnicast();
                        }
                        //cmdCall.setEnabled(true);
                    } else {
                        calling.timer.stop();
                        if (transmitterpipe.isStreamingToUnicast() && transmitterpipe != null) {
                            client.sendMessage(callHim, "ended");
                            transmitterpipe.stopStreamingToUnicast();
                        }
                        if (receiverpipe.isReceivingFromUnicast() && receiverpipe != null) {
                            receiverpipe.stopReceivingFromUnicast();
                        }
                        //cmdCall.setEnabled(true);
//                        Window w = SwingUtilities.getWindowAncestor(calling);
//                        closeDialog(w);

                    }
                } catch (NullPointerException e) {
                    System.out.println(e.getMessage());
                }
			}
		});
	}
	
	private void reciever(final Network.Message msg) {
		//System.out.println("hehehehehhehehe" + msg.sender.name + " for " + msg.recipient.name);
		callReciever = new GUICallReceiver();
		callReciever.setCallerCredentials(msg.sender.name, msg.sender.id, msg.sender.ip);
        final Object[] options = {"Recieve Call"};
        final JOptionPane pane = new JOptionPane(callReciever, JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        JDialog dialog = new JDialog();
        //dialog.ow
        dialog.setTitle("Call from"+msg.sender.name);
        dialog.setContentPane(pane);
        dialog.setModal(false);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                //cmdCall.setEnabled(true);
                client.sendMessage(msg.sender, "rejected");
                dialog.dispose();
            }
        });
        pane.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                String prop = pce.getPropertyName();
                if (dialog.isVisible()
                        && (pce.getSource() == pane)
                        && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                    String value = pane.getValue().toString();
                    System.out.println(pane.getValue());
                    if (value.equals("Recieve Call")) {
                    	//System.out.println("SAKAM DA PRIMAM CALL");
                        recieving(msg);
                    } else {
                        
                        client.sendMessage(msg.sender, "rejected");
                        dialog.dispose();
                    }
                    dialog.setVisible(false);
                }

            }
        });
        dialog.pack();
        //dialog.setLocationRelativeTo(c);
        dialog.setVisible(true);
	}
	
	private void recieving(Message m) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				client.sendMessage(m.sender, "recieved");
				System.out.println("The receiver is starting to receive and send stream in the same time");
                receiverpipe.startReceivingFromUnicast();
                receiverpipe.printPipeline();
                transmitterpipe.startStreamingToUnicast(m.sender.ip);
                transmitterpipe.printPipeline();
                recieving = new GUICalling();
                recieving.lblUser.setText(m.sender.name);
                recieving.lblStatus.setText("Call From ");
                recieving.cmdMute2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        if (recieving.cmdMute2.isSelected()) {
                            receiverpipe.muteSound();
                            //receiverpipe.stopReceivingFromUnicast();
                        } else {
                            //receiverpipe.startReceivingFromUnicast();
                            receiverpipe.unmuteSound();
                        }
                    }
                });
                Object[] options = {"Drop Call"};
                int result = JOptionPane.showOptionDialog(null, recieving, "recieving from " + m.sender.name, JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                try {
                    if (result == JOptionPane.YES_OPTION) {
                        recieving.timer.stop();

                        if (receiverpipe.isReceivingFromUnicast()) {
                            client.sendMessage(m.sender, "dropped");
                            receiverpipe.stopReceivingFromUnicast();
                        }
                        if (transmitterpipe.isStreamingToUnicast() && transmitterpipe != null) {
                            transmitterpipe.stopStreamingToUnicast();
                        }
                        //cmdCall.setEnabled(true);
                    } else {
                        recieving.timer.stop();
                        if (receiverpipe.isReceivingFromUnicast()) {
                            client.sendMessage(m.sender, "dropped");
                            receiverpipe.stopReceivingFromUnicast();
                        }
                        if (transmitterpipe.isStreamingToUnicast() && transmitterpipe != null) {
                            transmitterpipe.stopStreamingToUnicast();
                        }
//                        cmdCall.setEnabled(true);
//                        Window w = SwingUtilities.getWindowAncestor(recieving);
//                        closeDialog(w);
                    }

                } catch (NullPointerException e) {
                    System.out.println(e.getMessage());
                }
			}
		});
	}
	 

	private void callDropped() {
		try {
            if (transmitterpipe.isStreamingToUnicast()) {
                transmitterpipe.stopStreamingToUnicast();

            }
            if (receiverpipe.isReceivingFromUnicast()) {
                receiverpipe.stopReceivingFromUnicast();

            }
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
	}
	
	//multicast part
	public static void sendJoinRoom(int lastSelectedRoom) {
		client.sendJoinRoom(lastSelectedRoom);
		String multicast = rooms[lastSelectedRoom].multicastIP;
        System.out.println(multicast);
        
        //initialize the pipeline to transmit audio to a multicast ip
        //ssrc is storing the client that wants to transmit
        ssrc = transmitterpipe.startStreamingToMulticast(multicast);
        
        //initialize the pipeline to receive audio from a multicast ip
        //ssrc is used to remove client's own voice when playing the incoming audio
        receiverpipe.startReceivingFromMulticast(multicast, ssrc);

        joinRoom(lastSelectedRoom);
	}

	private static void joinRoom(int lastSelectedRoom) {
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				multicast = new GUIMulticast();
                multicast.lblUser.setText(rooms[lastSelectedRoom].name);
                multicast.cmdMute2.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        if (multicast.cmdMute2.isSelected()) {
                            receiverpipe.muteSound();
                            //receiverpipe.stopReceivingFromUnicast();
                        } else {
                            //receiverpipe.startReceivingFromUnicast();
                            receiverpipe.unmuteSound();
                        }
                    }
                });
                Object[] options = {"Leave Room"};
                int result = JOptionPane.showOptionDialog(null, multicast, "calling " + rooms[lastSelectedRoom].name, JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (result == JOptionPane.YES_OPTION) {
                    multicast.timer.stop();
                    if (receiverpipe.isReceivingFromMulticast()) {
                        client.sendLeaveRoom();
                        transmitterpipe.stopStreamingToMulticast();
                        receiverpipe.stopReceivingFromMulticast();
                        ssrc = 0;
                        System.out.println("leave1");
                    }
                } else {
                    multicast.timer.stop();
                    if (receiverpipe.isReceivingFromMulticast()) {
                        client.sendLeaveRoom();
                        transmitterpipe.stopStreamingToMulticast();
                        receiverpipe.stopReceivingFromMulticast();
                        ssrc = 0;
                        System.out.println("Leave2");
                    }
                }
			}
		});
	}

	public static void refreshRooms() {
		client.sendRefreshLists();
	}
	
	public void setLastSelectedIndex(int number){
		lastSelected=number;
	}

}
