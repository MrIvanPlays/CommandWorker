package com.mrivanplays.commandworker.bukkit.argtypes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mrivanplays.commandworker.bukkit.registry.CmdRegistryHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.NamespacedKey;

/**
 * Represents an enum, holding all known minecraft brigadier types.
 *
 * <p>Source: <a href="https://minecraft.gamepedia.com/Argument_types">Minecraft wiki</a>, where you
 * can find examples.
 */
public enum MinecraftArgumentTypes {
  BLOCK_POS("block_pos"),
  BLOCK_PREDICATE("block_predicate"),
  BLOCK_STATE("block_state"),
  COLOR("color"),
  COMPONENT("component"),
  DIMENSION("dimension"),

  /** Constructor parameters: single, playersOnly */
  ENTITY("entity", boolean.class, boolean.class),

  ENTITY_ANCHOR("entity_anchor"),
  ENTITY_SUMMON("entity_summon"),
  FLOAT_RANGE("float_range"),
  FUNCTION("function"),
  GAME_PROFILE("game_profile"),
  INT_RANGE("int_range"),
  ITEM_ENCHANTMENT("item_enchantment"),
  ITEM_PREDICATE("item_predicate"),
  ITEM_SLOT("item_slot"),
  ITEM_STACK("item_stack"),
  MESSAGE("message"),
  MOB_EFFECT("mob_effect"),
  NBT_COMPOUND_TAG("nbt_compound_tag"),
  NBT_PATH("nbt_path"),
  NBT_TAG("nbt_tag"),
  OBJECTIVE("objective"),
  OBJECTIVE_CRITERIA("objective_criteria"),
  OPERATION("operation"),
  PARTICLE("particle"),

  /** Responsible for both advancements and recipes. */
  RESOURCE_LOCATION("resource_location"),

  ROTATION("rotation"),

  /** Constructor parameters: multiple */
  SCORE_HOLDER("score_holder", boolean.class),

  SCOREBOARD_SLOT("scoreboard_slot"),
  SWIZZLE("swizzle"),
  TEAM("team"),
  TIME("time"),
  UUID("uuid"),

  /** Constructor parameters: centerCorrect */
  VEC2("vec2", boolean.class),

  /** Constructor parameters: centerCorrect */
  VEC3("vec3", boolean.class);

  private final NamespacedKey namespacedKey;

  private ArgumentType<?> firstOption, secondOption, thirdOption, fourthOption;

  MinecraftArgumentTypes(String namespace, Class<?>... constructorParameters) {
    namespacedKey = NamespacedKey.minecraft(namespace);
    if (CmdRegistryHandler.isSupported()) {
      if (constructorParameters == null) {
        firstOption = MinecraftArgumentTypesAccessor.getByKey(namespacedKey);
      } else {
        try {
          Class<? extends ArgumentType<?>> argumentClass =
              MinecraftArgumentTypesAccessor.getArgumentClass(namespacedKey);
          Constructor<? extends ArgumentType<?>> constructor =
              argumentClass.getDeclaredConstructor(constructorParameters);
          if (constructorParameters.length == 1) {
            this.firstOption = constructor.newInstance(true);
            this.secondOption = constructor.newInstance(false);
          } else {
            this.firstOption = constructor.newInstance(false, true);
            this.secondOption = constructor.newInstance(true, false);
            this.thirdOption = constructor.newInstance(true, true);
            this.fourthOption = constructor.newInstance(false, false);
          }
        } catch (NullPointerException
            | NoSuchMethodException
            | IllegalAccessException
            | InstantiationException
            | InvocationTargetException e) {
          this.firstOption = null;
          this.secondOption = null;
          this.thirdOption = null;
          this.fourthOption = null;
        }
      }
    }
  }

  /**
   * Returns the brigadier {@link ArgumentType} of the argument, if it is present.
   *
   * @return argument type
   */
  public ArgumentType<?> getArgumentType() {
    return firstOption;
  }

  /**
   * Returns the key of this argument type.
   *
   * @return key
   */
  public NamespacedKey getKey() {
    return namespacedKey;
  }

  // bellow are the other types of the argument type.

  public ArgumentType<?> getSecondWay() {
    return secondOption == null ? firstOption : secondOption;
  }

  public ArgumentType<?> getThirdWay() {
    return thirdOption == null ? getSecondWay() : thirdOption;
  }

  public ArgumentType<?> getFourthWay() {
    return fourthOption == null ? getThirdWay() : fourthOption;
  }
}
