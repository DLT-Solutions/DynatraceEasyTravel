package com.dynatrace.easytravel.database.hibernate;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Rafal.Psciuk
 */
public class HibernateStatisticsFactoryBean implements FactoryBean<Statistics> {
	private SessionFactory sessionFactory;
	 
    @Override
    public Statistics getObject() throws Exception {
        return sessionFactory.getStatistics();
    }
 
    @Override
    public Class<?> getObjectType() {
        return Statistics.class;
    }
 
    @Override
    public boolean isSingleton() {
        return true;
    }
 
    public void setSessionFactory(EntityManagerFactory entityManagerFactory) {
        this.sessionFactory = ((HibernateEntityManagerFactory) entityManagerFactory).getSessionFactory();
    }
}
