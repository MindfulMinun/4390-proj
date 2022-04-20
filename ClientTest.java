import java.io.*; 
import java.net.*;
import java.util.*;
  
public class ClientTest {

  //function to send math requests to the server
  public static void mathRequest(String message, DatagramSocket socket, InetAddress IPaddress) throws IOException{
    byte[] byteMessage = new byte[1024];
    byteMessage = message.getBytes();
    socket.send(new DatagramPacket(byteMessage, byteMessage.length, IPaddress, 9876));
  }
  public static void main(String args[]) throws Exception {
    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
    DatagramSocket clientSocket = new DatagramSocket();

    //used to randomize how long the thread should sleep for next request
    Random r = new Random();

    System.out.println("This is a Basic Math Server");    

    InetAddress IPAddress = InetAddress.getByName("127.0.0.1");

    byte[] sendData = new byte[1024];
    byte[] receiveData = new byte[1024];

    System.out.println("Please enter a username: ");

    //reads in the name that the user provides
    String userName = inFromUser.readLine();
    sendData = userName.getBytes();

    //sends the name of the user to the server
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
    clientSocket.send(sendPacket);

    //client waits for the server to receive the message by receiving an ack
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    clientSocket.receive(receivePacket);

    //turn received name from server to display to the user to see if it is correct
    String modifiedSentence = new String(receivePacket.getData());

    System.out.println("Username according to the server:" + modifiedSentence);
    System.out.println("Hello " + modifiedSentence);
    System.out.println("For this server, you can only enter 3 expressions");

    //limits the user to only send three expressions right now
    for(int i = 0; i < 3; i++){
      //clear the buffer
      receiveData = new byte[1024];
      String sentence;

      System.out.println("Enter expression number: ");
      sentence = inFromUser.readLine();

      //sends math request to the server
      mathRequest(sentence, clientSocket, IPAddress);

      //client waits for the server to receive the message by receiving an ack
      DatagramPacket receieveRequest = new DatagramPacket(receiveData, receiveData.length);
      clientSocket.receive(receieveRequest);

      String mathAnswer = new String(receieveRequest.getData());
      System.out.println("The answer to your expression from the server: " + mathAnswer);

      //wait a few seconds before sending a new message
      Thread.sleep((1 + r.nextInt(6)) * 1000);

    }

    clientSocket.close();
  }
}
