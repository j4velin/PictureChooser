PictureChooser
==============

<table sytle="border: 0px;">
<tr>
<td><img width="200px" src="screenshot1.png" /></td>
<td><img width="200px" src="screenshot2.png" /></td>
<td><img width="200px" src="screenshot3.png" /></td>
</tr>
</table>


Usage
-----

Start with startActivityForResult(new Intent(context, de.j4velin.picturechooser.Main.class), PICTURE_CHOOSER); and read the "imgPath" StringExtra from the Intent you get in onActivityResult.
To allow the user to crop the image (which will create a copy of the image in your apps directory), add the optional boolean extra "crop" with value "true" to the intent.


Build
-----

Add the Android support library to build the project
