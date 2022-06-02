import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class mini_serv
{
	private ServerSocket server;
	private List<PrintWriter> allOut;
	public mini_serv(int port) throws Exception
	{
		server = new ServerSocket(port);
		allOut = new ArrayList<PrintWriter>();
	}

	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Wrong number of arguments");
			System.exit(1);
		}
		try
		{
			mini_serv server = new mini_serv(Integer.parseInt(args[0]));
			server.start();
		}
		catch (Exception e)
		{
			System.out.println("Fatal error");
			e.printStackTrace();
		}
	}

	private int getId(PrintWriter o)
	{
		for(int i = 0;i<allOut.size();i++)
		{
			if (allOut.get(i) == o)
				return i;
		}
		return -1;
	}

	private void addOut(PrintWriter out) {allOut.add(out);}
	private void removeOut(PrintWriter out) {allOut.remove(out);}
	private void sendMessage(String message, PrintWriter Pw)
	{
		for(PrintWriter out:allOut)
		{
			if (out != Pw)
				out.println(message);
		}
	}

	public void start()
	{
		try
		{
			while(true)
			{
				Socket socket = server.accept();
				ClientHandler handler = new ClientHandler(socket);
				Thread t = new Thread(handler);
				t.start();
			}
		}catch (Exception e) {e.printStackTrace();}
	}

	class ClientHandler implements Runnable
	{
		private Socket socket;
		private String host;
		private String nickName;
		public ClientHandler(Socket s) {socket = s;}
		
		public void run()
		{
			PrintWriter pw = null;
			try
			{
				InputStream in = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(in, "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				nickName = "client ";
				OutputStream out = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
				pw = new PrintWriter(osw, true);
				addOut(pw);
				System.out.println("server: "+nickName+getId(pw)+" just arrived");
				sendMessage("server: "+nickName+getId(pw)+" just arrived",pw);
				String message = null;
				while((message = br.readLine())!=null) {sendMessage(nickName+getId(pw)+": "+message, pw);}
			}
			catch (Exception e) {e.printStackTrace();}
			finally
			{
				System.out.println("server: "+nickName+getId(pw)+" just left");
				sendMessage("server: "+nickName+getId(pw)+" just left",pw);
				removeOut(pw);
				try {socket.close();} catch (Exception e) {e.printStackTrace();}
			}
		}
	}
}
