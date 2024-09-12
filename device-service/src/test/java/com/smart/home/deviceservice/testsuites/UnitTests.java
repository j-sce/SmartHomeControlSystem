package com.smart.home.deviceservice.testsuites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({"com.smart.home.deviceservice.controller", "com.smart.home.deviceservice.service.impl"})
public class UnitTests {
}
