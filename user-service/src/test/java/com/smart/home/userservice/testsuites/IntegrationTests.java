package com.smart.home.userservice.testsuites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({"com.smart.home.userservice.integration", "com.smart.home.userservice.security"})
public class IntegrationTests {
}
