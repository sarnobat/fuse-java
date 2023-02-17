#!/bin/sh

#----------------------------------------------------------------------------
# DESCRIPTION		
# DATE				[:VIM_EVAL:]strftime('%Y-%m-%d')[:END_EVAL:]
# AUTHOR			ss401533@gmail.com                                           
#----------------------------------------------------------------------------

if [ "$1" = "/" ]
then
# echo "nothing.txt"
echo "top_level.txt"
elif [ "$1" = "" ]
then

echo "bfile.txt"
#echo "$1"

else
	echo "a_dir/"
fi
