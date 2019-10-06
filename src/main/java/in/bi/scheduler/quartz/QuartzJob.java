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

import java.util.Arrays;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import in.bi.scheduler.service.JobExecutionService;

/**
 * Quartz Job to be executed
 * 
 * @author blockintercept
 * @see www.blockintercept.com
 *
 */


@Component
public class QuartzJob implements Job {

    Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private Environment environment;

    @Autowired
    private JobExecutionService jobService;

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());
        logger.info("running environment " + Arrays.toString(environment.getActiveProfiles()) + environment.getProperty("blockintercept.scheduler.cron"));

        jobService.executeSampleJob();

        logger.info("Next job scheduled @ {}", context.getNextFireTime());
    }
}
