package io.github.mykhailokulakov.genericspringservice.support.fixtures;

public interface ModelFixture<M> {

  Class<M> modelType();

  M newModel();
}
