package com.mrivanplays.commandworker.core;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mrivanplays.commandworker.core.argument.RequiredArgument;
import com.mrivanplays.commandworker.core.argument.parser.ArgumentHolder;
import org.junit.Assert;
import org.junit.Test;

public class ArgumentHolderTest {

  @Test
  public void testArgumentHolderIsTyped() {
    LiteralNode structure =
        LiteralNode.node()
            .argument(
                RequiredArgument.argument("subcommand", StringArgumentType.string())
                    .then(RequiredArgument.argument("message", StringArgumentType.greedyString())));

    String input = "hello this is my message";
    ArgumentHolder holder = new ArgumentHolder(input, structure);

    Assert.assertTrue(holder.isTyped("subcommand"));
    Assert.assertTrue(holder.isTyped("message"));
    Assert.assertEquals("hello", holder.getRawRequiredArgument("subcommand"));
    Assert.assertEquals("this is my message", holder.getRawRequiredArgument("message"));
  }
}
