package com.smart.home.weatherservice.testsuites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({"com.smart.home.weatherservice.controller", "com.smart.home.weatherservice.service"})
public class UnitTests {
}
