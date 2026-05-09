package io.github.bl3rune.blueprints.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Type-keyed container for plugin services. Services are registered once
 * during bootstrap and looked up by their declared type. Phase 2 introduces
 * the registry as the seam through which later phases can inject domain,
 * persistence, and infrastructure services without static singletons.
 */
public final class ServiceRegistry {

    private final Map<Class<?>, Object> services = new LinkedHashMap<>();

    public <T> void register(Class<T> type, T instance) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (instance == null) {
            throw new IllegalArgumentException("instance must not be null for " + type.getName());
        }
        if (services.containsKey(type)) {
            throw new IllegalStateException("Service already registered: " + type.getName());
        }
        services.put(type, instance);
    }

    public <T> T get(Class<T> type) {
        Object instance = services.get(type);
        if (instance == null) {
            throw new IllegalStateException("No service registered for " + type.getName());
        }
        return type.cast(instance);
    }

    public <T> T find(Class<T> type) {
        Object instance = services.get(type);
        return instance == null ? null : type.cast(instance);
    }

    public boolean contains(Class<?> type) {
        return services.containsKey(type);
    }

    public int size() {
        return services.size();
    }
}
