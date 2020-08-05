package com.mrivanplays.commandworker.core;

import org.junit.Assert;
import org.junit.Test;

public class FallbackPrefixAliasesTest {

  @Test
  public void testFallbackPrefixAliases() {
    String[] aliases = new String[] {"foo", "bar", "baz"};
    String fallbackPrefix = "hello";
    String[] expectedAliases =
        new String[] {"foo", "bar", "baz", "hello:foo", "hello:bar", "hello:baz"};

    String[] resultAliases = CommandManager.getAliases(fallbackPrefix, aliases);
    Assert.assertArrayEquals(expectedAliases, resultAliases);
  }
}
