@echo off
set ROOT="..\java-solutions\info\kgeorgiy\ja\morozov\bank"
set LIB="..\lib"
set LAUNCHER=%LIB%\junit-platform-console-standalone-1.13.0-M3.jar
javac -cp "%LIB%\*" -d out "%ROOT%\account\*.java" "%ROOT%\person\*.java" "%ROOT%\bank\*.java" "%ROOT%\*.java"

java -jar "%LAUNCHER%" execute --disable-banner --disable-ansi-colors --details=tree --class-path out --select-class info.kgeorgiy.ja.morozov.bank.BankJUnitTests
set CODE=%errorlevel%
rmdir /s /q out
exit /b %CODE%