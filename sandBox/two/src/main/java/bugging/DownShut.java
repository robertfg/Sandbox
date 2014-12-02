package bugging;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class DownShut {

	public void downShut() {
    InetAddress serverAddress = null;
    int serverPortNum = 8005;
    Socket socket = null;
    String magicWord = "SHUTDOWN";
    byte[] buffer = null;
    OutputStream os = null;   
	
    try {
        
         //* Change to InetAddress.getHostByName(somehostname) if client is
         //* running from a remote machine
         
        serverAddress = InetAddress.getLocalHost();
        socket = new Socket(serverAddress, serverPortNum);
        os = socket.getOutputStream();
        buffer = magicWord.getBytes();
        os.write(buffer, 0, buffer.length);
        os.flush();
    } catch (UnknownHostException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            os = null;
        }
    }  
}

	public static Future<Boolean> portIsOpen(final ExecutorService es,
			final String ip, final int port, final int timeout) {
		return es.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() {
				try {
					Socket socket = new Socket();
					socket.connect(new InetSocketAddress(ip, port), timeout);
					socket.close();
					return true;
				} catch (Exception ex) {
					return false;
				}
			}
		});
	}

	public static void main(final String... args) throws InterruptedException, ExecutionException {
		ExecutorService es = Executors.newFixedThreadPool(4);
		String ip = "127.0.0.1";
		int timeout = 200;
		List<Future<Boolean>> futures = new ArrayList<>();
		
		for (int port = 1; port <= 65535; port++) {
			futures.add(portIsOpen(es, ip, port, timeout));
		}  
		es.shutdown();
		int openPorts = 0;
		for (final Future<Boolean> f : futures) {
			if (f.get()) {
				openPorts++;
			}  
		}
		System.out.println("There are " + openPorts + " open ports on host "
				+ ip + " (probed with a timeout of " + timeout + "ms)");
	}

//	 class ScanResult {
//		private final int port;
//		private final boolean isOpen;
//		// constructor
//		// getters
//	}

}
