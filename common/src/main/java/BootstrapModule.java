import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public abstract class BootstrapModule extends AbstractModule {
    private static final Logger logger = LogManager.getLogger(BootstrapModule.class);

    @Override
    protected void configure() {
        try {
            Names.bindProperties(binder(), loadProperties());
        } catch (Exception e) {
            logger.error(e);
        }

        configureModules();
    }

    private Properties loadProperties() throws Exception {
        Properties properties = new Properties();
        properties.load(
                this.getClass()
                        .getClassLoader()
                        .getResourceAsStream("config.properties"));
        return properties;
    }

    protected <T> void bindSingleton(Class<T> bindClass) {
        bind(bindClass).asEagerSingleton();
    }

    protected abstract void configureModules();
}
