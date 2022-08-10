package org.taruts.djig.core.childContext;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Contains some adapted copy-paste from {@link BeanFactoryAnnotationUtils}.
 */
public class DjigBeanFactoryAnnotationUtils {

    /**
     * An adapted version of {@link BeanFactoryAnnotationUtils#qualifiedBeansOfType(ListableBeanFactory, Class, String)}.
     *
     * @see BeanFactoryAnnotationUtils#qualifiedBeansOfType(ListableBeanFactory, Class, String)
     * @see BeanFactoryAnnotationUtils#isQualifierMatch(Predicate, String, BeanFactory)
     * @see #isQualifierMatch(BeanFactory, String, Class, Object)
     */
    public static <T, A extends Annotation> Map<String, T> qualifiedBeansOfType(
            ConfigurableListableBeanFactory beanFactory,
            Class<T> beanClass,
            Class<A> annotationClass,
            String annotationValue
    ) {
        Map<String, T> result = new LinkedHashMap<>(4);

        String[] candidateBeanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, beanClass);
        for (String beanName : candidateBeanNames) {
            boolean qualifierMatch = isQualifierMatch(beanFactory, beanName, annotationClass, annotationValue);
            if (qualifierMatch) {
                T bean = beanFactory.getBean(beanName, beanClass);
                result.put(beanName, bean);
            }
        }

        return result;
    }

    /**
     * An adapted version of {@link BeanFactoryAnnotationUtils#isQualifierMatch(Predicate, String, BeanFactory)}.
     * <p>
     * 1. It can use qualifier annotations other than {@link Qualifier}
     * <p>
     * 2. Ignores the {@code annotationClass} annotations put on beans via placing it on the @Bean method or on the implementation class,
     * because this is not how are proxies are registered. Proxies are registered programmatically and contain the annotation data in the
     * bean definition.
     * <p>
     *
     * @see BeanFactoryAnnotationUtils#isQualifierMatch(Predicate, String, BeanFactory)
     */
    private static <A extends Annotation> boolean isQualifierMatch(
            BeanFactory beanFactory,
            String beanName,
            Class<A> annotationClass,
            Object requiredAnnotationValue
    ) {
        if (beanFactory instanceof ConfigurableBeanFactory configurableBeanFactory) {
            BeanDefinition beanDefinition = configurableBeanFactory.getMergedBeanDefinition(beanName);
            if (beanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition) {
                AutowireCandidateQualifier qualifier = abstractBeanDefinition.getQualifier(annotationClass.getName());
                if (qualifier != null) {
                    Object annotationValue = qualifier.getAttribute(AutowireCandidateQualifier.VALUE_KEY);
                    return requiredAnnotationValue.equals(annotationValue);
                }
            }
        }
        return false;
    }
}
