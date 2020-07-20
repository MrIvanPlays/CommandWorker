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

  /**
   * Returns whether or not this index range is a range.
   *
   * @return range
   */
  public boolean isRange() {
    return end != -1;
  }

  /**
   * Returns the index if this object doesn't represent a range, or the starting index if this
   * object is holding a range.
   *
   * @return index
   */
  public int getIndex() {
    return start;
  }

  /**
   * Returns the starting index of the range, or if it's not a range - the index.
   *
   * @return starting index
   */
  public int getStart() {
    return start;
  }

  /**
   * Returns the ending index of the range, or if it's not a range - <code>-1</code>
   *
   * @return ending index
   */
  public int getEnd() {
    return end;
  }
}
