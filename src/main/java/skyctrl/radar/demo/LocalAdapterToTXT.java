package skyctrl.radar.demo;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalAdapterToTXT implements IAdapter {
    private Queue<String> queueMSG;
    private SocketAddress address;
    private Socket socket;
    private ExecutorService executor;
    private ExecutorService executorSender;
    private Logger logger;

    private String fileNameToWrite;
    private File fileToWrite;

    public LocalAdapterToTXT() throws IOException {
        //logger=Logger.getLogger("log1");

        queueMSG = new LinkedBlockingDeque<>();

        //address = new InetSocketAddress("localhost", 9876);

        //socket = new Socket();
        //socket.connect(address, 10000);

        executor = Executors.newSingleThreadExecutor();
        executorSender = Executors.newScheduledThreadPool(1);
        send();
    }

    public void setFileNameToWrite(String fileNameToWrite) throws IOException {
        this.fileNameToWrite = fileNameToWrite;
        fileToWrite = Paths.get(fileNameToWrite).toFile();
        //FileUtils.forceDelete(fileToWrite);
    }

    private void send() {
        Runnable senderLoop = () -> {
            String msg;

            OutputStream out = null;
            try {
                out = socket.getOutputStream();
            } catch (IOException e) {
                logger.log(Level.ALL, "out = socket.getOutputStream() is null");
                throw new RuntimeException(e);
            }
            while (out != null && (msg = queueMSG.peek()) != null) {
                try {
                    out.write(msg.getBytes(StandardCharsets.UTF_8));
                    queueMSG.remove();
                    logger.log(Level.ALL, "OUT;" + msg);
                } catch (IOException e) {
                    logger.log(Level.ALL, "out.write(msg.getBytes(StandardCharsets.UTF_8)); ERROR!");
                    throw new RuntimeException(e);
                }

            }
        };

        Runnable toFile = () -> {
            while (!queueMSG.isEmpty()) {
                List<String> toWrite = new ArrayList<>(queueMSG);
                System.out.println(fileNameToWrite +" "+queueMSG.size() );
                //String line=queueMSG.peek();
                try {
                    FileUtils.writeLines(fileToWrite, toWrite, true);
                    queueMSG.removeAll(toWrite);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        };
        if (executorSender instanceof ScheduledExecutorService executorServiceSE) {
             executorServiceSE.scheduleAtFixedRate(toFile, 1, 2, TimeUnit.SECONDS);
        } else {
            executorSender.execute(toFile);
        }

    }

    @Override
    public void processing(String rawData) {
       queueMSG.add(rawData);
    }
}
