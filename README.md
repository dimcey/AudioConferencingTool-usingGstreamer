# AudioConferencingTool-usingGstreamer
A project developed within the course Multimedia Systems at Luleå University of Technology

# Introduction
This lab work is the continuation of lab 1 where we have done how to play a recorded media using gstreamer framework. In this lab, we are required to send an audio over network using Gstreamer framework for implementing audio conferencing application. To send audio over a network in real time, we need to use a protocol called Real-time Transport protocol (RTP). RTP provides end to end network transport functions to support realtime media transport. RTP does not provide resource reservation and does not guarantee quality of service. Because of this reason, RTP is used in conjunction with its control protocol RTCP. RTP is used to send actual media streams while its quality of service (QoS) is monitored by RTCP. RTP typically runs on top of User datagram protocol (UDP).

# Problem Specification and Approach
The main requirement for this laboratory work is to create real-time audio communication tool between at least three simultaneous users. The tool should be also able to handle simple packet loss in order to achieve for a smooth and fluid communication. Dealing with this problem, more advanced knowledge is required about network communication between more than two clients, with an emphasis on Gstreamer, by implementing unicast and multicast communication over a network.

To approach this problem, the group decision was to develop a server that should handle the clients in a multithreaded fashion and also to register them to a common middleware Network class. Through the server, the clients should be able to decide whether to start a private or a group conversation. Once a private call negotiation between the clients are done through the server, the actual communication is done peer to peer, meaning that the provided solution is going to have the NAT traversal problem. For the multicast communication, each client should be register to the same multicast IP address group through the server, for example 224.1.1.1, and during a group call the client should be able to receive and send audio on the provided multicast IP address.

In addition to the main requirement, the following extra features were added to improve the functionality of the real-time audio communication tool:
- Login capability 
- Participant list
- Mute Audio
- Private audio, by selecting a user and sending audio only to that user
- Support for both multicast and unicast
- Removing the client's own voice during multicast communication

# Code structure
The project consists 6 packages and 19 classes, as shown in the figure below. This code structure clearly defines and differentiates the client and the server part, as well as the graphical user interface and the Gstreamer functionalities.

Short description of each Package with its classes inside:

- Client 

  ○	ClientHandler (Registers the client to the server and to common middleware Network)
  
  ○	ClientMain (Includes the main class which have to be executed)
  
  ○	ClientOperations (Class that contains all the client operations and functionalities)


- Client.Pipeline

  ○	DecoderBin (Bin that fetches the incoming packets from rtpBin, decodes and pushes them to the sink for playing the audio)

  ○	EncoderBin (Bin that fetches the outgoing client’s packets, encodes and pushes them to the sink for sending through udp)

  ○	MulticastReceivingBin (Bin that contains all the necessary Gstreamer elements to receive and process udp packets during a multicast conversation )

  ○	ReceivingPipeline (Pipeline that initializes the audio receiving process, by selecting whether it is a multicast or a private conversation)

  ○	SendingPipeline (Pipeline that initializes the audio sending process, by fetching the client’s audio and selecting whether it is a multicast or a private conversation)

  ○	TransmittingBin (Bin called from SendingPipeline that contains all the necessary Gstreamer elements to process client’s audio and prepare the packets to be send via udp)
  
  ○	UnicastReceivingBin (Bin that contains all the necessary Gstreamer elements to receive and process udp packets during a private conversation)

-	GUI

  ○	GUICalling (GUI shown during a private conversation. Once both of the clients agree to start a private conversation, this GUI shows basic information about the private conversation and a button to drop the call)

  ○	GUICallReceiver (GUI shown before starting a private conversation, displaying basic information about the receiver of the call and a button to start transmitting and receiving audio. This GUI is also shown once a client receives a call, showing basic information about the caller and a button to start receiving and transmitting audio)

  ○	GUIClient(The main GUI for the client, displaying all the necessary information to the client by binding to the Client package and using most of the functionalities from there)

  ○	GUILoggin(GUI used at the beginning, waiting for client’s input in order to receive server’s IP address and Client’s username)

  ○	GUIMulticast(GUI shown after joining a public room for a multicast communication)

- Middleware

  ○	Constants(Class that initializes all the shared variables that are used by the client and the server part)

  ○	EventListener (Class that dynamically react to a specific event from the server by informing the client)

  ○	Network (Common Java library - kryonet - that provides a clean and simple API for efficient TCP and UDP client/server network communication using NIO. This class suits like a platform for registering both the server and the incoming clients by binding them together, and also registers the objects that are going to be transferred over the network like messages, active client list, public rooms for a multicast communication, etc)

-	Icons(Package that includes all the images for building the GUIs for nicer user experience)

- Server

  ○	ServerHandler (Class that initializes the server with its functionalities)

