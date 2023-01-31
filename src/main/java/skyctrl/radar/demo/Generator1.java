package skyctrl.radar.demo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Generator1 {
    public void setPort(int port)  {
        this.port = port;
        address = new InetSocketAddress("127.0.0.1",port);

    }

    private int port;
    private SocketAddress address;

    public Socket getSocket() throws IOException {
        if (socket==null || socket.isClosed()){
            socket=new Socket(InetAddress.getLocalHost(),port);
        }
        return socket;
    }

    private Socket socket;
    private ScheduledExecutorService executorService;
    public Generator1(){
        executorService= Executors.newSingleThreadScheduledExecutor();
    }
    public void send(){
        System.out.println ("send ");
        Runnable senderLoop = () -> {
            String msg;
            System.out.println ("send1 ");
            OutputStream out = null;
            try {
                out = getSocket().getOutputStream();
            } catch (IOException e) {
                //logger.log(Level.ALL, "out = socket.getOutputStream() is null");
                return;
            }
            try {
                System.out.println ("send 2");
                msg = "msg time"+ System.currentTimeMillis();
                out.write(msg.getBytes(StandardCharsets.UTF_8));
                out.flush();
                out.close();
                getSocket().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        executorService.scheduleAtFixedRate(senderLoop,3000,500, TimeUnit.MILLISECONDS);
    }
}
