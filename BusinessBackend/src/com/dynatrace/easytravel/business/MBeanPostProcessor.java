package com.dynatrace.easytravel.business;

import javax.management.ObjectName;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.dynatrace.easytravel.logging.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * this class calls the preregister method of the tomcat-jdbc DataSource which registers the ConnectionPool at the platform MBeanServer
 * otherwise no jmx-metrics of tomcat-jdbc would be available for usage in dynatrace...
 *
 * 
 */
public class MBeanPostProcessor implements BeanPostProcessor {
    
    public static final Logger log = LoggerFactory.make();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource) {
            try {
                ((DataSource)bean).preRegister(null, new ObjectName("*:*"));
            } catch (Exception e) {
                log.warn("error registering mbean", e);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
