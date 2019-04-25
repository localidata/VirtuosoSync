package semantic.sync;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.github.axet.wget.WGet;

public class Utils {

	private static final Logger log = Logger.getLogger(SemanticSyncProcess.class);
	
	private static final int defaultReadTimeOut = 60000;
	private static final int defaultTimeOut = 10000;
	
	
	public static int downloadResource(String fileUrl, File target) {
		try {

			URL url = new URL(fileUrl);
			WGet w = new WGet(url, target);
			w.download();
			return 1;
		} catch (Exception e) {
			log.error("Error downloading file: " + fileUrl, e);
			return -1;
		}
	}
	
	public static void stringToFile(String string, File file) throws Exception{
	      FileWriter fWriter;
	      BufferedWriter bWriter;
	      fWriter = new FileWriter(file);
	      bWriter = new BufferedWriter(fWriter);
	      bWriter.write(string);
	      bWriter.close();
	      fWriter.close();	    
	  }
	
	public static String processURLPost(String url, String urlParameters, Map<String, String> headers, String body) throws IOException {

		HttpURLConnection httpConnection = null;
		try {

			StringBuffer sb = new StringBuffer();
			
			if ((urlParameters!=null)&&(!urlParameters.equals("")))
				url=url + "?" + urlParameters;
			
			URL targetUrl = new URL(url);
			httpConnection = (HttpURLConnection) targetUrl.openConnection();
			httpConnection.setDoOutput(true);
			httpConnection.setRequestMethod("POST");
			httpConnection.setConnectTimeout(defaultTimeOut);
			httpConnection.setReadTimeout(defaultReadTimeOut);

			if (headers != null) {
				Iterator<Entry<String, String>> it = headers.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, String> pairs = it.next();
					httpConnection.setRequestProperty(pairs.getKey(), pairs.getValue());
				}
			}

			OutputStream outputStream = httpConnection.getOutputStream();
			outputStream.write(body.getBytes());
			outputStream.flush();

			BufferedReader responseBuffer;

			responseBuffer = new BufferedReader(new InputStreamReader((httpConnection.getInputStream())));

			String output;

			while ((output = responseBuffer.readLine()) != null) {
				sb.append(output);
			}

			if ((httpConnection.getResponseCode() > 299) && (httpConnection.getResponseCode() < 200)) {
				log.error("The URI does not return a 2XX code");
				log.error(httpConnection.getResponseCode());
				httpConnection.disconnect();
				return "";
			}
			httpConnection.disconnect();
			return sb.toString();

		} catch (IOException e) {
			log.error("Error procesing URL by Post");
			if (httpConnection != null)
				httpConnection.disconnect();
			throw e;
		}

	}
	
	public static void generateDefaultFile(String content, String fileName) {
		File commandsFile = new File(fileName);
		if (commandsFile.exists() == false) {
			try {
				Utils.stringToFile(content, commandsFile);
				log.info("Default file " + fileName + " generated");
			} catch (Exception e) {
				log.error("Error generating file: " + commandsFile.getName(), e);

			}
		} else {
			log.info("The file " + commandsFile.getName() + " already exits.");
		}
	}

	public static boolean pingURL(String url) {
	    url = url.replaceFirst("^https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

	    try {
	        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	        connection.setConnectTimeout(defaultReadTimeOut);
	        connection.setReadTimeout(defaultReadTimeOut);
	        connection.setRequestMethod("HEAD");
	        int responseCode = connection.getResponseCode();
	        return (200 <= responseCode && responseCode <= 399);
	    } catch (IOException exception) {
	        return false;
	    }
	}
	
	
	
}
