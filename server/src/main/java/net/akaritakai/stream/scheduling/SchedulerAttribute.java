package net.akaritakai.stream.scheduling;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class SchedulerAttribute<T> {
    private static final ConcurrentMap<String, SchedulerAttribute<?>> MAP = new ConcurrentHashMap<>();

    private final String name;
    private final Class<T> clazz;
    private SchedulerAttribute(String name, Class<T> clazz) {
        this.name = Objects.requireNonNull(name, "name");
        this.clazz = Objects.requireNonNull(clazz, "clazz");
    }

    private <T> SchedulerAttribute<T> coerce(Class<T> clazz) {
        if (clazz != this.clazz) {
            throw new IllegalStateException();
        }
        //noinspection unchecked,rawtypes
        return (SchedulerAttribute) this;
    }

    public static <T> SchedulerAttribute<T> instanceOf(String name, Class<T> clazz) {
        return MAP.computeIfAbsent(name, n -> new SchedulerAttribute<>(n, clazz)).coerce(clazz);
    }

    @Override
    public boolean equals(Object other) {
        return other == this;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ name.hashCode();
    }

    @Override
    public String toString() {
        return name + "<" + clazz.getName() + ">";
    }
}
