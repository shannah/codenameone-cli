#!/bin/sh
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file \
                         -Dfile=JavaSE.jar -DgroupId=ca.weblite \
                         -DartifactId=codename1-javase -Dversion=0.0.1 \
                         -Dpackaging=jar -DlocalRepositoryPath=my-repo