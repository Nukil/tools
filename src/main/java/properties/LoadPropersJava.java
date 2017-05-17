package properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Created by Nukil on 2017/4/
 */
public class LoadPropersJava{
    private Properties properties = null;
    private Logger logger = Logger.getLogger(LoadPropersJava.class);

    private LoadPropersJava() {}

    private static LoadPropersJava instance = null;

    public static LoadPropersJava getSingleInstance() {
        if (null == instance) {
            synchronized (LoadPropersJava.class) {
                if (null == instance) {
                    instance = new LoadPropersJava();
                }
            }
        }
        return instance;
    }

    private Properties loadProperties() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Properties tmpProperties = new Properties();
        InputStream configStream = classLoader.getResourceAsStream("server.properties");
        try {
            tmpProperties.load(configStream);
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                configStream.close();
            } catch (IOException x) {
                logger.error(x.getMessage());
            }
        }
        return tmpProperties;
    }

    public Properties getProperties() {
        if (null == properties) {
            synchronized (LoadPropersJava.class) {
                if (null == properties) {
                    properties = loadProperties();
                }
            }
        }
        return properties;
    }
}