# Architecture
The image below illustrates the main architecture of the developed real-time audio communication tool.
![alt tag](https://github.com/dimcey/AudioConferencingTool-usingGstreamer/blob/master/architecture.png)
The classes and packages dependencies have been thought to enable a clear development, to facilitate the reusability of our solution and to bring high scalability. Guided from the development of the first lab, the produced GUIs are also loosely coupled with the Client’s functionalities such as the ClientOperations class, the Gstreamer operations and the binding to the server, so that if an end user wants to develop his own GUIs, only an object is needed to be created from the mentioned classes and all of the Client’s functionalities would become available for the end user.
Once the server (ServerHandler) is started it will register himself to the middleware Network class, open 10 multicast public rooms and will initialize a EventListener that is going to listen for client’s messages and take certain action based on the type of the message.

Once the client (ClientMain) is started, it displays the GUILoggin class which is there to get the client’s input for the Server’s IP address and Client’s username. When those parameters are successfully entered, they are passed to the ClientOperation class, from where it tries to connect to the server through the ClientHandler class via TCP. The ClientHandler class is the “bridge” between the client interface and the server. So, if the connection to the server is successful, the ClientHandler class will register the client to the middleware Network class and initialize a EventListener that is going to listen for server’s messages and fire certain events based on the type of the message. Also, the ClientHandler class sends the actual requests, in form of messages, generated by the ClientOperations class to the server and of course listens to the respond.
When a client successfully connects, there is a possibility to send request (message) to the server for various type of needs, like getting the multicast public rooms, list of currently connected users to the server, request to call privately certain client or maybe join/leave a public room. 

# Algorithm description
This section will cover the Gstreamer pipelines construction that is used to record client’s audio, prepare the packet for sending over the network and also to the other end implementing receiving pipeline to fetch the packet from the network and playing them on the client’s speakers.

- Sending audio packets

There are various number of different approaches if the developer is building a pipeline which has to record an audio, and it mainly differs on what OS the developer is using. The “AlsaSrc” element is used in Linux OS for this purpose, however there is a possibility to use PulseSrc as well, but for Windows OS the only element developed to record audio is “Autoaudiosrc”. The decision of the group was to proceed with Linux OS, and therefore using the “AlsaSrc” element. After the audio packets are fetched with “AlsaSink”, they are pushed to the “tee” element, which basically enables one input and many outputs. Hence, in the SendingPipeline class there is a clear distinction if the client has chosen to start a private or a group conversation. The only difference is actually the destination IP address that is used at the end of the Pipeline to send the packets via UDP. For example, a private conversation will pass the IP address of the client that is about to receive a call, and a group conversation will pass the multicast IP, in this case 244.1.1.1 for instance.

The output of the “tee” element, not matter if its a multicast or a unicast call, it goes to the TransmittingBin class which has the EncoderBin. First the packets are queuing to be encoded, more specifically a PCMU audio is encoded into a RTP packet, and then are send over the network with the “UdpSink” element through the “rtpBin” element.
The whole process of sending the audio packets over the network is also explained in the following figure:
![alt tag](https://github.com/dimcey/AudioConferencingTool-usingGstreamer/blob/master/transmittingBin.png)

- Receiving audio packets

Depending on the incoming conversation, a client can start receiving a multicast or unicast packets. For instance if two clients agree to start a private conversation, they will both initialize the UnicastReceivingBin which includes the “UdpSink” element that is listening for a UDP trafic on a specific port number, in this case port 51002. This element will fetch the audio packets that are coming from the other client, decodes them and forward the packets to the “AlsaSink” that will play the audio on the client’s speakers. 

The group conversation is rather similar, the only difference is that a client doesn’t want to hear his own voice during a multicast call, because in reality he is sending audio to a multicast ip address and receives the all possible packets that are send to that multicast ip address .  Hence, the developed solution consists of a “FakeSink” element, in the Multicast receiving packets part, to which is forwarded the client’s own audio, meaning that those packets will not be played on the client’s speakers.
![alt tag](https://github.com/dimcey/AudioConferencingTool-usingGstreamer/blob/master/UnicastMulticast.png)

# Challenges
One tricky task regarding the multicast or the group conversation is to develop an algorithm that will remove the user’s own voice when receiving the packets from the multicast group IP. This means that every user subscribed to a group IP will send their audio and then the multicast group IP will forward each packet to everyone that is subscribed. This will result that a client while sending his audio the multicast IP group will forward to the same client even his own voice, which will result with unsatisfactory service. 
The approach for this problem is when the user is starting to transmit the audio to the multicast IP, the class transmittingPipiline will store the sender of the audio in an object and return that to the ClientOperations class, see the figures below. The object then will be passed to the receivingPipeline class, and a specific element will be created “FakeSink” which will receive the object. The “FakeSink” element will only get the audio packet from the sender, and will discard them when trying to play the incoming audio packet from the multicast IP. 

