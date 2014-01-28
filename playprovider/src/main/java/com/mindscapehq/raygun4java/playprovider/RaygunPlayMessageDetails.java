package com.mindscapehq.raygun4java.playprovider;

import com.mindscapehq.raygun4java.core.messages.RaygunMessageDetails;

public class RaygunPlayMessageDetails extends RaygunMessageDetails {

	private RaygunPlayRequestMessage request;

	public RaygunPlayRequestMessage getRequest() {
		return request;
	}
	public void setRequest(RaygunPlayJavaRequestMessage request) {
		this.request = request;
	}
  public void setRequest(RaygunPlayScalaRequestMessage request) {
    this.request = request;
  }
}
