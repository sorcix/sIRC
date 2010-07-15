#!/bin/sh

# Source directory
DIRSRC="src/main/java"
# Output directory for javadoc
DIRJDOC="api/"
# Output directory for class files
DIRBIN="bin/"
# Output directory for archives
DIRDIST="dist/"
# Temp folder
TEMP="/tmp/"

# Check if version number is given
if [ ! $1 ]; then
        echo "Version number argument required!"
	exit
fi

SUF=".gz"
SA="sIRC-source-"$1".tar"
SAG=$SA$SUF
JA="sIRC-javadoc-"$1".tar"
JAG=$JA$SUF
BA="sIRC-binary-"$1".tar"
BAG=$BA$SUF
JAR="sIRC.jar"

if [ -f $DIRDIST$SAG ] || [ -f $DIRDIST$JAG ] || [ -f $DIRDIST$BAG ]; then
	echo "Build for this release already exists?"
else

	if [ ! -d $DIRDIST ]; then
		mkdir $DIRDIST
	fi

	echo "Building sIRC v"$1

	# Create source archive
	echo "» Creating source archive.."
	tar -cf $TEMP$SA $DIRSRC
	tar -rf $TEMP$SA README
	tar -rf $TEMP$SA LICENSE
	echo "» Compressing archive.."
	gzip --best -S $SUF $TEMP$SA
	mv $TEMP$SAG $DIRDIST
	rm -f $TEMP$SAG
	echo "Done!"

	# Create javadoc
	if [ -d api ]; then
		rm -rf $DIRJDOC*
	else
		mkdir $DIRJDOC
	fi
	echo "» Generating javadoc.."
	javadoc \
		-d $DIRJDOC \
		-sourcepath $DIRSRC \
		-windowtitle "Sorcix Lib-IRC (sIRC)" \
		-quiet \
		com.sorcix.sirc
	echo "Done!"

	# Create javadoc archive
	echo "» Creating javadoc archive.."
	tar -cf $TEMP$JA $DIRJDOC
	tar -rf $TEMP$JA README
	tar -rf $TEMP$JA LICENSE
	echo "» Compressing javadoc archive.."
	gzip --best -S $SUF $TEMP$JA
	mv $TEMP$JAG $DIRDIST
	rm -f $TEMP$JAG
	echo "Done!"

	# Compile
	
fi
