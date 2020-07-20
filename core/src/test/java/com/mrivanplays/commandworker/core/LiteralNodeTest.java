package com.mrivanplays.commandworker.core;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mrivanplays.commandworker.core.argument.LiteralArgument;
import com.mrivanplays.commandworker.core.argument.RequiredArgument;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LiteralNodeTest {

  private LiteralNode node;
  private LiteralNode node1;

  @Before
  public void initializeNode() {
    node =
        LiteralNode.node()
            .argument(
                LiteralArgument.literal("baba")
                    .then(
                        RequiredArgument.argument("bebebaba123", IntegerArgumentType.integer())
                            .then(RequiredArgument.argument("ivancho", BoolArgumentType.bool())))
                    .then(LiteralArgument.literal("1")))
            .argument(RequiredArgument.argument("subcommand", StringArgumentType.string()));
    node1 =
        LiteralNode.node()
            .argument(
                LiteralArgument.literal("baba")
                    .then(
                        RequiredArgument.argument("bebebaba123", IntegerArgumentType.integer())
                            .then(RequiredArgument.argument("ivancho", BoolArgumentType.bool())))
                    .then(LiteralArgument.literal("1"))
                    .then(LiteralArgument.literal("10")))
            .argument(RequiredArgument.argument("subcommand", StringArgumentType.string()));
  }

  @After
  public void terminate() {
    node = null;
    node1 = null;
  }

  @Test
  public void testUsage() {
    String usage = node.buildUsage();

    Assert.assertEquals("baba|[<subcommand>] [<bebebaba123>]|1 [<ivancho>]", usage);
  }

  @Test
  public void testUsage1() {
    String usage = node1.buildUsage();

    Assert.assertEquals("baba|[<subcommand>] [<bebebaba123>]|1|10 [<ivancho>]", usage);
  }
}
