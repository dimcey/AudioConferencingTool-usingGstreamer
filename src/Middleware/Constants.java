package Middleware;
 
/**
 * @author Dimitar 
 */
public final class Constants {
    
    /**
     * Port number on which the TCP server listen.
     */
    static public final int      MAIN_SERVER_PORT    = 54555;
    
    /**
     * Prefix for multi-cast addresses.
     */
    public static final String   MULTICAST_IP_PREFIX = "224.1.1.";

    /**
     * Port number for the multi-cast.
     */
    static public final int      MULTICAST_PORT      = 51001;

    /**
     * Port number for the uni-cast.
     */
    static public final int      UNICAST_PORT        = 51002; 

    /**
     * Prefix for the name of the chat room.
     */
    public static final String   ROOM_NAME_PREFIX    = "Room ";

    /**
     * Number of chat room available.
     */
    public static final int      NUMBER_OF_ROOMS     = 10; 
    
    /**
     * Event code for receiving a List.
     */
    public static final int      EVT_CLIENT_LIST_RECEIVED               = 1;

    /**
     * Event code for receiving an array.
     */
    public static final int      EVT_ROOM_ARRAY_RECEIVED                = 2;

    /**
     * Event code for message from another client.
     */
    public static final int      EVT_MESSAGE_FROM_ANOTHER_CLIENT        = 3;    
    
    /**
     * Event code for receiving multi-cast room 
     */
    public static final int      EVT_ROOM__MULTICAST_RECIEVED           = 4;

	 

}

