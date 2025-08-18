@echo off
set IMPLEMENTOR_PATH="..\java-solutions\info\kgeorgiy\ja\morozov\implementor\Implementor.java"
set TEST_PACKAGE="info\kgeorgiy\java\advanced\implementor"
set MY_PACKAGE="info\kgeorgiy\ja\morozov\implementor"

set JARS="..\..\java-advanced-2025\artifacts\info.kgeorgiy.java.advanced.implementor"

javac -d implementor -cp "%JARS%.jar;%JARS%.tools.jar" %IMPLEMENTOR_PATH%

jar xf %JARS%.jar %TEST_PACKAGE%\Impler.class %TEST_PACKAGE%\ImplerException.class
jar xf %JARS%.tools.jar %TEST_PACKAGE%\tools\JarImpler.class

jar cmf MANIFEST.MF implementor.jar -C implementor %MY_PACKAGE%\Implementor.class %TEST_PACKAGE%\tools\JarImpler.class %TEST_PACKAGE%\Impler.class %TEST_PACKAGE%\ImplerException.class

rmdir info\ /s /q
rmdir implementor\ /s /q
