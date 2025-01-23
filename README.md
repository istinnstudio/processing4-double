An experimental version of Processing 4 core library that supports double precision coordinates

This is a quick and experimental modification of the Processing core library to support double-precision coordinates across all fundamental classes like PShape, PVector, and others.
This modification does not include the Processing special sketch IDE, should only be used separatelly as a library on a standalone IDE like NetBeans.
Note: This data type change may introduce issues with color/styling as many float values have been converted to double including some objects that should not, so there could be side effects. I have also created a float version for colors (stroke width used is float), but it needs special new constructors for other commonly used methods like e.g. "random", so it breaks those special constructors, it cannot be easily adjusted to classic processing code, vian simple casing to double or float. One all double version seems reasonable and easy to handle. A 2nd source version with color parameters as float is there for those who need to extend codebase to support both data types, but I would not use it as a real life library.
If both float and double data types will ever be supported together, even if this theoretically is possible, this process would add many new methods to codebase, so this looks like it will never happen. This can be consider workaround to this. So only one data type will be included here, "double" that replaces float, and that is the only way to use it.

Initial testing in a pure Java IDE, using PApplet, has shown promising results using casting, suggesting compatibility with existing code with minimal adjustments.
The original Processing framework supports only float precision for coordinates, which can lead to rounding errors in high-precision calculations. This modification aims to address those limitations.

OpenGL renderers (P2D, P3D) could have issues. Not tested. JAVA2D renderer looks fine.

A real life use case could be if processing classes will be used for spatial calculations alongside with other spatial data structures that support double precision coordinates like native Path2D.Double and JavaTopologySuite (JTS) https://github.com/locationtech/jts. Includes a file with the Processing Geometry Suite PGS 2.0 library converted to double precision https://github.com/micycle1/PGS. All libraries that depend on processing will have to change/support float to double precision also and will be compatible only with this "double" version of processing.

Rendering of very detailed objects should now be possible using a Java IDE.
An article about the float precision artifacts in processing is here: https://blog.generativedarkroom.com/why-i-switched-from-processing-to-openrndr-b50c931d21ae

Warning: Use with caution at your own risk, this code is unsupported. It is based on Processing version 4.3.2 sources available from the official repository at: https://github.com/processing/processing4.
