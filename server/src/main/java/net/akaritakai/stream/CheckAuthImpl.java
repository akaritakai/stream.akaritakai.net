package net.akaritakai.stream;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;

public class CheckAuthImpl implements CheckAuth {

    private final String _key;
    private final Map<Class<?>, Method> _getterMap = new IdentityHashMap<>();
    private final Map<Class<?>, Method> _setterMap = new IdentityHashMap<>();

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
            synchronized (_getterMap) {
                if (_getterMap.containsKey(o.getClass())) {
                    method = _getterMap.get(o.getClass());
                } else {
                    try {
                        method = o.getClass().getMethod("getKey");
                        if (!Modifier.isPublic(method.getModifiers())) {
                            method = null;
                        }
                    } catch (Exception e) {
                        method = null;
                    }
                    _getterMap.put(o.getClass(), method);
                }
            }
            if (method != null) {
                String key = String.valueOf(method.invoke(o));
                if (isAuthorizedKey(key)) {
                    synchronized (_setterMap) {
                        if (_setterMap.containsKey(o.getClass())) {
                            method = _setterMap.get(o.getClass());
                        } else {
                            try {
                                method = o.getClass().getMethod("setKey", String.class);
                                if (!Modifier.isPublic(method.getModifiers())) {
                                    method = null;
                                }
                            } catch (Exception e) {
                                method = null;
                            }
                            _setterMap.put(o.getClass(), method);
                        }
                    }
                    if (method != null) {
                        method.invoke(o, (String) null);
                    }
                    return true;
                }
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
