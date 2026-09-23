package com.singlemungle.backend.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.vertex-ai")
public class VertexAiProperties {

	private boolean enabled = false;
	private String projectId = "";
	private String location = "us-central1";
	private String imageModel = "gemini-3.1-flash-lite-image";
	private int timeoutSeconds = 30;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getImageModel() {
		return imageModel;
	}

	public void setImageModel(String imageModel) {
		this.imageModel = imageModel;
	}

	public int getTimeoutSeconds() {
		return timeoutSeconds;
	}

	public void setTimeoutSeconds(int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}
}
