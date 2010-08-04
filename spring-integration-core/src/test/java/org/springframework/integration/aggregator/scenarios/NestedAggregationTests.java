/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.aggregator.scenarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.GenericMessage;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * @author Dave Syer
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class NestedAggregationTests {

	@Autowired
	DirectChannel input;

	@Test
	public void testAggregatorWithNestedSplitter() throws Exception {
		List<String> result = sendAndReceiveMessage(input, 2000);
		assertNotNull("Expected result and got null", result);
		assertEquals("[[foo, bar, spam], [bar, foo]]", result.toString());
	}

	private List<String> sendAndReceiveMessage(DirectChannel channel, int timeout) {

		MessagingTemplate messagingTemplate = new MessagingTemplate();
		messagingTemplate.setReceiveTimeout(timeout);

		@SuppressWarnings("unchecked")
		Message<List<String>> message = (Message<List<String>>) messagingTemplate.sendAndReceive(channel,
				new GenericMessage<List<List<String>>>(Arrays.asList(Arrays.asList("foo", "bar", "spam"), Arrays.asList("bar",
						"foo"))));

		return message == null ? null : message.getPayload();

	}

}
