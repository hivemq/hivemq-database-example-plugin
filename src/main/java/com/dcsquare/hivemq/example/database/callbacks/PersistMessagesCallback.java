package com.dcsquare.hivemq.example.database.callbacks;

import com.dcsquare.hivemq.spi.callback.CallbackPriority;
import com.dcsquare.hivemq.spi.callback.events.OnPublishReceivedCallback;
import com.dcsquare.hivemq.spi.callback.exception.OnPublishReceivedException;
import com.dcsquare.hivemq.spi.message.PUBLISH;
import com.dcsquare.hivemq.spi.security.ClientData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Dominik Obermaier
 */
public class PersistMessagesCallback implements OnPublishReceivedCallback {

    private static Logger log = LoggerFactory.getLogger(PersistMessagesCallback.class);
    private final Provider<Connection> connectionProvider;

    private static final String SQLStatement = "INSERT INTO `Messages` (message,topic,qos,client) VALUES (?,?,?,?)";


    @Inject
    public PersistMessagesCallback(Provider<Connection> connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void onPublishReceived(final PUBLISH publish, final ClientData clientData) throws OnPublishReceivedException {
        final Connection connection = connectionProvider.get();

        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(SQLStatement);
            preparedStatement.setBytes(1, publish.getPayload());
            preparedStatement.setString(2, publish.getTopic());
            preparedStatement.setInt(3, publish.getQoS().getQosNumber());
            preparedStatement.setString(4, clientData.getClientId());

            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            log.error("An error occured while preparing the SQL statement", e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("An error occured while giving back a connection to the connection pool");
            }
        }
    }

    @Override
    public int priority() {
        return CallbackPriority.HIGH;
    }
}
