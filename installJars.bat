call mvn install:install-file -Dfile="libs\NodeService-0.0.1-SNAPSHOT-classes.jar" -DartifactId=NodeService -DgroupId=eu.vre4eic.evre -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar -DpomFile=pom.xml
call mvn install:install-file -Dfile="libs\NodeService-0.0.3-SNAPSHOT-classes.jar" -DartifactId=NodeService -DgroupId=eu.vre4eic.evre -Dversion=0.0.3-SNAPSHOT -Dpackaging=jar -DpomFile=pom.xml
call mvn install:install-file -Dfile="libs\BlazegraphOps-1.0-SNAPSHOT.jar" -DartifactId=BlazegraphOps -DgroupId=forth.ics -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar -DpomFile=pom.xml
call mvn install:install-file -Dfile="libs\VirtuosoOpsImpl-1.0-SNAPSHOT.jar" -DartifactId=VirtuosoOpsImpl -DgroupId=forth.ics -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar -DpomFile=pom.xml
pause





