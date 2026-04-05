package com.xjiebyiiiek12345.vk.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.tarantool.spring.data34.query.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonIgnoreProperties(ignoreUnknown = true) // for example bucket_id
@KeySpace("KV")
public class Entity {

	@Id
	@JsonProperty("key")
	public String key;

	@Field("value")
	@JsonProperty("value")
	public byte[] value;

	public Entity() {}
	
	public Entity(String key, byte[] value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

}
