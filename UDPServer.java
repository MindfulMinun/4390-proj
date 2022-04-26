import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class UDPServer {
  public static void main(String args[]) throws Exception {

    DatagramSocket serverSocket = new DatagramSocket(9876);
    boolean receivedEquation = false, userTerminated = false;
    byte[] receiveData = new byte[1024];
    byte[] sendData = new byte[1024];
    int connectedUsers = 0;
	  PrintWriter serverLog = new PrintWriter(new FileWriter("log.txt"),true);
    

    while (true) {
      //help code START
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

      serverSocket.receive(receivePacket);

      String sentence = new String(receivePacket.getData());
      sentence = sentence.trim();

      InetAddress IPAddress = receivePacket.getAddress();

      Date timestamp = new Date();
      
      int port = receivePacket.getPort();
      //END

      //check if the packet received is the equation
      Pattern ptrn = Pattern.compile("\\s*(\\d+\\.?\\d*)\\s*([\\+\\-\\*\\/])\\s*(\\d+\\.?\\d*)\\s*");
      Matcher mtch = ptrn.matcher(sentence);
      receivedEquation = mtch.matches();
      //check if the packet is the user requesting the connection to close
      Pattern p = Pattern.compile("[a-zA-Z]+:close");
      Matcher m = p.matcher(sentence);
      userTerminated = m.matches();

      if (!receivedEquation && !userTerminated)
      { 
          //add to the number of connected users to keep track of when to close the server
          connectedUsers++;
          //format the username to uppercase using help code
          sentence = sentence.toUpperCase();
          //run tracking and logging
          serverLogging(sentence, timestamp, port, IPAddress,sentence);
          //return the username to the server for validation
          sendData = sentence.getBytes();
          DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
          serverSocket.send(sendPacket);
      }
      //if the number of users are greater than one and the user has not terminated
      else if(connectedUsers > 0 && userTerminated)
      {
          //place the packet into a string array for the logging file
          String [] userPacket = sentence.split(":");
          //send the username as userPacket[0] and the flag to close the connection as userPacket[1]
          serverLogging(userPacket[0].trim(),timestamp,port,IPAddress,userPacket[1].trim());
          //remove the user from the number of connected users
          connectedUsers--;
          //if all users disconnected from the server, kill the server.
          //if you would like the server to run indefinitely you need only remove lines 64,67,68 for a dirty test
          if(connectedUsers == 0)
          break;
      }
      else 
      {
          //run tracking and logging
          serverLogging(sentence, timestamp, port, IPAddress,sentence);
          //perform the arithmetic and format to string for response
          sentence = String.valueOf(UDPServer.performMath(sentence));
          //return the total to the client
          sendData = sentence.getBytes();
          DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
          serverSocket.send(sendPacket);
      }

    //prep the array for the next message
    receiveData = null;
    receiveData = new byte[1024];  
    }
    //close the server program and the socket to prevent resource leakage
    serverLog.close();
    serverSocket.close();
  }

//This class logs and keeps track of all the users through the terminal and the log file
public static void serverLogging(String client, Date time, int userPort, InetAddress userIP, String flag)
{
    //check if the response is an arithmetic expression to log accordingly
    Pattern ptrn = Pattern.compile("\\s*(\\d+\\.?\\d*)\\s*([\\+\\-\\*\\/])\\s*(\\d+\\.?\\d*)\\s*");
    Matcher mtch = ptrn.matcher(flag);
    boolean hasMatch = mtch.matches();
    //open the file for writing and allow the file to continuously log for all clients
    try
    {   FileWriter fw = new FileWriter("log.txt", true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter output = new PrintWriter(bw);
        //if the user did not send an expression and the user did not send a close request
        if (!hasMatch && !flag.trim().matches("close"))
        {
            //receive initial log in from user, send initial response and log it to keep track of users
            output.println("Username: "+ client + ", Connection Established: " + time + ", Using Port: " + String.valueOf(userPort) + ", From IP: " + String.valueOf(userIP));
            //output the log info to the server terminal 
            System.out.println("Username: "+ client + ", Connection Established: " + time + ", Using Port: " + String.valueOf(userPort) + ", From IP: " + String.valueOf(userIP));
        }
        //if the flag from the user is requesting to close the connection
        else if(flag.trim().matches("close"))
        {
            //output the termination and keep track of the access through the log file
            output.println(client + ", Closed Connection, " + time + " Using Port: " + String.valueOf(userPort) + " From IP: " + String.valueOf(userIP));
            System.out.println(client + ", Closed Connection, " + time + " Using Port: " + String.valueOf(userPort) + " From IP: " + String.valueOf(userIP));
        }
        else 
        {
            //log the activity
            output.println("Response Sent: " + time + ", Using Port: " + String.valueOf(userPort) + ", From IP: " + String.valueOf(userIP));
            //output the interaction to the server terminal 
            System.out.println("Response Sent: " + time + ", Using Port: " + String.valueOf(userPort) + ", From IP: " + String.valueOf(userIP));            
        }
      output.close();
    } 
    catch (IOException e) 
    {

    }
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

    //System.out.println(expression);
    //System.out.println(hasMatch);

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
