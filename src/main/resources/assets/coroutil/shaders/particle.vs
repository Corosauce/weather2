#version 130
#extension GL_EXT_gpu_shader4 : enable

attribute vec3 position;
attribute vec2 texCoord;
attribute vec3 vertexNormal;
attribute mat4 modelMatrix;
attribute float brightness;
attribute vec4 rgba;
//attribute vec4 rgbaTest;
//in vec2 texOffset;

varying vec2 outTexCoord;
//flat varying float outBrightness;
varying vec4 outRGBA;

uniform mat4 modelViewMatrixCamera;
//uniform mat4 projectionMatrix;

//uniform int numCols;
//uniform int numRows;

void main()
{



	gl_Position = modelViewMatrixCamera * modelMatrix * vec4(position, 1.0);

	//vec4 eyePos = gl_ModelViewMatrix * gl_Position;
    //gl_FogFragCoord = abs(eyePos.z/eyePos.w);
    gl_FogFragCoord = abs(gl_Position.z);

	// Support for texture atlas, update texture coordinates
    //float x = (texCoord.x / numCols + texOffset.x);
    //float y = (texCoord.y / numRows + texOffset.y);

	outTexCoord = texCoord;
	//outBrightness = brightness;
	int lightMap = int(brightness);
	vec3 texMap = vec3(float((lightMap >> 16) & 255) / 255.0, float((lightMap >> 8) & 255) / 255.0, float(lightMap & 255) / 255.0);

	//temp
	//rgba.x = 1;
	//rgba.y = 1;
	//rgba.z = 1;
	//rgba.w = 1;

	outRGBA = rgba;
	outRGBA.x = rgba.x * texMap.x;
    outRGBA.y = rgba.y * texMap.y;
    outRGBA.z = rgba.z * texMap.z;
}