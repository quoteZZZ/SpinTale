package com.spintale.ai.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.junit.jupiter.api.Test;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

class AiModuleDependencyTest
{
    private static final ClassFileImporter importer = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS);

    @Test
    void core_should_not_depend_on_any_project_modules()
    {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..ai.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..ai.runtime..",
                        "..ai.provider..",
                        "..ai.rag..",
                        "..ai.agent..",
                        "..ai.starter..",
                        "..ai.console..",
                        "..spintale.common..",
                        "..spintale.framework..",
                        "..spintale.system.."
                )
                .because("core should only contain stable abstractions without project dependencies");

        rule.check(importer.importPackages("com.spintale.ai.core"));
    }

    @Test
    void core_should_not_depend_on_spring_framework()
    {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..ai.core..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "dev.langchain4j..",
                        "io.opentelemetry.."
                )
                .because("core should not depend on Spring, LangChain4j, or OpenTelemetry SDK");

        rule.check(importer.importPackages("com.spintale.ai.core"));
    }

    @Test
    void runtime_should_not_depend_on_provider_rag_agent_console()
    {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..ai.runtime..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..ai.provider..",
                        "..ai.rag..",
                        "..ai.agent..",
                        "..ai.starter..",
                        "..ai.console.."
                )
                .because("runtime should only depend on core");

        rule.check(importer.importPackages("com.spintale.ai.runtime"));
    }

    @Test
    void provider_should_not_depend_on_runtime_rag_agent_console()
    {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..ai.provider..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..ai.runtime..",
                        "..ai.rag..",
                        "..ai.agent..",
                        "..ai.starter..",
                        "..ai.console.."
                )
                .because("provider should only depend on core");

        rule.check(importer.importPackages("com.spintale.ai.provider"));
    }

    @Test
    void rag_should_not_depend_on_provider_agent_console()
    {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..ai.rag..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..ai.provider..",
                        "..ai.agent..",
                        "..ai.starter..",
                        "..ai.console.."
                )
                .because("rag should only depend on core and runtime");

        rule.check(importer.importPackages("com.spintale.ai.rag"));
    }

    @Test
    void agent_should_not_depend_on_provider_rag_console()
    {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..ai.agent..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..ai.provider..",
                        "..ai.rag..",
                        "..ai.starter..",
                        "..ai.console.."
                )
                .because("agent should only depend on core and runtime");

        rule.check(importer.importPackages("com.spintale.ai.agent"));
    }

    @Test
    void starter_should_not_depend_on_console_or_ruoyi()
    {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..ai.starter..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..ai.console..",
                        "..spintale.common..",
                        "..spintale.framework..",
                        "..spintale.system.."
                )
                .because("starter should not depend on console or RuoYi modules");

        rule.check(importer.importPackages("com.spintale.ai.starter"));
    }

    @Test
    void console_should_not_be_accessed_by_ai_modules()
    {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "..ai.core..",
                        "..ai.runtime..",
                        "..ai.provider..",
                        "..ai.rag..",
                        "..ai.agent..",
                        "..ai.starter.."
                )
                .should().dependOnClassesThat()
                .resideInAPackage("..ai.console..")
                .because("console should not be accessed by any AI backend module");

        rule.check(importer.importPackages("com.spintale.ai"));
    }

    @Test
    void console_should_not_depend_on_provider()
    {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..ai.console..")
                .should().dependOnClassesThat()
                .resideInAPackage("..ai.provider..")
                .because("console should access provider config through runtime management facade");

        rule.check(importer.importPackages("com.spintale.ai.console"));
    }
}
