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
package in.bi.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.bi.scheduler.utils.CommonConstants;

/**
 * 
 * This is the execution point for initialization agent process.
 * 
 * @author blockintercept
 * @see www.blockintercept.com
 */

@Service
public class JobExecutionService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Environment environment;

	public void executeSampleJob() {

		logger.info("Job Service has started...");
		if (CommonConstants.AGENT_TYPE_HTTP
				.equals(environment.getProperty(CommonConstants.BLOCKINTERCEPT_AGENT_TYPE))) {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate
					.getForEntity(environment.getProperty(CommonConstants.BLOCKINTERCEPT_AGENT_URL), String.class);
			if (response.getStatusCode().isError()) {
				logger.error(
						"Agent not responding @" + environment.getProperty(CommonConstants.BLOCKINTERCEPT_AGENT_URL),
						response.getStatusCode());
			} else {
				logger.info("Agent started " + response.getBody());
			}

		}

	}

}
