package oeg.interoperability.client;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClientApplication {

	public static final int DISCOVERY_THREADS = 500;
	public static String GRAPHDB_ARGUMENT = "--server.repository=";
	public static String repositoryEndpoint = "http://localhost:7200/repositories/discovery";
	public static final String VERSION = "0.1.0";
	private Logger log = Logger.getLogger(ClientApplication.class.getName());


	public static void main(String[] args) {
		// main
		for(String arg:args) {
			if(arg.startsWith(GRAPHDB_ARGUMENT))
				repositoryEndpoint = arg.replace(GRAPHDB_ARGUMENT, "").trim();
		}
		

		SpringApplication.run(ClientApplication.class, args);
	}
	
	@PostConstruct
	public void printVersion() {
		log.info("Current verion: "+VERSION);
	}
}
