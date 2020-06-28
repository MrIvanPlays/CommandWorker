package com.mrivanplays.commandworker.core.argument.parser;

/**
 * Represents index range.
 *
 * <p>Index range is a integer range, with an exception being that it can accept only start, which
 * then is being considered a single index, without being a range.
 *
 * <p>Usage examples can be found in {@link ArgumentHolder}
 */
public final class IndexRange {

  private final int start;
  private final int end;

  public IndexRange(int start, int end) {
    this.start = start;
    this.end = end;
  }

  public IndexRange(int index) {
    this.start = index;
    this.end = -1;
  }

  public boolean isRange() {
    return end != -1;
  }

  public int getIndex() {
    return start;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }
}
