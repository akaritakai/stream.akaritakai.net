package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.chat.ChatManagerMBean;
import net.akaritakai.stream.scheduling.SchedulerAttribute;
import net.akaritakai.stream.scheduling.Utils;
import net.akaritakai.stream.streamer.StreamerMBean;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import javax.script.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public class ProcessJob implements InterruptableJob, ScriptContext {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessJob.class);
    public static final SchedulerAttribute<ScriptEngine> SCRIPT_ENGINE
            = SchedulerAttribute.instanceOf("scriptEngine", ScriptEngine.class);

    public static final String INPUT = "INPUT";

    /**
     * This is the engine scope bindings.
     * By default, a <code>SimpleBindings</code> is used. Accessor
     * methods setBindings, getBindings are used to manage this field.
     * @see SimpleBindings
     */
    protected Bindings engineScope;

    /**
     * This is the global scope bindings.
     * By default, a null value (which means no global scope) is used. Accessor
     * methods setBindings, getBindings are used to manage this field.
     */
    protected Bindings globalScope;
    private Reader reader;
    private Writer errorWriter;
    private Writer writer;

    private FutureTask<Object> task;

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        try {
            if (task.cancel(true)) {
                return;
            }
        } catch (Exception ex) {
            throw new UnableToInterruptJobException(ex);
        }
        throw new UnableToInterruptJobException("Cannot cancel");
    }


    private Object task(JobExecutionContext context) throws Exception {
        ScriptEngine scriptEngine = Utils.get(context.getScheduler(), SCRIPT_ENGINE);
        Object input = context.get(INPUT);
        if (input == null) {
            reader = new InputStreamReader(new ByteArrayInputStream(new byte[0]));
        } else {
            String inputString = input.toString();
            if (inputString.startsWith("file://")) {
                reader = new BufferedReader(new FileReader(inputString.substring(7)));
            } else {
                reader = new StringReader(inputString);
            }
        }

        ObjectName chat = new ObjectName(Optional.ofNullable(context.get("CHAT")).map(Objects::toString)
                .orElse("net.akaritakai.stream:type=ChatManager"));
        ObjectName streamer = new ObjectName(Optional.ofNullable(context.get("CHAT")).map(Objects::toString)
                .orElse("net.akaritakai.stream:type=Streamer"));

        errorWriter = logWriter(obj -> LOG.warn("{}", obj));
        writer = logWriter(obj -> LOG.info("{}", obj));

        engineScope.put("chat", Utils.beanProxy(chat, ChatManagerMBean.class));
        engineScope.put("streamer", Utils.beanProxy(streamer, StreamerMBean.class));

        Object source = context.get("SOURCE");
        if (source instanceof File) {
            return scriptEngine.eval(new FileReader((File) source), this);
        }
        String stringSource = String.valueOf(source);
        if (stringSource.startsWith("file://")) {
            return scriptEngine.eval(new FileReader(stringSource.substring(7)), this);
        }
        return scriptEngine.eval(stringSource, this);
    }

    @Override
    public synchronized void execute(JobExecutionContext context) throws JobExecutionException {
        engineScope = new SimpleBindings(context.getMergedJobDataMap());
        FutureTask<Object> task = new FutureTask<>(() -> task(context));
        try {
            this.task = task;
            task.run();
            context.setResult(task.get());
        } catch (Exception e) {
            throw new JobExecutionException(e);
        } finally {
            if (!task.isDone()) {
                task.cancel(true);
            }
            if (this.task == task) {
                this.task = null;
            }
        }
    }

    private Writer logWriter(Consumer<Object> consumer) {
        return new PrintWriter(new OutputStreamWriter(new PrintStream(new ByteArrayOutputStream() {
            @Override
            public void flush() throws IOException {
                super.flush();
                byte[] bytes = this.toByteArray();
                reset();
                consumer.accept(new Object() {
                    private volatile String string;
                    @Override
                    public String toString() {
                        if (string == null) {
                            string = new String(bytes, StandardCharsets.UTF_8);
                        }
                        return string;
                    }
                });
            }
        }, true, StandardCharsets.UTF_8)));
    }

    /**
     * Sets a <code>Bindings</code> of attributes for the given scope.  If the value
     * of scope is <code>ENGINE_SCOPE</code> the given <code>Bindings</code> replaces the
     * <code>engineScope</code> field.  If the value
     * of scope is <code>GLOBAL_SCOPE</code> the given <code>Bindings</code> replaces the
     * <code>globalScope</code> field.
     *
     * @param bindings The <code>Bindings</code> of attributes to set.
     * @param scope The value of the scope in which the attributes are set.
     *
     * @throws IllegalArgumentException if scope is invalid.
     * @throws NullPointerException if the value of scope is <code>ENGINE_SCOPE</code> and
     * the specified <code>Bindings</code> is null.
     */
    public void setBindings(Bindings bindings, int scope) {

        switch (scope) {

            case ENGINE_SCOPE:
                if (bindings == null) {
                    throw new NullPointerException("Engine scope cannot be null.");
                }
                engineScope = bindings;
                break;
            case GLOBAL_SCOPE:
                globalScope = bindings;
                break;
            default:
                throw new IllegalArgumentException("Invalid scope value.");
        }
    }


    /**
     * Retrieves the value of the attribute with the given name in
     * the scope occurring earliest in the search order.  The order
     * is determined by the numeric value of the scope parameter (lowest
     * scope values first.)
     *
     * @param name The name of the attribute to retrieve.
     * @return The value of the attribute in the lowest scope for
     * which an attribute with the given name is defined.  Returns
     * null if no attribute with the name exists in any scope.
     * @throws NullPointerException if the name is null.
     * @throws IllegalArgumentException if the name is empty.
     */
    public Object getAttribute(String name) {
        checkName(name);
        if (engineScope.containsKey(name)) {
            return getAttribute(name, ENGINE_SCOPE);
        } else if (globalScope != null && globalScope.containsKey(name)) {
            return getAttribute(name, GLOBAL_SCOPE);
        }

        return null;
    }

    /**
     * Gets the value of an attribute in a given scope.
     *
     * @param name The name of the attribute to retrieve.
     * @param scope The scope in which to retrieve the attribute.
     * @return The value of the attribute. Returns <code>null</code> is the name
     * does not exist in the given scope.
     *
     * @throws IllegalArgumentException
     *         if the name is empty or if the value of scope is invalid.
     * @throws NullPointerException if the name is null.
     */
    public Object getAttribute(String name, int scope) {
        checkName(name);
        switch (scope) {

            case ENGINE_SCOPE:
                return engineScope.get(name);

            case GLOBAL_SCOPE:
                if (globalScope != null) {
                    return globalScope.get(name);
                }
                return null;

            default:
                throw new IllegalArgumentException("Illegal scope value.");
        }
    }

    /**
     * Remove an attribute in a given scope.
     *
     * @param name The name of the attribute to remove
     * @param scope The scope in which to remove the attribute
     *
     * @return The removed value.
     * @throws IllegalArgumentException
     *         if the name is empty or if the scope is invalid.
     * @throws NullPointerException if the name is null.
     */
    public Object removeAttribute(String name, int scope) {
        checkName(name);
        switch (scope) {

            case ENGINE_SCOPE:
                if (getBindings(ENGINE_SCOPE) != null) {
                    return getBindings(ENGINE_SCOPE).remove(name);
                }
                return null;

            case GLOBAL_SCOPE:
                if (getBindings(GLOBAL_SCOPE) != null) {
                    return getBindings(GLOBAL_SCOPE).remove(name);
                }
                return null;

            default:
                throw new IllegalArgumentException("Illegal scope value.");
        }
    }

    /**
     * Sets the value of an attribute in a given scope. If the scope is <code>GLOBAL_SCOPE</code>
     * and no Bindings is set for <code>GLOBAL_SCOPE</code>, then setAttribute call is a no-op.
     *
     * @param name The name of the attribute to set
     * @param value The value of the attribute
     * @param scope The scope in which to set the attribute
     *
     * @throws IllegalArgumentException
     *         if the name is empty or if the scope is invalid.
     * @throws NullPointerException if the name is null.
     */
    public void setAttribute(String name, Object value, int scope) {
        checkName(name);
        switch (scope) {

            case ENGINE_SCOPE:
                engineScope.put(name, value);
                return;

            case GLOBAL_SCOPE:
                if (globalScope != null) {
                    globalScope.put(name, value);
                }
                return;

            default:
                throw new IllegalArgumentException("Illegal scope value.");
        }
    }

    /** {@inheritDoc} */
    public Writer getWriter() {
        return writer;
    }

    /** {@inheritDoc} */
    public Reader getReader() {
        return reader;
    }

    /** {@inheritDoc} */
    public void setReader(Reader reader) {
        this.reader = reader;
    }

    /** {@inheritDoc} */
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /** {@inheritDoc} */
    public Writer getErrorWriter() {
        return errorWriter;
    }

    /** {@inheritDoc} */
    public void setErrorWriter(Writer writer) {
        this.errorWriter = writer;
    }

    /**
     * Get the lowest scope in which an attribute is defined.
     * @param name Name of the attribute
     * .
     * @return The lowest scope.  Returns -1 if no attribute with the given
     * name is defined in any scope.
     * @throws NullPointerException if name is null.
     * @throws IllegalArgumentException if name is empty.
     */
    public int getAttributesScope(String name) {
        checkName(name);
        if (engineScope.containsKey(name)) {
            return ENGINE_SCOPE;
        } else if (globalScope != null && globalScope.containsKey(name)) {
            return GLOBAL_SCOPE;
        } else {
            return -1;
        }
    }

    /**
     * Returns the value of the <code>engineScope</code> field if specified scope is
     * <code>ENGINE_SCOPE</code>.  Returns the value of the <code>globalScope</code> field if the specified scope is
     * <code>GLOBAL_SCOPE</code>.
     *
     * @param scope The specified scope
     * @return The value of either the  <code>engineScope</code> or <code>globalScope</code> field.
     * @throws IllegalArgumentException if the value of scope is invalid.
     */
    public Bindings getBindings(int scope) {
        if (scope == ENGINE_SCOPE) {
            return engineScope;
        } else if (scope == GLOBAL_SCOPE) {
            return globalScope;
        } else {
            throw new IllegalArgumentException("Illegal scope value.");
        }
    }

    /** {@inheritDoc} */
    public List<Integer> getScopes() {
        return scopes;
    }

    private void checkName(String name) {
        Objects.requireNonNull(name);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
    }

    private static List<Integer> scopes;
    static {
        scopes = new ArrayList<Integer>(2);
        scopes.add(ENGINE_SCOPE);
        scopes.add(GLOBAL_SCOPE);
        scopes = Collections.unmodifiableList(scopes);
    }
}
