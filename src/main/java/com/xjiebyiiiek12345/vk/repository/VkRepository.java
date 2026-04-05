package com.xjiebyiiiek12345.vk.repository;

import java.util.stream.Stream;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.xjiebyiiiek12345.vk.entity.Entity;

@Repository
public interface VkRepository extends CrudRepository<Entity, String>{
	
	 Stream<Entity> findByKeyBetween(String keyBegin, String keyEnd);
}
