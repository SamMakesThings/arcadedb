/*
 * Copyright (c) - Arcade Data LTD (https://arcadedata.com)
 */

package com.arcadedb.graph;

import com.arcadedb.database.RID;
import com.arcadedb.log.LogManager;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class EdgeIterator implements Iterator<Edge>, Iterable<Edge> {
  private final RID              vertex;
  private final Vertex.DIRECTION direction;
  private       EdgeSegment      currentContainer;
  private final AtomicInteger    currentPosition = new AtomicInteger(MutableEdgeSegment.CONTENT_START_POSITION);
  private       RID              nextEdgeRID;
  private       RID              nextVertexRID;

  public EdgeIterator(final EdgeSegment current, final RID vertex, final Vertex.DIRECTION direction) {
    if (current == null)
      throw new IllegalArgumentException("Edge chunk is null");

    this.currentContainer = current;
    this.vertex = vertex;
    this.direction = direction;
  }

  @Override
  public boolean hasNext() {
    if (currentContainer == null)
      return false;

    if (currentPosition.get() < currentContainer.getUsed())
      return true;

    currentContainer = currentContainer.getNext();
    if (currentContainer != null) {
      currentPosition.set(MutableEdgeSegment.CONTENT_START_POSITION);
      return currentPosition.get() < currentContainer.getUsed();
    }
    return false;
  }

  @Override
  public Edge next() {
    if (!hasNext())
      throw new NoSuchElementException();

    nextEdgeRID = currentContainer.getRID(currentPosition);
    // SKIP VERTEX
    nextVertexRID = currentContainer.getRID(currentPosition);

    if (nextEdgeRID.getPosition() < 0) {
      // CREATE LIGHTWEIGHT EDGE
      final String edgeType = currentContainer.getDatabase().getSchema().getTypeByBucketId(nextEdgeRID.getBucketId()).getName();

      if (direction == Vertex.DIRECTION.OUT)
        return new ImmutableLightEdge(currentContainer.getDatabase(), edgeType, nextEdgeRID, vertex, nextVertexRID);
      else
        return new ImmutableLightEdge(currentContainer.getDatabase(), edgeType, nextEdgeRID, nextVertexRID, vertex);
    }

    return nextEdgeRID.getEdge();
  }

  @Override
  public void remove() {
    if (nextEdgeRID == null)
      throw new NoSuchElementException();

    try {
      if (nextEdgeRID.getPosition() < 0) {
        // CREATE LIGHTWEIGHT EDGE
        final String edgeType = currentContainer.getDatabase().getSchema().getTypeByBucketId(nextEdgeRID.getBucketId()).getName();

        if (direction == Vertex.DIRECTION.OUT)
          new ImmutableLightEdge(currentContainer.getDatabase(), edgeType, nextEdgeRID, vertex, nextVertexRID).delete();
        else
          new ImmutableLightEdge(currentContainer.getDatabase(), edgeType, nextEdgeRID, nextVertexRID, vertex).delete();
      } else
        nextEdgeRID.getEdge().delete();
    } catch (Exception e) {
      LogManager.instance().log(this, Level.WARNING, "Error on deleting edge record %s", e, nextEdgeRID);
    }

    currentContainer.removeEntry(currentPosition.get());
  }

  @Override
  public Iterator<Edge> iterator() {
    return this;
  }
}
