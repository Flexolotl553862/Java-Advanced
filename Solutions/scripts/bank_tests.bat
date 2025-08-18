@echo off
set ROOT="..\java-solutions\info\kgeorgiy\ja\morozov\bank"
set LIB="..\lib\*"
javac -cp "%LIB%" -d out "%ROOT%\account\*.java" "%ROOT%\person\*.java" "%ROOT%\bank\*.java" "%ROOT%\*.java"

java -cp "out;%LIB%" info.kgeorgiy.ja.morozov.bank.BankTests
set CODE=%errorlevel%
rmdir /s /q out
exit /b %CODE%