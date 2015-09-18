package com.hivemq.example.database.callbacks;

import com.hivemq.spi.callback.CallbackPriority;
import com.hivemq.spi.callback.exception.AuthenticationException;
import com.hivemq.spi.callback.security.OnAuthenticationCallback;
import com.hivemq.spi.security.ClientCredentialsData;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Dominik Obermaier
 */
public class DBAuthenticationCallback implements OnAuthenticationCallback {

    private static Logger log = LoggerFactory.getLogger(DBAuthenticationCallback.class);

    private static final String SQLStatement = "SELECT password from `Users` where username = ?";

    private final Provider<Connection> connectionProvider;

    @Inject
    public DBAuthenticationCallback(final Provider<Connection> connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Boolean checkCredentials(final ClientCredentialsData clientData) throws AuthenticationException {
        if (clientData.getUsername().isPresent() && clientData.getPassword().isPresent()) {
            //You probably want to use caching here. You can either implement it your own,
            //use a caching framework or use the HiveMQ @Cached annotation on this method
            return lookupInDB(clientData);
        }
        return false;
    }

    private boolean lookupInDB(final ClientCredentialsData clientData) {
        final Connection connection = connectionProvider.get();

        try {
            final PreparedStatement statement = connection.prepareStatement(SQLStatement);
            statement.setString(1, clientData.getUsername().get());

            final ResultSet resultSet = statement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                //No result
                log.warn("User {} was not found in the database", clientData.getUsername().get());
                return false;
            }
            //We know there can only be at most one result because the query was on a unique field
            resultSet.next();
            final String password = resultSet.getString("password");

            //NEVER EVER even dream about doing this in production
            //ALWAYS use a proper hashing mechanism and use salts.
            //Read here how to do this properly: https://crackstation.net/hashing-security.htm
            if (clientData.getPassword().get().equals(password)) {
                log.info("Client {} authenticated successfully", clientData.getClientId());
                return true;
            }

        } catch (SQLException e) {
            log.error("An SQL error occured", e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Error while giving back connection to connection pool", e);
            }
        }
        return false;
    }

    @Override
    public int priority() {
        return CallbackPriority.HIGH;
    }
}
