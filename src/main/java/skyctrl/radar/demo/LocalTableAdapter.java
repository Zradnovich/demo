package skyctrl.radar.demo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Queue;

public class LocalTableAdapter extends JFrame implements IAdapter{
    public static SimpleDateFormat dateFormat= new SimpleDateFormat("hh:mm:ss");
    static class Message{
        Long timeMoment;
        String[] msg;
        Message(String src){
            msg = src.split(",");
            timeMoment = System.currentTimeMillis();
        }
        Object[] getRowData(){
            Object[] result=new Object[6];
            StringBuilder sb=new StringBuilder("");
            int i=1;
            result[i++]  = dateFormat.format(new Date(timeMoment));
            result[i++]  = sb
                    .append(msg[17])
                    .append(",")
                    .append(msg[18]);
            result[i++]  = dateFormat.format(new Date(timeMoment));
            return result;
        }
    }
    private Queue<Message> store;
    private long millisToHold;
    private JTable table;
    private DefaultTableModel model;
    private String[] headers = new String[]{
            "id","DateTime","Lat,Long","Speed","RCS","Classification"};
    public LocalTableAdapter(){
        store = new ArrayDeque<>() ;
        millisToHold = 1000*60*5;
        createUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

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
    private Object[][] getActialData(){
        Object[][] result;
        deleteOldData();
        result=new Object[store.size()][headers.length];
        return result;
    }
    private void start(){
        Runnable process = ()->{

        };
    }

    @Override
    public void processing(String rawData) {
        store.add(new Message(rawData));
    }
}
