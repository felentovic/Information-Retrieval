javac -cp ../lib/commons-cli-1.4.jar:../lib/lucene-analyzers-common-7.2.1.jar:../lib/lucene-core-7.2.1.jar $(find . -name "*.java")

java -cp ../lib/commons-cli-1.4.jar:../lib/lucene-analyzers-common-7.2.1.jar:../lib/lucene-core-7.2.1.jar indexcreation.Main -i /home/felentovic/Documents/TUWien/Semester_4/Advanced_Information_Retrieval/Excercise1/documents/Adhoc/fbis -o /home/felentovic/tmp -l -c -n -w -t SP -is 500000 
