/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Middleware.Constants;
import Middleware.Network;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import Middleware.Network.*;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;

/**
 * @author  
 */
public class ServerHandler {
    //+------------------------------------------------------------------------+
    //|************************    VARIABLES     ******************************|
    //+------------------------------------------------------------------------+
    /**
     * Declaration of a server object with kryonet library.
     */
    public com.esotericsoftware.kryonet.Server server;
    /**
     * Declaration of an Array of chat rooms.
     */
    public Room[] rooms;

    //+------------------------------------------------------------------------+
    //|**********************      CONSTRUCTOR     ****************************|
    //+------------------------------------------------------------------------+

    /**
     * Constructor creating a new server.
     * 
     * @throws IOException
     */
    public ServerHandler() throws IOException {

        //initialize rooms
        rooms = new Room[Constants.NUMBER_OF_ROOMS];
        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = new Room();
            rooms[i].multicastIP = Constants.MULTICAST_IP_PREFIX + String.valueOf(i + 1);
            rooms[i].name = Constants.ROOM_NAME_PREFIX + String.valueOf(i + 1);
        }

        server = new com.esotericsoftware.kryonet.Server() {
            @Override
            protected Connection newConnection() {
                return new VCConnection();
            }
        };

        //register types of messages
        Network.register(server);

        server.addListener(new Listener() {
            @Override
            public void received(Connection c, Object object) {
                VCConnection connection = (VCConnection) c;
                
                //if a client sent a register message
                if (object instanceof RegisterName) {
                    // Ignore the object if a client has already registered a name. This is
                    // impossible with our client, but a hacker could send messages at any time.
                    if (connection.client != null) {
                        return;
                    }
                    // Ignore the object if the name is invalid.
                    String name = ((RegisterName) object).name;
                    if (name == null) {
                        return;
                    }
                    name = name.trim();
                    if (name.length() == 0) {
                        return;
                    }

                    connection.client = new Client(name, connection.getRemoteAddressTCP().getHostString(), connection.getID());
                    connection.room = null;
                    synchronized (rooms) {
                        connection.sendTCP(rooms); //this will send all of the created rooms to the newly registered client 
                    }
                    
                    //send clients list to all
                    server.sendToAllTCP(getClientsList()); //this will send all of the connected clientd to the newly registered client

                    return;
                } 
                
                //connect to room
                else if (object instanceof JoinRoom) {

                    // Ignore the object if a client tries to communicate before  sucesfully registering a name
                    if (connection.client == null) {
                        return;
                    }

                    JoinRoom joinRoom = (JoinRoom) object;
                    if (joinRoom.room >= 0 && joinRoom.room < Constants.NUMBER_OF_ROOMS && rooms[joinRoom.room] != null) {
                        synchronized (rooms) {
                            if (connection.room != null) {
                                rooms[connection.room].clients.remove(connection.client);
                            }
                            rooms[joinRoom.room].clients.add(connection.client);
                            connection.room = joinRoom.room;
                            server.sendToAllTCP(rooms);
                          
                        }

                    }
                    return;
                } 
                
                //leave room                
                else if (object instanceof LeaveRoom) {
                    // Ignore the object if a client tries to communicate before  sucesfully registering a name
                    if (connection.client == null) {
                        return;
                    }
                    synchronized (rooms) {
                        if (connection.room != null) {
                            rooms[connection.room].clients.remove(connection.client);
                            connection.room = null;
                        }
                        server.sendToAllTCP(rooms);
                    }

                    return;
                } 
                
                //refresh lists             
                else if (object instanceof RefreshLists) {
                    // Ignore the object if a client tries to communicate before  sucesfully registering a name
                    if (connection.client == null) {
                        return;
                    }
                    synchronized (rooms) {
                        connection.sendTCP(rooms);
                        connection.sendTCP(getClientsList());
                    }

                    return;
                } 
                
                
                else if (object instanceof Message) {
                    // Ignore the object if a client tries to communicate before  sucesfully registering a name

                    if (connection.client == null) {
                    	System.out.println("ERROR NEKOJ");
                        return;
                    }
                    Message message = ((Message) object);
                    System.out.println("Dobi message od "+ message.sender);
                    System.out.println("Nameneta za " + message.recipient);
                    if (message.recipient != null && message.sender == null) {
                        Connection[] connections = server.getConnections();
                        for (int i = 0; i < connections.length; i++) {
                            VCConnection conn = (VCConnection) connections[i];
                            if (conn.client != null && conn.client.equals(message.recipient)) {
                                Message encMessage = new Message(message.recipient, connection.client, message.text);
                                System.out.println("Prakjam poraka " + message.text);
                                server.sendToTCP(conn.getID(), encMessage);
                                
                            }
                        }
                    }

                    return;
                }
            }

            @Override
            public void disconnected(Connection c) {
                VCConnection connection = (VCConnection) c;

                if (connection.client != null) {
                    synchronized (rooms) {
                        if (connection.room != null) {
                            rooms[connection.room].clients.remove(connection.client);
                        }
                        server.sendToAllTCP(rooms);
                    }
                    server.sendToAllTCP(getClientsList());
                }
            }
        });

        //server.bind("localhost",Constants.MAIN_SERVER_PORT);
        server.bind(Constants.MAIN_SERVER_PORT);
        //server.
        System.out.println("Server is runnig " + "--IP[" + Inet4Address.getLocalHost().getHostAddress() + "] --port[" + Constants.MAIN_SERVER_PORT + "]");
        server.start();

    }

    //+------------------------------------------------------------------------+
    //|**********************     METHODS     *********************************|
    //+------------------------------------------------------------------------+

    /**
     * Main method creating a new Voice Chat Server instance.
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //Log.set(Log.LEVEL_DEBUG);
        new ServerHandler();
    }
    
    /**
     * Get the list of registered clients.
     * 
     * @return arrayList of client registered.
     */
    public ArrayList<Client> getClientsList() {
        Connection[] connections = server.getConnections();
        ArrayList<Client> list = new ArrayList<>();
        
        for (int i = 0; i < connections.length; i++) {
            VCConnection conn = (VCConnection) connections[i];
            if (conn.client != null) {
                list.add(conn.client);
            }
        }
        return list;
    }

    static class VCConnection extends Connection {

        public Integer room;
        public Client client;
    }
    
    /**
     * Get the room address of a specific room.
     * @param room is the room number
     * @return the room address
     */
    public String getRoomAddress(int room){
        return rooms[room].multicastIP;
    }

}
