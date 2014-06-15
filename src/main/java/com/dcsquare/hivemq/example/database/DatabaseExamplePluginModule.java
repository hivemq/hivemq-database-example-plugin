package com.dcsquare.hivemq.example.database;

import com.dcsquare.hivemq.spi.HiveMQPluginModule;
import com.dcsquare.hivemq.spi.PluginEntryPoint;
import com.dcsquare.hivemq.spi.plugin.meta.Information;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.configuration.AbstractConfiguration;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;

import static com.dcsquare.hivemq.spi.config.Configurations.noConfigurationNeeded;

/**
 * @author Dominik Obermaier
 */
@Information(
        name = "Database Example Plugin",
        version = "1.0-SNAPSHOT",
        author = "dc-square GmbH",
        description = "A example plugin which persists every message to the database and authenticates clients from the database")
public class DatabaseExamplePluginModule extends HiveMQPluginModule {


    @Provides
    public Connection provideConnection(final HikariDataSource ds) throws SQLException {
        return ds.getConnection();
    }

    @Provides
    @Singleton
    public HikariDataSource provideConnectionPool() {

        //See https://github.com/brettwooldridge/HikariCP

        final HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(15);
        config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        config.addDataSourceProperty("serverName", "localhost");
        config.addDataSourceProperty("port", "8889");
        config.addDataSourceProperty("databaseName", "HiveMQ");
        config.addDataSourceProperty("user", "root");
        config.addDataSourceProperty("password", "root");

        //See https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration

        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("useServerPrepStmts", true);

        return new HikariDataSource(config);

    }

    @Override
    public Provider<Iterable<? extends AbstractConfiguration>> getConfigurations() {
        //You probably want an external file configuration
        return noConfigurationNeeded();
    }

    @Override
    protected void configurePlugin() {
    }

    @Override
    protected Class<? extends PluginEntryPoint> entryPointClass() {
        return DatabaseExamplePlugin.class;
    }
}