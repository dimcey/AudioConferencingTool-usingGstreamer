package Client;
 
import java.io.IOException;
import java.util.ArrayList;
 
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.io.IOException;
import java.util.ArrayList;
import Middleware.Network;
import Middleware.Network.*;
import Middleware.Constants;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import Middleware.Constants;
import Middleware.EventListener;
import Middleware.Network;
import Middleware.Network.Message;
import Middleware.Network.RegisterName;
import Middleware.Network.Room;
import Middleware.Network.Client;

/**
 * @author Dimitar 
 */
public class ClientHandler {
	
    private com.esotericsoftware.kryonet.Client client;
    String myName;
    String serverIP;
    EventListener listener = null;
    
    public ClientHandler(String serverIP, String name) {
        this.myName = name;
        this.serverIP = serverIP;
        //initialize client class with buffer enough parameters
        client = new com.esotericsoftware.kryonet.Client(8192, 25000);
        new Thread(client).start();

        // For consistency, the classes to be sent over the network are
        // registered by the same method for both the client and server.
        Network.register(client);

        //add listeners for connection
        client.addListener(new Listener() {

            //when a user connects to the server, he will be registered (in Middleware.Network)
            @Override
            public void connected(Connection connection) {
                RegisterName registerName = new RegisterName();
                registerName.name = myName;
                client.sendTCP(registerName); //sending to the server that a user is registered
            }

            //when received object(message) from the server
            @Override
            public void received(Connection connection, Object object) {
                
            	//received new client list (the received object-message is from ArrayList type
                if (object instanceof ArrayList) {
                    sendToListener(Constants.EVT_CLIENT_LIST_RECEIVED, object); //the client fires a event message to himself, it will be received in the ClientOperations
                    return;
                } 
                
                //if the object is from type Room[]
                else if (object instanceof Room[]) {
                    sendToListener(Constants.EVT_ROOM_ARRAY_RECEIVED, object); //the client fires a event message to himself, it will be received in the ClientOperations
                } //received new message from another client
                
                else if (object instanceof Message) {
                    Message message = ((Message) object);
                    if (message.recipient != null && message.sender != null) {
                    	//System.out.println(((Message) object).text + "ooooooooo NE VLAGA");
                        Client tmp = new Client(myName, null, connection.getID());
                        if (tmp.equals(message.recipient)) {
                        	//System.out.println(((Message) object).text + "ooooooooo VLAGA");
                            sendToListener(Constants.EVT_MESSAGE_FROM_ANOTHER_CLIENT, object);
                        }

                    }

                }//received new multicast address from server
                else if (object instanceof String) {
                    sendToListener(Constants.EVT_ROOM__MULTICAST_RECIEVED, object);
                }
            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("disconnected");
            }
        });

    }
    
    public void connect() {

        new Thread("Connect to server") {
            @Override
            public void run() {
                try {
                	//client.connect();
                    client.connect(Constants.MAIN_SERVER_PORT, serverIP, Constants.MAIN_SERVER_PORT);
                    // Server communication after connection can go here, or in Listener#connected().
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        }.start();

    }
    
    public void setListener(EventListener lst) {
        this.listener = lst;
    }
    
    public void sendToListener(int message, Object o) {
        if (listener != null) {
            listener.fireEvent(message, o);
        }

    }
    
    /**
     * Send a request to refresh clients and rooms information.
     */
    public void sendRefreshLists() {
    	System.out.println("NE VLAGA");
    	if (client != null) {
    		System.out.println("VLAGA");
        	client.sendTCP(new RefreshLists());
        }
    }

    /**f
     * Send request to join a specific room.
     *
     * @param room is the index of the room.
     */
    public void sendJoinRoom(int room) {
        if (client != null) {
            client.sendTCP(new JoinRoom(room));
        }
    }

    /**
     * Send request to leave the room.
     */
    public void sendLeaveRoom() {
        if (client != null) {
            client.sendTCP(new LeaveRoom());
        }
    }

    /**
     * Send message to another Client
     *
     * @param recipient is the recipient of the message.
     * @param text is the content of the message.
     */
    public void sendMessage(Client recipient, String text) {
        if (client != null) {
            System.out.println("SENT MESSAGE" + recipient.name +" "+ text);
            Message message = new Message(recipient, text);
            
            client.sendTCP(message);
        }
    }
    
    /**
     * Get the ID of the client.
     * @return the client ID
     */
    public int getID(){
        return client.getID();
    } 


}
