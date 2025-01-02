An experimental version of Processing 4 core library that supports double precision coordinates

This is a quick and experimental modification of the Processing core library to support double-precision coordinates across all fundamental classes like PShape, PVector, and others.
This modification does not include the Processing special sketch IDE, should only be used separatelly as a library on a standalone IDE like NetBeans.
Note: This data type change may introduce issues with color/styling as many float values have been converted to double including some objects that should not, so there could be side effects. I have also created a float version for colors (stroke width used is float), but it needs special new constructors for other commonly used methods like e.g. "random". It seems though it does not make much of a difference and it breaks those special constructors of processing like "random", so it cannot be easily adjusted to classic processing code. One all float or one all double version seems more reasonable.
If both float and double data types will ever be supported together, even this theoretically is possible, a process like this would add many new methods to codebase, so this looks like that it will never be touched officially.
So only one data type will be included here, "double" that replaces float.

Initial testing in a pure Java IDE, using PApplet, has shown promising results using casting, suggesting compatibility with existing code with minimal adjustments.
The original Processing framework supports only float precision for coordinates, which can lead to rounding errors in high-precision calculations. This modification aims to address those limitations.

OpenGL renderers (P2D, P3D) could have issues. Not tested. JAVA2D renderer looks fine.

A real life use case could be if processing classes will be used for spatial calculations alongside with other spatial data structures that support double precision coordinates like native Path2D.Double and JavaTopologySuite (JTS) https://github.com/locationtech/jts.
Other than that, rendering of very detailed objects should now be possible using a Java IDE.
An article about the float precision artifacts in processing is here: https://blog.generativedarkroom.com/why-i-switched-from-processing-to-openrndr-b50c931d21ae

Warning: Use with caution at your own risk, this code is unsupported. It is based on Processing version 4.3.2 sources available from the official repository at: https://github.com/processing/processing4.
