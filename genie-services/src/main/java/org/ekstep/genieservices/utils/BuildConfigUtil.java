package org.ekstep.genieservices.utils;

import org.ekstep.genieservices.commons.utils.ReflectionUtil;

/**
 * Created on 4/18/2017.
 *
 * @author anil
 */
public class BuildConfigUtil {

    public static Class<?> getBuildConfigClass(String packageName) {
        return ReflectionUtil.getClass(packageName + ".BuildConfig");
    }

    public static <T> T getBuildConfigValue(String packageName, String property) {
        Object value = ReflectionUtil.getStaticFieldValue(getBuildConfigClass(packageName), property);
        return (T) value;
    }
}