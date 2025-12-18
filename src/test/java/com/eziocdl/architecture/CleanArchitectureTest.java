package com.eziocdl.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Architecture tests using ArchUnit to enforce Clean Architecture rules.
 *
 * These tests ensure that:
 * 1. Dependencies point inward (Infrastructure -> Application -> Domain)
 * 2. Domain layer has no external dependencies
 * 3. Each layer only accesses allowed layers
 */
@DisplayName("Clean Architecture Rules")
class CleanArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.eziocdl");
    }

    @Nested
    @DisplayName("Layer Dependency Rules")
    class LayerDependencyTests {

        @Test
        @DisplayName("Layered architecture should be respected")
        void layeredArchitectureShouldBeRespected() {
            ArchRule rule = layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("API").definedBy("..api..")
                    .layer("Application").definedBy("..application..")
                    .layer("Domain").definedBy("..domain..")
                    .layer("Infrastructure").definedBy("..infrastructure..")

                    .whereLayer("API").mayOnlyBeAccessedByLayers("Infrastructure")
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("API", "Infrastructure")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "API")
                    .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain should NOT depend on Infrastructure")
        void domainShouldNotDependOnInfrastructure() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain should NOT depend on Application")
        void domainShouldNotDependOnApplication() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..application..");

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain should NOT depend on API layer")
        void domainShouldNotDependOnApi() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..api..");

            rule.check(classes);
        }

        @Test
        @DisplayName("Application should NOT depend on Infrastructure")
        void applicationShouldNotDependOnInfrastructure() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

            rule.check(classes);
        }

        @Test
        @DisplayName("Application should NOT depend on API layer")
        void applicationShouldNotDependOnApi() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..api..");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Domain Layer Rules")
    class DomainLayerTests {

        @Test
        @DisplayName("Domain should NOT use Spring annotations (except allowed)")
        void domainShouldNotUseSpringAnnotations() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage("..domain.model..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web..",
                            "org.springframework.security..",
                            "org.springframework.data.."
                    );

            rule.check(classes);
        }

        @Test
        @DisplayName("Domain exceptions should extend RuntimeException")
        void domainExceptionsShouldExtendRuntimeException() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..domain.exception..")
                    .and().haveSimpleNameEndingWith("Exception")
                    .should().beAssignableTo(RuntimeException.class);

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("API Layer Rules")
    class ApiLayerTests {

        @Test
        @DisplayName("Controllers should be in api.controller package")
        void controllersShouldBeInCorrectPackage() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..api.controller..");

            rule.check(classes);
        }

        @Test
        @DisplayName("DTOs should be records or have no business logic")
        void dtosShouldBeInCorrectPackage() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..api.dto..")
                    .should().haveSimpleNameEndingWith("Request")
                    .orShould().haveSimpleNameEndingWith("Response");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Application Layer Rules")
    class ApplicationLayerTests {

        @Test
        @DisplayName("Use cases should be in application.usecase package")
        void useCasesShouldBeInCorrectPackage() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("UseCase")
                    .should().resideInAPackage("..application.usecase..");

            rule.check(classes);
        }

        @Test
        @DisplayName("Ports should be interfaces in application.port package")
        void portsShouldBeInterfaces() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..application.port..")
                    .and().haveSimpleNameEndingWith("Port")
                    .should().beInterfaces();

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Infrastructure Layer Rules")
    class InfrastructureLayerTests {

        @Test
        @DisplayName("Adapters should be in infrastructure.adapter package")
        void adaptersShouldBeInCorrectPackage() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Adapter")
                    .should().resideInAPackage("..infrastructure.adapter..");

            rule.check(classes);
        }

        @Test
        @DisplayName("Configs should be in infrastructure.config package")
        void configsShouldBeInCorrectPackage() {
            ArchRule rule = classes()
                    .that().haveSimpleNameEndingWith("Config")
                    .should().resideInAPackage("..infrastructure.config..")
                    .orShould().resideInAPackage("..api.docs..");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("Naming Convention Rules")
    class NamingConventionTests {

        @Test
        @DisplayName("Services should have Service suffix")
        void servicesShouldHaveCorrectSuffix() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..domain.service..")
                    .should().haveSimpleNameEndingWith("Service");

            rule.check(classes);
        }

        @Test
        @DisplayName("Repositories should have Repository suffix")
        void repositoriesShouldHaveCorrectSuffix() {
            ArchRule rule = classes()
                    .that().resideInAPackage("..infrastructure.adapter.persistence..")
                    .and().areNotInterfaces()
                    .should().haveSimpleNameEndingWith("Repository");

            rule.check(classes);
        }
    }
}
