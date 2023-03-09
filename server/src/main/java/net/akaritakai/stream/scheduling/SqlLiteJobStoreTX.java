package net.akaritakai.stream.scheduling;

import org.quartz.impl.jdbcjobstore.AttributeRestoringConnectionInvocationHandler;
import org.quartz.impl.jdbcjobstore.JobStoreTX;

import java.lang.reflect.Proxy;
import java.sql.Connection;

public class SqlLiteJobStoreTX extends JobStoreTX {
    /**
     * Wrap the given <code>Connection</code> in a Proxy such that attributes
     * that might be set will be restored before the connection is closed
     * (and potentially restored to a pool).
     */
    protected Connection getAttributeRestoringConnection(Connection conn) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                new AttributeRestoringConnectionInvocationHandler(conn));

    }
}
