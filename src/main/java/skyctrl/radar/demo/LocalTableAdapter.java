package skyctrl.radar.demo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import static com.sun.java.accessibility.util.AWTEventMonitor.addActionListener;

public class LocalTableAdapter extends JFrame implements IAdapter {
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");

    static class Message {
        Long timeMoment;
        String id, H, DateTime, Lat, Long, Speed, RCS, Classification;
        int speed_ms;

        String[] msg;

        Message(String src, Message previous) {
            this(src);
            if (previous != null) {
                //TODO calc speed in moment
            }
        }

        Message(String src) {
            msg = src.split(",");
            timeMoment = System.currentTimeMillis();
            id = msg[14];

            H = "" + Math.round(Double.parseDouble(msg[1]) * Math.sin(Double.parseDouble(msg[4]) * Math.PI / 180));
            DateTime = dateFormat.format(new Date(timeMoment));
            Lat = msg[18];
            Long = msg[19];
            //Speed = msg[7];
            //RCS = msg[8];
            Classification = msg[12];
        }

        Object[] getRowData() {
            Object[] result = new Object[5];

            int i = 0;
            result[i++] = id;
            result[i++] = H;
            result[i++] = "" + Lat + "," + Long;
            //result[i++]  = Speed;
            //result[i++]  = RCS;
            result[i++] = Classification;
            result[i++] = DateTime;

            return result;
        }
    }

    private Queue<Message> store;
    private Map<String, Message> storeMap;

    private long millisToHold;
    private JTable table;
    private int currentRow;
    private DefaultTableModel model;
    private ExecutorService executorService;
    private String[] headers = new String[]{
            "id", "H", "Lat,Long", "Classification", "DateTime"};

    public LocalTableAdapter() {
        store = new ArrayDeque<>();
        storeMap = new ConcurrentHashMap<>();
        millisToHold = 1000 * 60 * 2;
        createUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 300);
        //this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        executorService = Executors.newScheduledThreadPool(1);

    }

    private void createUI() {
        model = new DefaultTableModel();
        table = new JTable(model);
        currentRow = -1;
        model.setColumnCount(headers.length);
        model.setColumnIdentifiers(headers);
        table.setAutoCreateRowSorter(true);

        final JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItemCopy = new JMenuItem("Copy");
        menuItemCopy.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentRow == -1){
                    return;
                }
                int rowIndex = currentRow;

                int columnIndexH = table.getColumn("H").getModelIndex();
                int columnIndexLatLong = table.getColumn("Lat,Long").getModelIndex();

                String height = (String) table.getValueAt(rowIndex, columnIndexH);
                String LatLong = (String) table.getValueAt(rowIndex, columnIndexLatLong);

                StringSelection stringSelection = new StringSelection(height + "\t"+ LatLong);

                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(stringSelection, null);
                currentRow = -1;
            }


        });
        popup.add(menuItemCopy);

        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 3) {
                    int rowIndex = table.rowAtPoint(e.getPoint());
                    if (rowIndex == -1){
                        return;
                    }
                    currentRow = rowIndex;
                    int columnIndex = table.columnAtPoint(e.getPoint());

                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }

        });
        add(new JScrollPane(table));
    }

    private void deleteOldData() {
        long limitTimeMoment = System.currentTimeMillis() - millisToHold;
        while (store.peek() != null && store.peek().timeMoment < limitTimeMoment) {
            store.remove();
        }
        Set<String> keySet = storeMap.keySet();
        for (String key : keySet) {
            if (storeMap.get(key).timeMoment < limitTimeMoment) {
                storeMap.remove(key);
            }
        }
    }

    private Object[][] getActualData() {
        Object[][] result;
        if (store.size() == 0) {
            return new Object[0][headers.length];
        }


        deleteOldData();
        //List<Message> messagesList = new ArrayList<>();
        //messagesList.addAll(storeMap.values());
        //messagesList.sort((msg1, msg2) -> Long.compare(msg1.timeMoment, msg1.timeMoment));
        result = new Object[storeMap.values().size()][];
        int i = 0;
        for (Message row : storeMap.values()) {
            result[i++] = row.getRowData();
        }
        return result;
    }

    private void start() {
        Runnable process = () -> {
            int columnIndexId = table.getColumn("id").getModelIndex();

            int n = table.getRowCount()-1;

            Set<String> founded=new HashSet<>();
            while (n>=0){
                Message msg = storeMap.get(table.getValueAt(n,columnIndexId));
                if (msg == null){
                    table.remove(n);
                }else {
                    founded.add(msg.id);
                    Object[] rowData=msg.getRowData();
                    for(int i=0;i<rowData.length;i++){
                        table.setValueAt(rowData[i],n,i);
                    }

                }
                n--;
            }

            for (Message row : storeMap.values()) {
                if (founded.contains(row.id)){
                    continue;
                }

                Object[] rowData=row.getRowData();
                model.addRow(rowData);

            }

            //model.setDataVector(getActualData(), headers);


        };
        if (executorService instanceof ScheduledExecutorService) {
            ((ScheduledExecutorService) executorService).scheduleAtFixedRate(process, 10, 5, TimeUnit.SECONDS);
        } else {
            executorService.execute(process);
        }
    }

    @Override
    public void processing(String rawData) {
        Message msg = new Message(rawData);
        store.add(msg);
        storeMap.put(msg.id, msg);
    }
}
