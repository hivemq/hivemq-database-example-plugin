package com.hivemq.example.database.callbacks;

import com.hivemq.spi.callback.CallbackPriority;
import com.hivemq.spi.callback.events.broker.OnBrokerStop;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author Dominik Obermaier
 */
public class ShutdownCallback implements OnBrokerStop {

    private static Logger log = LoggerFactory.getLogger(ShutdownCallback.class);
    private final HikariDataSource hikariDataSource;


    @Inject
    public ShutdownCallback(final HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    @Override
    public void onBrokerStop() {
        log.info("Shutting down DB connection pool");
        hikariDataSource.shutdown();
    }

    @Override
    public int priority() {
        return CallbackPriority.LOW;
    }
}
