package org.georgemalandrakis.archion.dao;

import org.georgemalandrakis.archion.core.ConnectionManager;
import org.georgemalandrakis.archion.core.ArchionRequest;

import java.sql.*;

public abstract class AbstractDAO {
    protected final ConnectionManager connectionObject;
    protected final Connection connection;

    public AbstractDAO(ConnectionManager connectionObject) {
        this.connectionObject = connectionObject;
        this.connection = getConnection();
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://" + this.connectionObject.getBaseServer() + "/" + this.connectionObject.getBaseName(),
                    this.connectionObject.getBaseUsername(), this.connectionObject.getBasePassword());
            connection.setAutoCommit(true); //Perhaps unsafe
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE); //Nästintill omöjligt att få transaction-anomalies

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return connection;
    }

    public static void closeSilently(AutoCloseable... autoCloseables) {
        //The autocloseables are assumed to be in low-to-high order. That is, resultSet-statement-connection. Different orders are exception prone.
        for (AutoCloseable autoCloseable : autoCloseables) {
            if (autoCloseable != null) {
                try {
                    autoCloseable.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
