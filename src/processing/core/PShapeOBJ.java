package processing.core;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is not part of the Processing API and should not be used
 * directly. Instead, use loadShape() and methods like it, which will make
 * use of this class. Using this class directly will cause your code to break
 * when combined with future versions of Processing.
 * <p>
 * OBJ loading implemented using code from Saito's OBJLoader library:
 * http://code.google.com/p/saitoobjloader/
 * and OBJReader from Ahmet Kizilay
 * http://www.openprocessing.org/visuals/?visualID=191
 *
 */
public class PShapeOBJ extends PShape {

  /**
   * Initializes a new OBJ Object with the given filename.
   */
  public PShapeOBJ(PApplet parent, String filename) {
    this(parent, parent.createReader(filename), getBasePath(parent, filename));
  }

  public PShapeOBJ(PApplet parent, BufferedReader reader) {
    this(parent, reader, "");
  }

  public PShapeOBJ(PApplet parent, BufferedReader reader, String basePath) {
    ArrayList<OBJFace> faces = new ArrayList<OBJFace>();
    ArrayList<OBJMaterial> materials = new ArrayList<OBJMaterial>();
    ArrayList<PVector> coords = new ArrayList<PVector>();
    ArrayList<PVector> normals = new ArrayList<PVector>();
    ArrayList<PVector> texcoords = new ArrayList<PVector>();
    parseOBJ(parent, basePath, reader,
             faces, materials, coords, normals, texcoords);

    // The OBJ geometry is stored with each face in a separate child shape.
    parent = null;
    family = GROUP;
    addChildren(faces, materials, coords, normals, texcoords);
  }


  protected PShapeOBJ(OBJFace face, OBJMaterial mtl,
                      ArrayList<PVector> coords,
                      ArrayList<PVector> normals,
                      ArrayList<PVector> texcoords) {
    family = GEOMETRY;
    if (face.vertIdx.size() == 3) {
      kind = TRIANGLES;
    } else if (face.vertIdx.size() == 4) {
      kind = QUADS;
    } else {
      kind = POLYGON;
    }

    stroke = false;
    fill = true;

    // Setting material properties for the new face
    fillColor = rgbaValue(mtl.kd);
    ambientColor = rgbaValue(mtl.ka);
    specularColor = rgbaValue(mtl.ks);
    shininess = mtl.ns;
    if (mtl.kdMap != null) {
      // If current material is textured, then tinting the texture using the
      // diffuse color.
      tintColor = rgbaValue(mtl.kd, mtl.d);
    }

    vertexCount = face.vertIdx.size();
    vertices = new double[vertexCount][12];
    for (int j = 0; j < face.vertIdx.size(); j++){
      int vertIdx, normIdx, texIdx;
      PVector vert, norms, tex;

      vert = norms = tex = null;

      vertIdx = face.vertIdx.get(j).intValue() - 1;
      vert = coords.get(vertIdx);

      if (j < face.normIdx.size()) {
        normIdx = face.normIdx.get(j).intValue() - 1;
        if (-1 < normIdx) {
          norms = normals.get(normIdx);
        }
      }

      if (j < face.texIdx.size()) {
        texIdx = face.texIdx.get(j).intValue() - 1;
        if (-1 < texIdx) {
          tex = texcoords.get(texIdx);
        }
      }

      vertices[j][X] = vert.x;
      vertices[j][Y] = vert.y;
      vertices[j][Z] = vert.z;

      vertices[j][PGraphics.R] = mtl.kd.x;
      vertices[j][PGraphics.G] = mtl.kd.y;
      vertices[j][PGraphics.B] = mtl.kd.z;
      vertices[j][PGraphics.A] = 1;

      if (norms != null) {
        vertices[j][PGraphics.NX] = norms.x;
        vertices[j][PGraphics.NY] = norms.y;
        vertices[j][PGraphics.NZ] = norms.z;
      }

      if (tex != null) {
        vertices[j][PGraphics.U] = tex.x;
        vertices[j][PGraphics.V] = tex.y;
      }

      if (mtl != null && mtl.kdMap != null) {
        image = mtl.kdMap;
      }
    }
  }


