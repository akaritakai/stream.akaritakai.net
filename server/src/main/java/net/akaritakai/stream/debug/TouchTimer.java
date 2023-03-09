package net.akaritakai.stream.debug;

import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.util.StringBuilderFormattable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public class TouchTimer implements StringBuilderFormattable {

    public static final String KEY = "TouchTimer";
    private static final ThreadLocal<TouchTimer> LOCAL = new ThreadLocal<>();

    public static Optional<TouchTimer> current() {
        return Optional.ofNullable(LOCAL.get());
    }

    public static Optional<TouchTimer> of(RoutingContext context) {
        return Optional.ofNullable(context.get(KEY, null));
    }

    final long startTime = System.currentTimeMillis();
    final Entry first = new Entry();
    Entry last = first;
    transient String stringValue;

    private TouchTimer appendEntry(Entry entry) {
        Entry prev = last;
        do {
            Entry next;
            while ((next = prev.get()) != null) {
                prev = next;
            }
            entry.ordinal = prev.ordinal + 1;
        } while (!prev.compareAndSet(null, entry));
        last = entry;
        stringValue = null;
        return this;
    }

    public TouchTimer touch(String message) {
        return appendEntry(new SimpleEntry(message));
    }

    public TouchTimer touch(String message, Object arg) {
        return appendEntry(new SingleArgEntry(message, arg));
    }


    public Instant getStartInstant() {
        return Instant.ofEpochMilli(startTime);
    }

    public long getStartNanos() {
        return first.nanos;
    }

    public int size() {
        return last.ordinal;
    }

    public long durationNanos() {
        return last.nanos - first.nanos;
    }

    public Duration duration() {
        return Duration.ofNanos(durationNanos());
    }

    private static final long NANOS_PER_HOUR = Duration.ofHours(1).toNanos();
    private static final long NANOS_PER_MINUTE = Duration.ofMinutes(1).toNanos();
    private static final long NANOS_PER_SECOND = Duration.ofSeconds(1).toNanos();
    private static final long NANOS_PER_MILLIS = Duration.ofMillis(1).toNanos();
    private static final long NANOS_PER_MICROS = NANOS_PER_MILLIS / 1000L;

    protected void format(StringBuilder buffer, long nanos) {

        long hours = nanos / NANOS_PER_HOUR; nanos %= NANOS_PER_HOUR;
        if (hours > 0) {
            buffer.append(hours).append('h');
        }

        long minutes = nanos / NANOS_PER_MINUTE; nanos %= NANOS_PER_MINUTE;
        if (minutes > 0) {
            buffer.append(minutes).append('m');
        }

        long seconds = nanos / NANOS_PER_SECOND; nanos %= NANOS_PER_SECOND;
        if (seconds > 0) {
            buffer.append(seconds).append('s');
        }

        if (hours > 0) {
            return;
        }

        long millis = nanos / NANOS_PER_MILLIS; nanos %= NANOS_PER_MILLIS;
        if (millis > 0) {
            buffer.append(millis).append("ms");
        }

        if (minutes > 0 || seconds > 0) {
            return;
        }

        long micros = nanos /= NANOS_PER_MICROS;
        if (nanos > 0) {
            buffer.append(micros).append("us");
        }
    }

    protected void format(StringBuilder buffer, Thread thread, String threadName) {
        buffer.append(" [").append(thread.getId()).append(':').append(threadName).append(']');
    }

    protected void format(StringBuilder buffer, Object arg) {
        if (arg instanceof Supplier) {
            arg = ((Supplier<?>) arg).get();
        }
        if (arg instanceof StringBuilderFormattable) {
            ((StringBuilderFormattable) arg).formatTo(buffer);
        } else if (arg instanceof LongSupplier) {
            buffer.append(((LongSupplier) arg).getAsLong());
        } else if (arg instanceof IntSupplier) {
            buffer.append(((IntSupplier) arg).getAsInt());
        } else if (arg instanceof DoubleSupplier) {
            buffer.append(((DoubleSupplier) arg).getAsDouble());
        } else if (arg instanceof BooleanSupplier) {
            buffer.append(((BooleanSupplier) arg).getAsBoolean());
        } else if (arg instanceof CharSequence) {
            buffer.append((CharSequence) arg);
        } else if (arg instanceof Number) {
            if (arg instanceof Integer) {
                buffer.append((int) (Integer) arg);
            } else if (arg instanceof Double) {
                buffer.append((double) (Double) arg);
            } else if (arg instanceof Float) {
                buffer.append((float) (Float) arg);
            } else {
                buffer.append(((Number) arg).longValue());
            }
        } else {
            buffer.append(arg);
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        if (stringValue == null) {
            StringBuilder sb = new StringBuilder();
            formatTo(sb);
            stringValue = sb.toString();
        }
        return stringValue;
    }

    protected void lineSeparator(StringBuilder sb) {
        sb.append(System.lineSeparator()).append('\t');
    }

    @Override
    public void formatTo(StringBuilder buffer) {
        Entry e = first;
        final Instant start = getStartInstant();
        final long startNanos = getStartNanos();
        Instant current = start;
        long previousNanos = startNanos;
        String previousThread = null;
        lineSeparator(buffer.append('[').append(start).append("] Begin"));
        while ((e = e.get()) != null) {
            current = start.plus(e.nanos - startNanos, ChronoUnit.NANOS);
            long nanos = e.nanos - previousNanos;
            previousNanos = e.nanos;

            if (nanos > 0) {
                format(buffer.append(' '), nanos);
            }

            //noinspection StringEquality
            if (previousThread != e.threadName) {
                previousThread = e.threadName;
                format(buffer, e.thread, e.threadName);
            }

            format(buffer.append(' '), e);
            lineSeparator(buffer);
        }

        Duration duration = Duration.between(start, current);
        buffer.append(" Total Duration ").append(duration).append(", ").append(size()).append(" events");
    }

    protected void format(StringBuilder buffer, Entry entry) {
        entry.formatTo(buffer);
        if (entry.stackFrame != null) {
            format(buffer, entry.stackFrame);
        }
    }

    protected void format(StringBuilder buffer, StackWalker.StackFrame stackFrame) {
        // IDEs like IntelliJ will recognise this format and allow hyperlinking from it
        buffer.append(" at ").append(stackFrame.getClassName())
                .append('.').append(stackFrame.getMethodName())
                .append('(').append(stackFrame.getFileName())
                .append(':').append(stackFrame.getLineNumber())
                .append(')');
    }

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final String PACKAGE = TouchTimer.class.getPackageName();

    private static class Entry extends AtomicReference<Entry> {
        final Thread thread = Thread.currentThread();
        final String threadName = thread.getName();
        final long nanos = System.nanoTime();
        int ordinal;
        final StackWalker.StackFrame stackFrame = STACK_WALKER
                .walk(s -> s.filter(frame -> !frame.getClassName().startsWith(PACKAGE)).findFirst())
                .orElse(null);

        void formatTo(StringBuilder buffer) {
        }
    }

    private static class SimpleEntry extends Entry {
        final String message;

        private SimpleEntry(String message) {
            this.message = message;
        }

        void formatTo(StringBuilder buffer) {
            buffer.append(message);
        }
    }

    private class SingleArgEntry extends SimpleEntry {

        private final Object arg;

        private SingleArgEntry(String message, Object arg) {
            super(message);
            this.arg = arg;
        }

        void formatTo(StringBuilder builder) {
            int index = message.indexOf("{}");
            if (index < 0) {
                super.formatTo(builder);
                format(builder, arg);
            } else {
                builder.append(message, 0, index);
                format(builder, arg);
                builder.append(message, index + 2, message.length());
            }
        }
    }

    public final Executor executor(Executor execute) {
        return runnable -> execute.execute(runnable(runnable));
    }

    public final Executor executor() {
        return executor(Runnable::run);
    }

    public final Runnable runnable(Runnable runnable) {
        return () -> {
            Optional<TouchTimer> save = current();
            try {
                LOCAL.set(this);
                runnable.run();
            } finally {
                LOCAL.set(save.orElse(null));
            }
        };
    }
}
