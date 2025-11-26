package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPL = new HashMap<>();
    private static final Map<Class<?>, Object> INSTANCES = new HashMap<>();
    private static final Injector INJECTOR = new Injector();

    public Injector() {
        INTERFACE_IMPL.put(FileReaderService.class, FileReaderServiceImpl.class);
        INTERFACE_IMPL.put(ProductParser.class, ProductParserImpl.class);
        INTERFACE_IMPL.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return INJECTOR;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(declaredField.getType());
                clazzImplInstance = createNewInstance(clazz);

                declaredField.setAccessible(true);
                try {
                    declaredField.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field: "
                            + "Class: " + clazz.getName() + " Filed: " + declaredField.getName());
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(clazz);
        }
        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (INSTANCES.containsKey(clazz)) {
            return INSTANCES.get(clazz);
        }

        try {
            Constructor<?> constructors = clazz.getConstructor();
            Object instance = constructors.newInstance();
            INSTANCES.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Unsupported class " + interfaceClazz.getName());
        }

        return interfaceClazz.isInterface()
                ? INTERFACE_IMPL.get(interfaceClazz) :
                interfaceClazz;
    }
}
