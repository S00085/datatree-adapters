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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import org.datatree.dom.BASE64;
import org.datatree.dom.Config;
import org.datatree.dom.Priority;
import org.datatree.dom.builtin.AbstractTextAdapter;
import org.datatree.dom.converters.DataConverterRegistry;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

/**
 * <b>SNAKEYAML YAML ADAPTER</b><br>
 * <br>
 * Description: SnakeYAML API without Jackson API.<br>
 * <br>
 * <b>Dependency:</b><br>
 * <br>
 * https://mvnrepository.com/artifact/org.yaml/snakeyaml<br>
 * compile group: 'org.yaml', name: 'snakeyaml', version: '1.18'<br>
 * <br>
 * <b>Invoke serializer and deserializer:</b><br>
 * <br>
 * Tree node = new Tree(inputString, "yaml");<br>
 * String outputString = node.toString("yaml");
 * 
 * @author Andras Berkes [andras.berkes@programmer.net]
 */
@Priority(10)
public class YamlSnakeYaml extends AbstractTextAdapter {

	// --- NAME OF THE FORMAT ---

	@Override
	public String getFormat() {
		return "yaml";
	}

	// --- MAPPER INSTANCES ---

	public Yaml mapper;
	public Yaml prettyMapper;

	// --- CONSTRUCTOR ---

	public YamlSnakeYaml() {

		// Representer
		ExtensibleRepresenter representer = new ExtensibleRepresenter();

		// Install MongoDB / BSON serializers
		tryToAddSerializers("org.datatree.dom.adapters.YamlSnakeYamlBsonSerializers", mapper);
		
		// InetAddress
		addSerializer(representer, InetAddress.class, (value) -> {
			return value.getCanonicalHostName();
		});
		addSerializer(representer, Inet4Address.class, (value) -> {
			return value.getCanonicalHostName();
		});
		addSerializer(representer, Inet6Address.class, (value) -> {
			return value.getCanonicalHostName();
		});

		// UUID
		addSerializer(representer, UUID.class, (value) -> {
			return value.toString();
		});

		// UUID
		addSerializer(representer, byte[].class, (value) -> {
			return BASE64.encode(value);
		});

		// Date
		addSerializer(representer, Date.class, (value) -> {
			if (Config.USE_TIMESTAMPS) {
				return DataConverterRegistry.convert(String.class, value);
			}
			return Long.toString(value.getTime());
		});

		// Create flow-style YAML normalMapper
		DumperOptions optionsNormal = new DumperOptions();
		optionsNormal.setDefaultFlowStyle(FlowStyle.FLOW);
		mapper = new Yaml(representer, optionsNormal);

		// Create "pretty" YAML normalMapper
		DumperOptions optionsPretty = new DumperOptions();
		optionsPretty.setDefaultFlowStyle(FlowStyle.BLOCK);
		prettyMapper = new Yaml(representer, optionsPretty);
	}

	// --- IMPLEMENTED WRITER METHOD ---

	@Override
	public String toString(Object value, Object meta, boolean pretty, boolean insertMeta) {
		return toString(value, meta, insertMeta, (input) -> {
			if (pretty) {
				return prettyMapper.dumpAll(Collections.singleton(input).iterator());
			}
			return mapper.dumpAll(Collections.singleton(input).iterator());
		});
	}

	// --- IMPLEMENTED PARSER METHOD ---

	@Override
	public Object parse(String source) throws Exception {
		return mapper.load(source);
	}

	// --- ADD CUSTOM SERIALIZER ---

	public static final <T> void addSerializer(ExtensibleRepresenter representer, Class<T> type,
			Function<T, String> function) {
		representer.addRepresenter(type, new Represent() {

			@SuppressWarnings("unchecked")
			@Override
			public Node representData(Object data) {
				return new ScalarNode(Tag.STR, function.apply((T) data), null, null, null);
			}

		});
	}

	public class ExtensibleRepresenter extends Representer {

		public void addRepresenter(Class<?> type, Represent represent) {
			representers.put(type, represent);
		}

	}

}