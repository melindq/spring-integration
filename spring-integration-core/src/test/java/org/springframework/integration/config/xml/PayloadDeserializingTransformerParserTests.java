/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.integration.config.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.serializer.Deserializer;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.integration.transformer.MessageTransformationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileCopyUtils;

/**
 * @author Mark Fisher
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class PayloadDeserializingTransformerParserTests {

	@Autowired
	private MessageChannel directInput;

	@Autowired
	private MessageChannel queueInput;

	@Autowired
	private MessageChannel customDeserializerInput;

	@Autowired
	private PollableChannel output;

	@Autowired
	@Qualifier("direct.handler")
	private MessageHandler handler;


	@Test
	public void directChannelWithSerializedStringMessage() throws Exception {
		byte[] bytes = serialize("foo");
		directInput.send(new GenericMessage<byte[]>(bytes));
		Message<?> result = output.receive(10000);
		assertThat(result).isNotNull();
		assertThat(result.getPayload() instanceof String).isTrue();
		assertThat(result.getPayload()).isEqualTo("foo");
		Set<?> patterns = TestUtils.getPropertyValue(this.handler, "transformer.converter.whiteListPatterns",
				Set.class);
		assertThat(patterns.size()).isEqualTo(1);
		assertThat(patterns.iterator().next()).isEqualTo("*");
	}

	@Test
	public void queueChannelWithSerializedStringMessage() throws Exception {
		byte[] bytes = serialize("foo");
		queueInput.send(new GenericMessage<byte[]>(bytes));
		Message<?> result = output.receive(10000);
		assertThat(result).isNotNull();
		assertThat(result.getPayload() instanceof String).isTrue();
		assertThat(result.getPayload()).isEqualTo("foo");
	}

	@Test
	public void directChannelWithSerializedObjectMessage() throws Exception {
		byte[] bytes = serialize(new TestBean());
		directInput.send(new GenericMessage<byte[]>(bytes));
		Message<?> result = output.receive(10000);
		assertThat(result).isNotNull();
		assertThat(result.getPayload().getClass()).isEqualTo(TestBean.class);
		assertThat(((TestBean) result.getPayload()).name).isEqualTo("test");
	}

	@Test
	public void queueChannelWithSerializedObjectMessage() throws Exception {
		byte[] bytes = serialize(new TestBean());
		queueInput.send(new GenericMessage<byte[]>(bytes));
		Message<?> result = output.receive(10000);
		assertThat(result).isNotNull();
		assertThat(result.getPayload().getClass()).isEqualTo(TestBean.class);
		assertThat(((TestBean) result.getPayload()).name).isEqualTo("test");
	}

	@Test(expected = MessageTransformationException.class)
	public void invalidPayload() {
		byte[] bytes = new byte[] { 1, 2, 3 };
		directInput.send(new GenericMessage<byte[]>(bytes));
	}

	@Test
	public void customDeserializer() throws Exception {
		customDeserializerInput.send(new GenericMessage<byte[]>("test".getBytes("UTF-8")));
		Message<?> result = output.receive(10000);
		assertThat(result).isNotNull();
		assertThat(result.getPayload().getClass()).isEqualTo(String.class);
		assertThat(result.getPayload()).isEqualTo("TEST");
	}


	private static byte[] serialize(Object object) throws Exception {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
		objectStream.writeObject(object);
		return byteStream.toByteArray();
	}


	@SuppressWarnings("serial")
	private static class TestBean implements Serializable {

		TestBean() {
			super();
		}

		public final String name = "test";

	}


	public static class TestDeserializer implements Deserializer<Object> {

		@Override
		public Object deserialize(InputStream source) throws IOException {
			return FileCopyUtils.copyToString(new InputStreamReader(source, "UTF-8")).toUpperCase();
		}

	}

}
