package com.recipeai.recipebackend.service;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VisionService {
	public List<String> extractIngredients(MultipartFile file) throws IOException {
	    if (file == null || file.isEmpty()) {
	        throw new IllegalArgumentException("File is empty");
	    }

	    List<String> ingredients = new ArrayList<>();
	    ByteString imgBytes = ByteString.readFrom(file.getInputStream());

	    Image image = Image.newBuilder().setContent(imgBytes).build();
	    Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();

	    AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
	            .addFeatures(feature)
	            .setImage(image)
	            .build();

	    List<AnnotateImageRequest> requests = List.of(request);
	    try (ImageAnnotatorClient visionClient = ImageAnnotatorClient.create()) {
	        BatchAnnotateImagesResponse batchResponse = visionClient.batchAnnotateImages(
	                BatchAnnotateImagesRequest.newBuilder().addAllRequests(requests).build()
	        );

	        AnnotateImageResponse response = batchResponse.getResponsesList().get(0);

	        if (response.hasError()) {
	            throw new IOException("Vision API error: " + response.getError().getMessage());
	        }

	        for (EntityAnnotation annotation : response.getLabelAnnotationsList()) {
	            if (annotation.getScore() > 0.70) {
	                ingredients.add(annotation.getDescription());
	            }
	        }
	    }

	    return ingredients;
	}
}
