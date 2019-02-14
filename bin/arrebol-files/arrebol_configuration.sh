#bash
ARREBOL_MENU_PYTHON="arrebol_context_menu/arrebolContextMenu.py"
ARREBOL_MENU_PYTHON_ICONS="arrebol_context_menu/icons/*"

MENU_SCRIPT_LOCATION="$HOME/.local/share/nautilus-python/extensions"
ICONS_LOCATION="$HOME/.icons/hicolor/48x48/emblems"

ARREBOL_CLIENT=arrebol

#sudo apt-get install python-nautilus
echo "s" | sudo apt-get install python-nautilus

echo "s" | sudo apt-get install python-tk

echo "Creating directory: $MENU_SCRIPT_LOCATION"
mkdir -p $MENU_SCRIPT_LOCATION
echo "Creating directory: $ICONS_LOCATION"
mkdir -p $ICONS_LOCATION

ARREBOL_CLIENT_PATH=$(echo $PWD"/"$ARREBOL_CLIENT)
echo "Preparing arrebol context menu script to use arrebol client: $ARREBOL_CLIENT_PATH"
#Copying scripts and icons for arrebol context menu

echo "Copying arrebol context menu script $ARREBOL_MENU_PYTHON to $MENU_SCRIPT_LOCATION"
cp $ARREBOL_MENU_PYTHON $MENU_SCRIPT_LOCATION
echo "Copying arrebol context menu script from $ARREBOL_MENU_PYTHON_ICONS to $ICONS_LOCATION/"
cp $ARREBOL_MENU_PYTHON_ICONS $ICONS_LOCATION

CONTEXT_MENU_COMPLETE_PATH=$MENU_SCRIPT_LOCATION"/arrebolContextMenu.py"
echo "Injecting arrebol client on context menu script ON $CONTEXT_MENU_COMPLETE_PATH"
sed -i -e 's#<TAG-VALUE-CLIENT-PATH>#'$ARREBOL_CLIENT_PATH'#g' $CONTEXT_MENU_COMPLETE_PATH
nautilus -q



