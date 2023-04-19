@echo off

set TOMCAT_HOME=..\ThirdPartyLibraries\Apache\Tomcat
set COMMONS_HOME=..\ThirdPartyLibraries\Apache\Commons
set LOGGING_HOME=..\ThirdPartyLibraries\Logging
set SPRING_HOME=..\ThirdPartyLibraries\Spring
set HIBERNATE_HOME=..\ThirdPartyLibraries\Hibernate
set DERBY_HOME=..\ThirdPartyLibraries\Apache\Derby
set JDBC_HOME=..\ThirdPartyLibraries\ThirdPartyJDBC

set TOMCAT_LIBS=%TOMCAT_HOME%\annotations-api.jar;%TOMCAT_HOME%\catalina-ha.jar;%TOMCAT_HOME%\catalina-tribes.jar;%TOMCAT_HOME%\catalina.jar;%TOMCAT_HOME%\el-api.jar;%TOMCAT_HOME%\jasper-el.jar;%TOMCAT_HOME%\jasper-jdt.jar;%TOMCAT_HOME%\jasper.jar;%TOMCAT_HOME%\jsp-api.jar;%TOMCAT_HOME%\servlet-api.jar;%TOMCAT_HOME%\tomcat-coyote.jar;%JDBC_HOME%\tomcat-jdbc.jar;%TOMCAT_HOME%\tomcat-juli.jar
set COMMONS_LIBS=%COMMONS_HOME%\commons-logging-1.2.jar;%COMMONS_HOME%\commons-httpclient-3.1.jar;%COMMONS_HOME%\commons-fileupload-1.2.1.jar;%COMMONS_HOME%\commons-lang-2.5.jar;%COMMONS_HOME%\commons-collections-3.2.2.jar;%COMMONS_HOME%\commons-io-2.6.jar;%COMMONS_HOME%\commons-codec-1.8.jar
set LOGGING_LIBS=%LOGGING_HOME%\slf4j-api-1.6.1.jar;%LOGGING_HOME%\slf4j-jdk14-1.6.1.jar
set SPRING_VER=3.0.5.RELEASE
set SPRING_LIBS=%SPRING_HOME%\org.springframework.core-%SPRING_VER%.jar;%SPRING_HOME%\org.springframework.context-%SPRING_VER%.jar;%SPRING_HOME%\org.springframework.web-%SPRING_VER%.jar;%SPRING_HOME%\org.springframework.beans-%SPRING_VER%.jar;%SPRING_HOME%\org.springframework.asm-%SPRING_VER%.jar;%SPRING_HOME%\org.springframework.expression-%SPRING_VER%.jar;%SPRING_HOME%\org.springframework.orm-%SPRING_VER%.jar;%SPRING_HOME%\org.springframework.transaction-%SPRING_VER%.jar;%SPRING_HOME%\org.springframework.jdbc-%SPRING_VER%.jar;%SPRING_HOME%\org.springframework.web.servlet-%SPRING_VER%.jar;%SPRING_HOME%\org.springframework.aop-%SPRING_VER%.jar;%SPRING_HOME%\aopalliance.jar
set HIBERNATE_LIBS=%HIBERNATE_HOME%\jpa\hibernate-jpa-2.0-api-1.0.0.Final.jar;%HIBERNATE_HOME%\hibernate3.jar;%HIBERNATE_HOME%\required\dom4j-1.6.1.jar;%HIBERNATE_HOME%\required\javassist-3.12.0.GA.jar;%HIBERNATE_HOME%\required\jta-1.1.jar;%HIBERNATE_HOME%\required\antlr-2.7.6.jar
set DERBY_LIBS=%DERBY_HOME%\lib\derbyclient.jar
set MAIL_LIBS=..\ThirdPartyLibraries\Javamail\mail.jar
set LOCAL_CP=resources;build\ant;..\DatabaseLayer\build\ant;..\DatabaseLayer\resources;..\commons-demo\build\ant

set DT_AGENT=

set PROPERTIES=-Djava.util.logging.config.file=resources\logging.properties

set CP=%LOCAL_CP%;%TOMCAT_LIBS%;%COMMONS_LIBS%;%LOGGING_LIBS%;%SPRING_LIBS%;%HIBERNATE_LIBS%;%DERBY_LIBS%

echo Using Classpath: %CP% 

java %PROPERTIES% -cp %CP% %DT_AGENT% com.dynatrace.easytravel.customer.RunFrontendTomcat
