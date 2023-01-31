package skyctrl.radar.demo;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

//@SpringBootApplication
//@ImportResource({"classpath*:spring.xml"})
public class App {
	private static App instance;
	private static ConfigurableApplicationContext ctx;
	/*public static DemoApplication getInstance(){
		return instance;
	}

	 */
	public static ConfigurableApplicationContext getCTX(){
		return App.ctx;
	}
	public static void main(String[] args) {
		/*ConfigurableApplicationContext applicationContext =
		SpringApplication.run(DemoApplication.class, args);
		instance= new DemoApplication(applicationContext);
		getInstance();
		IRadarServer stv= (IRadarServer) getCTX().getBean("localServerName");
		 */
		ctx = new ClassPathXmlApplicationContext("spring.xml");
		instance = (App)ctx.getBean("App");
		IRadarServer radarServer=(IRadarServer)ctx.getBean("localServerName");

	}
	public App(){
	}

}
