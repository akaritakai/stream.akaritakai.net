package net.akaritakai.stream;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;

public class CheckAuthImpl implements CheckAuth {

    private final String _key;
    private final Map<Class<?>, Method> _methodMap = new IdentityHashMap<>();

    public CheckAuthImpl(String key) {
        _key = key;
    }

    @Override
    public boolean isAuthorizedRequest(Object o) {
        if (o == null) {
            return false;
        }
        try {
            Method method;
            synchronized (_methodMap) {
                if (_methodMap.containsKey(o.getClass())) {
                    method = _methodMap.get(o.getClass());
                } else {
                    try {
                        method = o.getClass().getMethod("getKey");
                        if (!Modifier.isPublic(method.getModifiers())) {
                            method = null;
                        }
                    } catch (Exception e) {
                        method = null;
                    }
                    _methodMap.put(o.getClass(), method);
                }
            }
            if (method != null) {
                String key = String.valueOf(method.invoke(o));
                return isAuthorizedKey(key);
            }
        } catch (Exception ex) {
            // not auth;
        }
        return false;
    }

    public boolean isAuthorizedKey(String key) {
        return _key.equals(key);
    }
}
