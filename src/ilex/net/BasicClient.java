/*
*  $Id$
*
*  $Source$
*
*  $State$
*
*  $Log$
*  Revision 1.4  2011/06/09 18:13:14  shweta
*  added try catch in disconnect method
*
*  Revision 1.3  2008/11/29 21:08:00  mjmaloney
*  merge with opensrc
*
*  Revision 1.1  2008/11/15 01:12:17  mmaloney
*  Copied from separate ilex tree
*
*  Revision 1.8  2005/08/03 15:43:46  mjmaloney
*  2-step connect to control timeout to 20 sec. Bogus port numbers were causing
*  very long (2 minute) timeouts.
*
*  Revision 1.7  2004/08/30 14:50:21  mjmaloney
*  Javadocs
*
*  Revision 1.6  2004/03/01 20:18:16  mjmaloney
*  Upgraded socket apps to flush data after each interaction.
*
*  Revision 1.5  2003/04/09 19:38:27  mjmaloney
*  Update lastConnectAttempt before attempting.
*
*  Revision 1.4  2003/04/09 15:16:11  mjmaloney
*  dev.
*
*  Revision 1.3  2002/11/22 16:13:42  mjmaloney
*  Added getClientName and isConnected utilities.
*
*  Revision 1.2  2000/09/08 15:05:25  mike
*  Added outputData method to avoid NullPointerExceptions when client tries
*  to write data and the socket is closed.
*
*  Revision 1.1  2000/01/23 19:30:29  mike
*  created.
*
*/
package ilex.net;

import ilex.util.Logger;

import java.net.*;
import java.io.*;
import java.rmi.UnknownHostException;

/**
This class encapsulates common functions for a TCP/IP client.
You can implement a client by subclassing this class or by wrapping an
object of this type.
*/
public class BasicClient
{
	/** port to connect to. */
	protected int port;
	/** host to connect to. */
	protected String host;

	/** The socket is opened and I/O streams are created. */
	protected Socket socket;
	/** The input stream */
	protected InputStream input;
	/** The output stream */
	protected OutputStream output;

	/** (deprecated) If desired set a PrintStream for debug/trace messages. */
	protected PrintStream debug;

	/** Allows sub-class to control how often connect attempts are made: */
	protected long lastConnectAttempt;

	/**
	* Construct client for specified port and host. Note, this creates
	* the client object but the connection is not made until the
	* 'connect()' method is called.
	* @param host the host name
	* @param port the port number
	*/
	public BasicClient( String host, int port )
	{
		this.port = port;
		this.host = host;
		socket = null;
		input = null;
		output = null;
		debug = null;
		lastConnectAttempt = 0L;
	}

	/**
	* Finalizer makes sure socket and stream resources are freed. If
	* disconnect() was already called, no harm done.
	*/
	protected void finalize( )
	{
		disconnect();
	}

	/**
	* Call this function to specify a print stream for debug messages.
	* Debug messages will be generated by BasicClient and the derived
	* classes for various network actions.
	* If you don't call this function, no debug messages are generated.
	* @param debug debug stream
	* @deprecated
	*/
	public void setDebugStream( PrintStream debug )
	{
		this.debug = debug;
	}

	/**
	* Connect to the previously specified host and port.
	* @throws IOException if can't open socket.
	* @throws UnknownHostException if host cannot be resolved.
	*/
	public void connect( ) throws IOException, UnknownHostException
	{
		lastConnectAttempt = System.currentTimeMillis();
		if (debug != null)
			debug.println("Connecting to host "
				+ (host != null ? host : "(unknown)")
				+ "', port " + port);
		socket = doConnect(host, port);
		input = socket.getInputStream();
		output = socket.getOutputStream();
	}

	/**
	* When several client objects are created in different threads, the
	* connect call can get an interrupted system call (inside the native
	* implementation) and subsequently generate a SocketException. The
	* use of this method prevents this by synchronizing at the class
	* level. Only one socket connection can be made at a time.
	* @param host the host name
	* @param port the port number
	* @return the new Socket
	* @throws IOException if can't open socket.
	* @throws UnknownHostException if host cannot be resolved.
	*/
	private static synchronized Socket doConnect( String host, int port ) throws IOException, UnknownHostException
	{
		Socket ret = new Socket();				
		InetSocketAddress iaddr = new InetSocketAddress(host, port);		
		if (iaddr.isUnresolved())
			throw new UnknownHostException(host);
		ret.connect(iaddr, 20000);
		return ret;
		
		//return new Socket(host, port);
	}

	/**
	* Disconnect from the server. Close input & output streams and release
	* all socket resources.
	* This function should be called when any of the other methods throws
	* an exception. You can then call connect() again to reconnect to the
	* server.
	*/
	public void disconnect( )
	{
		try 
		{
			try
			{
				if (input != null)
					input.close();
			}
			catch (Exception e)
			{
				Logger.instance().failure("Error closing input: "+e.getMessage());
				e.printStackTrace();
			}
			try
			{
				if (output != null)
					output.close();
			}
			catch (Exception e)
			{
				Logger.instance().failure("Error closing ouput: "+e.getMessage());
				e.printStackTrace();
			}
			try
			{
				if (socket != null)
					socket.close();
			}
			catch (Exception e)
			{
				Logger.instance().failure("Error closing socket: "+e.getMessage());
				e.printStackTrace();
			}
			
			if (debug != null)
				debug.println("Disconnected form host '"
					+ (host != null ? host : "(unknown)")
					+ "', port " + port);
		}
		catch(Exception e) {}
		finally
		{
			input = null;
			output = null;
			socket = null;
		}
	}

	/**
	* Sends a byte array of data to the socket and flushes it.
	* @param data the byte array.
	* @throws IOException on IO error
	*/
	public void sendData( byte[] data ) throws IOException
	{
		if (output == null)
			throw new IOException("BasicClient socket closed.");
		output.write(data);
		output.flush();
	}

	/**
	* @return String in the form host:port
	*/
	public String getName( )
	{
		return host + ":" + port;
	}

	/**
	* @return true if currently connected
	*/
	public boolean isConnected( )
	{
		
		return socket != null;
	}

	/**
	* @return port number
	*/
	public int getPort( ) { return port; }

	/**
	* Set the port number.
	* If currently connected this does nothing until you dis and re connect.
	* @param port the number
	*/
	public void setPort( int port ) { this.port = port; }

	/**
	* @return the host name
	*/
	public String getHost( ) { return host; }

	/**
	* Sets the host name. 
	* If currently connected this does nothing until you dis and re connect.
	* @param host
	*/
	public void setHost( String host ) { this.host = host; }

	/**
	* @return Socket for this connection, or null if not connected.
	*/
	public Socket getSocket( ) { return socket; }

	/**
	* @return InputStream for this connection, or null if not connected.
	*/
	public InputStream getInputStream( ) { return input; }

	/**
	* @return OutputStream for this connection, or null if not connected.
	*/
	public OutputStream getOutputStream( ) { return output; }

	/**
	* @return Java msec time when last connect attempt was made.
	*/
	public long getLastConnectAttempt( ) { return lastConnectAttempt; }
}

