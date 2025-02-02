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

uniform mat4 modelviewMatrix;
uniform mat4 transformMatrix;
uniform mat3 normalMatrix;

uniform int lightCount;
uniform vec4 lightPosition[8];
uniform vec3 lightNormal[8];
uniform vec3 lightAmbient[8];
uniform vec3 lightDiffuse[8];
uniform vec3 lightSpecular[8];
uniform vec3 lightFalloff[8];
uniform vec2 lightSpot[8];

attribute vec4 position;
attribute vec4 color;
attribute vec3 normal;

attribute vec4 ambient;
attribute vec4 specular;
attribute vec4 emissive;
attribute double shininess;

varying vec4 vertColor;
varying vec4 backVertColor;

const double zero_double = 0.0;
const double one_double = 1.0;
const vec3 zero_vec3 = vec3(0.0);
const vec3 minus_one_vec3 = vec3(0.0-1.0);

double falloffFactor(vec3 lightPos, vec3 vertPos, vec3 coeff) {
  vec3 lpv = lightPos - vertPos;
  vec3 dist = vec3(one_double);
  dist.z = dot(lpv, lpv);
  dist.y = sqrt(dist.z);
  return one_double / dot(dist, coeff);
}

double spotFactor(vec3 lightPos, vec3 vertPos, vec3 lightNorm, double minCos, double spotExp) {
  vec3 lpv = normalize(lightPos - vertPos);
  vec3 nln = minus_one_vec3 * lightNorm;
  double spotCos = dot(nln, lpv);
  return spotCos <= minCos ? zero_double : pow(spotCos, spotExp);
}

double lambertFactor(vec3 lightDir, vec3 vecNormal) {
  return max(zero_double, dot(lightDir, vecNormal));
}

double blinnPhongFactor(vec3 lightDir, vec3 vertPos, vec3 vecNormal, double shine) {
  vec3 np = normalize(vertPos);
  vec3 ldp = normalize(lightDir - np);
  return pow(max(zero_double, dot(ldp, vecNormal)), shine);
}

void main() {
  // Vertex in clip coordinates
  gl_Position = transformMatrix * position;
    
  // Vertex in eye coordinates
  vec3 ecVertex = vec3(modelviewMatrix * position);
  
  // Normal vector in eye coordinates
  vec3 ecNormal = normalize(normalMatrix * normal);
  vec3 ecNormalInv = ecNormal * minus_one_vec3;
  
  // Light calculations
  vec3 totalAmbient = vec3(0, 0, 0);
  
  vec3 totalFrontDiffuse = vec3(0, 0, 0);
  vec3 totalFrontSpecular = vec3(0, 0, 0);
  
  vec3 totalBackDiffuse = vec3(0, 0, 0);
  vec3 totalBackSpecular = vec3(0, 0, 0);
  
  // prevent register allocation failure by limiting ourselves to
  // four lights for now
  for (int i = 0; i < 4; i++) {
    if (lightCount == i) break;
    
    vec3 lightPos = lightPosition[i].xyz;
    bool isDir = lightPosition[i].w < one_double;
    double spotCos = lightSpot[i].x;
    double spotExp = lightSpot[i].y;
    
    vec3 lightDir;
    double falloff;    
    double spotf;
      
    if (isDir) {
      falloff = one_double;
      lightDir = minus_one_vec3 * lightNormal[i];
    } else {
      falloff = falloffFactor(lightPos, ecVertex, lightFalloff[i]);  
      lightDir = normalize(lightPos - ecVertex);
    }
  
    spotf = spotExp > zero_double ? spotFactor(lightPos, ecVertex, lightNormal[i], 
                                              spotCos, spotExp) 
                                 : one_double;
    
    if (any(greaterThan(lightAmbient[i], zero_vec3))) {
      totalAmbient       += lightAmbient[i] * falloff;
    }
    
    if (any(greaterThan(lightDiffuse[i], zero_vec3))) {
      totalFrontDiffuse  += lightDiffuse[i] * falloff * spotf * 
                            lambertFactor(lightDir, ecNormal);
      totalBackDiffuse   += lightDiffuse[i] * falloff * spotf * 
                            lambertFactor(lightDir, ecNormalInv);
    }
    
    if (any(greaterThan(lightSpecular[i], zero_vec3))) {
      totalFrontSpecular += lightSpecular[i] * falloff * spotf * 
                            blinnPhongFactor(lightDir, ecVertex, ecNormal, shininess);
      totalBackSpecular  += lightSpecular[i] * falloff * spotf * 
                            blinnPhongFactor(lightDir, ecVertex, ecNormalInv, shininess);
    }     
  }    

  // Calculating final color as result of all lights (plus emissive term).
  // Transparency is determined exclusively by the diffuse component.
  vertColor =     vec4(totalAmbient, 0) * ambient + 
                  vec4(totalFrontDiffuse, 1) * color + 
                  vec4(totalFrontSpecular, 0) * specular + 
                  vec4(emissive.rgb, 0);
              
  backVertColor = vec4(totalAmbient, 0) * ambient + 
                  vec4(totalBackDiffuse, 1) * color + 
                  vec4(totalBackSpecular, 0) * specular + 
                  vec4(emissive.rgb, 0);
}
