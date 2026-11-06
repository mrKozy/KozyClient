// ☭ Red Star Client — ModuleConfig
package com.redstarclient.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Serializable POJO holding a module's persistent state.
 */
public class ModuleConfig {

    public boolean enabled = false;
    public int keybind = -1;
    public Map<String, Object> settings = new HashMap<>();

    public ModuleConfig() {}

    public ModuleConfig(boolean enabled, int keybind) {
        this.enabled = enabled;
        this.keybind = keybind;
    }

    /** Get a setting value, safely converting Number/primitive types or returning {@code defaultValue}. */
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String key, T defaultValue) {
        Object val = settings.get(key);
        if (val == null) return defaultValue;
        if (defaultValue == null) return (T) val;

        try {
            if (defaultValue instanceof Integer && val instanceof Number num) {
                return (T) Integer.valueOf(num.intValue());
            }
            if (defaultValue instanceof Float && val instanceof Number num) {
                return (T) Float.valueOf(num.floatValue());
            }
            if (defaultValue instanceof Double && val instanceof Number num) {
                return (T) Double.valueOf(num.doubleValue());
            }
            if (defaultValue instanceof Long && val instanceof Number num) {
                return (T) Long.valueOf(num.longValue());
            }
            if (defaultValue instanceof Short && val instanceof Number num) {
                return (T) Short.valueOf(num.shortValue());
            }
            if (defaultValue instanceof Byte && val instanceof Number num) {
                return (T) Byte.valueOf(num.byteValue());
            }
            if (defaultValue instanceof Boolean) {
                if (val instanceof Boolean b) return (T) b;
                if (val instanceof String s) return (T) Boolean.valueOf(Boolean.parseBoolean(s));
                if (val instanceof Number num) return (T) Boolean.valueOf(num.intValue() != 0);
            }
            if (defaultValue instanceof String) {
                return (T) String.valueOf(val);
            }
            return (T) val;
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    public void putSetting(String key, Object value) {
        settings.put(key, value);
    }
}
