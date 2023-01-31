package skyctrl.radar.demo;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class RadatServer implements IRadarServer{
    private int port;
    private AtomicBoolean isRunning;
    private ServerSocket serverSocket;
    private final Queue<String> queueMSG;
    private List<IAdapter> adapters;

    private final ExecutorService executor;
    private final ExecutorService executorAdapters;
    private final ExecutorService executorDistribute;

    public RadatServer(){
        queueMSG=new LinkedBlockingDeque<>();
        executor= Executors.newSingleThreadExecutor();
        executorAdapters = Executors.newFixedThreadPool(3);
        executorDistribute = Executors.newFixedThreadPool(3);
        isRunning  = new AtomicBoolean(false);
        //executorAdapters = Executors.newFixedThreadPool(adapters.size());
    }
    @Override
    public void setPort(int port) {
        this.port=port;
    }

    @Override
    public void setAdapters(List<IAdapter> adapters) {
        this.adapters=adapters;
    }

    private void distribute(){

        Runnable process=()-> {
            while (!queueMSG.isEmpty()) {
                String msg = queueMSG.poll();
                List<Callable<Integer>> runnableList = new ArrayList<>(adapters.size());

                adapters.stream().forEach(adapter -> runnableList.add(() -> {
                    adapter.processing(msg);
                    return null;
                }));
                try {
                    executorAdapters.invokeAll(runnableList,10,TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        executorDistribute.execute(process);

    }
    @Override
    public void start() {
        isRunning.getAndSet(true);
        Runnable mainThread=()->{
            Socket socket;

            try {
                serverSocket=new ServerSocket(port,50,InetAddress.getByName("127.0.0.1"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (isRunning.get()){
                try {
                    socket=serverSocket.accept();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String msg;
                    while ((msg=reader.readLine())!=null){

                        queueMSG.add(msg);
                        distribute();
                    }
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        executor.execute(mainThread);

    }

    @Override
    public void stop() {
        isRunning.getAndSet(false);
        executor.close();
        executorAdapters.close();
    }

}
