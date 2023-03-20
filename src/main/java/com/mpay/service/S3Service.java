package com.mpay.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;

public class S3Service {

	public static AmazonS3 getS3Service() {
		AWSCredentials credentials = new BasicAWSCredentials("AKIA574J3NOT4FLYW5ME",
				"SFLC9SB3lRsp4cdby5EEU/FexXoaQHT34AE3v9n1");
		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.AP_SOUTHEAST_1).build();

//        ListObjectsV2Result result = s3.listObjectsV2("relovedev");
//        List<S3ObjectSummary> objects = result.getObjectSummaries();
//        for (S3ObjectSummary os : objects) {
//            System.out.println("* " + os.getKey());
//        }
		return s3;
	}

	public static String uploadOrderReceipt(byte[] receiptImg, Long orderId) {

		InputStream inputStream = new ByteArrayInputStream(receiptImg);
		AmazonS3 s3 = getS3Service();
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(receiptImg.length);
		metadata.setContentType("image/png");
		// metadata.setCacheControl("public, max-age=31536000");
		String filename = "receipt_#" + orderId;
		s3.putObject("relovedev", filename, inputStream, metadata);
		URL url = s3.getUrl("relovedev", filename);
		return url.toString();
	}

}
