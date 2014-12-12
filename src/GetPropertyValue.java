import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class GetPropertyValue {

	public Properties getProps() throws IOException{
		java.util.Properties prop = new Properties();
		String propFileName = "config.properties";
		InputStream inputStream;
		
		inputStream =  getClass().getClassLoader().getResourceAsStream(propFileName);
		prop.load(inputStream);
		if (inputStream == null) {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		
		inputStream.close();
		return prop;
	}
}
