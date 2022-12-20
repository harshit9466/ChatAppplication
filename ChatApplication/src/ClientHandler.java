import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable 
{
	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); // keep tracks of all the clients.
	private Socket socket;
	private BufferedReader bufferedReader; // Reads Messages from Clients.
	private BufferedWriter bufferedWriter; // Send Messages to Clients.
	private String clientUsername;
	
	//ClientHandler()
	public ClientHandler(Socket socket)
	{
		try 
		{
			this.socket = socket;     
			this.bufferedWriter = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) ); // using the OutputstreamWriter so that the the Byte Oriented Stream from the socket.getOutputStrem can be converted to Character Stream.
			this.bufferedReader = new BufferedReader ( new InputStreamReader( socket.getInputStream() ) );
			this.clientUsername = bufferedReader.readLine();    // reads the Client Username upto the newline(i.e. Enterbutton).
			clientHandlers.add(this);                          // adds the new Client Object to the ArrayList of Clients.
			broadcastMessage("SERVER: " + clientUsername + " has entered the  chat");
		}catch(IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}
	
	//run()
    @Override
    public void run()
    {
    	String messageFromClient;
    	while(socket.isConnected())
    	{
    		try 
    		{
    			messageFromClient = bufferedReader.readLine();
    			broadcastMessage(messageFromClient);
    		}catch(IOException e) {
    			closeEverything(socket, bufferedReader, bufferedWriter);
    			break;
    		}
    	}
    }
    
    //broadcastMessage()
    public void broadcastMessage(String messageToSend)
    {
    	for (ClientHandler clientHandler : clientHandlers)
    	{
    		try 
    		{
    			if(!clientHandler.clientUsername.equals(clientUsername))
    			{
    				clientHandler.bufferedWriter.write(messageToSend);
    				clientHandler.bufferedWriter.newLine(); // above write method do not send newLine character /n so we explictlt sent it.
    				clientHandler.bufferedWriter.flush(); // flushing the rest of the buffer as the message might not be that big to fill the whole buffer.
    			}
    		}catch(IOException e) {
    			closeEverything(socket, bufferedReader, bufferedWriter);
    		}
    	}
    }
    
    //removeClientHandler()
    public void removeClientHandler()
    {
    	clientHandlers.remove(this);
    	broadcastMessage("SERVER " + clientUsername + " has left the chat");
    }
    
    //closeEverything()
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter)
    {
    	removeClientHandler();
    	try
    	{
    		if(bufferedReader != null)
    		   bufferedReader.close();
    		
      		if(bufferedWriter != null)
    		   bufferedWriter.close();
      		
      		if(socket != null)
      		   socket.close();
      		
    	}catch(IOException e) {
    		e.printStackTrace();
    	}
    }
    
}
