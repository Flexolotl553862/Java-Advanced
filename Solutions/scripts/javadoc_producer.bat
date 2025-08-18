@echo off
set IMPLEMENTOR_PATH="..\java-solutions\info\kgeorgiy\ja\morozov\implementor\Implementor.java"

set JARS="..\..\java-advanced-2025\artifacts\info.kgeorgiy.java.advanced.implementor"
set IMPLER="..\..\java-advanced-2025\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\Impler.java"
set JAR_IMPLER="..\..\java-advanced-2025\modules\info.kgeorgiy.java.advanced.implementor.tools\info\kgeorgiy\java\advanced\implementor\tools\JarImpler.java"

javadoc -private -d ..\javadoc -classpath "%JARS%.jar;%JARS%.tools.jar" %IMPLEMENTOR_PATH% %IMPLER% %JAR_IMPLER%