package io.github.mykhailokulakov.genericspringservice.support.fixtures;

import static org.instancio.Select.fields;

import io.github.mykhailokulakov.genericspringservice.common.persistence.Auditable;
import io.github.mykhailokulakov.genericspringservice.common.persistence.Identifiable;
import io.github.mykhailokulakov.genericspringservice.common.persistence.Versioned;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import org.instancio.Instancio;
import org.instancio.settings.AssignmentType;
import org.instancio.settings.Keys;
import org.instancio.settings.OnSetFieldError;
import org.instancio.settings.Settings;

public final class RandomEntities {

  private static final Settings SETTINGS =
      Settings.create()
          .set(Keys.ASSIGNMENT_TYPE, AssignmentType.FIELD)
          .set(Keys.ON_SET_FIELD_ERROR, OnSetFieldError.IGNORE)
          .lock();

  private RandomEntities() {}

  public static <E> E create(Class<E> entityType) {
    return Instancio.of(entityType)
        .withSettings(SETTINGS)
        .ignore(fields().declaredIn(Identifiable.class))
        .ignore(fields().declaredIn(Auditable.class))
        .ignore(fields().declaredIn(Versioned.class))
        .ignore(fields().annotated(ManyToOne.class))
        .ignore(fields().annotated(OneToOne.class))
        .ignore(fields().annotated(OneToMany.class))
        .ignore(fields().annotated(ManyToMany.class))
        .lenient()
        .create();
  }
}
