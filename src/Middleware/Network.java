package Middleware;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Dimitar
 */
public class Network {

    static public final int port = 54555;

    // This registers objects that are going to be sent over the network.
    static public void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(RegisterName.class);
        kryo.register(Client.class);
        kryo.register(ArrayList.class);
        kryo.register(Room.class);
        kryo.register(Room[].class);
        kryo.register(RefreshLists.class);
        kryo.register(JoinRoom.class);
        kryo.register(LeaveRoom.class);
        kryo.register(SimpleMessage.class);
        kryo.register(Message.class);
    }

    static public class RegisterName {
        public String name;
    }

    static public class Client {
        public String name;
        public String ip;
        public int id;

        public Client() {            
        }
        
        public Client(String name, String ip, int id) {
            super();
            this.name = name;
            this.ip = ip; 
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Client other = (Client) obj;
            
            if(this.id != other.id) {
               return false; 
            }
            if(this.name == null || other.name == null || !this.name.equals(other.name)) {
               return false; 
            }
            /*if(this.ip == null || other.ip == null || !this.ip.equals(other.ip)) {
               return false; 
            }*/
        
            return true;
        }
        
    }

    static public class Room {
        public String name;
        public String multicastIP;
        public ArrayList<Client> clients;

        public Room() {
            super();
            clients = new ArrayList<>();
        }
        
    }
    
    static public class JoinRoom {
        public int room;

        public JoinRoom(int room) {
            this.room = room;
        }

        public JoinRoom() {
        }
        
    }
    
    static public class LeaveRoom {
    }
    
    static public class RefreshLists {
    }
    
    
    static public class SimpleMessage {
        public String text;
    }
    
    
    static public class Message {
        public String text;
        public Client recipient;
        public Client sender;

        public Message() {
            
        }
        public Message(Client recipient, Client sender, String text) {
            super();
            this.text = text;
            this.recipient = recipient;
            this.sender = sender;
        }
        public Message(Client recipient, String text) {
            super();
            this.text = text;
            this.recipient = recipient;
            this.sender = null;
        }

        @Override
        public String toString() {
            return "Message{" + "text=" + text + ", recipient.id=" + recipient.id + ", sender.id=" + sender.id + '}';
        }
        
    }
}
