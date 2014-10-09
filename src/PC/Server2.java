package PC;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

import biz.source_code.base64Coder.Base64Coder;


public class Server2
{
	// Constant specifying the packet size to send/receive
	static final int MAX_PACKET_SIZE = 256;
	private int echoClientPort = 2228; /* Echo client port */
	private String clientIP = "192.168.0.109"; /* IP address of Android */
	DatagramSocket socketServer = null;
	byte[] key = new byte[MAX_PACKET_SIZE];
	byte[] dbuffer = new byte[MAX_PACKET_SIZE];
	DatagramPacket reply;
	public long keyTransmit = 0;
	public boolean fileTransferFinished = false;
	private ArrayList<ArrayList<String>> receivedStrings;
	private ServerSocket server;
	private Socket client;
	private PrintStream out;
	private BufferedReader buf;
	
	/** Creates a new instance of Server 
	 * @param receivedStrings2 
	 * @param androidIP 
	 * @throws IOException */
	public Server2(ArrayList<ArrayList<String>> receivedStrings2, String androidIP) throws IOException
	{
		
		System.out.println("Server 2");
		this.clientIP = androidIP;
		System.out.println("ANdroid IP: " + this.clientIP);
		this.receivedStrings = receivedStrings2;
		System.out.println("server2 finish initialization");
	}
	
	public boolean getAndroidKey() throws IOException
	{	
		
		System.out.println("Waiting for android key");
		client = server.accept();
		out = new PrintStream(client.getOutputStream());
		buf =  new BufferedReader(new InputStreamReader(client.getInputStream()));
		String keyString = buf.readLine(); 
		boolean output = true;
		System.out.println("received String : " + keyString);
	    key = Base64Coder.decode(keyString.toCharArray());
	    //get the key from the Android device
	    System.out.println("key from Android " + Base64Coder.encodeLines(key));
	  
	  return output;
	}
	
	//makes the connection for decryption
	public boolean makeDConnection(String[] fileNames) throws IOException
	{	
		
		boolean output = true;
		String message="";
//		sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(("key:"+new String(key)).getBytes()).getBytes()));
		//receive packets until told that the process is finished
		
			dbuffer = new byte[MAX_PACKET_SIZE];

			listen(2223, fileNames);
		//closes the socket
		
		return output;
	}
	
	//creates the connection for encryption, connect with android
	public boolean makeEConnection(String[] filenames) throws IOException
	{
		boolean output = true;
		
		byte[] buffer = new byte[MAX_PACKET_SIZE];
		
		long start = System.currentTimeMillis();
		System.out.println("waiting for android key");
		server = new ServerSocket(1234);
		client = server.accept();
		out = new PrintStream(client.getOutputStream());
		buf =  new BufferedReader(new InputStreamReader(client.getInputStream()));
		String keyString = buf.readLine();
		System.out.println("string received : " + keyString);
		
		key = Base64Coder.decode(keyString.toCharArray());
		System.out.println("key from android " + Base64Coder.encodeLines(key));
		
		keyTransmit = System.currentTimeMillis() - start;
		//sends all pieces to the Android device
		System.out.println("start to send file content");
		System.out.println("filename length : " + filenames.length);
		for(int i=0;i<filenames.length;i++){
			buffer = new byte[MAX_PACKET_SIZE];

			String file = filenames[i].replace(".txt", ".piece");
		    System.out.println(file);
		    System.out.println("Go to TALK");
			talk(file);
		}
		
		//sends a message letting the Android know the process is finished

		
		//socketServer.close();
		System.out.println("Socket Closed");
		return output;
	}

