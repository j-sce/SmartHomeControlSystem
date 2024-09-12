package com.smart.home.scenarioservice.testsuites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({"com.smart.home.scenarioservice.controller", "com.smart.home.scenarioservice.service"})
public class UnitTests {
}
