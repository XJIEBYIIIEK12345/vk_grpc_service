package com.xjiebyiiiek12345.vk.service;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import com.xjiebyiiiek12345.vk.repository.VkRepository;
import com.google.protobuf.ByteString;
import com.xjiebyiiiek12345.vk.VkGrpc;
import com.xjiebyiiiek12345.vk.VkProto.*;
import com.xjiebyiiiek12345.vk.entity.Entity;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class VkService extends VkGrpc.VkImplBase {
	
	@Autowired
	private VkRepository repository;
	
	@Override
	public void put(PutRequest request, StreamObserver<PutReply> responseObserver) {
		
		System.out.println("Put method called with key: " + request.getKey());
		System.out.println("Value length: " + request.getValue().size());
		
		try {
			
			repository.save(new Entity(request.getKey(), request.getValue().toByteArray()));
		
			responseObserver.onNext(PutReply.newBuilder()
					.setSuccess(true)
					.setMessage("Successful")
					.build());
		} catch (Exception e) {
			
			responseObserver.onNext(PutReply.newBuilder()
					.setSuccess(false)
					.setMessage(e.getMessage())
					.build());
		}
		responseObserver.onCompleted();
	}
	
	@Override
	public void get(GetRequest request, StreamObserver<GetReply> responseObserver) {
		
		try {
				var entity = repository.findById(request.getKey());

			responseObserver.onNext(GetReply.newBuilder()
					.setKey(request.getKey())
					.setValue(entity.map(e -> com.google.protobuf.ByteString.copyFrom(e.getValue())).orElse(com.google.protobuf.ByteString.EMPTY))
					.setFound(entity.isPresent())
					.build());
		} catch (Exception e) {
			
			responseObserver.onNext(GetReply.newBuilder()
					.setKey(request.getKey())
					.setFound(false)
					.build());
		}
		responseObserver.onCompleted();
	}
	
	@Override
	public void delete(DeleteRequest request, StreamObserver<DeleteReply> responseObserver) {
		
		try {
			
			boolean exists = repository.existsById(request.getKey());
			
			if (exists) {
				
				repository.deleteById(request.getKey());
				
				responseObserver.onNext(DeleteReply.newBuilder()
						.setSuccess(true)
						.setMessage("Successful")
						.build());
			} else {
			
			repository.deleteById(request.getKey());
		
			responseObserver.onNext(DeleteReply.newBuilder()
					.setSuccess(false)
					.setMessage("Not found")
					.build());
			}
		} catch (Exception e) {
			
			responseObserver.onNext(DeleteReply.newBuilder()
					.setSuccess(false)
					.setMessage(e.getMessage())
					.build());
		}
		responseObserver.onCompleted();
	}
	
	@Override
	public void range(RangeRequest request, StreamObserver<RangeReply> responseObserver) {
		
		try {
			
			Stream<Entity> entities = repository.findByKeyBetween(request.getKeyBegin(), request.getKeyEnd());
			
			entities.forEach(entity -> {
					responseObserver.onNext(RangeReply.newBuilder()
							.setKey(entity.getKey())
							.setValue(ByteString.copyFrom(entity.getValue()))
							.build());
			}
					);
		} catch (Exception e) {
			
			responseObserver.onError(e);
			return;
		}
		responseObserver.onCompleted();
	}
	
	@Override
	public void count(CountRequest request, StreamObserver<CountReply> responseObserver) {
		
		try {
			
			long count = repository.count();
		
			responseObserver.onNext(CountReply.newBuilder()
					.setCount(count)
					.build());
		} catch (Exception e) {
			
			responseObserver.onNext(CountReply.newBuilder()
					.setCount(0)
					.build());
		}
		responseObserver.onCompleted();
	}
}
