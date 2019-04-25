package semantic.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


public class SshCommands {

	private final static Logger log = Logger.getLogger(SshCommands.class);

	private static final String unknowMessage = "I don't know what to do";
	private String user;
	private String host;
	private String password;
	private String eldaURI;
	private ArrayList<String> commands;
	private ArrayList<String> commandsType;
	private ArrayList<String> uploads;

	private String virtuosoPathToIsql;
	private String virtuosoPort;
	private String virtuosoUser;
	private String virtuosoPassword;

	public SshCommands() {
		user = "";
		host = "";
		password = "";
		eldaURI = "";
		commands = new ArrayList<String>();
		commandsType = new ArrayList<String>();
		uploads = new ArrayList<String>();
		virtuosoPort = "";
		virtuosoUser = "";
		virtuosoPassword = "";
		virtuosoPathToIsql = ""; 
	 }

	public SshCommands(Configuration conf) {
		user = conf.getUser();
		host = conf.getHost();
		password = conf.getPassword();
		eldaURI = conf.getEldaURI();
		commands = new ArrayList<String>();
		commandsType = new ArrayList<String>();
		uploads = new ArrayList<String>();
		virtuosoPort = conf.getVirtuosoPort();
		virtuosoUser = conf.getVirtuosoUser();
		virtuosoPassword = conf.getVirtuosoPassword();
		virtuosoPathToIsql = conf.getVirtuosoPathToIsql();
	}
	
	public String getEldaURI() {
		return eldaURI;
	}



	public void setEldaURI(String eldaURI) {
		this.eldaURI = eldaURI;
	}



	public ArrayList<String> getUploads() {
		return uploads;
	}

	public void setUploads(ArrayList<String> uploads) {
		this.uploads = uploads;
	}

	public ArrayList<String> getCommandsType() {
		return commandsType;
	}

	public void setCommandsType(ArrayList<String> commandsType) {
		this.commandsType = commandsType;
	}

	public String getVirtuosoPort() {
		return virtuosoPort;
	}

	public void setVirtuosoPort(String virtuosoPort) {
		this.virtuosoPort = virtuosoPort;
	}

	public String getVirtuosoUser() {
		return virtuosoUser;
	}

	public void setVirtuosoUser(String virtuosoUser) {
		this.virtuosoUser = virtuosoUser;
	}

	public String getVirtuosoPassword() {
		return virtuosoPassword;
	}

	public void setVirtuosoPassword(String virtuosoPassword) {
		this.virtuosoPassword = virtuosoPassword;
	}

	public ArrayList<String> getCommands() {
		return commands;
	}

