package skyctrl.radar.demo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocalTableAdapter extends JFrame implements IAdapter{
    public static SimpleDateFormat dateFormat= new SimpleDateFormat("hh:mm:ss");
    static class Message{
        Long timeMoment;
        String id,H,DateTime,Lat,Long,Speed,RCS,Classification;

        String[] msg;
        Message(String src){
            msg = src.split(",");
            timeMoment = System.currentTimeMillis();
            id=msg[14];

            H=""+Math.round(Double.parseDouble(msg[1])*Math.sin(Double.parseDouble(msg[4])*Math.PI/180));
            DateTime = dateFormat.format(new Date(timeMoment));
            Lat = msg[18];
            Long = msg[19];
            //Speed = msg[7];
            //RCS = msg[8];
            Classification = msg[12];
        }
        Object[] getRowData(){
            Object[] result=new Object[5];

            int i=0;
            result[i++]  = id;
            result[i++]  = H;
            result[i++]  = ""+Lat+","+Long;
            //result[i++]  = Speed;
            //result[i++]  = RCS;
            result[i++]  = Classification;
            result[i++]  = DateTime;
            return result;
        }
    }
    private Queue<Message> store;
    private long millisToHold;
    private JTable table;
    private DefaultTableModel model;
    private ExecutorService executorService;
    private String[] headers = new String[]{
            "id","H","Lat,Long","Classification","DateTime"};
    public LocalTableAdapter(){
        store = new ArrayDeque<>() ;
        millisToHold = 1000*60*2;
        createUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600,300);
        //this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        executorService = Executors.newScheduledThreadPool(1);

    }
    private void createUI(){
        model = new DefaultTableModel();
        table = new JTable(model);
        model.setColumnCount(headers.length);
        model.setColumnIdentifiers(headers);
        add(new JScrollPane(table));
    }
    private void deleteOldData(){
        long limitTimeMoment=System.currentTimeMillis()-millisToHold;
        while (store.peek()!=null && store.peek().timeMoment<limitTimeMoment){
            store.remove();
        }
    }
    private Object[][] getActualData(){
        Object[][] result;
        if (store.size()==0){
            return new Object[0][headers.length];
        }

        deleteOldData();
        result=new Object[store.size()][];
        int i=0;
        for (Message row:store){
            result[i++]= row.getRowData();
        }
        return result;
    }
    private void start(){
        Runnable process = ()->{
            model.setDataVector(getActualData(),headers);
        };
        if (executorService instanceof ScheduledExecutorService) {
            ((ScheduledExecutorService)executorService).scheduleAtFixedRate(process, 10, 5, TimeUnit.SECONDS);
        } else {
            executorService.execute(process);
        }
    }

    @Override
    public void processing(String rawData) {
        store.add(new Message(rawData));
    }
}
