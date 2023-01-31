package skyctrl.radar.demo;

import java.util.List;

public interface IRadarServer {

    void setPort(int port);
    void setAdapters(List<IAdapter> adapters);
    void start();
    void stop();
}
