package net.akaritakai.stream.scheduling;

import org.quartz.impl.jdbcjobstore.StdJDBCDelegate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlLiteJDBCDelegate extends StdJDBCDelegate {

    /**
     * <p>
     * This method should be overridden by any delegate subclasses that need
     * special handling for BLOBs. The default implementation uses standard
     * JDBC <code>java.sql.Blob</code> operations.
     * </p>
     *
     * @param rs
     *          the result set, already queued to the correct row
     * @param colName
     *          the column name for the BLOB
     * @return the deserialized Object from the ResultSet BLOB
     * @throws ClassNotFoundException
     *           if a class found during deserialization cannot be found
     * @throws IOException
     *           if deserialization causes an error
     */
    protected Object getObjectFromBlob(ResultSet rs, String colName)
            throws ClassNotFoundException, IOException, SQLException {
        Object obj = null;

        byte[] blobLocator = rs.getObject(colName, byte[].class);
        if (blobLocator != null && blobLocator.length != 0) {
            ByteArrayInputStream binaryInput = new ByteArrayInputStream(blobLocator);
            if (binaryInput.available() > 0) {
                try (ObjectInputStream in = new ObjectInputStream(binaryInput)) {
                    obj = in.readObject();
                }
            }
        }
        return obj;
    }

    /**
     * <p>
     * This method should be overridden by any delegate subclasses that need
     * special handling for BLOBs for job details. The default implementation
     * uses standard JDBC <code>java.sql.Blob</code> operations.
     * </p>
     *
     * @param rs
     *          the result set, already queued to the correct row
     * @param colName
     *          the column name for the BLOB
     * @return the deserialized Object from the ResultSet BLOB
     * @throws ClassNotFoundException
     *           if a class found during deserialization cannot be found
     * @throws IOException
     *           if deserialization causes an error
     */
    protected Object getJobDataFromBlob(ResultSet rs, String colName)
            throws ClassNotFoundException, IOException, SQLException {
        if (canUseProperties()) {
            byte[] blobLocator = rs.getObject(colName, byte[].class);
            if (blobLocator != null) {
                return new ByteArrayInputStream(blobLocator);
            } else {
                return null;
            }
        }

        return getObjectFromBlob(rs, colName);
    }

}
