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


Build
-----

Add the Android support library to build the project
