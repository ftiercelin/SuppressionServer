package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class WebServerApp {
	protected static final Logger LOGGER = LoggerFactory.getLogger(WebServerApp.class);

	public static void main(String[] args) {
		long start=System.currentTimeMillis();
	    try {
	        SpringApplication.run(WebServerApp.class, args);
			banner(start);
			LOGGER.info("started");
	    } catch (BeanCreationException ex) {
	        Throwable realCause = unwrap(ex);
	        LOGGER.error("Failed to start");
	        if(realCause!=null) {
		        LOGGER.error("real cause",realCause);
		        LOGGER.error("{}",realCause.getMessage());
	        } else 
		        LOGGER.error("no real cause: ",ex);
	    }		
	}
	protected static float banner(long start) {
		String sepLine="======================================================================";
		StringBuilder sb = new StringBuilder(sepLine+System.lineSeparator());
		sb.append("        #####                                                         "+System.lineSeparator());
		sb.append("       #     #   #####    ##    #####    #####  ######  #####         "+System.lineSeparator());
		sb.append("       #           #     #  #   #    #     #    #       #    #        "+System.lineSeparator());
		sb.append("        #####      #    #    #  #    #     #    #####   #    #        "+System.lineSeparator());
		sb.append("             #     #    ######  #####      #    #       #    #        "+System.lineSeparator());
		sb.append("       #     #     #    #    #  #   #      #    #       #    #        "+System.lineSeparator());
		sb.append("        #####      #    #    #  #    #     #    ######  #####         "+System.lineSeparator());
		sb.append(sepLine+System.lineSeparator());

		float time = (System.currentTimeMillis()-start);
		time/=1000;
		sb.append(
			 String.format("                      - %.3f seconds -", time)+System.lineSeparator());
		sb.append(sepLine+System.lineSeparator());
		System.out.println(sb);
		return time;
	}

	public static Throwable unwrap(Throwable ex) {
	    if (ex != null && BeanCreationException.class.isAssignableFrom(ex.getClass())) {
	        return unwrap(ex.getCause());
	    } else {
	        return ex;
	    }
	}
}
