package com.mrivanplays.commandworker.bukkit.argtypes;

import com.mojang.brigadier.arguments.ArgumentType;
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
  ENTITY("entity"),
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
  RESOURCE_LOCATION("resource_location"),
  ROTATION("rotation"),
  SCORE_HOLDER("score_holder"),
  SCOREBOARD_SLOT("scoreboard_slot"),
  SWIZZLE("swizzle"),
  TEAM("team"),
  TIME("time"),
  UUID("uuid"),
  VEC2("vec2"),
  VEC3("vec3");

  private final ArgumentType<?> argumentType;

  MinecraftArgumentTypes(String namespace) {
    argumentType = MinecraftArgumentTypesAccessor.getByKey(NamespacedKey.minecraft(namespace));
  }

  /**
   * Returns the brigadier {@link ArgumentType} of the argument, if it is present.
   *
   * @return argument type
   */
  public ArgumentType<?> getArgumentType() {
    return argumentType;
  }
}
