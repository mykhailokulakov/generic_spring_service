package io.github.mykhailokulakov.genericspringservice.support.fixtures;

public interface RepoFixture<E> {

  Class<E> entityType();

  E newPersistable();
}
