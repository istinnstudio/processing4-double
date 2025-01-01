This is a quick and experimental modification of the Processing core library to support double-precision coordinates across fundamental classes like PShape, PVector, and others.
This modification does not include the Processing special sketch IDE, should only be used separatelly as a library on a standalone IDE like NetBeans.
Note: This change may introduce issues with color/styling as many float values have been converted to double including some objects that should not, and in later a version their constructors should be reverted back to the original float format.
As there are countless float to double replacements on color and styling, more of them should not be there.  
Initial testing in a pure Java IDE using PApplet has shown promising results using casting, suggesting compatibility with existing code with minimal adjustments.
The original Processing framework supports only float precision for coordinates, which can lead to rounding errors in high-precision calculations. This modification aims to address those limitations.
Warning: Use with caution. This is based on Processing version 4.3.2 from the repository at https://github.com/processing/processing4.

So color and styling should be casted back to float or use int constructors, but there could be side effects. 
This is more useful if processing classes are being used for spatial calculations or parallel usage with objects that support double precision like Path2D and JTS.
