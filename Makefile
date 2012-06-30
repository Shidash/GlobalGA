JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $*.java

CLASSES = \
        Server.java \
        ServerThread.java \
        Client.java \
        ClientApplet.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
