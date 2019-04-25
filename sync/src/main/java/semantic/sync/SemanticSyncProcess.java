package semantic.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class SemanticSyncProcess 
{
	private static final Logger log = Logger.getLogger(SemanticSyncProcess.class);
	
	private static final String RdfFilePath = "download"+File.separator+"catalog.rdf";
	
	private Configuration configuration;
		
	public Configuration getConfiguration() {
		return configuration;
	}

	public void readConfigurationFile(String file) throws IOException {
		
		Properties prop = new Properties();
		InputStream inputstream=null;
		
		inputstream = new FileInputStream(file);
		prop.load(inputstream);
		
		configuration=new Configuration();
			
		configuration.setCatalogURL(prop.getProperty("catalogURL"));			
		configuration.setHost(prop.getProperty("host"));
		configuration.setUser(prop.getProperty("user"));
		configuration.setPassword(prop.getProperty("password"));

		configuration.setVirtuosoUser(prop.getProperty("virtuosoUser"));
		configuration.setVirtuosoPassword(prop.getProperty("virtuosoPassword"));
		configuration.setVirtuosoPort(prop.getProperty("virtuosoPort"));
		
		configuration.setEldaURI(prop.getProperty("eldaURI"));
	}
	
	public int dowloadOrCopyFile()
	{
		File downloadFolder = new File("download");
    	File downloadFile = new File(RdfFilePath);
    	File downloadFileTemp = new File("download"+File.separator+"catalogTemp.rdf");
    	boolean fileOpStatus=false;
    	int status=0;   	
    	
    	
    	if (configuration.getCatalogURL().contains("http"))
    	{
    		
    	
    	
    	 log.info("Downloading file: "+configuration.getCatalogURL());
         
         
         if (!downloadFolder.exists()){
         	fileOpStatus=downloadFolder.mkdir();
         }else{
         	fileOpStatus=true;
         }
         if (fileOpStatus==false)
         {
         	log.error("The download folder can´t be created");
         	return -1;
         }
         
         if (!downloadFile.exists()){         	
         	status=Utils.downloadResource(configuration.getCatalogURL(), downloadFile);
         	if (status<0){
         		log.error("Error downloading file");
         		return -1;
         	}
         	log.info("File downloaded");
         }
         else{         	
         	status=Utils.downloadResource(configuration.getCatalogURL(), downloadFileTemp);        	
         	if (status>0)
         	{
         		log.info("Temp. file downloaded");
         		fileOpStatus=downloadFile.delete();        		
         		if (fileOpStatus)
         		{
         			log.info("Old file deleted");
         			fileOpStatus=downloadFileTemp.renameTo(downloadFile);
         			if (fileOpStatus)
         			{
         				log.info("New file stored as default catalog file");
         			}else{
         				log.error("Error renaming file");
         				return -1;
         			}
         		}
         		else{
         			log.error("Error deleting file");
         			return -1;
         		}
         	}
         	else{
         		log.error("Error downloading file");
         		return -1;
         	}
         }
         
    	}else{
    		 log.info("Copying file: "+configuration.getCatalogURL());
    		 
    		 File originFile=new File(configuration.getCatalogURL());
    		 
    		 try {
				FileUtils.copyFile(originFile, downloadFile);
				System.out.println("File copied");
			} catch (IOException e) {
				System.out.println("Error, please check the log file");
				log.error("Error copying file",e);
				return -1;
			}
    		 
    	}
         
         return 1;
	}
	
	
	public void generateDefaultFiles() {
		String content = "";
		content += "virtuoso SPARQL CLEAR GRAPH <http://www.localidata.com/graph>" + System.getProperty("line.separator");
		content += "virtuoso ld_dir_all('/temp/data','*.rdf','http://www.localidata.com/graph')" + System.getProperty("line.separator");
		content += "virtuoso rdf_loader_run()" + System.getProperty("line.separator");				
		content += "virtuoso DB.DBA.RDF_OBJ_FT_RULE_ADD (null, null, 'All')"+ System.getProperty("line.separator");
		content += "virtuoso DB.DBA.VT_INC_INDEX_DB_DBA_RDF_OBJ()"+ System.getProperty("line.separator");
		content += "virtuoso DB.DBA.VT_INDEX_DB_DBA_RDF_OBJ()"+ System.getProperty("line.separator");

		Utils.generateDefaultFile(content, "commands.properties");

		content = "";
		content += "catalogURL=http://datos.gob.es/sites/default/files/catalogo.rdf"+ System.getProperty("line.separator");
		content += "" + System.getProperty("line.separator");
		content += "host=localhost" + System.getProperty("line.separator");
		content += "user=localidata" + System.getProperty("line.separator");
		content += "password=localidata" + System.getProperty("line.separator");
		content += "" + System.getProperty("line.separator");
		content += "virtuosoPort=1111" + System.getProperty("line.separator");
		content += "virtuosoUser=dba" + System.getProperty("line.separator");
		content += "virtuosoPassword=dba" + System.getProperty("line.separator");
		content += "" + System.getProperty("line.separator");
		content += "eldaURI=http://localhost:8080/api" + System.getProperty("line.separator");

		Utils.generateDefaultFile(content, "sync.properties");

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
	}
	
	
    public static void main( String[] args )
    {
    	File logF = new File("log4j.properties");   
    	int status=0;

		if (logF.exists()) {
			PropertyConfigurator.configure("log4j.properties");
		} else {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.INFO);
		}
    	
        log.info( "Semantic Sync 1.0" );
        SemanticSyncProcess app=new SemanticSyncProcess();   
        SshCommands commandos = null;
        
        if (args.length == 1) {
			if (args[0].equals("init")) {
				app.generateDefaultFiles();
				return;
			}
        }
        
        try {
        	app.readConfigurationFile("sync.properties");
        	commandos = new SshCommands(app.configuration);
        	
        } catch (IOException e) {
        	log.error("Error reading configuration file",e);
        	return;
        }
        
        
        
        if ((app.getConfiguration().getCatalogURL()!=null)&&(!app.getConfiguration().getCatalogURL().equals("")))
        {
        	log.info("Getting file...");
        	status=app.dowloadOrCopyFile();
        	if (status<0)
        	{
        		return;
        	}
        }else {
        	log.info("Initial file to load ommited");
        }
               
        try
        {        	
	        Date actualDate = new Date();
	        String TempFilePath = "download"+File.separator+"catalog"+actualDate.hashCode()+".rdf";
	        
	        log.info("Adding hash to catalog file: "+TempFilePath);
	        
	        File catalogFile = new File(RdfFilePath);
	        File hashCatalogFile = new File(TempFilePath);
	        catalogFile.renameTo(hashCatalogFile);
	        	
			commandos.readCommandsFile("commands.properties");
			commandos.execute();		
			commandos.clearCache();
			
			log.info("Removing hash to catalog file: "+catalogFile);
			hashCatalogFile.renameTo(catalogFile);
        }
        catch (Exception e)
        {
        	log.error("Error executing commands",e);
        }
        
                	
        log.info("End of process");
    }

	
}
