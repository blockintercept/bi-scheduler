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

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import in.bi.scheduler.config.AutoWiringSpringBeanJobFactory;
import in.bi.scheduler.utils.CommonConstants;

/**
 * Spring Quartz Scheduler
 * 
 * @author blockintercept
 * @see www.blockintercept.com
 *
 */

@Configuration
@EnableAutoConfiguration
@ConditionalOnExpression("'${using.spring.schedulerFactory}'=='true'")
public class SpringQrtzScheduler {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void init() {
		logger.info("Configured to use Spring scheduler...");
	}

	@Autowired
	private Environment environment;

	@Bean
	public SpringBeanJobFactory springBeanJobFactory() {
		AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
		logger.debug("Configuring Job factory");

		jobFactory.setApplicationContext(applicationContext);
		return jobFactory;
	}

	@Bean
	public SchedulerFactoryBean scheduler(Trigger trigger, JobDetail job, DataSource quartzDataSource) {

		SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		schedulerFactory.setConfigLocation(new FileSystemResource(
				environment.getProperty(CommonConstants.SPRING_RESOURCE_LOCATION).replace("file:", "")));

		logger.debug("Setting the Scheduler up");
		schedulerFactory.setJobFactory(springBeanJobFactory());
		schedulerFactory.setJobDetails(job);
		schedulerFactory.setTriggers(trigger);

		// Comment the following line to use the default Quartz job store.
		schedulerFactory.setDataSource(quartzDataSource);

		return schedulerFactory;
	}

	@Bean
	public JobDetailFactoryBean jobDetail() {

		JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(QuartzJob.class);
		jobDetailFactory.setName(CommonConstants.JOB_NAME);
		jobDetailFactory.setDescription("Invoke Job service...");
		jobDetailFactory.setDurability(true);
		return jobDetailFactory;
	}

	@Bean
	public SimpleTriggerFactoryBean trigger(JobDetail job) {

		SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(job);

		int frequencyInSec = 10;
		logger.info("Configuring trigger to fire every {} seconds", frequencyInSec);

		trigger.setRepeatInterval(frequencyInSec * 1000);
		trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
		trigger.setName(CommonConstants.QRTZ_TRIGGER_KEY);

		return trigger;
	}

	@Bean
	@QuartzDataSource
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource quartzDataSource() {
		return DataSourceBuilder.create().build();
	}

}
