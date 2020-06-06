package com.mrivanplays.commandworker.core;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mrivanplays.commandworker.core.argument.RequiredArgument;
import com.mrivanplays.commandworker.core.argument.parser.ArgumentHolder;
import java.util.Arrays;
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

    String input = "testcmd hello this is my message";
    String[] inputSplit = input.split(" ");
    String[] args = Arrays.copyOfRange(inputSplit, 1, inputSplit.length);
    ArgumentHolder holder = new ArgumentHolder(args, structure);

    Assert.assertTrue(holder.isTyped("subcommand"));
    Assert.assertTrue(holder.isTyped("message"));
    Assert.assertEquals("hello", holder.getRawRequiredArgument("subcommand"));
    Assert.assertEquals("this is my message", holder.getRawRequiredArgument("message"));
  }
}
