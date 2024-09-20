package com.smart.home.deviceservice.testsuites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.springframework.test.context.ActiveProfiles;

@Suite
@SelectPackages({"com.smart.home.deviceservice.integration", "com.smart.home.deviceservice.security"})
public class IntegrationTests {
}
