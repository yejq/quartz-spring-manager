Welcome to Quartz Spring Manager!
=====================

A jar for the xml of Spring integrate Quartz, can be use for web.

How to use:

- copy /src/main/webapp/scheduler to your project;
- copy com.yejq.spring.quartz.* to your project;
- modify springmvc cinfig file(mvc-config.xml), add config like:

```xml
	<!-- quartz-spring-manager begin -->
	<!-- <mvc:resources mapping="/scheduler/**" location="/WEB-INF/scheduler/" /> -->
	
	<bean id="schedulerConfig"
		class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">
		<property name="templateLoaderPath" value="/scheduler/ftl/" />
		<property name="defaultEncoding" value="UTF-8" />
		<property name="freemarkerSettings">  
	        <props>  
	            <prop key="template_update_delay">10</prop>  
	            <prop key="locale">zh_CN</prop> 
	            <prop key="datetime_format">yyyy-MM-dd HH:mm:ss</prop>  
                <prop key="date_format">yyyy-MM-dd</prop>  
                <prop key="number_format">#.##</prop>  
	            <prop key="classic_compatible">true</prop>  
	            <prop key="template_exception_handler">ignore</prop>  
	        </props>
	    </property> 
	</bean>
	<bean id="viewResolver"
		class="org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver">
		<!-- <property name="cache" value="true" /> -->
		<property name="prefix" value="" />
		<property name="suffix" value=".ftl" />
		<property name="contentType" value="text/html;charset=UTF-8"></property>
	</bean>
	
	<!-- quartz-spring-manager end -->
```

-  modify pom.xml, add dependency like:

```xml
	<dependency>
		<groupId>org.freemarker</groupId>
		<artifactId>freemarker</artifactId>
		<version>2.3.20</version>
	</dependency>
	<dependency>
		<groupId>org.json</groupId>
		<artifactId>json</artifactId>
		<version>20140107</version>
	</dependency>
	<dependency>
		<groupId>dom4j</groupId>
		<artifactId>dom4j</artifactId>
		<version>1.6.1</version>
	</dependency>
```

then, visit [http://localhost/scheduler/](http://localhost/scheduler/) ：

![](http://t.williamgates.net/image-7DD4_5433ABB0.jpg)