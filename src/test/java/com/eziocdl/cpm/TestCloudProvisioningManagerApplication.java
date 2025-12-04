package com.eziocdl.cpm;

import org.springframework.boot.SpringApplication;

public class TestCloudProvisioningManagerApplication {

	public static void main(String[] args) {
		SpringApplication.from(CloudProvisioningManagerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
