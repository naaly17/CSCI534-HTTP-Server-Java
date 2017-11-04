/**
 *
 * @author nadiaaly
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;


final class HttpRequest implements Runnable
{
  final static String CRLF = "\r\n";
        final static String GET = "GET";
        final static String Head = "HEAD";
        Socket socket;
        String path;

  // Constructor
  public HttpRequest(Socket socket, String path) throws Exception 
  {
    this.socket = socket;
                this.path = path;
  }

  // Implement the run() method of the Runnable interface.
        public void run(){
  try {
    processRequest();
  } catch (Exception e) {
    System.out.println(e);
  }}

        private static void sendBytes(FileInputStream fis, OutputStream os) 
            throws Exception{
   //buffer to hold bytes to socket
                byte[] buffer = new byte[1024];
                int bytes = 0;

                while((bytes = fis.read(buffer)) != -1 ) {
                   os.write(buffer, 0, bytes);
                }
         }
        
    
        //Method to parse the input after Get-Modified-Since, takes three different 
        //formatting, returns 0 if the date is not in correct format
    private long parseDate(String inputDate) throws Exception{
        
        StringTokenizer tokens = new StringTokenizer(inputDate," ,");
        int numTokens = tokens.countTokens();
        long milliseconds = 0;
        
        if( numTokens != 6 && numTokens !=5 && numTokens !=4 )
            return 0;
        
        SimpleDateFormat df=null;
        
        
        if(numTokens==6 ){
            df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            try{
                
            Date date1 = df.parse(inputDate);
             milliseconds = date1.getTime();
            }
            catch( Exception e ){
                milliseconds=0;
            }
           }
        
        else if( numTokens==4 ) {
           df = new SimpleDateFormat("EEEEE, dd-MMM-yy HH:mm:ss z");
           df.setTimeZone(TimeZone.getTimeZone("GMT"));

            try{
            Date date1 = df.parse(inputDate);
             milliseconds = date1.getTime();
            System.out.println(milliseconds);}
            catch( Exception e ){
                milliseconds=0;
            }
           }
        
        else {
            df = new SimpleDateFormat("EEE  MMM dd HH:mm:ss yyyy");
            try{
            Date date1 = df.parse(inputDate);
             milliseconds = date1.getTime();
            System.out.println(milliseconds);}
            catch( Exception e ){
                milliseconds=0;
            }
           }
        return milliseconds;
       }
        
   
  
  private void processRequest() throws Exception
        {
// Get a reference to the socket's input and output streams.
      InputStream is = socket.getInputStream();
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            
            PrintStream ps = new PrintStream(os);
        // Set up input stream filters.
        
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
    


      
//Get the request line of the HTTP request message.
       String requestLine = br.readLine();

        // Get and display the header lines.
       String headerLine = "";
       String fileName = "";
       String forwardHeader = "";

       boolean badRequest = false;
       boolean method_unavailable = false;

        String result = "";
        Date ifModified = null;
        // Display the request line
        //System.out.println();
        //System.out.println("Request:");
        //System.out.println(requestLine);
        long ifModifiedSeconds = 2;
        boolean check_modified = false;

        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
         
      if( headerLine.startsWith("If-Modified-Since: ") ){
                check_modified = true;
                headerLine = headerLine.substring(19,headerLine.length());
                System.out.println(headerLine);
                StringTokenizer tokens_1 = new StringTokenizer(headerLine," ,");
                System.out.println(tokens_1.countTokens());
                ifModifiedSeconds = parseDate(headerLine);
                }    
      }
              
  if(ifModifiedSeconds < 1){
        badRequest =true;
          }

    System.out.println(badRequest);
     
  StringTokenizer tokens = new StringTokenizer(requestLine);
            
     //fileName = tokens.nextToken();


    //Check how the input is formatted:
    String method_request = tokens.nextToken();
    fileName = tokens.nextToken();
        if(fileName.startsWith("/")==true)
               fileName = fileName.substring(1);
           if(fileName.startsWith("http")){
               String[] output_split = fileName.split("/");
               System.out.println(output_split.length);
               if(output_split.length< 4){
               badRequest= true;
               }
               else{
            fileName = output_split[3];}
           }

           
//Check if the path contains the root directory path or if we will need to
//concatenate the root path to the beginning of our filename

String check_path = "";

if(fileName.length()>= path.length()){
    check_path = fileName.substring(0,path.length()-1);
    System.out.println("check path");
    System.out.println(check_path);
    check_path = "/" + check_path;
}
if(path.equals(check_path)){
  fileName = "/" + fileName;
        }
else{
  fileName = path + fileName;
}

        
      
//Find if the file exists 
    File file = new File(fileName);
    boolean isDirectory = file.isDirectory(); // Check if we have been given a directory
    boolean fileExists = file.exists();
    FileInputStream fis = null;
    


    String statusLine = "";
    String entityBody = "";
    String contentLengthLine = "";
    String lastModifiedLine = "";

   
   //if the file is a directory search for index.html in that directory
   
    String new_filename = null;
    boolean index_exists = false;
    System.out.println(isDirectory);
    File check;

if(isDirectory){
   
   if(fileName.endsWith("/")==true){
       fileName = fileName + "index.html";
   }
   else{
     fileName = fileName + "/index.html";

   }
    
   file = new File(fileName);
   if(file.exists()){
       index_exists = true;
       fileExists = true;}
   else
       fileExists = false;  
}



//Try to read file

 if(fileExists){   
      try {
    fis = new FileInputStream(file);
                
        
    }  catch  (FileNotFoundException e) {
      fileExists = false;
    }
 }
 
//Now if we have need to check if modified, check if it has been modified since
//provided date
boolean hasBeenModified = false;
long fileModified = 0;
  if((ifModifiedSeconds != 0) && check_modified && fileExists){
      fileModified = file.lastModified();
      if((ifModifiedSeconds - fileModified)<=0){
          hasBeenModified = true;
          
      }
            
   }
        
    //check if method is not head or get, if not this is not supported method 
    if (!(method_request.equals("GET") || method_request.equals("HEAD")))
      {
        method_unavailable= true;
      }

    
    //Get the date for the ouput message
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    String dateLine = "Date: " + dateFormat.format(calendar.getTime()) + CRLF;
    String serverLine = "Server: Nadia Aly's SNadiaimple Java Http Server" + CRLF;
    String connectionLine = "Connection: close" + CRLF;
    
    
    //Cover all cases for return headers:
    
    //File found 200 response message:
    if (fileExists && !method_unavailable &&!badRequest || index_exists && !method_unavailable &&!badRequest) {
        if(!(check_modified && !hasBeenModified)){
            statusLine = "HTTP:/1.1 200 OK" + CRLF;
            //contentTypeLine = "Content-type: " +       contentType(fileName)  + CRLF;  
            contentLengthLine = "Content-Length: "
              + fis.available() + CRLF;
          
            os.writeBytes(statusLine);
            os.writeBytes(connectionLine);
            os.writeBytes(dateLine);
            os.writeBytes(serverLine);
            //extra return line for if modified request
          if(check_modified && method_request.equals("GET)")){
            lastModifiedLine = "Last-Modified: " + dateFormat.format(fileModified)+ CRLF;
            os.writeBytes(lastModifiedLine);
            }
            
             os.writeBytes(contentLengthLine);
             os.writeBytes(CRLF);
  //If the method is get, actually return the file
  if (method_request.equals("GET")) {
    try 
      {
        sendBytes(fis, os);
              fis.close();
              os.close();
              socket.close();
      }
    catch (Exception e)
      {
        System.out.println("Exception raised");
      }
        }

      }
    }
    
    //Not modified return header 304
    if(check_modified && !hasBeenModified && !method_unavailable && fileExists && !badRequest){
        statusLine= "HTTP/1.1 304 Not Modified" + CRLF;
      //contentTypeLine = "Content-type: " + "text/html" + CRLF;
        entityBody = "<HTML>" + "<HEAD><TITLE>File has not been modified since time "
                    + "supplied </TITLE></HEAD>" +  CRLF;
      os.writeBytes(statusLine);
      os.writeBytes(entityBody);
     
        
    }
    
    //
    if(!fileExists && !method_unavailable && !badRequest){
     statusLine= "HTTP/1.1 404 Not Found" + CRLF;
      //contentTypeLine = "Content-type: " + "text/html" + CRLF;
            entityBody = "<HTML><TITLE>Not Found</TITLE></HEAD>" + CRLF;
      os.writeBytes(statusLine);
      os.writeBytes(entityBody);
     
}
        
    
    if (badRequest && !method_unavailable) {
      statusLine= "HTTP/1.1 400 Bad Request" + CRLF;
      //contentTypeLine = "Content-type: " + "text/html" + CRLF;
      entityBody = "<HTML>" + "<HEAD><TITLE>Bad Request</TITLE></HEAD>" 
      + "<BODY>" + requestLine + " is a bad request, request is not in the proper syntax. "
              + "</BODY></HTML>" + CRLF;
        os.writeBytes(statusLine);
        os.writeBytes(entityBody);
    }

    if (method_unavailable) {
      statusLine= "HTTP/1.1 501 Method Not Implemented" + CRLF;
      //contentTypeLine = "Content-type: " + "text/html" + CRLF;
      entityBody = "<HTML>" + "<HEAD><TITLE>Not Implemented</TITLE></HEAD>" 
      + "<BODY> Method " + method_request + " Not Implemented</BODY></HTML>" + CRLF;
      os.writeBytes(statusLine);
      os.writeBytes(entityBody);
    }
    

    //close streams/sockets everything if not null
   if(os !=null) os.close();
      if(br !=null) br.close();
      if (is!=null) is.close();
      socket.close();

        }

}


