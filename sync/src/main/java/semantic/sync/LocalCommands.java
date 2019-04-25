package semantic.sync;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LocalCommands {
	
	private final static Logger log = Logger.getLogger(LocalCommands.class);
	
	private ProcessBuilder processBuilder = new ProcessBuilder();
	
	public void run(String command)
	{
		processBuilder.command("bash", "-c", command);

		try {

			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			String line="";
			List<String> lines=new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}

			int exitVal = process.waitFor();
			if (exitVal == 0) {				
				for (String tmp:lines)
				{
					log.info("\t"+tmp);
				}				
				log.info("Success");
			} else {
				log.error("Wrong exit val");
			}

		} catch (Exception e) {
			log.error("Error with command: "+command,e);		
		}
	}
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		LocalCommands commandos=new LocalCommands();
		commandos.run("ls");
	}
	
}