	/**
	* Listens for incoming packets and echos them back to clients.
	*
	* @param port The port on which to listen for incoming packets.
	 * @param serverListener2 
	 * @param sender2 
	*/
	public void listen(int port, String[] fileNames) 
	{	
		fileTransferFinished = false;
		try
		{
byte[] buffer = new byte[MAX_PACKET_SIZE];
			
			//get the number of packets to be sent

			String receivedString = buf.readLine();
			System.out.println("Received : " + receivedString);
			String[] receivedStringArray = receivedString.split(":");
			String fileName = receivedStringArray[0];
			String fileContent = receivedStringArray[1];
			
			
				

			System.out.println("finish receing file content");
			System.out.println("Filename:"+fileName);
			//display message saying file received
			fileName = fileName.replaceAll("\\..*","");
			fileName+=".piece";
			System.out.println("FileName changed is:"+fileName);
		    Hash hash = new Hash();
		    byte[] temp_hash = hash.GetHash(fileContent.getBytes(), 1);
		    System.out.println("Hash value of received file is " + Base64Coder.encodeLines(temp_hash));
			File folder=new File("C:\\Users\\xiao\\Documents\\" + fileName);
			folder.createNewFile();
			FileOutputStream fos = null;
			fos = new FileOutputStream(folder);
			fos.write(Base64Coder.decode(fileContent.toCharArray()));
			fos.flush();
			fos.close();
			client.close();
			
			fileTransferFinished = true;
		}
		catch (SocketException e)
		{
			System.err.println("Error creating the server socket: " + e.getMessage());
		}
		catch (IOException e)
		{
			System.err.println("Server socket Input/Output error: " + e.getMessage());
		}
	}

	//sends the input file to the Android device
	public void talk(String fileName)
	{
		File oldFile = new File(fileName);
		String echoFileName = oldFile.getName();
		byte[] echoBytes = null;
		int numPackets = 0;

			System.out.println("filename: "+echoFileName);
			//gets the contents of the file
			echoBytes = read(fileName);
			System.out.println("contents(bytes): " + echoBytes);
		
		//determines the number of packets needed to send to Android
		if(echoBytes.length%MAX_PACKET_SIZE==0)
		{
			numPackets = echoBytes.length/MAX_PACKET_SIZE;
		}
		else
		{
			numPackets = (echoBytes.length/MAX_PACKET_SIZE)+1;
		}
		System.out.println("#packs: " + numPackets);
			String fileContent = String.valueOf(Base64Coder.encode(echoBytes));
			String Sendout = echoFileName + ":"+fileContent;
			System.out.println("Send out String : " + Sendout);
			out.println(Sendout);
			//saves number of packets to a byte array
			
			deleteFile(fileName);
			System.out.println("file sent");
	}
			
			//read the contents of a file and stores them in a byte array
			public static byte[] read(String aInputFileName)
			{
			    File file = new File(aInputFileName);
			    byte[] result = new byte[(int)file.length()];
			    try {
			      InputStream input = null;
			        int totalBytesRead = 0;
			        input = new BufferedInputStream(new FileInputStream(file));
			        while(totalBytesRead < result.length){
			          int bytesRemaining = result.length - totalBytesRead;
			          //input.read() returns -1, 0, or more :
			          int bytesRead = input.read(result, totalBytesRead, bytesRemaining); 
			          if (bytesRead > 0){
			            totalBytesRead = totalBytesRead + bytesRead;
			          }
			        }
			        /*
			         the above style is a bit tricky: it places bytes into the 'result' array; 
			         'result' is an output parameter;
			         the while loop usually has a single iteration only.
			        */
			        input.close();
			    } catch (FileNotFoundException ex) {
			    } catch (IOException ex) {
			    }
			    return result;
			  }
			
			//deletes a file
			private static byte[] findNulls(byte[] buffer)
			{
				int terminationPoint = findLastMeaningfulByte(buffer);
				byte[] output;
				output = new byte[terminationPoint + 1];
				System.arraycopy(buffer, 0, output, 0, terminationPoint + 1);
				return output;
			}
			private static int findLastMeaningfulByte(byte[] array)
			{
				//System.out.println("Attempting to find the last meaningful byte of " + asHex(array));
				int index=0;

				for (index=(array.length - 1); index>0; index--) {
				//System.out.println("testing index " + index + ". Value: " + array[index]);
				if (array[index] != (byte)(0)) {
				//System.out.println("Last meaningful byte found at index " + index);
				return index;
				}
				}
				System.out.println("No meaningful bytes found.  Perhaps this is an array full of nulls...");
				return index;
			}
			 private static void deleteFile(String file)
			 {
				  File f1 = new File(file);
				  f1.delete();
			 }
}