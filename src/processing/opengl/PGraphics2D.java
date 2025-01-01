/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2012-21 The Processing Foundation
  Copyright (c) 2004-12 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation, version 2.1.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

package processing.opengl;

import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PShapeSVG;


public class PGraphics2D extends PGraphicsOpenGL {

  public PGraphics2D() {
    super();
  }


  //////////////////////////////////////////////////////////////

  // RENDERER SUPPORT QUERIES


  @Override
  public boolean is2D() {
    return true;
  }


  @Override
  public boolean is3D() {
    return false;
  }


  //////////////////////////////////////////////////////////////

  // HINTS


  @Override
  public void hint(int which) {
    if (which == ENABLE_STROKE_PERSPECTIVE) {
      showWarning("Strokes cannot be perspective-corrected in 2D.");
      return;
    }
    super.hint(which);
  }


  //////////////////////////////////////////////////////////////

  // PROJECTION


  @Override
  public void ortho() {
    showMethodWarning("ortho");
  }


  @Override
  public void ortho(double left, double right,
                    double bottom, double top) {
    showMethodWarning("ortho");
  }


  @Override
  public void ortho(double left, double right,
                    double bottom, double top,
                    double near, double far) {
    showMethodWarning("ortho");
  }


  @Override
  public void perspective() {
    showMethodWarning("perspective");
  }


  @Override
  public void perspective(double fov, double aspect, double zNear, double zFar) {
    showMethodWarning("perspective");
  }


  @Override
  public void frustum(double left, double right, double bottom, double top,
                      double znear, double zfar) {
    showMethodWarning("frustum");
  }


  @Override
  protected void defaultPerspective() {
    super.ortho(0, width, -height, 0, -1, +1);
  }


  //////////////////////////////////////////////////////////////

  // CAMERA


  @Override
  public void beginCamera() {
    showMethodWarning("beginCamera");
  }


  @Override
  public void endCamera() {
    showMethodWarning("endCamera");
  }


  @Override
  public void camera() {
    showMethodWarning("camera");
  }


  @Override
  public void camera(double eyeX, double eyeY, double eyeZ,
                     double centerX, double centerY, double centerZ,
                     double upX, double upY, double upZ) {
    showMethodWarning("camera");
  }


  @Override
  protected void defaultCamera() {
    eyeDist = 1;
    resetMatrix();
  }


  //////////////////////////////////////////////////////////////

  // MATRIX MORE!


  @Override
  protected void begin2D() {
    pushProjection();
    defaultPerspective();
    pushMatrix();
    defaultCamera();
  }


  @Override
  protected void end2D() {
    popMatrix();
    popProjection();
  }


  //////////////////////////////////////////////////////////////

  // SHAPE


  @Override
  public void shape(PShape shape) {
    if (shape.is2D()) {
      super.shape(shape);
    } else {
      showWarning("The shape object is not 2D, cannot be displayed with " +
                  "this renderer");
    }
  }


  @Override
  public void shape(PShape shape, double x, double y) {
    if (shape.is2D()) {
      super.shape(shape, x, y);
    } else {
      showWarning("The shape object is not 2D, cannot be displayed with " +
                  "this renderer");
    }
  }


  @Override
  public void shape(PShape shape, double a, double b, double c, double d) {
    if (shape.is2D()) {
      super.shape(shape, a, b, c, d);
    } else {
      showWarning("The shape object is not 2D, cannot be displayed with " +
                  "this renderer");
    }
  }


  @Override
  public void shape(PShape shape, double x, double y, double z) {
    showDepthWarningXYZ("shape");
  }


  @Override
  public void shape(PShape shape, double x, double y, double z,
                    double c, double d, double e) {
    showDepthWarningXYZ("shape");
  }



  //////////////////////////////////////////////////////////////

  // SHAPE I/O


  static protected boolean isSupportedExtension(String extension) {
    return extension.equals("svg") || extension.equals("svgz");
  }


  static protected PShape loadShapeImpl(PGraphics pg,
                                        String filename, String extension) {
    if (extension.equals("svg") || extension.equals("svgz")) {
      PShapeSVG svg = new PShapeSVG(pg.parent.loadXML(filename));
      return PShapeOpenGL.createShape((PGraphicsOpenGL) pg, svg);
    }
    return null;
  }


