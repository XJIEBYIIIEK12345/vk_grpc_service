package com.xjiebyiiiek12345.vk;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.CountDownLatch;

import com.google.protobuf.ByteString;
import com.xjiebyiiiek12345.vk.VkGrpc;
import com.xjiebyiiiek12345.vk.VkProto.CountReply;
import com.xjiebyiiiek12345.vk.VkProto.CountRequest;
import com.xjiebyiiiek12345.vk.VkProto.DeleteReply;
import com.xjiebyiiiek12345.vk.VkProto.DeleteRequest;
import com.xjiebyiiiek12345.vk.VkProto.GetReply;
import com.xjiebyiiiek12345.vk.VkProto.GetRequest;
import com.xjiebyiiiek12345.vk.VkProto.PutReply;
import com.xjiebyiiiek12345.vk.VkProto.PutRequest;
import com.xjiebyiiiek12345.vk.VkProto.RangeReply;
import com.xjiebyiiiek12345.vk.VkProto.RangeRequest;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    public static void main(String[] args) {
        System.out.println("=== Starting gRPC Client Tests ===");

        ManagedChannel channel = ManagedChannelBuilder
            .forAddress(HOST, PORT)
            .usePlaintext()
            .build();

        VkGrpc.VkStub asyncStub = VkGrpc.newStub(channel);

        VkGrpc.VkBlockingStub blockingStub = VkGrpc.newBlockingStub(channel);

        testPutOperation(blockingStub);
        testGetOperation(blockingStub);
        testDeleteOperation(blockingStub);
        testRangeOperation(asyncStub);
        testCountOperation(blockingStub);

        channel.shutdown();
        System.out.println("\n=== All tests completed ===");
    }

    private static void testPutOperation(VkGrpc.VkBlockingStub stub) {
        System.out.println("\n--- Testing PUT operation ---");

        String testKey = "test-key-" + System.currentTimeMillis();
        ByteString testValue = ByteString.copyFromUtf8("test-value-" + System.currentTimeMillis());

        PutRequest request = PutRequest.newBuilder()
            .setKey(testKey)
            .setValue(testValue)
            .build();

        System.out.println("Sending PUT request:");
        System.out.println("  Key: " + request.getKey());
        System.out.println("  Value length: " + request.getValue().size() + " bytes");

        try {
            PutReply response = stub.put(request);
            System.out.println("PUT Response received:");
            System.out.println("  Success: " + response.getSuccess());
            System.out.println("  Message: " + response.getMessage());

            if (response.getSuccess()) {
                System.out.println("✅ PUT test PASSED");
            } else {
                System.out.println("❌ PUT test FAILED");
            }
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            System.out.println("❌ PUT test FAILED (exception)");
        }
    }

    private static void testGetOperation(VkGrpc.VkBlockingStub stub) {
        System.out.println("\n--- Testing GET operation ---");

        String testKey = "get-test-" + System.currentTimeMillis();
        stub.put(PutRequest.newBuilder()
            .setKey(testKey)
            .setValue(ByteString.copyFromUtf8("get-test-value"))
            .build());

        GetRequest getRequest = GetRequest.newBuilder()
            .setKey(testKey)
            .build();

        System.out.println("Sending GET request for key: " + testKey);

        try {
            GetReply response = stub.get(getRequest);
            System.out.println("GET Response received:");
            System.out.println("  Found: " + response.getFound());

            if (response.getFound()) {
                System.out.println("  Retrieved value: " + response.getValue().toStringUtf8());
                System.out.println("  Value length: " + response.getValue().size() + " bytes");
                System.out.println("✅ GET test PASSED");
            } else {
                System.out.println("❌ GET test FAILED: key not found");
            }
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            System.out.println("❌ GET test FAILED (exception)");
        }
    }

    private static void testDeleteOperation(VkGrpc.VkBlockingStub stub) {
        System.out.println("\n--- Testing DELETE operation ---");

        String testKey = "delete-test-" + System.currentTimeMillis();
        stub.put(PutRequest.newBuilder()
            .setKey(testKey)
            .setValue(ByteString.copyFromUtf8("to-be-deleted"))
            .build());

        DeleteRequest deleteRequest = DeleteRequest.newBuilder()
            .setKey(testKey)
            .build();

        System.out.println("Sending DELETE request for key: " + testKey);

        try {
            DeleteReply response = stub.delete(deleteRequest);
            System.out.println("DELETE Response received:");
            System.out.println("  Success: " + response.getSuccess());
            System.out.println("  Message: " + response.getMessage());

            if (response.getSuccess()) {
                GetReply verifyResponse = stub.get(GetRequest.newBuilder().setKey(testKey).build());
                if (!verifyResponse.getFound()) {
                    System.out.println("✅ DELETE test PASSED (entry successfully removed)");
                } else {
                    System.out.println("⚠️ DELETE test WARNING: entry still exists after deletion");
                }
            } else {
                System.out.println("❌ DELETE test FAILED: " + response.getMessage());
            }
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            System.out.println("❌ DELETE test FAILED (exception)");
        }
    }

    private static void testRangeOperation(VkGrpc.VkStub stub) {
        System.out.println("\n--- Testing RANGE operation ---");

        String prefix = "range-test-" + System.currentTimeMillis() + "-";
        for (int i = 0; i < 3; i++) {
            String key = prefix + String.format("%03d", i);
            CountDownLatch putLatch = new CountDownLatch(1);

            stub.put(
                PutRequest.newBuilder()
                    .setKey(key)
                    .setValue(ByteString.copyFromUtf8("range-value-" + i))
                    .build(),
                new io.grpc.stub.StreamObserver<PutReply>() {
                    @Override
                    public void onNext(PutReply reply) {}

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("Error saving test data: " + t.getMessage());
                putLatch.countDown();
            }

            @Override
            public void onCompleted() {
                putLatch.countDown();
            }
        }
    );

            try {
                if (!putLatch.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    System.out.println("❌ Failed to save test data for range test");
                    return;
                }
            } catch (InterruptedException e) {
                System.out.println("❌ Interrupted while saving test data");
                return;
            }
        }

        RangeRequest rangeRequest = RangeRequest.newBuilder()
            .setKeyBegin(prefix + "000")
            .setKeyEnd(prefix + "999")
            .build();

        System.out.println("Sending RANGE request with range: [" + prefix + "000, " + prefix + "999]");

        try {
            CountDownLatch latch = new CountDownLatch(1);
            int[] count = {0};

            io.grpc.stub.StreamObserver<RangeReply> responseObserver =
                new io.grpc.stub.StreamObserver<RangeReply>() {
                    @Override
                    public void onNext(RangeReply reply) {
                        count[0]++;
                System.out.println("Received range entry " + count[0] + ":");
                System.out.println("  Key: " + reply.getKey());
                System.out.println("  Value length: " + reply.getValue().size() + " bytes");
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Stream error: " + t.getMessage());
                System.out.println("❌ RANGE test FAILED (stream error)");
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("RANGE stream completed. Total entries: " + count[0]);
                if (count[0] >= 3) {
                    System.out.println("✅ RANGE test PASSED");
                } else {
                    System.out.println("❌ RANGE test FAILED: expected at least 3 entries, got " + count[0]);
                }
                latch.countDown();
            }
        };

            stub.range(rangeRequest, responseObserver);

            if (!latch.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
                System.out.println("❌ RANGE test FAILED: timeout waiting for stream completion");
            }

        } catch (Exception e) {
            System.err.println("RPC failed: " + e.getMessage());
            System.out.println("❌ RANGE test FAILED (exception)");
        }
    }


    private static void testCountOperation(VkGrpc.VkBlockingStub stub) {
        System.out.println("\n--- Testing COUNT operation ---");

        CountRequest countRequest = CountRequest.newBuilder().build();

        System.out.println("Sending COUNT request");

        try {
            CountReply response = stub.count(countRequest);
            System.out.println("COUNT Response received:");
            System.out.println("  Total count: " + response.getCount());

            if (response.getCount() >= 0) {
                System.out.println("✅ COUNT test PASSED");
            } else {
                System.out.println("❌ COUNT test FAILED: negative count returned");
            }
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            System.out.println("❌ COUNT test FAILED (exception)");
        }
    }
}
