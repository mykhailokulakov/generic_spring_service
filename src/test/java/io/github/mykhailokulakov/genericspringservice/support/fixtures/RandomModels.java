package io.github.mykhailokulakov.genericspringservice.support.fixtures;

import org.instancio.Instancio;
import org.instancio.settings.AssignmentType;
import org.instancio.settings.Keys;
import org.instancio.settings.OnSetFieldError;
import org.instancio.settings.Settings;

public final class RandomModels {

  private static final Settings SETTINGS =
      Settings.create()
          .set(Keys.ASSIGNMENT_TYPE, AssignmentType.FIELD)
          .set(Keys.ON_SET_FIELD_ERROR, OnSetFieldError.IGNORE)
          .lock();

  private RandomModels() {}

  public static <M> M create(Class<M> modelType) {
    return Instancio.of(modelType).withSettings(SETTINGS).create();
  }
}
