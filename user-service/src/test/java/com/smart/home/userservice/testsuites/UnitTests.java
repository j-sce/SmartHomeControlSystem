package com.smart.home.userservice.testsuites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({"com.smart.home.userservice.controller", "com.smart.home.userservice.service"})
public class UnitTests {
}