  //////////////////////////////////////////////////////////////

  // SCREEN TRANSFORMS


  @Override
  public double modelX(double x, double y, double z) {
    showDepthWarning("modelX");
    return 0;
  }


  @Override
  public double modelY(double x, double y, double z) {
    showDepthWarning("modelY");
    return 0;
  }


  @Override
  public double modelZ(double x, double y, double z) {
    showDepthWarning("modelZ");
    return 0;
  }


  //////////////////////////////////////////////////////////////

  // SHAPE CREATION


//  @Override
//  protected PShape createShapeFamily(int type) {
//    return new PShapeOpenGL(this, type);
//  }
//
//
//  @Override
//  protected PShape createShapePrimitive(int kind, double... p) {
//    return new PShapeOpenGL(this, kind, p);
//  }


  /*
  @Override
  public PShape createShape(PShape source) {
    return PShapeOpenGL.createShape2D(this, source);
  }


  @Override
  public PShape createShape() {
    return createShape(PShape.GEOMETRY);
  }


  @Override
  public PShape createShape(int type) {
    return createShapeImpl(this, type);
  }


  @Override
  public PShape createShape(int kind, double... p) {
    return createShapeImpl(this, kind, p);
  }


  static protected PShapeOpenGL createShapeImpl(PGraphicsOpenGL pg, int type) {
    PShapeOpenGL shape = null;
    if (type == PConstants.GROUP) {
      shape = new PShapeOpenGL(pg, PConstants.GROUP);
    } else if (type == PShape.PATH) {
      shape = new PShapeOpenGL(pg, PShape.PATH);
    } else if (type == PShape.GEOMETRY) {
      shape = new PShapeOpenGL(pg, PShape.GEOMETRY);
    }
    shape.set3D(false);
    return shape;
  }


  static protected PShapeOpenGL createShapeImpl(PGraphicsOpenGL pg,
                                                int kind, double... p) {
    PShapeOpenGL shape = null;
    int len = p.length;

    if (kind == POINT) {
      if (len != 2) {
        showWarning("Wrong number of parameters");
        return null;
      }
      shape = new PShapeOpenGL(pg, PShape.PRIMITIVE);
      shape.setKind(POINT);
    } else if (kind == LINE) {
      if (len != 4) {
        showWarning("Wrong number of parameters");
        return null;
      }
      shape = new PShapeOpenGL(pg, PShape.PRIMITIVE);
      shape.setKind(LINE);
    } else if (kind == TRIANGLE) {
      if (len != 6) {
        showWarning("Wrong number of parameters");
        return null;
      }
      shape = new PShapeOpenGL(pg, PShape.PRIMITIVE);
      shape.setKind(TRIANGLE);
    } else if (kind == QUAD) {
      if (len != 8) {
        showWarning("Wrong number of parameters");
        return null;
      }
      shape = new PShapeOpenGL(pg, PShape.PRIMITIVE);
      shape.setKind(QUAD);
    } else if (kind == RECT) {
      if (len != 4 && len != 5 && len != 8 && len != 9) {
        showWarning("Wrong number of parameters");
        return null;
      }
      shape = new PShapeOpenGL(pg, PShape.PRIMITIVE);
      shape.setKind(RECT);
    } else if (kind == ELLIPSE) {
      if (len != 4 && len != 5) {
        showWarning("Wrong number of parameters");
        return null;
      }
      shape = new PShapeOpenGL(pg, PShape.PRIMITIVE);
      shape.setKind(ELLIPSE);
    } else if (kind == ARC) {
      if (len != 6 && len != 7) {
        showWarning("Wrong number of parameters");
        return null;
      }
      shape = new PShapeOpenGL(pg, PShape.PRIMITIVE);
      shape.setKind(ARC);
    } else if (kind == BOX) {
      showWarning("Primitive not supported in 2D");
    } else if (kind == SPHERE) {
      showWarning("Primitive not supported in 2D");
    } else {
      showWarning("Unrecognized primitive type");
    }

    if (shape != null) {
      shape.setParams(p);
    }

    shape.set3D(false);
    return shape;
  }
  */


  //////////////////////////////////////////////////////////////

  // BEZIER VERTICES


