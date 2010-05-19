#!/bin/sh

# Output directory for javadoc
# -- A directory called "api" will be created in here
JDOC="/home/sorcix/Website/htdocs/dev/java/sirc/"
# Output directory for archives
REL="/home/sorcix/Public/projects/sirc/"
# Temp folder
TEMP="/tmp/"

# Check if version number is given
if [ ! $1 ]; then
        echo "Version number argument required!"
	exit
fi

PROJ=${pwd}
SUF=".gz"
SA="sIRC-source-"$1".tar"
SAG=$SA$SUF
JA="sIRC-javadoc-"$1".tar"
JAG=$JA$SUF

# Create source archive
if [ -f $REL$SAG ]; then
	echo "ERROR: Source archive already exists!"
else
	echo "Creating source archive.."
	tar -cf $TEMP$SA src/
	tar -rf $TEMP$SA README
	tar -rf $TEMP$SA LICENSE
	echo "Compressing archive.."
	gzip --best -S $SUF $TEMP$SA
	mv $TEMP$SAG $REL
	echo "-- Done!"
fi

# Create javadoc
if [ -f $REL$JAG ]; then
	echo "ERROR: Javadoc archive already exists!"
else
	if [ -d api ]; then
		rm -rf api/*
	else
		mkdir api
	fi
	echo "Generating javadoc.."
	javadoc -d api \
		-sourcepath src \
		-windowtitle "Sorcix Lib-IRC (sIRC)" \
		-quiet \
		com.sorcix.sirc
	echo "-- Done!"

	# Create archive
	echo "Creating javadoc archive.."
	tar -cf $TEMP$JA api
	tar -rf $TEMP$JA README
	tar -rf $TEMP$JA LICENSE
	echo "Compressing javadoc archive.."
	gzip --best -S $SUF $TEMP$JA
	mv $TEMP$JAG $REL
	echo "-- Done!"

	# Move to web publish directory
	if [ -d $JDOC ]; then
		if [ -d $JDOC"api" ]; then
			rm -rf $JDOC"api/*"
		else
			mkdir $JDOC"api"
		fi
		echo "Copying javadoc to web publish directory.."
		cp -rf api/* $JDOC"api"
		echo "-- Done!"
	else
		echo "ERROR: Javadoc directory doesn't exist!"
	fi
fi
