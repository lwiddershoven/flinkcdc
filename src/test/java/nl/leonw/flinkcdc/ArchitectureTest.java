package nl.leonw.flinkcdc;


import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureTest {

    @Test
    @DisplayName("Ensure that the streamtransform classes do not depend on orders classes")
    void streamtransformMayNotDependOnOrders() {
        JavaClasses classes = new ClassFileImporter().importPackages("nl.leonw.flinkcdc");

        noClasses().that()
                .resideInAPackage("..streamtransform..")
                .should().dependOnClassesThat()
                .resideInAPackage("..orders..")
                .check(classes);

    }

    @Test
    @DisplayName("Ensure that the orders classes do not depend on streamtransform classes")
    void ordersMayNotDependOnStreamtransform() {
        JavaClasses classes = new ClassFileImporter().importPackages("nl.leonw.flinkcdc");

        noClasses().that()
                .resideInAPackage("..orders..")
                .should().dependOnClassesThat()
                .resideInAPackage("..streamtransform..")
                .check(classes);

    }
}