  @Override
  public void bezierVertex(double x2, double y2, double z2,
                           double x3, double y3, double z3,
                           double x4, double y4, double z4) {
    showDepthWarningXYZ("bezierVertex");
  }


  //////////////////////////////////////////////////////////////

  // QUADRATIC BEZIER VERTICES


  @Override
  public void quadraticVertex(double x2, double y2, double z2,
                         double x4, double y4, double z4) {
    showDepthWarningXYZ("quadVertex");
  }


  //////////////////////////////////////////////////////////////

  // CURVE VERTICES


  @Override
  public void curveVertex(double x, double y, double z) {
    showDepthWarningXYZ("curveVertex");
  }


  //////////////////////////////////////////////////////////////

  // BOX


  @Override
  public void box(double w, double h, double d) {
    showMethodWarning("box");
  }


  //////////////////////////////////////////////////////////////

  // SPHERE


  @Override
  public void sphere(double r) {
    showMethodWarning("sphere");
  }


  //////////////////////////////////////////////////////////////

  // VERTEX SHAPES


  @Override
  public void vertex(double x, double y, double z) {
    showDepthWarningXYZ("vertex");
  }

  @Override
  public void vertex(double x, double y, double z, double u, double v) {
    showDepthWarningXYZ("vertex");
  }

  //////////////////////////////////////////////////////////////

  // MATRIX TRANSFORMATIONS

  @Override
  public void translate(double tx, double ty, double tz) {
    showDepthWarningXYZ("translate");
  }

  @Override
  public void rotateX(double angle) {
    showDepthWarning("rotateX");
  }

  @Override
  public void rotateY(double angle) {
    showDepthWarning("rotateY");
  }

  @Override
  public void rotateZ(double angle) {
    showDepthWarning("rotateZ");
  }

  @Override
  public void rotate(double angle, double vx, double vy, double vz) {
    showVariationWarning("rotate");
  }

  @Override
  public void applyMatrix(PMatrix3D source) {
    showVariationWarning("applyMatrix");
  }

  @Override
  public void applyMatrix(double n00, double n01, double n02, double n03,
                          double n10, double n11, double n12, double n13,
                          double n20, double n21, double n22, double n23,
                          double n30, double n31, double n32, double n33) {
    showVariationWarning("applyMatrix");
  }

  @Override
  public void scale(double sx, double sy, double sz) {
    showDepthWarningXYZ("scale");
  }

  //////////////////////////////////////////////////////////////

  // SCREEN AND MODEL COORDS

  @Override
  public double screenX(double x, double y, double z) {
    showDepthWarningXYZ("screenX");
    return 0;
  }

  @Override
  public double screenY(double x, double y, double z) {
    showDepthWarningXYZ("screenY");
    return 0;
  }

  @Override
  public double screenZ(double x, double y, double z) {
    showDepthWarningXYZ("screenZ");
    return 0;
  }

  @Override
  public PMatrix3D getMatrix(PMatrix3D target) {
    showVariationWarning("getMatrix");
    return target;
  }

  @Override
  public void setMatrix(PMatrix3D source) {
    showVariationWarning("setMatrix");
  }

  //////////////////////////////////////////////////////////////

  // LIGHTS

  @Override
  public void lights() {
    showMethodWarning("lights");
  }

  @Override
  public void noLights() {
    showMethodWarning("noLights");
  }

  @Override
  public void ambientLight(double red, double green, double blue) {
    showMethodWarning("ambientLight");
  }

  @Override
  public void ambientLight(double red, double green, double blue,
                           double x, double y, double z) {
    showMethodWarning("ambientLight");
  }

  @Override
  public void directionalLight(double red, double green, double blue,
                               double nx, double ny, double nz) {
    showMethodWarning("directionalLight");
  }

  @Override
  public void pointLight(double red, double green, double blue,
                         double x, double y, double z) {
    showMethodWarning("pointLight");
  }

  @Override
  public void spotLight(double red, double green, double blue,
                        double x, double y, double z,
                        double nx, double ny, double nz,
                        double angle, double concentration) {
    showMethodWarning("spotLight");
  }

  @Override
  public void lightFalloff(double constant, double linear, double quadratic) {
    showMethodWarning("lightFalloff");
  }

  @Override
  public void lightSpecular(double v1, double v2, double v3) {
    showMethodWarning("lightSpecular");
  }
}