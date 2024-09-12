package com.smart.home.weatherservice.testsuites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({"com.smart.home.weatherservice.integration", "com.smart.home.weatherservice.security"})
public class IntegrationTests {
}
