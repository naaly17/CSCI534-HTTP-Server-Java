
/**
 * MyWebServer Class is a functional webserver that can receive a request from a
 * socket, parse the request, find the data from the request, and respond to the
 * sender with the appropriate response information through the socket. 
 * Supports only GET and HEAD 
 * 
 * @author nadiaaly
 */


import java.io.*;
import java.net.*;
import java.util.*;

public final class MyWebServer {
   public static void main(String argv[]) throws Exception{
       
       
       int port = 0;
       try{
           port = Integer.parseInt(argv[0]);
       }
       //default to port if no argument provided
       catch(Exception e){
                System.out.println("Invalid Input for Port Directory");
                System.exit(0); 
       }
       
      String root = "";
      try {
       root = argv[1];
       if(!(root.endsWith("/"))){
           root =  root + "/";
       }
      } catch (Exception e) {
          System.out.println("Invalid Input for Root Directory");
          System.exit(0); 
      //root = "/Users/nadiaaly/";
    }
      
      ServerSocket listenSocket = new ServerSocket(port);
       

       while(true) {
           
        Socket connectionSocket = listenSocket.accept();

           // Construct an object to process the HTTP request message.
        HttpRequest request = new HttpRequest(connectionSocket, root);
     
// Create a new thread to process the request.
        Thread thread = new Thread(request);

        // Start the thread.
        thread.start();
       }

   }
}