  protected void addChildren(ArrayList<OBJFace> faces,
                             ArrayList<OBJMaterial> materials,
                             ArrayList<PVector> coords,
                             ArrayList<PVector> normals,
                             ArrayList<PVector> texcoords) {
    int mtlIdxCur = -1;
    OBJMaterial mtl = null;
    for (int i = 0; i < faces.size(); i++) {
      OBJFace face = faces.get(i);

      // Getting current material.
      if (mtlIdxCur != face.matIdx || face.matIdx == -1) {
        // To make sure that at least we get the default material
        mtlIdxCur = PApplet.max(0, face.matIdx);
        mtl = materials.get(mtlIdxCur);
      }

      // Creating child shape for current face.
      PShape child = new PShapeOBJ(face, mtl, coords, normals, texcoords);
      addChild(child);
    }
  }


  static protected void parseOBJ(PApplet parent, String path,
                                 BufferedReader reader,
                                 ArrayList<OBJFace> faces,
                                 ArrayList<OBJMaterial> materials,
                                 ArrayList<PVector> coords,
                                 ArrayList<PVector> normals,
                                 ArrayList<PVector> texcoords) {
    Map<String, Integer> mtlTable  = new HashMap<String, Integer>();
    int mtlIdxCur = -1;
    boolean readv, readvn, readvt;
    try {

      readv = readvn = readvt = false;
      String line;
      String gname = "object";
      while ((line = reader.readLine()) != null) {
       // Parse the line.
        line = line.trim();
        if (line.equals("") || line.indexOf('#') == 0) {
          // Empty line of comment, ignore line
          continue;
        }

        // The below patch/hack comes from Carlos Tomas Marti and is a
        // fix for single backslashes in Rhino obj files

        // BEGINNING OF RHINO OBJ FILES HACK
        // Statements can be broken in multiple lines using '\' at the
        // end of a line.
        // In regular expressions, the backslash is also an escape
        // character.
        // The regular expression \\ matches a single backslash. This
        // regular expression as a Java string, becomes "\\\\".
        // That's right: 4 backslashes to match a single one.
        while (line.contains("\\")) {
          line = line.split("\\\\")[0];
          final String s = reader.readLine();
          if (s != null)
            line += s;
        }
        // END OF RHINO OBJ FILES HACK

        String[] parts = line.split("\\s+");
        // if not a blank line, process the line.
        if (parts.length > 0) {
          if (parts[0].equals("v")) {
            // vertex
            PVector tempv = new PVector(Double.valueOf(parts[1]).doubleValue(),
                                        Double.valueOf(parts[2]).doubleValue(),
                                        Double.valueOf(parts[3]).doubleValue());
            coords.add(tempv);
            readv = true;
          } else if (parts[0].equals("vn")) {
            // normal
            PVector tempn = new PVector(Double.valueOf(parts[1]).doubleValue(),
                                        Double.valueOf(parts[2]).doubleValue(),
                                        Double.valueOf(parts[3]).doubleValue());
            normals.add(tempn);
            readvn = true;
          } else if (parts[0].equals("vt")) {
            // uv, inverting v to take into account Processing's inverted Y axis
            // with respect to OpenGL.
            PVector tempv = new PVector(Double.valueOf(parts[1]).doubleValue(),
                                        1 - Double.valueOf(parts[2]).
                                        doubleValue());
            texcoords.add(tempv);
            readvt = true;
          } else if (parts[0].equals("o")) {
            // Object name is ignored, for now.
          } else if (parts[0].equals("mtllib")) {
            if (parts[1] != null) {
              String fn = parts[1];
              if (fn.indexOf(File.separator) == -1 && !path.equals("")) {
                // Relative file name, adding the base path.
                fn = path + File.separator + fn;
              }
              BufferedReader mreader = parent.createReader(fn);
              if (mreader != null) {
                parseMTL(parent, fn, path, mreader, materials, mtlTable);
                mreader.close();
              }
            }
          } else if (parts[0].equals("g")) {
            gname = 1 < parts.length ? parts[1] : "";
          } else if (parts[0].equals("usemtl")) {
            // Getting index of current active material (will be applied on
            // all subsequent faces).
            if (parts[1] != null) {
              String mtlname = parts[1];
              if (mtlTable.containsKey(mtlname)) {
                Integer tempInt = mtlTable.get(mtlname);
                mtlIdxCur = tempInt.intValue();
              } else {
                mtlIdxCur = -1;
              }
            }
          } else if (parts[0].equals("f")) {
            // Face setting
            OBJFace face = new OBJFace();
            face.matIdx = mtlIdxCur;
            face.name = gname;

            for (int i = 1; i < parts.length; i++) {
              String seg = parts[i];

              if (seg.indexOf("/") > 0) {
                String[] forder = seg.split("/");

                if (forder.length > 2) {
                  // Getting vertex and texture and normal indexes.
                  if (forder[0].length() > 0 && readv) {
                    face.vertIdx.add(Integer.valueOf(forder[0]));
                  }

                  if (forder[1].length() > 0 && readvt) {
                    face.texIdx.add(Integer.valueOf(forder[1]));
                  }

                  if (forder[2].length() > 0 && readvn) {
                    face.normIdx.add(Integer.valueOf(forder[2]));
                  }
                } else if (forder.length > 1) {
                  // Getting vertex and texture/normal indexes.
                  if (forder[0].length() > 0 && readv) {
                    face.vertIdx.add(Integer.valueOf(forder[0]));
                  }

                  if (forder[1].length() > 0) {
                    if (readvt) {
                      face.texIdx.add(Integer.valueOf(forder[1]));
                    } else  if (readvn) {
                      face.normIdx.add(Integer.valueOf(forder[1]));
                    }

                  }

                } else if (forder.length > 0) {
                  // Getting vertex index only.
                  if (forder[0].length() > 0 && readv) {
                    face.vertIdx.add(Integer.valueOf(forder[0]));
                  }
                }
              } else {
                // Getting vertex index only.
                if (seg.length() > 0 && readv) {
                  face.vertIdx.add(Integer.valueOf(seg));
                }
              }
            }

            faces.add(face);
          }
        }
      }

      if (materials.size() == 0) {
        // No materials definition so far. Adding one default material.
        OBJMaterial defMtl = new OBJMaterial();
        materials.add(defMtl);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  static protected void parseMTL(PApplet parent, String mtlfn, String path,
                                 BufferedReader reader,
                                 ArrayList<OBJMaterial> materials,
                                 Map<String, Integer> materialsHash) {
    try {
      String line;
      OBJMaterial currentMtl = null;
      while ((line = reader.readLine()) != null) {
        // Parse the line
        line = line.trim();
        String[] parts = line.split("\\s+");
        if (parts.length > 0) {
          // Extract the material data.
          if (parts[0].equals("newmtl")) {
            // Starting new material.
            String mtlname = parts[1];
            currentMtl = addMaterial(mtlname, materials, materialsHash);
          } else {
            if (currentMtl == null) {
              currentMtl = addMaterial("material" + materials.size(),
                                       materials, materialsHash);
            }
            if (parts[0].equals("map_Kd") && parts.length > 1) {
              // Loading texture map.
              String texname = parts[1];
              if (texname.indexOf(File.separator) == -1 && !path.equals("")) {
                // Relative file name, adding the base path.
                texname = path + File.separator + texname;
              }

              File file = new File(parent.dataPath(texname));
              if (file.exists()) {
                currentMtl.kdMap = parent.loadImage(texname);
              } else {
                System.err.println("The texture map \"" + texname + "\" " +
                  "in the materials definition file \"" + mtlfn + "\" " +
                  "is missing or inaccessible, make sure " +
                  "the URL is valid or that the file has been " +
                  "added to your sketch and is readable.");
              }
            } else if (parts[0].equals("Ka") && parts.length > 3) {
              // The ambient color of the material
              currentMtl.ka.x = Double.valueOf(parts[1]).doubleValue();
              currentMtl.ka.y = Double.valueOf(parts[2]).doubleValue();
              currentMtl.ka.z = Double.valueOf(parts[3]).doubleValue();
            } else if (parts[0].equals("Kd") && parts.length > 3) {
              // The diffuse color of the material
              currentMtl.kd.x = Double.valueOf(parts[1]).doubleValue();
              currentMtl.kd.y = Double.valueOf(parts[2]).doubleValue();
              currentMtl.kd.z = Double.valueOf(parts[3]).doubleValue();
            } else if (parts[0].equals("Ks") && parts.length > 3) {
              // The specular color weighted by the specular coefficient
              currentMtl.ks.x = Double.valueOf(parts[1]).doubleValue();
              currentMtl.ks.y = Double.valueOf(parts[2]).doubleValue();
              currentMtl.ks.z = Double.valueOf(parts[3]).doubleValue();
            } else if ((parts[0].equals("d") ||
                        parts[0].equals("Tr")) && parts.length > 1) {
              // Reading the alpha transparency.
              currentMtl.d = Double.valueOf(parts[1]).doubleValue();
            } else if (parts[0].equals("Ns") && parts.length > 1) {
              // The specular component of the Phong shading model
              currentMtl.ns = Double.valueOf(parts[1]).doubleValue();
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected static OBJMaterial addMaterial(String mtlname,
                                           ArrayList<OBJMaterial> materials,
                                           Map<String, Integer> materialsHash) {
    OBJMaterial currentMtl = new OBJMaterial(mtlname);
    materialsHash.put(mtlname, Integer.valueOf(materials.size()));
    materials.add(currentMtl);
    return currentMtl;
  }

  protected static int rgbaValue(PVector color) {
    return 0xFF000000 | ((int)(color.x * 255) << 16) |
                        ((int)(color.y * 255) <<  8) |
                         (int)(color.z * 255);
  }


  protected static int rgbaValue(PVector color, double alpha) {
    return ((int)(alpha * 255)   << 24) |
           ((int)(color.x * 255) << 16) |
           ((int)(color.y * 255) <<  8) |
            (int)(color.z * 255);
  }


  // Stores a face from an OBJ file
  static protected class OBJFace {
    ArrayList<Integer> vertIdx;
    ArrayList<Integer> texIdx;
    ArrayList<Integer> normIdx;
    int matIdx;
    String name;

    OBJFace() {
      vertIdx = new ArrayList<Integer>();
      texIdx = new ArrayList<Integer>();
      normIdx = new ArrayList<Integer>();
      matIdx = -1;
      name = "";
    }
  }


  static protected String getBasePath(PApplet parent, String filename) {
    // Obtaining the path
    File file = new File(parent.dataPath(filename));
    if (!file.exists()) {
      file = parent.sketchFile(filename);
    }
    String absolutePath = file.getAbsolutePath();
    return absolutePath.substring(0,
            absolutePath.lastIndexOf(File.separator));
  }


  // Stores a material defined in an MTL file.
  static protected class OBJMaterial {
    String name;
    PVector ka;
    PVector kd;
    PVector ks;
    double d;
    double ns;
    PImage kdMap;

    OBJMaterial() {
      this("default");
    }

    OBJMaterial(String name) {
      this.name = name;
      ka = new PVector(0.5f, 0.5f, 0.5f);
      kd = new PVector(0.5f, 0.5f, 0.5f);
      ks = new PVector(0.5f, 0.5f, 0.5f);
      d = 1.0f;
      ns = 0.0f;
      kdMap = null;
    }
  }
}