	public void setCommands(ArrayList<String> commands) {
		this.commands = commands;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	

	public String getVirtuosoPathToIsql() {
		return virtuosoPathToIsql;
	}

	public void setVirtuosoPathToIsql(String virtuosoPathToIsql) {
		this.virtuosoPathToIsql = virtuosoPathToIsql;
	}

	public void addCommand(String command, String type) {
		if ((commands != null) && (commandsType != null)) {
			commands.add(command);
			commandsType.add(type);
		} else
			log.info("Object not initialized");

	}

	public void execute() throws JSchException, IOException {
		execute(false);
	}
	
	public void execute(boolean local) throws JSchException, IOException {
		JSch jsch = new JSch();
		LocalCommands comandos=null;
		Session session = null;
		
		if (local)
		{
			log.info("Running in local mode...");
		}
		else 
		{
			log.info("Connecting to Virtuoso host...");
			session = jsch.getSession(user, host, 22);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect(10 * 1000);
			
			log.info("Connected");

		}
						

		Channel channel = null;
		
		if (local)
		{
			comandos=new LocalCommands();
		}
				
		for (int i = 0; i < commands.size(); i++) {

			if (local)
			{
				String command = prepareCommand(commands.get(i), commandsType.get(i));
				String logCommand= prepareLogCommand(commands.get(i), commandsType.get(i));
				log.info("Command:" + logCommand);
				
				comandos.run(command);
				
			}			
			else 
			{
				channel = session.openChannel("exec");
	
				String command = prepareCommand(commands.get(i), commandsType.get(i));
				String logCommand= prepareLogCommand(commands.get(i), commandsType.get(i));
				log.info("Command:" + logCommand);
	
				((ChannelExec) channel).setCommand(command);
				channel.setInputStream(null);
				((ChannelExec) channel).setErrStream(System.err);
				InputStream in = channel.getInputStream();
	
				channel.connect();
	
				executionResponse(channel, in);
			
				log.info("Command: " + logCommand + " finished");
				
				channel.disconnect();
				session.disconnect();
			}
		}
		

		log.info("ends of execution");

	}

	private String prepareCommand(String command, String type) {
		String commando = "";

		if (type.toLowerCase().equals("virtuoso")) {
			commando =this.getVirtuosoPathToIsql()+" "+this.getHost()+":" + this.getVirtuosoPort() + " " + this.getVirtuosoUser() + " " + this.getVirtuosoPassword() + "  exec=\"" + command + "\";";
		} else if (type.toLowerCase().equals("system")) {
			commando = command;
		}
		return commando;
	}
	
	//En esta funcion omitimos el password, porque se va a logear
	private String prepareLogCommand(String command, String type) {
		String commando = "";

		if (type.toLowerCase().equals("virtuoso")) {
			commando = this.getVirtuosoPathToIsql()+"  "+this.getHost()+":" + this.getVirtuosoPort() + " " + this.getVirtuosoUser() + "  exec=\"" + command + "\";";
		} else if (type.toLowerCase().equals("system")) {
			commando = command;
		}
		return commando;
	}

	private void executionResponse(Channel channel, InputStream in) throws IOException {
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				log.info(new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				log.info("exit-status: " + channel.getExitStatus());
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}
	}

	public void readCommandsFile(String rutaFichero) throws IOException {
		commands = new ArrayList<String>();

		BufferedReader br = new BufferedReader(new FileReader(rutaFichero));
		try {

			String line = br.readLine();
			while (line != null) {
				if ((!line.equals("")) && (!line.startsWith("#"))) {
					commandsType.add(line.substring(0, line.indexOf(" ")));
					commands.add(line.substring(line.indexOf(" ") + 1));
				}
				line = br.readLine();
			}
			log.info(commands.size() + " Commands loaded");

		} finally {
			br.close();
		}
	}

	public void readServerFile(String file) throws IOException {
		InputStream inputstream = new FileInputStream(file);

		Properties prop = new Properties();
		prop.load(inputstream);

		this.setHost(prop.getProperty("host"));
		this.setUser(prop.getProperty("user"));
		this.setPassword(prop.getProperty("password"));

		this.setVirtuosoUser(prop.getProperty("virtuosoUser"));
		this.setVirtuosoPassword(prop.getProperty("virtuosoPassword"));
		this.setVirtuosoPort(prop.getProperty("virtuosoPort"));
		this.setVirtuosoPathToIsql(prop.getProperty("virtuosoPathToIsql"));
		
		this.setEldaURI(prop.getProperty("eldaURI"));

	}

	public void readUploadsFile(String rutaFichero) throws IOException {
		uploads = new ArrayList<String>();

		BufferedReader br = new BufferedReader(new FileReader(rutaFichero));
		try {

			String line = br.readLine();
			while (line != null) {
				if ((!line.equals("")) && (!line.startsWith("#"))) {
					uploads.add(line);

				}
				line = br.readLine();
			}
			log.info(uploads.size() + " Uploads loaded");

		} finally {
			br.close();
		}
	}

	public void uploads() throws JSchException, SftpException {

		JSch jsch = new JSch();

		Session session = jsch.getSession(user, host, 22);
		session.setPassword(password);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect(10 * 1000);

		Channel channel = session.openChannel("sftp");
		channel.connect();

		ChannelSftp sftp = (ChannelSftp) channel;

		for (String linea : uploads) {
			try {
				log.info("Upload: " + linea);
				sftp.put(linea.substring(0, linea.indexOf(" ")), linea.substring(linea.indexOf(" ") + 1));
				log.info("Upload: ok");

			} catch (Exception e) {
				log.error("Error executing command", e);
			}
		}

		channel.disconnect();
		session.disconnect();

	}

	

	
	
	public void clearCache()  {
		
		if ((eldaURI!=null)&&(!eldaURI.equals("")))
		{
		
			String url=eldaURI+"/control/clear-cache";
			try {
				log.info("Calling "+eldaURI+" to clear cache");
				Utils.processURLPost(url, "", null, "");
				log.info("cache cleared");
			} catch (IOException e) {
				log.error("Error clearing cache",e);			
			}
		
		}
	}
	

	public static void main(String[] args) {

		File logFile = new File("log4j.properties");

		if (logFile.exists())
			PropertyConfigurator.configure("log4j.properties");
		else
			org.apache.log4j.BasicConfigurator.configure();

		// log.info("hello");

		SshCommands commandos = new SshCommands();

		if (args.length == 1) {
			if (args[0].equals("init")) {
				String content = "";
				content += "virtuoso SPARQL CLEAR GRAPH <http://www.localidata.com/graph>" + System.getProperty("line.separator");
				content += "virtuoso ld_dir_all('/temp/data','*.rdf','http://www.localidata.com/graph')" + System.getProperty("line.separator");
				content += "virtuoso rdf_loader_run()" + System.getProperty("line.separator");				
				content += "virtuoso DB.DBA.RDF_OBJ_FT_RULE_ADD (null, null, 'All')"+ System.getProperty("line.separator");
				content += "virtuoso DB.DBA.VT_INC_INDEX_DB_DBA_RDF_OBJ()"+ System.getProperty("line.separator");
				content += "virtuoso DB.DBA.VT_INDEX_DB_DBA_RDF_OBJ()"+ System.getProperty("line.separator");

				Utils.generateDefaultFile(content, "commands.properties");

				content = "";
				content += "host=localhost" + System.getProperty("line.separator");
				content += "user=localidata" + System.getProperty("line.separator");
				content += "password=localidata" + System.getProperty("line.separator");
				content += "" + System.getProperty("line.separator");
				content += "virtuosoPort=1111" + System.getProperty("line.separator");
				content += "virtuosoUser=dba" + System.getProperty("line.separator");
				content += "virtuosoPassword=dba" + System.getProperty("line.separator");
				content += "" + System.getProperty("line.separator");
				content += "eldaURI=http://localhost:8080/api" + System.getProperty("line.separator");

				Utils.generateDefaultFile(content, "server.properties");

				content = "";
				content += "log4j.appender.fileAppender=org.apache.log4j.RollingFileAppender" + System.getProperty("line.separator");
				content += "log4j.appender.fileAppender.File=log.log" + System.getProperty("line.separator");
				content += "log4j.appender.fileAppender.Append=true" + System.getProperty("line.separator");
				content += "log4j.appender.fileAppender.MaxFileSize=1MB" + System.getProperty("line.separator");
				content += "log4j.appender.fileAppender.MaxBackupIndex=30" + System.getProperty("line.separator");
				content += "log4j.appender.fileAppender.layout=org.apache.log4j.PatternLayout" + System.getProperty("line.separator");
				content += "log4j.appender.fileAppender.layout.ConversionPattern=%d [%t] %-5p %c - %m%n" + System.getProperty("line.separator");
				content += "log4j.appender.fileAppender.Encoding=UTF-8" + System.getProperty("line.separator");

				content += "log4j.rootLogger=info, fileAppender";

				Utils.generateDefaultFile(content, "log4j.properties");

			} else {
				log.info(unknowMessage);
			}
			return;
		} else if (args.length == 0) {

			try {

				commandos.readServerFile("server.properties");
				commandos.readCommandsFile("commands.properties");
				// commandos.readUploadsFile("uploads.properties");

				commandos.execute();
				// commandos.uploads();
				
				commandos.clearCache();

				// String
				// ruta="C:\\Users\\Juan Carlos\\Google Drive\\Datos\\01_CNAE2009";
				// String directorioSalida="D:\\temp\\constructs";
				// commandos.constructs(ruta, directorioSalida);
				//
				// ruta="C:\\Users\\Juan Carlos\\Google Drive\\Datos\\02_Madrid";
				// commandos.constructs(ruta, directorioSalida);

			} catch (Exception e) {
				log.error("Error executing commands", e);
			}

		} else {
			log.info(unknowMessage);
		}

	}

}
