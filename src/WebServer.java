/*

Common Port Assignments and Corresponding RFC Numbers              

Port Common Name RFC#  Purpose
7     Echo        862   Echoes data back. Used mostly for testing.
9     Discard     863   Discards all data sent to it. Used mostly for testing.
13    Daytime     867   Gets the date and time.
17    Quotd       865   Gets the quote of the day.
19    Chargen     864   Generates characters. Used mostly for testing.
20    ftp-data    959   Transfers files. FTP stands for File Transfer Protocol.
21    ftp         959   Transfers files as well as commands.
23    telnet      854   Logs on to remote systems.
25    SMTP        821   Transfers Internet mail. Stands for Simple Mail Transfer Protocol.
37    Time        868   Determines the system time on computers.
43    whois       954   Determines a user's name on a remote system.
70    gopher     1436   Looks up documents, but has been mostly replaced by HTTP.
79    finger     1288   Determines information about users on other systems.
80    http       1945   Transfer documents. Forms the foundation of the Web.
110   pop3       1939   Accesses message stored on servers. Stands for Post Office Protocol, version 3.
443   https      n/a    Allows HTTP communications to be secure. Stands for Hypertext Transfer Protocol over Secure Sockets Layer (SSL).

*/

///A Simple Web Server (WebServer.java)

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 * 
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 * 
 * @author Jeff Heaton
 * @version 1.0
 */

/**
 * 
 * @author IvanAranda
 * I studied and modify this Server implementation enough to serve .html .pdf .jpeg .jpg .png files
 * and to support Thread 
 *
 * To test the server I put together for this assignment
 * run this file
 * 
 * the following will get you to index.html
 * http://localhost:8080/
 * 
 * the following will get you to the image
 * http://localhost:8080/nyc_image.jpg
 * 
 * the following will get you to the PDF file
 * http://localhost:8080/section6_5.pdf
 * 
 * the following will get you to the resorce not found 404.html page
 * this goes for anything that you request and it is not in the server files
 * http://localhost:8080/tests
 * 
 */
public class WebServer  implements Runnable{

  /**
   * WebServer constructor.
   */
	static final String DEFAULT_FILE = "index.html";
	static final String METHOD_NOT_IMPLEMENTED_FILE = "not_implemented.html";
	static final File SERVER_ROOT = new File(".");
	static final int SERVER_PORT = 8080;
	
	// verbose mode
		static final boolean verbose = true;
		
	// Client Connection via Socket Class
		private Socket acceptedSocket;
		
		public WebServer(Socket c) {
			acceptedSocket = c;
		}
		
@Override
  public void run() {

    System.out.println("Waiting for connection");

      try {
        // wait for a connection
        Socket remote = acceptedSocket;
        // remote is now the connected socket
        System.out.println("Connection, sending data.");
        BufferedReader in = new BufferedReader(new InputStreamReader(
            remote.getInputStream()));
        PrintWriter out = new PrintWriter(remote.getOutputStream());
        BufferedOutputStream dataOut = new BufferedOutputStream(remote.getOutputStream());;
        String input = in.readLine();
        System.out.println("this is the input line "+input);
        StringTokenizer parser = new StringTokenizer(input);
        
        String method = parser.nextToken().toUpperCase();
        System.out.println("this is input token "+method);
        
        String fileRequested = parser.nextToken().toLowerCase();
        System.out.println("this is the file requested "+fileRequested);
        
//        while (parser.hasMoreElements()) {
//			System.out.println(parser.nextElement());
//		}
        if(method.equals("GET") || method.equals("HEAD")) {
        	//if there is no file requested in the GET line
        	if(fileRequested.equals("/")){
        		//here  I append index.html 
        		//because there are no file requested in the GET request
        		fileRequested += DEFAULT_FILE;//<-- this is basically /index.html
        	}
        	//Now we create the response file
        	//we pass it the root File and the file requested to complete the path
        	//to the file requested
        	File responseFile = new File(SERVER_ROOT,fileRequested);
        	if(!responseFile.exists()) {
        		fileRequested = "/404.html";
        		responseFile = new File(SERVER_ROOT,fileRequested);
        	}
        	
        	int fileLength = (int) responseFile.length();
        	//the following method call make sure the file type, .html or else
        	//if it ends with .html or .htm returns text/html else text/plain
        	String content = getContentType(fileRequested);
        	//if it is GET and not HEAD
        	if(method.equals("GET")) {
        		byte[] fileData = readFileData(responseFile, fileLength);
				
				// send HTTP Headers
				out.println("HTTP/1.1 200 OK");
				out.println("Server: Java HTTP Server from SSaurel : 1.0");
				out.println("Date: " + new Date());
				out.println("Content-type: " + content);
				out.println("Content-length: " + fileLength);
				out.println(); // blank line between headers and content, very important !
				out.flush(); // flush character output stream buffer
				
				dataOut.write(fileData, 0, fileLength);
				dataOut.flush();
        	}
        	
        	
        }else {
        	// we return the not supported file to the client
			File file = new File(SERVER_ROOT, METHOD_NOT_IMPLEMENTED_FILE);
			int fileLength = (int) file.length();
			String contentMimeType = "text/html";
			//read content to return to client
			byte[] fileData = readFileData(file, fileLength);
				
			// we send HTTP Headers with data to client
			out.println("HTTP/1.1 501 Not Implemented");
			out.println("Server: Java HTTP Server from SSaurel : 1.0");
			out.println("Date: " + new Date());
			out.println("Content-type: " + contentMimeType);
			out.println("Content-length: " + fileLength);
			out.println(); // blank line between headers and content, very important !
			out.flush(); // flush character output stream buffer
			// file
			dataOut.write(fileData, 0, fileLength);
			dataOut.flush();
        }
        remote.close();
      } catch (Exception e) {
        System.out.println("Error: " + e);
      }
  }

  /**
   * Start the application.
   * 
   * @param args
   *            Command line parameters are not used.
   */
  public static void main(String args[]) {
	  try {
			ServerSocket serverConnect = new ServerSocket(SERVER_PORT);
			System.out.println("Server started.\nListening for connections on port : " + SERVER_PORT + " ...\n");
			
			// we listen until user halts server execution
			while (true) {
				WebServer myServer = new WebServer(serverConnect.accept());
				
				if (verbose) {
					System.out.println("Connecton opened. (" + new Date() + ")");
				}
				
				// create dedicated thread to manage the client connection
				Thread thread = new Thread(myServer);
				thread.start();
				System.out.println("Id = " + thread.getId());
				System.out.println("Number of thread running = " + Thread.activeCount());
			}
			
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}

  }
  
  private String getContentType(String fileRequested) {
	  if(fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
		  return "text/html";
	  else if(fileRequested.endsWith(".pdf"))
		  return "application/pdf";
	  else if(fileRequested.endsWith(".jpeg") || fileRequested.endsWith(".jpg"))
		  return "image/jpeg";
	  else if(fileRequested.endsWith(".png"))
		  return "image/png";
	  else
		  return "text/plain";
  }
  private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
	}

}