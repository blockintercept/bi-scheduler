/*******************************************************************************
 * Copyright (C) 2019 blockintercept
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package in.bi.scheduler.quartz;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import in.bi.scheduler.config.AutoWiringSpringBeanJobFactory;
import in.bi.scheduler.utils.CommonConstants;


/**
 * Quartz Scheduler
 * 
 * @author blockintercept
 * @see www.blockintercept.com
 *
 */


@Configuration
@EnableAutoConfiguration
@ConditionalOnExpression("'${using.spring.schedulerFactory}'=='false'")
public class QrtzScheduler {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Environment environment;

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void init() {
		logger.info("Hello world from Quartz...");
	}

	@Bean
	public SpringBeanJobFactory springBeanJobFactory() {
		AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
		logger.debug("Configuring Job factory");

		jobFactory.setApplicationContext(applicationContext);
		return jobFactory;
	}

	@Bean
	public Scheduler scheduler(Trigger trigger, JobDetail job, SchedulerFactoryBean factory) throws SchedulerException {
		logger.debug("Getting a handle to the Scheduler");
		Scheduler scheduler = factory.getScheduler();
		if (!scheduler.checkExists(JobKey.jobKey(CommonConstants.JOB_NAME))) {
			scheduler.scheduleJob(job, trigger);
		} else {
			logger.info("Job already configured in the Scheduler...scheduled");
		}

		logger.debug("Starting Scheduler threads");
		scheduler.start();
		return scheduler;
	}

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		factory.setJobFactory(springBeanJobFactory());
		factory.setQuartzProperties(quartzProperties());
		return factory;
	}

	public Properties quartzProperties() throws IOException {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new FileSystemResource(
				environment.getProperty(CommonConstants.SPRING_RESOURCE_LOCATION).replace("file:", "")));
		propertiesFactoryBean.afterPropertiesSet();
		return propertiesFactoryBean.getObject();
	}

	@Bean
	public JobDetail jobDetail() {

		return newJob().ofType(QuartzJob.class).storeDurably().withIdentity(JobKey.jobKey(CommonConstants.JOB_NAME))
				.withDescription("Invoke Sample Job service...").build();
	}

	@Bean
	public Trigger trigger(JobDetail job) {

		if (environment.getProperty(CommonConstants.property_quartz_cron_expression) != null) {
			logger.info("Configuring trigger to use cron "
					+ environment.getProperty(CommonConstants.property_quartz_cron_expression));
			return newTrigger().forJob(job).withIdentity(TriggerKey.triggerKey(CommonConstants.QRTZ_TRIGGER_KEY))
					.withDescription(CommonConstants.TRIGGER_DESC)
					.withSchedule(CronScheduleBuilder
							.cronSchedule(environment.getProperty(CommonConstants.property_quartz_cron_expression)))
					.build();

		} else {
			logger.info("Configuring trigger to use frequency "
					+ environment.getProperty(CommonConstants.property_quartz_frequency));
			return newTrigger().forJob(job).withIdentity(TriggerKey.triggerKey(CommonConstants.QRTZ_TRIGGER_KEY))
					.withDescription(CommonConstants.TRIGGER_DESC)
					.withSchedule(simpleSchedule()
							.withIntervalInSeconds(Integer
									.parseInt(environment.getProperty(CommonConstants.property_quartz_frequency)))
							.repeatForever())
					.build();
		}
	}

	@Bean
	@QuartzDataSource
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource quartzDataSource() {
		return DataSourceBuilder.create().build();
	}
}
