package com.neu.edu.assignment2.service;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

@Service("amazonSNSClient")
public class AmazonSNSClient {


    private static String topicArn = "";

    private static final Logger logger = LoggerFactory.getLogger(AmazonSNSClient.class);

    public void sendEmailToUser(String message) {
        AmazonSNS snsClient =  AmazonSNSClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(DefaultAWSCredentialsProviderChain.getInstance()).build();
        PublishRequest request = new PublishRequest("arn:aws:sns:us-east-1:597569852494:user-updates-topic", message);
        logger.info("AmazonSNSClientClass- Published Request : " + request.toString() + "--------");
        try{
            PublishResult result = snsClient.publish(request);
            logger.info("result----------"+result.toString());
        }catch (Exception e) {
            logger.error((e.getMessage()));
        }

    }
}
