# BlockIntercept Agent Scheduler  

This is a basic scheduler that starts passive agents for data capture and blockchain processing. It's a spring boot batch application that accepts passive agent configuration, schedulig parameters and launches the agents for blockchain processing.

##### Requirements

* java 1.8 or more
* mysql (Optional - needed for persistent job store) 


##### Configuration File

```
config/bi-scheduler.properties
```
Copy this file to any directory <config.path>

##### Run the bi-scheduler

###### through spring-boot maven plugin

```
	mvn spring:boot:run -Dspring.config.location=file:/<config.path>
```

###### through built java application (from target directory)

```
	java -jar bi-scheduler-0.0.1.jar --spring.config.location=file:/<config.path>
```

##### configuration parameters

For selecting the agent type (currently supporting http)

```
	blockintercept.agent.type=http
```

For setting up agent url 
```
	blockintercept.agent.url=http://localhost:8090/start
```

For setting up cron (works only with quartz) 
```
	blockintercept.quartz.scheduler.cron=<cron>
```

For setting up frequency (works only with quartz) 
```
	blockintercept.quartz.scheduler.frequency.seconds=10
```

For selecting the scheduler type, true for spring, false for Quartz 

```
	using.spring.schedulerFactory=true
```

For setting the spring job store type - RAM (memory), Persistent (jdbc) 

```
	spring.quartz.job-store-type=jdbc
```
JDBC parameters for Quartz 

```
	org.quartz.dataSource.quartzDataSource.* 
```

JDBC parameters for Spring 

```
	spring.datasource.* 
```