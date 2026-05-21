package io.github.mykhailokulakov.genericspringservice.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(
    packages = "io.github.mykhailokulakov.genericspringservice",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  // Web here means the controller-shaped edge (controllers, exception handlers,
  // OpenAPI annotation aggregators). DTOs in ..web.dto.. are excluded from the
  // layer because they are cross-layer carriers — Service reads them on the way
  // down and Web writes them on the way out. The packaging convention from
  // DESIGN.md section 3.6 still keeps them under ..web.dto..; the layering check
  // only constrains who may depend on the framework-facing classes.
  @ArchTest
  static final ArchRule layered =
      layeredArchitecture()
          .consideringOnlyDependenciesInLayers()
          .layer("Web")
          .definedBy(resideInAPackage("..web..").and(not(resideInAPackage("..web.dto.."))))
          .layer("Service")
          .definedBy("..service..")
          .layer("Repository")
          .definedBy("..repository..")
          .layer("Entity")
          .definedBy("..domain.entity..")
          .layer("Persistence")
          .definedBy("..common.persistence..")
          .whereLayer("Web")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Service")
          .mayOnlyBeAccessedByLayers("Web")
          .whereLayer("Repository")
          .mayOnlyBeAccessedByLayers("Service")
          .whereLayer("Entity")
          .mayOnlyBeAccessedByLayers("Repository", "Service")
          .whereLayer("Persistence")
          .mayOnlyBeAccessedByLayers("Entity");

  @ArchTest
  static final ArchRule noFieldInjection =
      noFields().should().beAnnotatedWith(Autowired.class).as("No field injection (@Autowired)");

  @ArchTest static final ArchRule noStdout = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

  @ArchTest
  static final ArchRule noCycles =
      slices().matching("..genericspringservice.(*)..").should().beFreeOfCycles();

  @ArchTest
  static final ArchRule preAuthorizeOnlyInAnnotationPackage =
      classes()
          .that()
          .areAnnotatedWith(PreAuthorize.class)
          .should()
          .resideInAPackage("..security.annotation..");

  @ArchTest
  static final ArchRule mappedSuperclassOnlyInPersistencePackage =
      classes()
          .that()
          .areAnnotatedWith(MappedSuperclass.class)
          .should()
          .resideInAPackage("..common.persistence..");

  @ArchTest
  static final ArchRule entitiesNotInWeb =
      noClasses()
          .that()
          .resideInAPackage("..web..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..domain.entity..");

  @ArchTest
  static final ArchRule controllersInWeb =
      classes().that().areAnnotatedWith(RestController.class).should().resideInAPackage("..web..");

  @ArchTest
  static final ArchRule servicesInService =
      classes().that().areAnnotatedWith(Service.class).should().resideInAPackage("..service..");

  @ArchTest
  static final ArchRule entitiesInEntityPackage =
      classes()
          .that()
          .areAnnotatedWith(Entity.class)
          .should()
          .resideInAPackage("..domain.entity..");

  @ArchTest
  static final ArchRule dtosAreRecords =
      classes()
          .that()
          .resideInAPackage("..web.dto..")
          .and()
          .haveSimpleNameNotEndingWith("package-info")
          .should()
          .beRecords();

  // From prompt 8: any meta-annotation aggregating springdoc @ApiResponse(s)
  // must live in ..web.annotation.. — the OpenAPI documentation cross-cut,
  // mirroring how @PreAuthorize is centralised in ..security.annotation..
  @ArchTest
  static final ArchRule apiResponseOnlyInAnnotationPackage =
      classes()
          .that()
          .areAnnotatedWith(ApiResponses.class)
          .or()
          .areAnnotatedWith(ApiResponse.class)
          .should()
          .resideInAPackage("..web.annotation..");

  // From prompt 16: @TestComponent is a Spring test-only stereotype. The
  // DoNotIncludeTests import option scopes this rule to production sources so
  // a @TestComponent that leaks into src/main fails the build.
  @ArchTest
  static final ArchRule testComponentsInTestSourcesOnly =
      noClasses().should().beAnnotatedWith(TestComponent.class);
}
