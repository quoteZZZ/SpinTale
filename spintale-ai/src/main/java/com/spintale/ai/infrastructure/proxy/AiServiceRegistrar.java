package com.spintale.ai.infrastructure.proxy;

import com.spintale.ai.core.annotation.AiService;
import com.spintale.ai.core.annotation.EnableAiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Registers proxy beans for declarative AI service interfaces.
 */
public class AiServiceRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final Logger log = LoggerFactory.getLogger(AiServiceRegistrar.class);

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Set<String> basePackages = resolveBasePackages(importingClassMetadata);
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        return beanDefinition.getMetadata().isInterface();
                    }
                };
        scanner.addIncludeFilter(new AnnotationTypeFilter(AiService.class));
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        for (String basePackage : basePackages) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                registerProxy(candidate, registry);
            }
        }
    }

    private Set<String> resolveBasePackages(AnnotationMetadata metadata) {
        Map<String, Object> values = metadata.getAnnotationAttributes(EnableAiServices.class.getName());
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(values);
        Set<String> packages = new LinkedHashSet<>();

        if (attributes != null) {
            for (String basePackage : attributes.getStringArray("basePackages")) {
                if (StringUtils.hasText(basePackage)) {
                    packages.add(basePackage);
                }
            }
            for (Class<?> basePackageClass : attributes.getClassArray("basePackageClasses")) {
                packages.add(ClassUtils.getPackageName(basePackageClass));
            }
        }

        if (packages.isEmpty()) {
            packages.add(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return packages;
    }

    private void registerProxy(BeanDefinition candidate, BeanDefinitionRegistry registry) {
        String className = candidate.getBeanClassName();
        if (!StringUtils.hasText(className)) {
            return;
        }
        try {
            ClassLoader classLoader = resourceLoader == null
                    ? ClassUtils.getDefaultClassLoader()
                    : resourceLoader.getClassLoader();
            Class<?> serviceInterface = ClassUtils.forName(className, classLoader);
            String beanName = buildBeanName(serviceInterface, registry);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ServiceProxyFactory.class);
            builder.addConstructorArgValue(serviceInterface);
            registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
            log.info("Registered AI service proxy: {}", className);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Failed to load AI service interface: " + className, ex);
        }
    }

    private String buildBeanName(Class<?> serviceInterface, BeanDefinitionRegistry registry) {
        AiService annotation = serviceInterface.getAnnotation(AiService.class);
        if (annotation != null && StringUtils.hasText(annotation.name())) {
            return annotation.name();
        }
        String beanName = Introspector.decapitalize(serviceInterface.getSimpleName());
        return registry.containsBeanDefinition(beanName) ? serviceInterface.getName() : beanName;
    }
}
