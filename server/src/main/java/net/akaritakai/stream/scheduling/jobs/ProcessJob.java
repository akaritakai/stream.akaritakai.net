package net.akaritakai.stream.scheduling.jobs;

import net.akaritakai.stream.scheduling.SchedulerAttribute;
import net.akaritakai.stream.scheduling.Utils;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public class ProcessJob implements InterruptableJob, ScriptContext {

    public static final SchedulerAttribute<ScriptEngine> SCRIPT_ENGINE
            = SchedulerAttribute.instanceOf("scriptEngine", ScriptEngine.class);

    private static final StringWriter outputWriter = new StringWriter();

    @Override
    public void interrupt() throws UnableToInterruptJobException {

    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        ScriptEngine scriptEngine = Utils.get(context.getScheduler(), SCRIPT_ENGINE);

        try {
            scriptEngine.eval("", this);
        } catch (ScriptException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {

    }

    @Override
    public Bindings getBindings(int scope) {
        return null;
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {

    }

    @Override
    public Object getAttribute(String name, int scope) {
        return null;
    }

    @Override
    public Object removeAttribute(String name, int scope) {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public int getAttributesScope(String name) {
        return 0;
    }

    @Override
    public Writer getWriter() {
        return null;
    }

    @Override
    public Writer getErrorWriter() {
        return null;
    }

    @Override
    public void setWriter(Writer writer) {

    }

    @Override
    public void setErrorWriter(Writer writer) {

    }

    @Override
    public Reader getReader() {
        return null;
    }

    @Override
    public void setReader(Reader reader) {

    }

    @Override
    public List<Integer> getScopes() {
        return null;
    }
}
