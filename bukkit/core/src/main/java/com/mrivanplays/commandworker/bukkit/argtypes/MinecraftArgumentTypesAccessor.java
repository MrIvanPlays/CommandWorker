package com.mrivanplays.commandworker.bukkit.argtypes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mrivanplays.commandworker.bukkit.registry.CmdRegistryHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

/**
 * Represents a utility class for accessing {@link ArgumentType}s provided by mojang in the server's
 * internals.
 */
public class MinecraftArgumentTypesAccessor {

  private MinecraftArgumentTypesAccessor() {}

  private static Constructor<?> MINECRAFT_KEY_CONSTRUCTOR;
  private static Method ARGUMENT_REGISTRY_GET_BY_KEY_METHOD;
  private static Field ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD;

  static {
    if (CmdRegistryHandler.isSupported()) {
      String nmsVersion =
          Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
      try {
        Class<?> minecraftKey =
            Class.forName("net.minecraft.server." + nmsVersion + ".MinecraftKey");
        MINECRAFT_KEY_CONSTRUCTOR = minecraftKey.getConstructor(String.class, String.class);
        MINECRAFT_KEY_CONSTRUCTOR.setAccessible(true);

        Class<?> argumentRegistry =
            Class.forName("net.minecraft.server." + nmsVersion + ".ArgumentRegistry");
        ARGUMENT_REGISTRY_GET_BY_KEY_METHOD =
            Arrays.stream(argumentRegistry.getDeclaredMethods())
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> minecraftKey.equals(method.getParameterTypes()[0]))
                .findFirst()
                .orElseThrow(NoSuchMethodException::new);
        ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.setAccessible(true);

        Class<?> argumentRegistryEntry = ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.getReturnType();
        ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD =
            Arrays.stream(argumentRegistryEntry.getDeclaredFields())
                .filter(field -> field.getType() == Class.class)
                .findFirst()
                .orElseThrow(NoSuchFieldException::new);
        ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD.setAccessible(true);
      } catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e) {
        throw new ExceptionInInitializerError(e);
      }
    }
  }

  public static void ensureSetup() {
    // do nothing; this is enough to trigger the static initializer
  }

  /**
   * Retrieves the registered argument type's class, matching the key specified, if brigadier is
   * supported.
   *
   * @param key the key of the argument type you want to retrieve
   * @return argument type class
   */
  @SuppressWarnings("unchecked")
  public static Class<? extends ArgumentType<?>> getArgumentClass(NamespacedKey key) {
    if (CmdRegistryHandler.isSupported()) {
      try {
        Object minecraftKey =
            MINECRAFT_KEY_CONSTRUCTOR.newInstance(key.getNamespace(), key.getKey());
        Object entry = ARGUMENT_REGISTRY_GET_BY_KEY_METHOD.invoke(null, minecraftKey);
        if (entry == null) {
          throw new IllegalArgumentException(
              "No such ArgumentType with key '" + key.toString() + "'");
        }

        Class<?> argument = (Class<?>) ARGUMENT_REGISTRY_ENTRY_CLASS_FIELD.get(entry);
        return (Class<? extends ArgumentType<?>>) argument;
      } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  /**
   * Retrieves the registered argument type, matching the key specified, if brigadier is supported.
   *
   * @param key the key of the argument type you want to retrieve
   * @return argument type
   */
  public static ArgumentType<?> getByKey(NamespacedKey key) {
    if (CmdRegistryHandler.isSupported()) {
      try {
        Constructor<? extends ArgumentType<?>> argumentConstructor =
            getArgumentClass(key).getDeclaredConstructor();
        argumentConstructor.setAccessible(true);
        return argumentConstructor.newInstance();
      } catch (InstantiationException
          | InvocationTargetException
          | NoSuchMethodException
          | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }
}
