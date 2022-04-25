import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UDPServer {
  public static void main(String args[]) throws Exception {

    DatagramSocket serverSocket = new DatagramSocket(9876);
    boolean receivedUsername = false;
    byte[] receiveData = new byte[1024];
    byte[] sendData = new byte[1024];
    String response;
	  PrintWriter serverLog = new PrintWriter(new FileWriter("log.txt"),true);
    

    while (true) {
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

      serverSocket.receive(receivePacket);

      String sentence = new String(receivePacket.getData());
      sentence = sentence.trim();

      InetAddress IPAddress = receivePacket.getAddress();

      Date timestamp = new Date();
      
      int port = receivePacket.getPort();
      //initial connection to server 
      if (!receivedUsername)
      { //format the username to uppercase using help code
        response = sentence.toUpperCase();
        //receive initial log in from user, username and log it to keep track of users
        serverLog.println("Username: "+ response + "Connection Established: " + timestamp + " Using Port: " + String.valueOf(port) + " From IP: " + String.valueOf(IPAddress));
        //output the log info to the server terminal 
        System.out.println("Username: "+ response + "Connection Established: " + timestamp + " Using Port: " + String.valueOf(port) + " From IP: " + String.valueOf(IPAddress));
        //return the username to the server for validation
        sendData = response.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        serverSocket.send(sendPacket);
        receivedUsername = true;
      } else {
        //receive initial log in from user, username and log it to keep track of users
        serverLog.println("Response Sent: " + timestamp + " Using Port: " + String.valueOf(port) + " From IP: " + String.valueOf(IPAddress));
        //output the log info to the server terminal 
        System.out.println("Response Sent: " + timestamp + " Using Port: " + String.valueOf(port) + " From IP: " + String.valueOf(IPAddress));
        //perform the arithmetic and format to string for response
        response = String.valueOf(UDPServer.performMath(sentence));
        //return the total to the client
        sendData = response.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        serverSocket.send(sendPacket);
      }
      if(sentence.matches("close connection"))
      {
          serverLog.println("Connection Closed: " + timestamp + " Using Port: " + String.valueOf(port) + " From IP: " + String.valueOf(IPAddress));
          System.out.println("Connection Closed: " + timestamp + " Using Port: " + String.valueOf(port) + " From IP: " + String.valueOf(IPAddress));  
          break;
      }
    //prep the array for the next message
    receiveData = null;
    receiveData = new byte[1024];  
    }
    serverLog.close();
    serverSocket.close();
  }
  
public static double performMath(String expression) {
    // Compile a pattern that matches a simple arithmetic expression
    // This pattern has three groups:
    //     A number (with or without a decimal)
    //     An operator (+, -, *, or /)
    //     Another number (with or without a decimal)
    Pattern ptrn = Pattern.compile("\\s*(\\d+\\.?\\d*)\\s*([\\+\\-\\*\\/])\\s*(\\d+\\.?\\d*)\\s*");
    // Pattern ptrn = Pattern.compile("h");
    Matcher mtch = ptrn.matcher(expression);
    
    // Match the expression against the pattern
    // The pattern must match the entire expression, so nothing before or after the expression is allowed (except whitespace)
    boolean hasMatch = mtch.matches();
    
    // We could throw an error here, but I'm just returning zero for now.
    if (!hasMatch) return 0;

    // Get the groups from the match corresponding to the numbers and operators and parse them.
    double a = Double.parseDouble(mtch.group(1));
    String op = mtch.group(2);
    double b = Double.parseDouble(mtch.group(3));

    // Perform the operation.
    switch (op) {
        case "+": return a + b;
        case "-": return a - b;
        case "*": return a * b;
        case "/": return a / b;
        // If the operator is not one of the four above, we have an error.
        // We could also throw an error here.
        default: return 0;
    }
  }
}
