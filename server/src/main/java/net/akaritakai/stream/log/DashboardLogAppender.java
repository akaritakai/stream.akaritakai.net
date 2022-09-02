package net.akaritakai.stream.log;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.*;

import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.util.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Plugin(name = DashboardLogAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class DashboardLogAppender extends AbstractAppender {
    public static final String PLUGIN_NAME = "DashboardLog";
    private static DashboardManagerFactory factory = new DashboardManagerFactory();

    /**
     * Immediate flush means that the underlying writer or output stream will be flushed at the end of each append
     * operation. Immediate flush is slower but ensures that each append request is actually written. If
     * <code>immediateFlush</code> is set to {@code false}, then there is a good chance that the last few logs events
     * are not actually written to persistent media if and when the application crashes.
     */
    private final boolean immediateFlush;
    private DashboardManager manager;

    /**
     * Instantiates a WriterAppender and set the output destination to a new {@link java.io.OutputStreamWriter}
     * initialized with <code>os</code> as its {@link java.io.OutputStream}.
     *
     * @param name The name of the Appender.
     * @param layout The layout to format the message.
     * @param properties optional properties
     * @param manager The OutputStreamManager.
     */
    protected DashboardLogAppender(final String name, final Layout<? extends Serializable> layout,
                                   final Filter filter, OutputStreamManager manager,
                                   final boolean ignoreExceptions, final boolean immediateFlush,
                                   final Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.immediateFlush = immediateFlush;
        this.manager = (DashboardManager) manager;
    }

    @PluginBuilderFactory
    public static <B extends DashboardLogAppender.Builder<B>> B newBuilder() {
        return new DashboardLogAppender.Builder<B>().asBuilder();
    }

    /**
     * Builds ConsoleAppender instances.
     * @param <B> The type to build
     */
    public static class Builder<B extends DashboardLogAppender.Builder<B>> extends AbstractOutputStreamAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<DashboardLogAppender> {

        @Override
        public DashboardLogAppender build() {
            if (!isValid()) {
                return null;
            }
            final Layout<? extends Serializable> layout = getOrCreateLayout(StandardCharsets.UTF_8);
            return new DashboardLogAppender(getName(), layout, getFilter(), getManager(layout),
                    isIgnoreExceptions(), isImmediateFlush(), getPropertyArray());
        }
    }


    private static OutputStreamManager getManager(final Layout<? extends Serializable> layout) {
        final OutputStream os = getOutputStream();
        final String managerName = "DashboardManager";
        return OutputStreamManager.getManager(managerName, new FactoryData(os, managerName, layout), factory);
    }

    /**
     * Gets the immediate flush setting.
     *
     * @return immediate flush.
     */
    public boolean getImmediateFlush() {
        return immediateFlush;
    }

    @Override
    public void append(LogEvent event) {
        try {
            tryAppend(event);
        } catch (final AppenderLoggingException ex) {
            error("Unable to write to stream " + manager.getName() + " for appender " + getName(), event, ex);
            throw ex;
        }
    }

    private void tryAppend(final LogEvent event) {
        if (Constants.ENABLE_DIRECT_ENCODERS) {
            directEncodeEvent(event);
        } else {
            writeByteArrayToManager(event);
        }
    }

    protected void directEncodeEvent(final LogEvent event) {
        getLayout().encode(event, manager);
        if (getImmediateFlush() || event.isEndOfBatch()) {
            manager.flush();
        }
    }

    protected void writeByteArrayToManager(final LogEvent event) {
        final byte[] bytes = getLayout().toByteArray(event);
        if (bytes != null && bytes.length > 0) {
            manager.write(bytes, getImmediateFlush() || event.isEndOfBatch());
        }
    }

    protected static class DashboardManager extends OutputStreamManager {
        protected DashboardManager(OutputStream os, String streamName, Layout<?> layout, boolean writeHeader) {
            super(os, streamName, layout, writeHeader);
        }

        public void write(final byte[] bytes, final boolean immediateFlush) {
            super.write(bytes, immediateFlush);
        }
    }


    /**
     * Data to pass to factory method.Unable to instantiate
     */
    private static class FactoryData {
        private final OutputStream os;
        private final String name;
        private final Layout<? extends Serializable> layout;

        /**
         * Constructor.
         *
         * @param os The OutputStream.
         * @param type The name of the target.
         * @param layout A Serializable layout
         */
        public FactoryData(final OutputStream os, final String type, final Layout<? extends Serializable> layout) {
            this.os = os;
            this.name = type;
            this.layout = layout;
        }
    }

    /**
     * Factory to create the Appender.
     */
    private static class DashboardManagerFactory implements ManagerFactory<DashboardManager, FactoryData> {

        /**
         * Create an OutputStreamManager.
         *
         * @param name The name of the entity to manage.
         * @param data The data required to create the entity.
         * @return The OutputStreamManager
         */
        @Override
        public DashboardManager createManager(final String name, final FactoryData data) {
            return new DashboardManager(data.os, data.name, data.layout, true);
        }
    }

    public interface DashboardLogListener {
        void acceptLog(byte[] data);
    }

    public static class DashboardOutputStream extends ByteArrayOutputStream {

        private final Set<DashboardLogListener> _listeners = ConcurrentHashMap.newKeySet();

        @Override
        public void flush() throws IOException {
            byte[] bytes = flush0();
            _listeners.forEach(listener -> listener.acceptLog(bytes));
        }

        private synchronized byte[] flush0() throws IOException {
            byte[] bytes = toByteArray();
            reset();
            return bytes;
        }

        public void addListener(DashboardLogListener listener) {
            _listeners.add(listener);
        }
    }

    private static DashboardOutputStream OUTPUT_STREAM;

    public static synchronized DashboardOutputStream getOutputStream() {
        if (OUTPUT_STREAM == null) {
            OUTPUT_STREAM = new DashboardOutputStream();
        }
        return OUTPUT_STREAM;
    }
}
