/**
 * This software is licensed under the Apache 2 license, quoted below.<br>
 * <br>
 * Copyright 2017 Andras Berkes [andras.berkes@programmer.net]<br>
 * <br>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at<br>
 * <br>
 * http://www.apache.org/licenses/LICENSE-2.0<br>
 * <br>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.datatree.dom.adapters;

import java.io.IOException;

import org.datatree.dom.Priority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.serializers.BsonSerializer;

/**
 * <b>JACKSON BINARY BSON ADAPTER</b><br>
 * <br>
 * Description: Binary BSON reader and writer.<br>
 * <br>
 * <b>Dependency:</b><br>
 * <br>
 * https://mvnrepository.com/artifact/de.undercouch/bson4jackson<br>
 * compile group: 'de.undercouch', name: 'bson4jackson', version: '2.7.0'<br>
 * <br>
 * <b>Invoke serializer and deserializer:</b><br>
 * <br>
 * Tree node = new Tree(inputBytes, "bson");<br>
 * byte[] outputBytes = node.toBytes("bson");
 * 
 * @author Andras Berkes [andras.berkes@programmer.net]
 */
@Priority(10)
public class BsonJackson extends AbstractJacksonBinaryAdapter {

	// --- NAME OF THE FORMAT ---
	
	@Override
	public String getFormat() {
		return "bson";
	}
	
	// --- CONSTRUCTOR ---
	
	public BsonJackson() {
		super(new ObjectMapper(new BsonFactory()));
		
		// Install MongoDB / BSON serializers
		tryToAddSerializers("org.datatree.dom.adapters.BsonJacksonBsonSerializers", mapper);
	}
	
	// --- ADD CUSTOM SERIALIZER ---
	
	public static final <T> void addSerializer(SimpleModule module, Class<T> type,
			CheckedBiConsumer<T, BsonGenerator> consumer) {
		module.addSerializer(type, new BsonSerializer<T>() {

			@Override
			public final void serialize(T value, BsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
				consumer.accept(value, gen);
			}

		});
	}

}